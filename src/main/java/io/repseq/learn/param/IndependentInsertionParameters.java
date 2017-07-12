package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class IndependentInsertionParameters implements InsertionParameters {
    private final double[] insertSizeDistr;
    private final double[] baseProbs, logBaseProbs = new double[4];

    public IndependentInsertionParameters(double[] insertSizeDistr, double[] baseProbs) {
        this.insertSizeDistr = ProbabilityUtil.ensureNormalized(insertSizeDistr);
        this.baseProbs = ProbabilityUtil.ensureNonSingularNormalized(baseProbs);

        for (int i = 0; i < 4; i++) {
            logBaseProbs[i] = Math.log(baseProbs[i]);
        }
    }

    @Override
    public double getInsertionProb(byte previousBase, byte currentBase) {
        return baseProbs[currentBase];
    }

    @Override
    public double getLogInsertionProb(byte previousBase, byte currentBase) {
        return logBaseProbs[currentBase];
    }

    @Override
    public double getInsertionProb(byte currentBase) {
        return baseProbs[currentBase];
    }

    @Override
    public double getLogInsertionProb(byte currentBase) {
        return logBaseProbs[currentBase];
    }

    @Override
    public double getInsertSizeProb(int size) {
        if (size >= insertSizeDistr.length)
            return 0;
        else
            return insertSizeDistr[size];
    }
}
