package io.repseq.learn;

import io.repseq.learn.param.SegmentTrimmingParameters;

/**
 * Created by mikesh on 7/13/17.
 */
public class TransitionProbabilityUtil {
    public static void assignTrimmingProbabilities(DTrimmingSTM dTrimmingSTM,
                                                   SegmentTrimmingParameters segmentTrimmingParameters) {
        for (DTrimmingSet dTrimmingSet : dTrimmingSTM.getDTrimmingSets()) {
            double prob = 0;
            for (DTrimming dTrimming : dTrimmingSet.getdTrimmings()) {
                prob += segmentTrimmingParameters.getTrimmingProb(dTrimming.getPos5(), dTrimming.getPos3());
            }
            dTrimmingSet.setTrimmingProbSum(prob);
        }
        dTrimmingSTM.setInitialized(true);
    }
}
