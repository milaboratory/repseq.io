package io.repseq.learn;

import java.util.Set;

/**
 * Created by mikesh on 7/12/17.
 */
public class DTrimmingMatch {
    private final DTrimmingSet dTrimmingSet;
    private final int mmCount, matchSize;

    public DTrimmingMatch(DTrimmingSet dTrimmingSet, int mmCount, int matchSize) {
        this.dTrimmingSet = dTrimmingSet;
        this.mmCount = mmCount;
        this.matchSize = matchSize;
    }

    public DTrimmingSet getdTrimmingSet() {
        return dTrimmingSet;
    }

    public int getMmCount() {
        return mmCount;
    }

    public int getMatchSize() {
        return matchSize;
    }
}
