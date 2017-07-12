package io.repseq.learn;

import java.util.Set;

/**
 * Created by mikesh on 7/12/17.
 */
public class DTrimmingMatch {
    private final Set<DTrimming> dTrimmings;
    private final int mmCount, matchSize;

    public DTrimmingMatch(Set<DTrimming> dTrimmings, int mmCount, int matchSize) {
        this.dTrimmings = dTrimmings;
        this.mmCount = mmCount;
        this.matchSize = matchSize;
    }

    public Set<DTrimming> getdTrimmings() {
        return dTrimmings;
    }

    public int getMmCount() {
        return mmCount;
    }

    public int getMatchSize() {
        return matchSize;
    }
}
