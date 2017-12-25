/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package io.repseq.core;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.TranslationParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Object stores information about sequence partitioning (positions of specific anchor points)
 */
public abstract class SequencePartitioning {
    /**
     * Return position of specific anchor point
     *
     * @param referencePoint anchor point
     * @return position of anchor point
     */
    public abstract int getPosition(ReferencePoint referencePoint);

    /**
     * Return true if sequence partitioning is defined on the reverse-complement strand of the sequence,
     * false if on forward strand.
     *
     * @return true if partitioning is defined on the reverse-complement strand
     */
    public abstract boolean isReversed();

    /**
     * Checks if position of anchor point can be obtained using this partitioning
     *
     * @param referencePoint anchor point
     * @return true if position of anchor point can be obtained using this partitioning
     */
    public boolean isAvailable(ReferencePoint referencePoint) {
        return getPosition(referencePoint) >= 0;
    }

    /**
     * Checks if position of gene feature can be obtained using this partitioning
     *
     * @param feature gene feature
     * @return true if position of gene feature can be obtained using this partitioning
     */
    public boolean isAvailable(GeneFeature feature) {
        for (GeneFeature.ReferenceRange region : feature)
            if (!isAvailable(region))
                return false;
        return true;
    }

    /**
     * Return sequence range for non-composite gene features
     *
     * @param feature gene feature
     * @return range
     */
    public Range getRange(GeneFeature feature) {
        if (feature.isComposite())
            throw new IllegalArgumentException("Composite feature");

        return getRange(feature.getReferenceRange(0));
    }

    public Range[] getRanges(GeneFeature feature) {
        Range[] result = new Range[feature.size()];
        for (int i = 0; i < feature.size(); ++i) {
            if ((result[i] = getRange(feature.getReferenceRange(i))) == null)
                return null;
            if (i != 0 && result[i - 1].intersectsWith(result[i]) &&
                    result[i].isReverse() == result[i - 1].isReverse())
                throw new IllegalArgumentException("Inconsistent feature partition.");
        }
        return result;
    }

    public int getLength(GeneFeature feature) {
        int length = 0, l;
        for (GeneFeature.ReferenceRange r : feature) {
            if ((l = getLength(r)) == -1)
                return -1;
            length += l;
        }
        return length;
    }

    protected Range getRange(GeneFeature.ReferenceRange refRange) {
        int begin = getPosition(refRange.begin);
        if (begin < 0)
            return null;
        int end = getPosition(refRange.end);
        if (end < 0)
            return null;
        return new Range(begin, end);
    }

    protected int getLength(GeneFeature.ReferenceRange refRange) {
        int begin = getPosition(refRange.begin);
        if (begin < 0)
            return -1;
        int end = getPosition(refRange.end);
        if (end < 0)
            return -1;
        return Math.abs(end - begin);
    }

    protected boolean isAvailable(GeneFeature.ReferenceRange refRange) {
        int begin = getPosition(refRange.begin);
        if (begin < 0)
            return false;
        int end = getPosition(refRange.end);
        if (end < 0)
            return false;
        return true;
    }

    /**
     * Returns a relative range of specified {@code subFeature} in specified {@code feature} or null if this is not
     * available.
     *
     * @param feature    gene feature
     * @param subFeature a part of feature
     * @return relative range of specified {@code subFeature} in specified {@code feature} or null if this is not
     * available
     */
    public Range getRelativeRange(GeneFeature feature, GeneFeature subFeature) {
        // Get target ranges of
        Range[] featureRanges = getRanges(feature);
        if (featureRanges == null)
            return null;
        Range[] subFeatureRanges = getRanges(subFeature);
        if (subFeatureRanges == null)
            return null;
        int offset = 0, begin = -1, end = -1;
        int subFeaturePointer = 0;
        int state = 0; // 0 - before; 1 - rightOnBegin; 2 - inside
        for (Range range : featureRanges) {
            int from = subFeatureRanges[subFeaturePointer].getFrom();
            if (state == 0
                    && range.containsBoundary(from)
                    && subFeatureRanges[subFeaturePointer].hasSameDirection(range)) {
                state = 1;
                begin = offset + range.convertBoundaryToRelativePosition(from);
            }

            int to = subFeatureRanges[subFeaturePointer].getTo();
            if (state > 0
                    && subFeaturePointer == subFeatureRanges.length - 1) {
                if (!range.containsBoundary(to))
                    return null;
                end = offset + range.convertBoundaryToRelativePosition(to);
                break;
            }

            if (state == 1) {
                if (to != range.getTo())
                    return null;
                state = 2;
                ++subFeaturePointer;
            } else if (state == 2) {
                if (!subFeatureRanges[subFeaturePointer].equals(range))
                    return null;
                ++subFeaturePointer;
            }

            offset += range.length();
        }
        if (begin == -1 || end == -1)
            return null;
        return new Range(begin, end);
    }

    /**
     * Returns a relative position of specified {@code referencePoint} in specified {@code feature} or -1 if this
     * position is not available.
     *
     * @param feature        gene feature
     * @param referencePoint reference point
     * @return a relative position of specified {@code referencePoint} in specified {@code feature} or -1 if this
     * position is not available
     */
    public int getRelativePosition(GeneFeature feature, ReferencePoint referencePoint) {
        int absolutePosition = getPosition(referencePoint);
        if (absolutePosition == -1)
            return -1;
        Range[] ranges = getRanges(feature);
        if (ranges == null)
            return -1;

        int relativePosition = 0;
        for (int i = 0; i < ranges.length; i++) {
            Range range = ranges[i];
            if (!feature.getReferenceRange(i).isReversed() && range.containsBoundary(absolutePosition))
                return relativePosition + range.convertBoundaryToRelativePosition(absolutePosition);
            else relativePosition += range.length();
        }
        return -1;
    }

    /**
     * Returns absolute position in reference sequence for the specified local position in specified {@code feature}
     * or -1 if this position can't be projected.
     *
     * @param feature           gene feature
     * @param positionInFeature local position in gene feature
     * @return absolute position in reference sequence for the specified local position in specified {@code feature}
     * or -1 if this position can't be projected
     */
    public int getAbsolutePosition(GeneFeature feature, int positionInFeature) {
        if (positionInFeature < 0)
            return -1;

        Range[] ranges = getRanges(feature);
        if (ranges == null)
            return -1;

        for (int i = 0; i < ranges.length; i++) {
            Range range = ranges[i];
            if (positionInFeature > range.length()) {
                positionInFeature -= range.length();
                continue;
            }
            return range.convertBoundaryToAbsolutePosition(positionInFeature);
        }
        return -1;
    }

    /**
     * Calculates translation parameters ( ~ translation frame ) for given gene feature using current sequence
     * partitioning. Return null for untranslatable gene features (like 5'UTR).
     *
     * @param feature target gene feature
     * @return translation parameters
     */
    public TranslationParameters getTranslationParameters(GeneFeature feature) {
        if (!feature.equals(GeneFeature.getCodingGeneFeature(feature)))
            return null;

        if (feature.getFirstPoint().isTripletBoundary() && feature.getLastPoint().isTripletBoundary())
            return TranslationParameters.FromCenter;

        if (feature.getFirstPoint().getWithoutOffset().isTripletBoundary())
            return TranslationParameters.withIncompleteCodon(floorMod(feature.getFirstPoint().getOffset(), 3));

        int featureLength = getLength(feature);

        if (feature.getLastPoint().getWithoutOffset().isTripletBoundary())
            return TranslationParameters.withIncompleteCodon(floorMod(
                    feature.getFirstPoint().getOffset() - featureLength,
                    3));

        int relativePosition;
        for (GeneFeature.ReferenceRange range : feature)
            for (ReferencePoint point : range.getIntermediatePoints())
                if (point.isTripletBoundary())
                    if ((relativePosition = getRelativePosition(feature, point)) >= 0)
                        return TranslationParameters.withIncompleteCodon(relativePosition);

        return null;
    }

    /**
     * Returns RangeTranslationParameters for all ranges in current partitioning that can be transcribed
     *
     * @param length length of original sequence (Integer.MAX_VALUE can be used if value is not known in advance, this
     *               value will be set for final range and must be processed accordingly)
     */
    public List<RangeTranslationParameters> getTranslationParameters(int length) {
        final boolean reversed = isReversed();

        // Creating list of points
        List<PointPosition> points = new ArrayList<>();
        PointPosition previousPoint = new PointPosition(null, 0);
        for (ReferencePoint currentPoint : ReferencePoint.DefaultReferencePoints) {
            int position = getPosition(currentPoint);
            if (position == -1
                    || (position < previousPoint.position && !reversed)
                    || (position > previousPoint.position && reversed))
                continue;
            if (currentPoint.isTripletBoundary() || currentPoint.isCodingSequenceBoundary()) {
                if (previousPoint.point != null
                        && previousPoint.position == position) {
                    if (previousPoint.point.isCodingOnBothSides() && currentPoint.isCodingOnBothSides())
                        if (previousPoint.point.isTripletBoundary())
                            // Skipping this point, it changes nothing (zero-length coding sequence)
                            continue;
                        else
                            // Current point supersedes previous one
                            points.set(points.size() - 1, previousPoint = new PointPosition(currentPoint, position));
                    else if (!previousPoint.point.isTripletBoundary()
                            && !currentPoint.isTripletBoundary()
                            && previousPoint.point.isCodingSequenceOnTheLeft() == currentPoint.isCodingSequenceOnTheRight()) {
                        // Points annihilation
                        points.remove(points.size() - 1);
                        previousPoint = points.get(points.size() - 1);
                    } else
                        // Both points must be considered in the same position
                        points.add(previousPoint = new PointPosition(currentPoint, position));
                } else
                    // Adding point
                    points.add(previousPoint = new PointPosition(currentPoint, position));
            }
        }

        if (points.isEmpty())
            return Collections.EMPTY_LIST;

        RangeTranslationParameters.Accumulator acc = new RangeTranslationParameters.Accumulator();

        // Processing left edge
        if (points.get(0).point.isTripletBoundary()
                && points.get(0).point.isCodingSequenceOnTheLeft())
            acc.put(new RangeTranslationParameters(null, points.get(0).point,
                    new Range(reversed ? length : 0, points.get(0).position)));

        // Processing intermediate ranges
        for (int i = 1; i < points.size(); i++)
            if (points.get(i - 1).point.isCodingSequenceOnTheRight()
                    && points.get(i).point.isCodingSequenceOnTheLeft()
                    && points.get(i - 1).position != points.get(i).position)
                acc.put(new RangeTranslationParameters(
                        points.get(i - 1).point, points.get(i).point,
                        new Range(points.get(i - 1).position, points.get(i).position)));

        // Processing right edge
        if (points.get(points.size() - 1).point.isCodingSequenceOnTheRight()
                && points.get(points.size() - 1).point.isTripletBoundary())
            acc.put(new RangeTranslationParameters(
                    points.get(points.size() - 1).point,
                    null,
                    new Range(points.get(points.size() - 1).position, reversed ? 0 : length)));

        List<RangeTranslationParameters> result = acc.getResult();

        // Adding codon leftovers
        for (int i = 1; i < result.size(); i++) {
            if (result.get(i - 1).acceptCodonLeftover() && result.get(i).leftIncompleteCodonRange() != null)
                result.set(i - 1, result.get(i - 1).withCodonLeftover(result.get(i).leftIncompleteCodonRange()));
            if (result.get(i).acceptCodonLeftover() && result.get(i - 1).rightIncompleteCodonRange() != null)
                result.set(i, result.get(i).withCodonLeftover(result.get(i - 1).rightIncompleteCodonRange()));
        }

        return result;
    }

    private static final class PointPosition {
        final ReferencePoint point;
        final int position;

        public PointPosition(ReferencePoint point, int position) {
            this.point = point;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PointPosition)) return false;

            PointPosition that = (PointPosition) o;

            if (position != that.position) return false;
            return point.equals(that.point);
        }

        @Override
        public int hashCode() {
            int result = point.hashCode();
            result = 31 * result + position;
            return result;
        }
    }

    public static int floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }
}
