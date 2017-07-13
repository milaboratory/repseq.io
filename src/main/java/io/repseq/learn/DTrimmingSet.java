package io.repseq.learn;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mikesh on 7/12/17.
 */
public class DTrimmingSet {
    public static final DTrimmingSet EMPTY = new DTrimmingSet(new HashSet<>());

    private final Set<DTrimming> dTrimmings;
    private double trimmingProbSum = 0;

    public DTrimmingSet(Set<DTrimming> dTrimmings) {
        this.dTrimmings = dTrimmings;
    }

    public Set<DTrimming> getdTrimmings() {
        return dTrimmings;
    }

    public double getTrimmingProbSum() {
        return trimmingProbSum;
    }

    public void setTrimmingProbSum(double trimmingProbSum) {
        this.trimmingProbSum = trimmingProbSum;
    }
}
