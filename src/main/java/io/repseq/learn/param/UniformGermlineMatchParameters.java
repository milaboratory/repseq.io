package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class UniformGermlineMatchParameters implements GermlineMatchParameters {
    private final double matchProb, mismatchProb;

    public UniformGermlineMatchParameters(double errorProb) {
        if (errorProb <= 0 || errorProb >= 1)
            throw new IllegalArgumentException("Mismatch probability should be in (0,1).");
        this.matchProb = 1.0 - errorProb;
        this.mismatchProb = errorProb / 3;
    }

    @Override
    public double getSubstitutionProb(byte from, byte to) {
        return from == to ? matchProb : mismatchProb;
    }

    @Override
    public double getLogSubstitutionProb(byte from, byte to) {
        return from == to ? Math.log(matchProb) : Math.log(mismatchProb);
    }

    @Override
    public double getMatchProb() {
        return matchProb;
    }

    @Override
    public double getMismatchProb() {
        return mismatchProb;
    }
}
