package io.repseq.gen;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents information on number of nucleotides deleted from 3' V and 5' J; or sizes of corresponding
 * P-segments. Negative values represent deletions, positive values represent length of P-segment.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class VJTrimming {
    public final int vTrimming, jTrimming;

    @JsonCreator
    public VJTrimming(@JsonProperty("vTrimming") int vTrimming,
                      @JsonProperty("jTrimming") int jTrimming) {
        this.vTrimming = vTrimming;
        this.jTrimming = jTrimming;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VJTrimming that = (VJTrimming) o;

        if (vTrimming != that.vTrimming) return false;
        return jTrimming == that.jTrimming;
    }

    @Override
    public int hashCode() {
        int result = vTrimming;
        result = 31 * result + jTrimming;
        return result;
    }
}
