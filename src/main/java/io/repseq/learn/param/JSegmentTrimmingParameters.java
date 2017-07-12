package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class JSegmentTrimmingParameters implements SegmentTrimmingParameters {
    private final double[] trimmingProbs;

    public JSegmentTrimmingParameters(double[] trimmingProbs) {
        this.trimmingProbs = ProbabilityUtil.ensureNormalized(trimmingProbs);
    }

    @Override
    public double getTrimmingProb(int pos5, int pos3) {
        return trimmingProbs[pos5];
    }
}
