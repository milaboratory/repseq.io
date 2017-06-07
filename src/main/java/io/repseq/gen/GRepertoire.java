package io.repseq.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents immunological clone repertoire
 */
public final class GRepertoire {
    public final List<GClone> clones;
    public final double totalAbundance;

    public GRepertoire(List<GClone> clones) {
        this.clones = Collections.unmodifiableList(new ArrayList<>(clones));
        double sum = 0;
        for (GClone clone : clones)
            sum += clone.abundance;
        this.totalAbundance = sum;
    }
}
