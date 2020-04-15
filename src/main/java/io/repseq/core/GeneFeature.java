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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.milaboratory.primitivio.annotations.Serializable;
import com.milaboratory.util.ArrayIterator;
import com.milaboratory.util.ParseUtil;
import io.repseq.util.Doc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static io.repseq.core.ReferencePoint.*;

//DRegion
//DRegion(-10, +6)
//DRegionBegin(-10):JRegionEnd(+6)
//DRegionBegin(-10):DRegionBegin(+6)
//DRegionBegin(-10, +6)

//[DRegionBegin(-10, +6), DRegionBegin(-10, +6)]
@JsonDeserialize(using = GeneFeature.Deserializer.class)
@JsonSerialize(using = GeneFeature.Serializer.class)
@Serializable(by = GeneFeatureSerializer.class)
public final class GeneFeature implements Iterable<GeneFeature.ReferenceRange>, Comparable<GeneFeature>,
        java.io.Serializable {
    /* V, D, J, Regions */

    public static final int GermlinePRegionSize = 20;

    /* PSegments in rearrenged sequences */
    @Doc("P-segment of V gene")
    public static final GeneFeature VPSegment = new GeneFeature(VEnd, VEndTrimmed);
    @Doc("P-segment of J gene")
    public static final GeneFeature JPSegment = new GeneFeature(JBeginTrimmed, JBegin);
    @Doc("Left P-segment of D gene")
    public static final GeneFeature DLeftPSegment = new GeneFeature(DBeginTrimmed, DBegin);
    @Doc("Right P-segment of D gene")
    public static final GeneFeature DRightPSegment = new GeneFeature(DEnd, DEndTrimmed);


    /* PSegments in germline */

    @Doc("P-segment of V gene to be used as alignment reference")
    public static final GeneFeature GermlineVPSegment = new GeneFeature(VEnd, VEnd.move(-GermlinePRegionSize));
    @Doc("P-segment of J gene to be used as alignment reference")
    public static final GeneFeature GermlineJPSegment = new GeneFeature(JBegin.move(GermlinePRegionSize), JBegin);
    @Doc("P-segment of D gene to be used as alignment reference")
    public static final GeneFeature GermlineDPSegment = new GeneFeature(DEnd, DBegin);


    @Doc("Full V Region; germline")
    public static final GeneFeature VRegion = new GeneFeature(FR1Begin, VEnd);
    @Doc("Full V Region with P-segment; to be used as alignment reference")
    public static final GeneFeature VRegionWithP = VRegion.append(GermlineVPSegment);
    @Doc("Full V Region in rearranged sequence, e.g. after trimming")
    public static final GeneFeature VRegionTrimmed = new GeneFeature(FR1Begin, VEndTrimmed);
    @Doc("Full D Region; germline")
    public static final GeneFeature DRegion = new GeneFeature(DBegin, DEnd);
    @Doc("Full D Region with P-segment; germline; to be used as alignment reference")
    public static final GeneFeature DRegionWithP = GermlineDPSegment.append(DRegion).append(GermlineDPSegment);
    @Doc("Full D Region in rearranged sequence, e.g. after trimming; same as DRegionTrimmed")
    public static final GeneFeature DCDR3Part = new GeneFeature(DBeginTrimmed, DEndTrimmed);
    @Doc("Full D Region in rearranged sequence, e.g. after trimming; same as DCDR3Part")
    public static final GeneFeature DRegionTrimmed = DCDR3Part;
    @Doc("Full J Region; germline")
    public static final GeneFeature JRegion = new GeneFeature(JBegin, FR4End);
    @Doc("Full J Region with P-segment; to be used as alignment reference")
    public static final GeneFeature JRegionWithP = GermlineJPSegment.append(JRegion);
    @Doc("Full J Region in rearranged sequence, e.g. after trimming")
    public static final GeneFeature JRegionTrimmed = new GeneFeature(JBeginTrimmed, FR4End);


    /* Major gene parts */

    @Doc("5'UTR; germline")
    public static final GeneFeature V5UTRGermline = new GeneFeature(UTR5Begin, V5UTREnd);
    @Doc("5'UTR in aligned sequence; trimmed")
    public static final GeneFeature V5UTR = new GeneFeature(V5UTRBeginTrimmed, V5UTREnd);
    @Doc("Part of lider sequence in first exon. The same as ``Exon1``.")
    public static final GeneFeature L1 = new GeneFeature(L1Begin, L1End);
    @Doc("Intron in V region.")
    public static final GeneFeature VIntron = new GeneFeature(VIntronBegin, VIntronEnd);
    @Doc("Part of lider sequence in second exon.")
    public static final GeneFeature L2 = new GeneFeature(L2Begin, L2End);
    @Doc("``L1`` + ``VIntron`` + ``L2``")
    public static final GeneFeature VLIntronL = new GeneFeature(L1Begin, L2End);

    /* Frameworks and CDRs */

    @Doc("Framework 1")
    public static final GeneFeature FR1 = new GeneFeature(FR1Begin, FR1End);
    @Doc("CDR1 (Complementarity determining region 1)")
    public static final GeneFeature CDR1 = new GeneFeature(CDR1Begin, CDR1End);
    @Doc("Framework 2")
    public static final GeneFeature FR2 = new GeneFeature(FR2Begin, FR2End);
    @Doc("CDR2 (Complementarity determining region 2)")
    public static final GeneFeature CDR2 = new GeneFeature(CDR2Begin, CDR2End);
    @Doc("Framework 2")
    public static final GeneFeature FR3 = new GeneFeature(FR3Begin, FR3End);
    @Doc("CDR3 (Complementarity determining region 3). Cys from V region and Phe/Trp from J region included.")
    public static final GeneFeature CDR3 = new GeneFeature(CDR3Begin, CDR3End);
    @Doc("CDR3 (Complementarity determining region 3). Cys from V region and Phe/Trp from J region excluded.")
    public static final GeneFeature ShortCDR3 = new GeneFeature(CDR3, +3, -3);
    @Doc("Framework 4 (J region after CDR3)")
    public static final GeneFeature FR4 = new GeneFeature(FR4Begin, FR4End);

    /* Subregions of CDR3 */

    @Doc("Part of V region inside CDR3 (commonly starts from Cys)")
    public static final GeneFeature VCDR3Part = new GeneFeature(CDR3Begin, VEndTrimmed);
    @Doc("Part of J region inside CDR3 (commonly ends with Phe/Trp)")
    public static final GeneFeature JCDR3Part = new GeneFeature(JBeginTrimmed, CDR3End);
    @Doc("Part of V region inside CDR3 (commonly starts from Cys)")
    public static final GeneFeature GermlineVCDR3Part = new GeneFeature(CDR3Begin, VEnd);
    @Doc("Part of J region inside CDR3 (commonly ends with Phe/Trp)")
    public static final GeneFeature GermlineJCDR3Part = new GeneFeature(JBegin, CDR3End);
    @Doc("N region between V and D genes; not defined for loci without D genes and for V(D)J rearrangement " +
            "with unidentified D region.")
    public static final GeneFeature VDJunction = new GeneFeature(VEndTrimmed, DBeginTrimmed);
    @Doc("N region between V and D genes; not defined for loci without D genes and for V(D)J rearrangement " +
            "with unidentified D region.")
    public static final GeneFeature DJJunction = new GeneFeature(DEndTrimmed, JBeginTrimmed);
    @Doc("Region between V and J regions. For loci without D genes - fully composed from non-template nucleotides. May contain D region.")
    public static final GeneFeature VJJunction = new GeneFeature(VEndTrimmed, JBeginTrimmed);

    /* Exons. */

    @Doc("First exon. The same as ``L1``.")
    public static final GeneFeature Exon1 = new GeneFeature(L1Begin, L1End);
    @Doc("Full second exon of IG/TCR gene.")
    public static final GeneFeature Exon2 = new GeneFeature(L2Begin, FR4End);

    /* Region Exons */

    @Doc("Second exon of V gene.")
    public static final GeneFeature VExon2 = new GeneFeature(L2Begin, VEnd);

    @Doc("Second exon of V gene trimmed. Ends within CDR3 in V(D)J rearrangement.")
    public static final GeneFeature VExon2Trimmed = new GeneFeature(L2Begin, VEndTrimmed);

    /* C Region */

    @Doc("First exon of C Region")
    public static final GeneFeature CExon1 = new GeneFeature(CBegin, CExon1End);

    @Doc("Full C region")
    public static final GeneFeature CRegion = new GeneFeature(CBegin, CEnd);

    /* Composite features */

    @Doc("Full leader sequence")
    public static final GeneFeature L = new GeneFeature(L1, L2);

    @Doc("``Exon1`` + ``VExon2``. Common reference feature used in alignments for mRNA data obtained without 5'RACE.")
    public static final GeneFeature VTranscriptWithout5UTR = new GeneFeature(Exon1, VExon2);
    @Doc("``V5UTR`` + ``Exon1`` + ``VExon2``. Common reference feature used in alignments for cDNA data obtained using 5'RACE (that may contain UTRs).")
    public static final GeneFeature VTranscript = new GeneFeature(V5UTRGermline, Exon1, VExon2);
    @Doc("``{V5UTRBegin:VEnd}``. Common reference feature used in alignments for genomic DNA data.")
    public static final GeneFeature VGene = new GeneFeature(UTR5Begin, VEnd);

    @Doc("``Exon1`` + ``VExon2``. Common reference feature used in alignments for mRNA data obtained without 5'RACE. Contains reference for P region.")
    public static final GeneFeature VTranscriptWithout5UTRWithP = new GeneFeature(Exon1, VExon2, GermlineVPSegment);
    @Doc("``V5UTR`` + ``Exon1`` + ``VExon2``. Common reference feature used in alignments for cDNA data obtained using 5'RACE (that may contain UTRs). Contains reference for P region.")
    public static final GeneFeature VTranscriptWithP = new GeneFeature(V5UTRGermline, Exon1, VExon2, GermlineVPSegment);
    @Doc("``{V5UTRBegin:VEnd}``. Common reference feature used in alignments for genomic DNA data. Contains reference for P region.")
    public static final GeneFeature VGeneWithP = new GeneFeature(UTR5Begin, VEnd).append(GermlineVPSegment);

    @Doc("First two exons of IG/TCR gene.")
    public static final GeneFeature VDJTranscriptWithout5UTR = new GeneFeature(Exon1, Exon2);
    @Doc("First two exons with 5'UTR of IG/TCR gene.")
    public static final GeneFeature VDJTranscript = new GeneFeature(V5UTRGermline, Exon1, Exon2);

    /* Full length assembling features */
    @Doc("Full V, D, J assembly without 5'UTR and leader sequence.")
    public static final GeneFeature VDJRegion = new GeneFeature(FR1Begin, FR4End);


    //regions are sorted in natural ordering using indexes
    final ReferenceRange[] regions;

    public GeneFeature(final GeneFeature... features) {
        if (features.length == 0)
            throw new IllegalArgumentException();
        int total = 0;
        for (GeneFeature feature : features)
            total += feature.regions.length;
        ReferenceRange[] regions = new ReferenceRange[total];
        int offset = 0;
        for (GeneFeature feature : features) {
            System.arraycopy(feature.regions, 0, regions, offset, feature.regions.length);
            offset += feature.regions.length;
        }
        this.regions = merge(regions);
    }

    public GeneFeature(ReferencePoint begin, ReferencePoint end) {
        this.regions = new ReferenceRange[]{
                new ReferenceRange(begin, end)};
    }

    public GeneFeature(GeneFeature feature, int leftOffset, int rightOffset) {
        this.regions = feature.regions.clone();
        ReferenceRange r = this.regions[0];
        this.regions[0] = new ReferenceRange(r.begin.move(leftOffset), r.end);
        r = this.regions[this.regions.length - 1];
        this.regions[this.regions.length - 1] = new ReferenceRange(r.begin, r.end.move(rightOffset));
    }

    public GeneFeature(ReferencePoint referencePoint, int leftOffset, int rightOffset) {
        this.regions = new ReferenceRange[]{
                new ReferenceRange(referencePoint.move(leftOffset), referencePoint.move(rightOffset))};
    }

    private GeneFeature(ReferenceRange range) {
        this.regions = new ReferenceRange[]{range};
    }

    GeneFeature(ReferenceRange[] regions, boolean unsafe) {
        assert unsafe;
        this.regions = regions;
    }

    public ReferenceRange getReferenceRange(int i) {
        return regions[i];
    }

    public GeneFeature append(GeneFeature gf) {
        return new GeneFeature(this, gf);
    }

    public int size() {
        return regions.length;
    }

    public boolean hasReversedRegions() {
        for (ReferenceRange region : regions)
            if (region.isReversed())
                return true;
        return false;
    }

    public GeneFeature reverse() {
        ReferenceRange[] res = new ReferenceRange[regions.length];
        for (int i = 0; i < res.length; i++)
            res[i] = regions[regions.length - 1 - i].reverse();
        return new GeneFeature(res, true);
    }

    public GeneType getGeneType() {
        GeneType gt = regions[0].getGeneType(), tmp;

        if (gt == null)
            return null;

        for (int i = 1; i < regions.length; i++)
            if (regions[i].getGeneType() != gt)
                return null;

        return gt;
    }

    /**
     * Return true if this gene feature contains more then one disjoint region
     */
    public boolean isComposite() {
        return regions.length != 1;
    }

    /**
     * Return true if contains at least one alignment attached point
     */
    public boolean isAlignmentAttached() {
        for (ReferenceRange region : regions)
            if (region.begin.isAttachedToAlignmentBound() ||
                    region.end.isAttachedToAlignmentBound())
                return true;
        return false;
    }

    @Override
    public String toString() {
        return Arrays.toString(regions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneFeature feature = (GeneFeature) o;

        return Arrays.equals(regions, feature.regions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(regions);
    }

    private ReferenceRange firstRegion() {
        return regions[0];
    }

    private ReferenceRange lastRegion() {
        return regions[regions.length - 1];
    }

    GeneFeature first() {
        return new GeneFeature(firstRegion());
    }

    GeneFeature last() {
        return new GeneFeature(lastRegion());
    }

    GeneFeature withoutFirst() {
        return new GeneFeature(Arrays.copyOfRange(regions, 1, regions.length), true);
    }

    GeneFeature withoutLast() {
        return new GeneFeature(Arrays.copyOf(regions, regions.length - 1), true);
    }

    public static GeneFeature intersectionStrict(GeneFeature gf1, GeneFeature gf2) {
        return intersection(gf1, gf2, true);
    }

    public static GeneFeature intersection(GeneFeature gf1, GeneFeature gf2) {
        return intersection(gf1, gf2, false);
    }

    public static GeneFeature intersection(GeneFeature gf1, GeneFeature gf2, boolean strict) {
        //tnj t,exbq rjcnskm
        GeneFeature gf1left = null, gf1right = null;
        if (gf1.firstRegion().isReversed()) {
            gf1left = gf1.first();
            gf1 = gf1.withoutFirst();
        }
        if (gf1.lastRegion().isReversed()) {
            gf1right = gf1.last();
            gf1 = gf1.withoutLast();
        }
        GeneFeature gf2left = null, gf2right = null;
        if (gf2.firstRegion().isReversed()) {
            gf2left = gf2.first();
            gf2 = gf2.withoutFirst();
        }
        if (gf2.lastRegion().isReversed()) {
            gf2right = gf2.last();
            gf2 = gf2.withoutLast();
        }

        GeneFeature gfLeft = strict ?
                intersection1RStrict(gf1left, gf2left, gf1, gf2) :
                intersection1R(gf1left, gf2left, gf1, gf2);
        GeneFeature gfRight = strict ?
                intersection1RStrict(gf1right, gf2right, gf1, gf2) :
                intersection1R(gf1right, gf2right, gf1, gf2);

        GeneFeature r = intersection0(gf1, gf2);

        if (gfLeft != null)
            r = new GeneFeature(gfLeft, r);
        if (gfRight != null)
            r = new GeneFeature(r, gfRight);

        return r;
    }

    private static GeneFeature reverse(GeneFeature gf) {
        return gf == null ? null : gf.reverse();
    }

    private static GeneFeature intersection1RStrict(GeneFeature gf1r, GeneFeature gf2r, GeneFeature gf1, GeneFeature gf2) {
        if (gf1r == null || gf2r == null)
            return null;
        else
            return reverse(intersection0(gf1r.reverse(), gf2r.reverse()));
    }

    private static GeneFeature intersection1R(GeneFeature gf1r, GeneFeature gf2r, GeneFeature gf1, GeneFeature gf2) {
        if (gf1r == null && gf2r == null)
            return null;

        if (gf1r == null)
            return reverse(intersection0(gf2r.reverse(), gf1));

        if (gf2r == null)
            return reverse(intersection0(gf1r.reverse(), gf2));

        return reverse(intersection0(gf1r.reverse(), gf2r.reverse()));
    }

    private static GeneFeature intersection0(GeneFeature gf1, GeneFeature gf2) {
        ReferencePoint firstReferencePoint1 = gf1.regions[0].begin;
        ReferencePoint firstReferencePoint2 = gf2.regions[0].begin;
        if (firstReferencePoint1.compareTo(firstReferencePoint2) > 0)
            return intersection(gf2, gf1);
        int rangePointer1 = 0;
        while (gf1.regions[rangePointer1].end.compareTo(firstReferencePoint2) <= 0)
            if (++rangePointer1 == gf1.regions.length)
                return null;

        if (gf1.regions[rangePointer1].begin.compareTo(firstReferencePoint2) > 0)
            throw new IllegalArgumentException();

        ArrayList<ReferenceRange> result = new ArrayList<>();
//        result.add(new ReferenceRange(firstReferencePoint2, gf1.regions[rangePointer1].end));
//
//        ++rangePointer1;
//        int rangePointer2 = 1;
        int rangePointer2 = 0;

        while (true) {
            if (rangePointer1 == gf1.regions.length || rangePointer2 == gf2.regions.length)
                break;

            if (rangePointer2 != 0 &&
                    !gf1.regions[rangePointer1].begin.equals(gf2.regions[rangePointer2].begin))
                throw new IllegalArgumentException();

            int c = gf1.regions[rangePointer1].end.compareTo(gf2.regions[rangePointer2].end);
            ReferencePoint maxBegin = max(gf1.regions[rangePointer1].begin, gf2.regions[rangePointer2].begin);
            if (c != 0) {
                if (c > 0) {
                    result.add(new ReferenceRange(maxBegin,
                            gf2.regions[rangePointer2].end));
                    if (rangePointer2 == gf2.regions.length - 1)
                        break;
                    ++rangePointer2;
                } else {
                    result.add(new ReferenceRange(maxBegin,
                            gf1.regions[rangePointer1].end));
                    if (rangePointer1 == gf1.regions.length - 1)
                        break;
                    ++rangePointer1;
                }
            } else {
                result.add(new ReferenceRange(maxBegin, gf1.regions[rangePointer1].end));

                ++rangePointer1;
                ++rangePointer2;
            }
        }

        return new GeneFeature(result.toArray(new ReferenceRange[result.size()]), true);
    }

    public ReferencePoint getFirstPoint() {
        return regions[0].begin;
    }

    public ReferencePoint getLastPoint() {
        return regions[regions.length - 1].end;
    }

    private static ReferencePoint min(ReferencePoint p1, ReferencePoint p2) {
        if (p1.compareTo(p2) > 0)
            return p2;
        else
            return p1;
    }

    private static ReferencePoint max(ReferencePoint p1, ReferencePoint p2) {
        if (p1.compareTo(p2) > 0)
            return p1;
        else
            return p2;
    }

    public boolean contains(GeneFeature geneFeature) {
        if (geneFeature.isComposite())
            throw new RuntimeException("Composite features are not supported.");
        for (ReferenceRange region : regions)
            if (region.contains(geneFeature.regions[0]))
                return true;
        return false;
    }

    /**
     * Special value
     */
    private static final ReferencePoint NULL_FRAME = new ReferencePoint(BasicReferencePoint.V5UTRBegin);
    /**
     * Cache for getFrameReference method
     */
    private static final Map<GeneFeature, ReferencePoint> frameReferenceCache = new HashMap<>();

    /**
     * Returns reference point that is triplet boundary (so defines reading frame) inside provided gene feature or
     * returns null.
     *
     * @param feature gene feature
     * @return reference point that is triplet boundary (so defines reading frame) inside provided gene feature or null
     */
    public static synchronized ReferencePoint getFrameReference(GeneFeature feature) {
        ReferencePoint rp = frameReferenceCache.get(feature);
        if (rp == null) {
            OUTER:
            for (ReferenceRange region : feature.regions)
                for (ReferencePoint intermediatePoint : region.getIntermediatePoints())
                    if (intermediatePoint.isTripletBoundary()) {
                        frameReferenceCache.put(feature, rp = intermediatePoint);
                        break OUTER;
                    }
            if (rp == null)
                // Caching null result
                frameReferenceCache.put(feature, rp = NULL_FRAME);
        }
        return rp == NULL_FRAME ? null : rp;
    }

    /**
     * Special value
     */
    private static final GeneFeature NULL_GENE_FEATURE = new GeneFeature(UTR5Begin, UTR5Begin);
    /**
     * Cache for getFrameReference method
     */
    private static final Map<GeneFeature, GeneFeature> codingGeneFeaturesCache = new HashMap<>();

    /**
     * Returns coding gene feature contained in input gene feature
     *
     * @param feature input gene feature
     * @return coding gene feature contained in input gene feature or null
     */
    public static synchronized GeneFeature getCodingGeneFeature(GeneFeature feature) {
        GeneFeature result = codingGeneFeaturesCache.get(feature);
        if (result == null) {

            List<ReferenceRange> resultRanges = new ArrayList<>();
            ReferencePoint previousPoint = null, lastPoint = null;

            for (ReferenceRange region : feature.regions) {
                previousPoint = null;

                for (ReferencePoint intermediatePoint : region.getBoundaryAndIntermediatePoints()) {
                    if (previousPoint == null && intermediatePoint.isCodingSequenceOnTheRight())
                        previousPoint = intermediatePoint;
                    else if (previousPoint != null && !intermediatePoint.isCodingSequenceOnTheRight()) {
                        if (!intermediatePoint.isCodingSequenceOnTheLeft())
                            throw new IllegalArgumentException(
                                    "Can't calculate coding feature for " + feature + ".");
                        resultRanges.add(new ReferenceRange(previousPoint, intermediatePoint));
                        previousPoint = null;
                    }
                    lastPoint = intermediatePoint;
                }

                if (previousPoint != null && previousPoint != lastPoint) {
                    if (!lastPoint.isCodingSequenceOnTheLeft())
                        throw new IllegalArgumentException(
                                "Can't calculate coding feature for " + feature + ".");
                    resultRanges.add(new ReferenceRange(previousPoint, lastPoint));
                }
            }

            if (resultRanges.isEmpty())
                // Caching null result
                codingGeneFeaturesCache.put(feature, result = NULL_GENE_FEATURE);
            else
                codingGeneFeaturesCache.put(feature, result = new GeneFeature(
                        resultRanges.toArray(new ReferenceRange[resultRanges.size()]), true));

        }
        return result == NULL_GENE_FEATURE ? null : result;
    }

    private static ReferenceRange[] merge(final ReferenceRange[] ranges) {
        if (ranges.length == 1)
            return ranges;
        Arrays.sort(ranges, ReferenceRange.BEGIN_COMPARATOR);
        ArrayList<ReferenceRange> result = new ArrayList<>(ranges.length);

        ReferenceRange prev = ranges[0], cur;
        for (int i = 1; i < ranges.length; ++i) {
            cur = ranges[i];
            if (cur.begin.compareTo(prev.end) < 0)
                throw new IllegalArgumentException("Intersecting ranges: " + cur + " and " + prev);
            if (cur.begin.equals(prev.end) && cur.isReversed() == prev.isReversed()) {
                //merge
                prev = new ReferenceRange(prev.begin, cur.end);
            } else {
                result.add(prev);
                prev = cur;
            }
        }
        result.add(prev);
        if (result.size() == ranges.length)
            return ranges;
        return result.toArray(new ReferenceRange[result.size()]);
    }

    @Override
    public int compareTo(GeneFeature o) {
        return getFirstPoint().compareTo(o.getFirstPoint());
    }

    @Override
    public Iterator<ReferenceRange> iterator() {
        return new ArrayIterator<>(regions);
    }

    @Serializable(by = IO.GeneFeatureReferenceRangeSerializer.class)
    public static final class ReferenceRange implements java.io.Serializable {
        //sorting using only begin index
        private static final Comparator<ReferenceRange> BEGIN_COMPARATOR = new Comparator<ReferenceRange>() {
            @Override
            public int compare(ReferenceRange o1, ReferenceRange o2) {
                return o1.getLeftBoundary().compareTo(o2.getLeftBoundary());
            }
        };

        public final ReferencePoint begin, end;

        ReferenceRange(ReferencePoint begin, ReferencePoint end) {
            this.begin = begin;
            this.end = end;
        }

        public List<ReferencePoint> getBoundaryAndIntermediatePoints() {
            List<ReferencePoint> points = getIntermediatePoints();
            if(!begin.equals(points.get(0)))
                points.add(0, begin);
            if(!end.equals(points.get(points.size() - 1)))
                points.add(end);
            return points;
        }

        public List<ReferencePoint> getIntermediatePoints() {
            List<ReferencePoint> rps = new ArrayList<>();
            if (begin.offset != 0)
                rps.add(begin);
            for (int i = begin.basicPoint.index; i <= end.basicPoint.index; i++) {
                ReferencePoint rp = new ReferencePoint(BasicReferencePoint.getByIndex(i));
                if (rp.compareTo(begin) < 0)
                    continue;
                if (rp.compareTo(end) > 0)
                    continue;
                rps.add(rp);
            }
            if (end.offset != 0)
                rps.add(end);
            return rps;
        }

        public boolean isReversed() {
            return begin.compareTo(end) > 0;
        }

        public ReferenceRange reverse() {
            return new ReferenceRange(end, begin);
        }

        public GeneType getGeneType() {
            GeneType gt = begin.getGeneType();
            if (gt == null)
                return null;
            if (gt != end.getGeneType())
                return null;
            return gt;
        }

        public boolean hasOffsets() {
            return begin.offset != 0 || end.offset != 0;
        }

        public ReferenceRange getWithoutOffset() {
            if (!hasOffsets())
                return this;
            return new ReferenceRange(begin.getWithoutOffset(), end.getWithoutOffset());
        }

        public boolean contains(ReferenceRange range) {
            return range.begin.compareTo(begin) >= 0 && range.end.compareTo(end) <= 0;
        }

        public ReferencePoint getLeftBoundary() {
            if (isReversed())
                return end;
            else
                return begin;
        }

        public ReferencePoint getRightBoundary() {
            if (isReversed())
                return begin;
            else
                return end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ReferenceRange range = (ReferenceRange) o;

            return begin.equals(range.begin) && end.equals(range.end);
        }

        @Override
        public int hashCode() {
            int result = begin.hashCode();
            result = 31 * result + end.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "[" + begin + ", " + end + "]";
        }
    }

    public static GeneFeature getRegion(GeneType type) {
        switch (type) {
            case Variable:
                return VRegion;
            case Diversity:
                return DRegion;
            case Joining:
                return JRegion;
            case Constant:
                return CRegion;
        }
        throw new RuntimeException();
    }

    static Map<String, GeneFeature> featuresByName = null;
    static Map<GeneFeature, String> nameByFeature = null;

    private static void ensureInitialized() {
        if (featuresByName == null) {
            synchronized (GeneFeature.class) {
                if (featuresByName == null) {
                    try {
                        Map<String, GeneFeature> fbn = new HashMap<>();
                        Map<GeneFeature, String> nbf = new HashMap<>();
                        Field[] declaredFields = GeneFeature.class.getDeclaredFields();
                        for (Field field : declaredFields)
                            if (Modifier.isStatic(field.getModifiers()) &&
                                    field.getType() == GeneFeature.class) {
                                GeneFeature value = (GeneFeature) field.get(null);
                                String name = field.getName();
                                fbn.put(name.toLowerCase(), value);
                                nbf.put(value, name);
                            }
                        featuresByName = fbn;
                        nameByFeature = nbf;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public static GeneFeature getFeatureByName(String pointName) {
        ensureInitialized();
        return featuresByName.get(pointName.toLowerCase());
    }

    public static String getNameByFeature(GeneFeature point) {
        ensureInitialized();
        return nameByFeature.get(point);
    }

    public static GeneFeature parse(String string) {
        string = string.replaceAll(" ", "");
        if ("null".equals(string))
            return null;

        String[] singles = ParseUtil.splitWithBrackets(string, '+', "(){}");
        ArrayList<GeneFeature> features = new ArrayList<>(singles.length);
        for (String single : singles)
            features.add(parseSingle(single));
        return new GeneFeature(features.toArray(new GeneFeature[features.size()]));
    }

    public static Map<String, GeneFeature> getFeaturesByName() {
        ensureInitialized();
        return Collections.unmodifiableMap(featuresByName);
    }

    public static Map<GeneFeature, String> getNameByFeature() {
        ensureInitialized();
        return Collections.unmodifiableMap(nameByFeature);
    }

    private static GeneFeature parseSingle(String string) {
        string = string.trim();
        if ("null".equals(string))
            return null;
        //single feature
        if (string.charAt(0) == '{') { // feature by points {from:to}
            if (string.charAt(string.length() - 1) != '}')
                throw new IllegalArgumentException("Incorrect input: " + string);
            string = string.substring(1, string.length() - 1);
            String[] fromTo = string.split(":");
            if (fromTo.length != 2)
                throw new IllegalArgumentException("Incorrect input: " + string);
            return new GeneFeature(ReferencePoint.parse(fromTo[0]), ReferencePoint.parse(fromTo[1]));
        } else { // feature by name CDR2(-2,3)
            int br = string.indexOf('(');

            if (br == -1) {
                GeneFeature base;
                base = getFeatureByName(string);
                if (base == null)
                    throw new IllegalArgumentException("Unknown feature: " + string);
                return base;
            } else {
                if (string.charAt(string.length() - 1) != ')')
                    throw new IllegalArgumentException("Wrong syntax: " + string);

                Object base;

                String baseName = string.substring(0, br);

                base = getFeatureByName(baseName);

                if (base == null)
                    base = ReferencePoint.getPointByName(baseName);

                if (base == null)
                    throw new IllegalArgumentException("Unknown feature / anchor point: " + baseName);

                int offset1, offset2;
                String[] offsets = string.substring(br + 1, string.length() - 1).split(",");
                try {
                    offset1 = Integer.parseInt(offsets[0].trim());
                    offset2 = Integer.parseInt(offsets[1].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Incorrect input: " + string);
                }
                if (base instanceof GeneFeature)
                    return new GeneFeature((GeneFeature) base, offset1, offset2);
                else
                    return new GeneFeature((ReferencePoint) base, offset1, offset2);
            }
        }
    }

    public static String encode(GeneFeature feature) {
        return encode(feature, true);
    }

    static String encode(GeneFeature feature, boolean compact) {
        ensureInitialized();
        if (compact) {
            String s = nameByFeature.get(feature);
            if (s != null)
                return s;
        }
        Collection<GeneFeature> available = featuresByName.values();
        final String[] encodes = new String[feature.regions.length];
        out:
        for (int i = 0; i < encodes.length; ++i) {
            ReferenceRange region = feature.regions[i];
            if (compact) {
                String base = null;
                for (GeneFeature a : available)
                    if (match(region, a)) {
                        GeneFeature known = new GeneFeature(region.getWithoutOffset());
                        base = getNameByFeature(known);
                    }

                if (region.begin.basicPoint == region.end.basicPoint)
                    base = ReferencePoint.encode(region.begin.getWithoutOffset(), true);

                if (base != null) {
                    if (region.hasOffsets())
                        base += "(" + region.begin.offset + "," + region.end.offset + ")";
                    encodes[i] = base;
                    continue out;
                }
            }
            encodes[i] = "{" + ReferencePoint.encode(region.begin, true) + ":" + ReferencePoint.encode(region.end, false) + "}";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; i++) {
            sb.append(encodes[i]);
            if (i == encodes.length - 1)
                break;
            sb.append("+");
        }
        return sb.toString();
    }

    public static boolean match(ReferenceRange a, GeneFeature b) {
        if (b.isComposite())
            return false;
        return a.begin.basicPoint == b.regions[0].begin.basicPoint
                && a.end.basicPoint == b.regions[0].end.basicPoint;
    }

    public static class Deserializer extends JsonDeserializer<GeneFeature> {
        @Override
        public GeneFeature deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return parse(jp.readValueAs(String.class));
        }
    }

    public static final class Serializer extends JsonSerializer<GeneFeature> {
        @Override
        public void serialize(GeneFeature value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            String name = encode(value);
            if (name == null)
                throw new RuntimeException("Not yet supported.");
            jgen.writeString(name);
        }
    }

}
