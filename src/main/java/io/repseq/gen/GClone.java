package io.repseq.gen;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents single clonotype
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class GClone {
    /**
     * Clone abundance, may be not normalised
     */
    public final double abundance;
    /**
     * Map from immunological chain to gene
     */
    public final Map<String, GGene> genes;

    private GClone(double abundance, Map<String, GGene> genes, boolean unsafe) {
        assert unsafe;
        this.abundance = abundance;
        this.genes = genes;
    }

    @JsonCreator
    public GClone(@JsonProperty("abundance") double abundance,
                  @JsonProperty("genes") Map<String, GGene> genes) {
        this(abundance, Collections.unmodifiableMap(new HashMap<>(genes)), true);
    }

    public GClone setAbundance(double abundance) {
        return new GClone(abundance, genes, true);
    }
}
