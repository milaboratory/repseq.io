package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class SimpleGermlineMatchParameters implements GermlineMatchParameters {
    @Override
    public double getSubstitutionProb(byte from, byte to) {
        return from == to ? 1 : 0;
    }

    @Override
    public double getLogSubstitutionProb(byte from, byte to) {
        return from == to ? 0 : -100;
    }
}
