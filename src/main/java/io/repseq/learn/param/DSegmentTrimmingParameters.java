package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class DSegmentTrimmingParameters implements SegmentTrimmingParameters {
    private final double[][] trimmingProbs;
    private final double noDFoundProb;

    public DSegmentTrimmingParameters(double[][] trimmingProbs, double noDFoundProb) {
        this.trimmingProbs = trimmingProbs;
        this.noDFoundProb = noDFoundProb;
    }

    @Override
    public double getTrimmingProb(int pos5, int pos3) {
        return trimmingProbs[pos5][pos3];
    }

    @Override
    public double getZeroLengthProb() {
        return noDFoundProb;
    }
}
