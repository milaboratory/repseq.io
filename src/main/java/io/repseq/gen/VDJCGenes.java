package io.repseq.gen;

import com.fasterxml.jackson.annotation.*;
import io.repseq.core.VDJCGene;

/**
 * Tuple of VDJC genes.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class VDJCGenes {
    /**
     * null represents absence of certain gene
     */
    public final VDJCGene v, d, j, c;

    @JsonCreator
    public VDJCGenes(
            @JsonProperty("v") VDJCGene v,
            @JsonProperty("d") VDJCGene d,
            @JsonProperty("j") VDJCGene j,
            @JsonProperty("c") VDJCGene c
    ) {
        this.v = v;
        this.d = d;
        this.j = j;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VDJCGenes vdjcGenes = (VDJCGenes) o;

        if (v != null ? !v.equals(vdjcGenes.v) : vdjcGenes.v != null) return false;
        if (d != null ? !d.equals(vdjcGenes.d) : vdjcGenes.d != null) return false;
        if (j != null ? !j.equals(vdjcGenes.j) : vdjcGenes.j != null) return false;
        return c != null ? c.equals(vdjcGenes.c) : vdjcGenes.c == null;
    }

    @Override
    public int hashCode() {
        int result = v != null ? v.hashCode() : 0;
        result = 31 * result + (d != null ? d.hashCode() : 0);
        result = 31 * result + (j != null ? j.hashCode() : 0);
        result = 31 * result + (c != null ? c.hashCode() : 0);
        return result;
    }
}
