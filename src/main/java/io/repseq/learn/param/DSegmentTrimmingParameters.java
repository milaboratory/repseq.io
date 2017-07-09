package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class DSegmentTrimmingParameters implements SegmentTrimmingParameters {
    private final double[][] trimmingProbs;

    public DSegmentTrimmingParameters(double[][] trimmingProbs) {
        this.trimmingProbs = trimmingProbs;
    }

    @Override
    public double getTrimmingProb(int trim5, int trim3) {
        return trimmingProbs[trim5][trim3];
    }
}
