package io.repseq.learn;

import java.util.Set;

/**
 * Created by mikesh on 7/13/17.
 */
public class DFactor {
    private final double prob;
    private final Set<DTrimming> dTrimmings;

    public DFactor(double prob, Set<DTrimming> dTrimmings) {
        this.prob = prob;
        this.dTrimmings = dTrimmings;
    }

    public double getProb() {
        return prob;
    }

    public Set<DTrimming> getdTrimmings() {
        return dTrimmings;
    }
}
