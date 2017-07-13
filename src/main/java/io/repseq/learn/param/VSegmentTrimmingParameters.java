package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class VSegmentTrimmingParameters implements SegmentTrimmingParameters {
    private final double[] trimmingProbs;

    public VSegmentTrimmingParameters(double[] trimmingProbs) {
        this.trimmingProbs = ProbabilityUtil.ensureNormalized(trimmingProbs);
    }

    @Override
    public double getTrimmingProb(int pos5, int pos3) {
        return trimmingProbs[pos3];
    }

    @Override
    public double getZeroLengthProb() {
        return 0;
    }
}
