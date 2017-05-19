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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GClone)) return false;

        GClone gClone = (GClone) o;

        if (Double.compare(gClone.abundance, abundance) != 0) return false;
        return genes.equals(gClone.genes);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(abundance);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + genes.hashCode();
        return result;
    }
}
