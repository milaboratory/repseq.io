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
    FR4End(15, GeneType.Joining, 20, true, true, false),

    // Points in C
    CBegin(16, GeneType.Constant, 21, true, true, false),
    CExon1End(17, GeneType.Constant, 22, true, true, false),
    CEnd(18, GeneType.Constant, 23, true, true, false);

    final int orderingIndex;
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


    BasicReferencePoint(int index, GeneType geneType, int orderingIndex, boolean codingSequenceOnTheLeft,
                        boolean codingSequenceOnTheRight, boolean isTripletBoundary) {
        this(index, geneType, orderingIndex, codingSequenceOnTheLeft, codingSequenceOnTheRight, isTripletBoundary,
                null);
    }

    BasicReferencePoint(int index, GeneType geneType, int orderingIndex, boolean codingSequenceOnTheLeft,
                        boolean codingSequenceOnTheRight, boolean isTripletBoundary, String activationPoint) {
        this.activationPointString = activationPoint;
        this.index = index;
        this.geneType = geneType;
        this.codingSequenceOnTheLeft = codingSequenceOnTheLeft;
        this.codingSequenceOnTheRight = codingSequenceOnTheRight;
        this.isTripletBoundary = isTripletBoundary;
        this.orderingIndex = orderingIndex;
    }

    public static BasicReferencePoint getByIndex(int index) {
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

    private final static BasicReferencePoint[] allReferencePoints;
    public static final int TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS = 19;

    static {
        allReferencePoints = new BasicReferencePoint[TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS];

        for (BasicReferencePoint rp : values()) {
            if (rp.isAttachedToAlignmentBound())
                continue;
            assert allReferencePoints[rp.index] == null;
            allReferencePoints[rp.index] = rp;
        }

        for (BasicReferencePoint rp : allReferencePoints)
            assert rp != null;

        V5UTRBegin.trimmedVersion = V5UTRBeginTrimmed;
        VEnd.trimmedVersion = VEndTrimmed;
        DBegin.trimmedVersion = DBeginTrimmed;
        DEnd.trimmedVersion = DEndTrimmed;
        JBegin.trimmedVersion = JBeginTrimmed;
    }
}
