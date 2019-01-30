/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.repseq.core;

import com.milaboratory.core.Range;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.Arrays;

public abstract class AbstractReferencePoints<T extends AbstractReferencePoints<T>> extends SequencePartitioning {
    final int[] points;
    final boolean reversed;

    AbstractReferencePoints(int[] points, boolean[] pointsToCheck) {
        if (points.length != pointsToCheck.length)
            throw new IllegalArgumentException("Illegal length of array.");
        Boolean rev = checkReferencePoints(points, pointsToCheck);
        this.reversed = rev == null ? false : rev;
        this.points = points;
    }

    AbstractReferencePoints(int start, int[] points, boolean[] pointsToCheck) {
        int[] array = new int[pointsToCheck.length];
        Arrays.fill(array, -1);
        System.arraycopy(points, 0, array, start, points.length);
        Boolean rev = checkReferencePoints(array, pointsToCheck);
        this.points = array;
        this.reversed = rev == null ? false : rev;
    }

    protected abstract T create(int[] points);

    protected abstract int indexFromReferencePoint(ReferencePoint point);

    final ReferencePoint referencePointFromIndex(int index) {
        return new ReferencePoint(basicReferencePointFromIndex(index));
    }

    abstract BasicReferencePoint basicReferencePointFromIndex(int index);

    static Boolean checkReferencePoints(int[] points, boolean[] pointsToCheck) {
        Boolean reversed = null;

        int first = -1;

        for (int i = 0; i < points.length; i++) {
            if (!pointsToCheck[i])
                continue;
            int ref = points[i];
            if (ref < -1)
                throw new IllegalArgumentException("Illegal input: " + ref);
            else if (ref > 0)
                if (first == -1)
                    first = ref;
                else if (first != ref) {
                    reversed = first > ref;
                    break;
                }
        }

        if (reversed == null)
            return null;

        int previousPoint = -1;
        for (int i = 0; i < points.length; i++) {
            if (!pointsToCheck[i])
                continue;

            int point = points[i];
            if (point == -1)
                continue;

            if (previousPoint == -1) {
                previousPoint = point;
                continue;
            }

            if (previousPoint != point &&
                    reversed ^ previousPoint > point)
                throw new IllegalArgumentException("Non-monotonic sequence of reference points.");

            previousPoint = point;
        }

        return reversed;
    }

    @Override
    public boolean isReversed() {
        return reversed;
    }

    public int numberOfDefinedPoints() {
        int ret = 0;
        for (int point : points)
            if (point >= 0)
                ++ret;
        return ret;
    }

    public T without(ReferencePoint referencePoint) {
        int[] newPoints = points.clone();
        newPoints[indexFromReferencePoint(referencePoint)] = -1;
        return create(newPoints);
    }

    public T move(int offset) {
        int[] result = new int[points.length];
        for (int i = 0; i < points.length; i++)
            result[i] = points[i] == -1 ? -1 : points[i] + offset;
        return create(result);
    }

    public T relative(Range range) {
        int[] result = new int[points.length];
        for (int i = 0; i < points.length; i++)
            result[i] = points[i] == -1 ? -1 : range.convertBoundaryToRelativePosition(points[i]);
        return create(result);
    }

    private int getPosition(int referencePointIndex) {
        if (referencePointIndex < 0
                || referencePointIndex >= points.length)
            return -1;
        return points[referencePointIndex];
    }

    public int getFirstAvailablePosition() {
        for (int i : points)
            if (i >= 0)
                return i;
        throw new IllegalStateException();
    }

    public int getLastAvailablePosition() {
        for (int i = points.length - 1; i >= 0; --i)
            if (points[i] >= 0)
                return points[i];
        throw new IllegalStateException();
    }

    public Range getContainingRegion() {
        return reversed ?
                new Range(getLastAvailablePosition(), getFirstAvailablePosition()) :
                new Range(getFirstAvailablePosition(), getLastAvailablePosition());
    }

    public int getLengthBetweenBoundaryPoints() {
        if (reversed)
            return getFirstAvailablePosition() - getLastAvailablePosition();
        else
            return getLastAvailablePosition() - getFirstAvailablePosition();
    }

    @Override
    public int getPosition(ReferencePoint referencePoint) {
        int point = getPosition(indexFromReferencePoint(referencePoint));
        if (point < 0)
            return -1;
        return point + (reversed ? -referencePoint.getOffset() : referencePoint.getOffset());
    }

    T getRelativeReferencePoints(GeneFeature geneFeature) {
        int[] newPoints = new int[points.length];
        for (int i = 0; i < points.length; ++i)
            newPoints[i] = getRelativePosition(geneFeature, new ReferencePoint(BasicReferencePoint.getByIndex(i)));
        return create(newPoints);
    }

    T applyMutations(Mutations<NucleotideSequence> mutations) {
        int[] newPoints = new int[points.length];
        for (int i = 0; i < points.length; ++i)
            if (points[i] == -1)
                newPoints[i] = -1;
            else if ((newPoints[i] = mutations.convertToSeq2Position(points[i])) < -1)
                newPoints[i] = ~newPoints[i];
        return create(newPoints);
    }

    GeneFeature getWrappingGeneFeature() {
        int start = 0, end = points.length - 1;
        for (; start < points.length && points[start] == -1; ++start) ;
        for (; end >= start && points[end] == -1; --end) ;
        if (points[start] == -1)
            return null;
        return new GeneFeature(referencePointFromIndex(start), referencePointFromIndex(end));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferencePoints that = (ReferencePoints) o;
        return Arrays.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        int hash = 31;
        for (int i = 0; i < points.length; ++i)
            hash = points[i] + hash * 17;
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(points);
    }
}
