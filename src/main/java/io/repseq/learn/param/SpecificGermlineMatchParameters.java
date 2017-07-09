package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class SpecificGermlineMatchParameters implements GermlineMatchParameters {
    private final double[][] substitutionProbs, logSubstitutionProbs = new double[4][4];

    public SpecificGermlineMatchParameters(double[][] substitutionProbs) {
        this.substitutionProbs = substitutionProbs;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                logSubstitutionProbs[i][j] = Math.log(substitutionProbs[i][j]);
            }
        }
    }


    @Override
    public double getSubstitutionProb(byte from, byte to) {
        return substitutionProbs[from][to];
    }

    @Override
    public double getLogSubstitutionProb(byte from, byte to) {
        return logSubstitutionProbs[from][to];
    }
}
