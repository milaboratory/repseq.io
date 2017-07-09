package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class VSegmentTrimmingParameters implements SegmentTrimmingParameters {
    private final double[] trimmingProbs;

    public VSegmentTrimmingParameters(double[] trimmingProbs) {
        this.trimmingProbs = trimmingProbs;
    }

    @Override
    public double getTrimmingProb(int trim5, int trim3) {
        return trimmingProbs[trim3];
    }
}
