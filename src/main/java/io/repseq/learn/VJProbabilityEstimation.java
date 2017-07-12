package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.misc.AtomicDouble;
import io.repseq.learn.misc.AtomicDoubleArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikesh on 7/6/17.
 */
public class VJProbabilityEstimation {
    public static final int MAX_VJ_INS_LEN = 100;
    public final AtomicDoubleArray substitutionMatrix = new AtomicDoubleArray(16),
            insertProbs = new AtomicDoubleArray(16),
            insertSizeProbs = new AtomicDoubleArray(MAX_VJ_INS_LEN);
    public final Map<String, AtomicDoubleArray> trimmingProbs = new HashMap<>();
    public final Map<SegmentTuple, AtomicDouble> vjProbs = new HashMap<>();

    private final SegmentUsage segmentUsage;

    public VJProbabilityEstimation(GermlineSequenceProvider germlineSequenceProvider,
                                   SegmentUsage segmentUsage) {
        for (Map.Entry<String, NucleotideSequence> entry : germlineSequenceProvider.asMap().entrySet()) {
            AtomicDoubleArray trimmingProbArr = new AtomicDoubleArray(entry.getValue().size());
            trimmingProbs.put(entry.getKey(), trimmingProbArr);
        }

        for (SegmentTuple segmentTuple : segmentUsage.getUniqueSegmentTuples()) {
            vjProbs.put(segmentTuple, new AtomicDouble());
        }

        this.segmentUsage = segmentUsage;
    }

    public void update(VJHmmTransitions vjHmmTransitions) {
        SegmentTuple segmentTuple = vjHmmTransitions.getSegments();
        double vjProb = segmentUsage.getProbability(segmentTuple);
        AtomicDoubleArray vTrimmingProb = trimmingProbs.get(segmentTuple.getvId()),
                jTrimmingProb = trimmingProbs.get(segmentTuple.getjId());

        double P = vjHmmTransitions.computePartialProbability();

        // VJ usage

        vjProbs.get(segmentTuple).addAndGet(P * vjProb);

        NucleotideSequence query = vjHmmTransitions.getQuery(),
                vRef = vjHmmTransitions.getvRef(),
                jRef = vjHmmTransitions.getjRef();

        // V trimming

        double vProb = 0, jProb = 0;
        int lenV = query.size() < vRef.size() ? query.size() : vRef.size();
        for (int i = lenV - 1; i >= 0; i--) {
            vProb += vjHmmTransitions.alpha[0][i] * vjHmmTransitions.beta[0][i];
            int index = convertIndex(vRef.codeAt(i), query.codeAt(i));
            double delta = vProb * vjProb;
            substitutionMatrix.addAndGet(index, delta);
            vTrimmingProb.addAndGet(i, delta);
        }

        // J trimming

        int lenJ = query.size() < jRef.size() ? query.size() : jRef.size();
        for (int i = lenJ - 1; i >= 0; i--) {
            int jPos = jRef.size() - i + 1, qPos = query.size() - i + 1;
            jProb += vjHmmTransitions.alpha[1][qPos] * vjHmmTransitions.beta[1][qPos];
            int index = convertIndex(jRef.codeAt(jPos), query.codeAt(qPos));
            double delta = jProb * vjProb;
            substitutionMatrix.addAndGet(index, delta);
            jTrimmingProb.addAndGet(jPos, delta);
        }

        // Insert size

        for (int i = 0; i <= lenV; i++) {
            double pDeltaSum = 0;

            double pV = vjHmmTransitions.alpha[0][i] * vjHmmTransitions.beta[0][i],
                    pJ = vjHmmTransitions.alpha[1][i + 1] * vjHmmTransitions.beta[1][i + 1],
                    pDelta = (pV + pJ - P) * vjProb;

            insertSizeProbs.addAndGet(0, pDelta);

            for (int j = i + 2; j < query.size(); j++) {
                int delta = j - i - 1;

                pV = vjHmmTransitions.alpha[0][i] * vjHmmTransitions.beta[0][i];
                pJ = vjHmmTransitions.alpha[1][j] * vjHmmTransitions.beta[1][j];
                pDelta = (pV + pJ - P) * vjProb;

                pDeltaSum += pDelta; // sum all rearrangements that include base "i+1"

                insertSizeProbs.addAndGet(delta, pDelta);
            }

            if (pDeltaSum > 0) { // ensure at least 1 added base
                insertProbs.addAndGet(convertIndex(query.codeAt(i), query.codeAt(i + 1)), pDeltaSum);
            }
        }
    }

    private static int convertIndex(byte b1, byte b2) {
        return b1 * 4 + b2;
    }
}
