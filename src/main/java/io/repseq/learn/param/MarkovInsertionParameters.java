package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class MarkovInsertionParameters implements InsertionParameters {
    private final double[] insertSizeDistr;
    private final double[][] baseProbs, logBaseProbs = new double[4][4];
    private final double[] baseProbsMarginal = new double[4], logBaseProbsMarginal = new double[4];

    public MarkovInsertionParameters(double[] insertSizeDistr, double[][] baseProbs) {
        this.insertSizeDistr = ProbabilityUtil.ensureNormalized(insertSizeDistr);
        this.baseProbs = ProbabilityUtil.ensureNonSingularNormalized(baseProbs);

        for (int j = 0; j < 4; j++) {
            double margin = 0;
            for (int i = 0; i < 4; i++) {
                margin += baseProbs[i][j];
                logBaseProbs[i][j] = baseProbs[i][j];
            }
            baseProbsMarginal[j] = margin;
            logBaseProbsMarginal[j] = Math.log(margin);
        }
    }

    @Override
    public double getInsertionProb(byte previousBase, byte currentBase) {
        return baseProbs[previousBase][currentBase];
    }

    @Override
    public double getLogInsertionProb(byte previousBase, byte currentBase) {
        return logBaseProbs[previousBase][currentBase];
    }

    @Override
    public double getInsertionProb(byte currentBase) {
        return baseProbsMarginal[currentBase];
    }

    @Override
    public double getLogInsertionProb(byte currentBase) {
        return logBaseProbsMarginal[currentBase];
    }

    @Override
    public double getInsertSizeProb(int size) {
        if (size >= insertSizeDistr.length)
            return 0;
        else
            return insertSizeDistr[size];
    }
}