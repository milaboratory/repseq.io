package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class SpecificGermlineMatchParameters implements GermlineMatchParameters {
    private final double[][] substitutionProbs, logSubstitutionProbs = new double[4][4];
    private final double matchProb, mismatchProb;

    public SpecificGermlineMatchParameters(double[][] substitutionProbs) {
        this.substitutionProbs = ProbabilityUtil.ensureNonSingularNormalized(substitutionProbs);

        double matchProb = 0;
        for (int i = 0; i < 4; i++) {
            matchProb += substitutionProbs[i][i];
            for (int j = 0; j < 4; j++) {
                logSubstitutionProbs[i][j] = Math.log(substitutionProbs[i][j]);
            }
        }
        this.matchProb = matchProb / 4;
        this.mismatchProb = (1 - matchProb) / 3;
    }


    @Override
    public double getSubstitutionProb(byte from, byte to) {
        return substitutionProbs[from][to];
    }

    @Override
    public double getLogSubstitutionProb(byte from, byte to) {
        return logSubstitutionProbs[from][to];
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
