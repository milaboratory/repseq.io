package io.repseq.learn.param;

/**
 * Created by mikesh on 04/07/17.
 */
public interface InsertionParameters {
    /**
     * Get probability of observing a certain base in the insert given the previous base.
     * The definition of previous base can vary. For example in can be 5' base in VD inserts and 3' base in DJ inserts.
     * @param previousBase previous base.
     * @param currentBase current base.
     * @return the probability of observing the inserted base conditioned on previous base.
     */
    double getInsertionProb(byte previousBase, byte currentBase);

    double getLogInsertionProb(byte previousBase, byte currentBase);

    /**
     * Get probability of observing a certain base in the insert.
     * Applicably to either insertion models that consider independent bases or the first insertion in a markov chain.
     * @param currentBase inserted base.
     * @return the probability of observing the inserted base.
     */
    double getInsertionProb(byte currentBase);

    double getLogInsertionProb(byte currentBase);

    /**
     * The probability of observing a random base insertion of a certain length.
     * @param size insertion size, strictly non-negative
     * @return insert size probability
     */
    double getInsertSizeProb(int size);
}
