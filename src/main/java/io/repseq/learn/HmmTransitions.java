package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

/**
 * Created by mikesh on 7/5/17.
 */
public class HmmTransitions {
    private final NucleotideSequence query;
    private final double[][] alpha, // V->J transitions
            beta; // J -> V transitions

    public HmmTransitions(NucleotideSequence query, double[][] alpha, double[][] beta) {
        this.query = query;
        this.alpha = alpha;
        this.beta = beta;
    }

    public NucleotideSequence getQuery() {
        return query;
    }

    public double[][] getAlpha() {
        return alpha;
    }

    public double[][] getBeta() {
        return beta;
    }
}
