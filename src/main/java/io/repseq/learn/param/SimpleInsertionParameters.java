package io.repseq.learn.param;

/**
 * Created by mikesh on 7/9/17.
 */
public class SimpleInsertionParameters implements InsertionParameters {
    private static final double PROB = 0.25, LOG_PROB = Math.log(PROB);

    private final double[] insertSizeDistr;

    public SimpleInsertionParameters(double[] insertSizeDistr) {
        this.insertSizeDistr = insertSizeDistr;
    }

    @Override
    public double getInsertionProb(byte previousBase, byte currentBase) {
        return PROB;
    }

    @Override
    public double getLogInsertionProb(byte previousBase, byte currentBase) {
        return LOG_PROB;
    }

    @Override
    public double getInsertionProb(byte currentBase) {
        return PROB;
    }

    @Override
    public double getLogInsertionProb(byte currentBase) {
        return LOG_PROB;
    }

    @Override
    public double getInsertSizeProb(int size) {
        if (size >= insertSizeDistr.length)
            return 0;
        else
            return insertSizeDistr[size];
    }
}
