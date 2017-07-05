package io.repseq.learn.param;

/**
 * Created by mikesh on 04/07/17.
 */
public interface GermlineMatchParameters {
    /**
     * Gets the probability of an erroneous substitution.
     * @param from original (reference) base.
     * @param to base in observed sequence.
     * @return substitution probability, or the probability of observing a correct base when from == to.
     */
    double getSubstitutionProb(byte from, byte to);

    double getLogSubstitutionProb(byte from, byte to);
}
