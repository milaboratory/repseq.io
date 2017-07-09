package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class JSegmentTrimmingParameters implements SegmentTrimmingParameters {
    private final double[] trimmingProbs;

    public JSegmentTrimmingParameters(double[] trimmingProbs) {
        this.trimmingProbs = trimmingProbs;
    }

    @Override
    public double getTrimmingProb(int trim5, int trim3) {
        return trimmingProbs[trim5];
    }
}
