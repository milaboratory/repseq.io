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


enum BasicReferencePoint implements java.io.Serializable {
    // Points in V
    V5UTRBegin(0, GeneType.Variable, 0, false, false, false),
    V5UTRBeginTrimmed(-1, GeneType.Variable, 1, false, false, false, "V5UTREnd"),
    V5UTREndL1Begin(1, GeneType.Variable, 2, false, true, true),
    L1EndVIntronBegin(2, GeneType.Variable, 3, true, false, false),
    VIntronEndL2Begin(3, GeneType.Variable, 4, false, true, false),
    L2EndFR1Begin(4, GeneType.Variable, 5, true, true, true),
    FR1EndCDR1Begin(5, GeneType.Variable, 6, true, true, true),
    CDR1EndFR2Begin(6, GeneType.Variable, 7, true, true, true),
    FR2EndCDR2Begin(7, GeneType.Variable, 8, true, true, true),
    CDR2EndFR3Begin(8, GeneType.Variable, 9, true, true, true),
    FR3EndCDR3Begin(9, GeneType.Variable, 10, true, true, true),
    VEndTrimmed(-2, GeneType.Variable, 11, true, true, false, "CDR3Begin(-3)"),
    VEnd(10, GeneType.Variable, 12, true, true, false),

    // Points in D
    DBegin(11, GeneType.Diversity, 13, true, true, false),
    DBeginTrimmed(-1, GeneType.Diversity, 14, true, true, false, null),
    DEndTrimmed(-2, GeneType.Diversity, 15, true, true, false, null),
    DEnd(12, GeneType.Diversity, 16, true, true, false),

    // Points in J
    JBegin(13, GeneType.Joining, 17, true, true, false),
    JBeginTrimmed(-1, GeneType.Joining, 18, true, true, false, "CDR3End(+3)"),
    CDR3EndFR4Begin(14, GeneType.Joining, 19, true, true, true),
    FR4End(15, GeneType.Joining, 20, true, false, false),

    // Points in C
    CBegin(16, GeneType.Constant, 21, false, true, false),
    CExon1End(17, GeneType.Constant, 22, true, false, false),
    CEnd(18, GeneType.Constant, 23, false, false, false);

    final int extendedIndex;
    final int index;
    final GeneType geneType;
    final boolean codingSequenceOnTheLeft, codingSequenceOnTheRight, isTripletBoundary;
    BasicReferencePoint trimmedVersion;

    /* Only for trimmed (attached to alignment boundary) points */

    // Defined for alignment boundary attached reference points
    // E.g. V5UTRBeginTrimmed is a left boundary of an alignment but only if it is on the left side of V5UTREnd/L1Begin
    // Not an object to solve cyclic dependence on ReferencePoint
    final String activationPointString;
    volatile ReferencePoint activationPoint;


    BasicReferencePoint(int index, GeneType geneType, int extendedIndex, boolean codingSequenceOnTheLeft,
                        boolean codingSequenceOnTheRight, boolean isTripletBoundary) {
        this(index, geneType, extendedIndex, codingSequenceOnTheLeft, codingSequenceOnTheRight, isTripletBoundary,
                null);
    }

    BasicReferencePoint(int index, GeneType geneType, int extendedIndex, boolean codingSequenceOnTheLeft,
                        boolean codingSequenceOnTheRight, boolean isTripletBoundary, String activationPoint) {
        this.activationPointString = activationPoint;
        this.index = index;
        this.geneType = geneType;
        this.codingSequenceOnTheLeft = codingSequenceOnTheLeft;
        this.codingSequenceOnTheRight = codingSequenceOnTheRight;
        this.isTripletBoundary = isTripletBoundary;
        this.extendedIndex = extendedIndex;
    }

    public static BasicReferencePoint getByIndex(int index) {
        return allBasicReferencePoints[index];
    }

    public static BasicReferencePoint getByExtendedIndex(int index) {
        return allReferencePoints[index];
    }

    public boolean isAttachedToAlignmentBound() {
        return index < 0;
    }

    public boolean isCodingSequenceOnTheLeft() {
        return codingSequenceOnTheLeft;
    }

    public boolean isCodingSequenceOnTheRight() {
        return codingSequenceOnTheRight;
    }

    public boolean isTripletBoundary() {
        return isTripletBoundary;
    }

    /**
     * Returns true if this point is nor attached to any alignments bound.
     *
     * <p>Information about this point is taken from reference sequence's reference points.</p>
     *
     * @return true if this point is nor attached to any alignments bound
     */
    public boolean isPure() {
        return index >= 0;
    }

    public boolean isAttachedToLeftAlignmentBound() {
        assert index < 0;
        return index == -1;
    }

    public boolean isTrimmable() {
        return trimmedVersion != null;
    }

    public ReferencePoint getActivationPoint() {
        if (activationPointString == null)
            return null;

        if (activationPoint == null) {
            synchronized (this) {
                if (activationPoint == null)
                    return activationPoint = ReferencePoint.parse(activationPointString);
            }
        }

        return activationPoint;
    }

    private final static BasicReferencePoint[] allBasicReferencePoints;
    private final static BasicReferencePoint[] allReferencePoints;
    public static final int TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS = 19;

    static {
        allBasicReferencePoints = new BasicReferencePoint[TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS];

        for (BasicReferencePoint rp : values()) {
            if (rp.isAttachedToAlignmentBound())
                continue;
            assert allBasicReferencePoints[rp.index] == null;
            allBasicReferencePoints[rp.index] = rp;
        }

        for (BasicReferencePoint rp : allBasicReferencePoints)
            assert rp != null;

        V5UTRBegin.trimmedVersion = V5UTRBeginTrimmed;
        VEnd.trimmedVersion = VEndTrimmed;
        DBegin.trimmedVersion = DBeginTrimmed;
        DEnd.trimmedVersion = DEndTrimmed;
        JBegin.trimmedVersion = JBeginTrimmed;

        allReferencePoints = new BasicReferencePoint[values().length];
        for (BasicReferencePoint rp : values()) {
            assert allReferencePoints[rp.extendedIndex] == null;
            allReferencePoints[rp.extendedIndex] = rp;
        }
        for (BasicReferencePoint rp : allReferencePoints)
            assert rp != null;
    }
}
