package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class UniformGermlineMatchParameters implements GermlineMatchParameters {
    private final double errorProb;

    public UniformGermlineMatchParameters(double errorProb) {
        this.errorProb = errorProb;
    }

    @Override
    public double getSubstitutionProb(byte from, byte to) {
        return from == to ? 1.0 - errorProb : (errorProb / 3);
    }

    @Override
    public double getLogSubstitutionProb(byte from, byte to) {
        return from == to ? Math.log(1.0 - errorProb) : Math.log(errorProb / 3);
    }
}
