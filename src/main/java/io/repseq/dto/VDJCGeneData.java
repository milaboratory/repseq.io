package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.repseq.core.BaseSequence;
import io.repseq.reference.GeneType;
import io.repseq.reference.ReferencePoint;

import java.util.Map;
import java.util.Set;

/**
 * DTO for VDJC Gene
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public class VDJCGeneData {
    final BaseSequence baseSequence;
    final String name;
    final GeneType geneType;
    final boolean isFunctional;
    final Set<String> chains;
    @JsonDeserialize(keyUsing = ReferencePoint.JsonKeyDeserializer.class)
    @JsonSerialize(keyUsing = ReferencePoint.JsonKeySerializer.class)
    final Map<ReferencePoint, Long> anchorPoints;

    @JsonCreator
    public VDJCGeneData(@JsonProperty("baseSequence") BaseSequence baseSequence,
                        @JsonProperty("name") String name,
                        @JsonProperty("geneType") GeneType geneType,
                        @JsonProperty("isFunctional") boolean isFunctional,
                        @JsonProperty("chains") Set<String> chains,
                        @JsonProperty("anchorPoints") Map<ReferencePoint, Long> anchorPoints) {
        this.baseSequence = baseSequence;
        this.name = name;
        this.geneType = geneType;
        this.isFunctional = isFunctional;
        this.chains = chains;
        this.anchorPoints = anchorPoints;
    }

    public BaseSequence getBaseSequence() {
        return baseSequence;
    }

    public String getName() {
        return name;
    }

    public GeneType getGeneType() {
        return geneType;
    }

    public boolean isFunctional() {
        return isFunctional;
    }

    public Set<String> getChains() {
        return chains;
    }

    public Map<ReferencePoint, Long> getAnchorPoints() {
        return anchorPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCGeneData)) return false;

        VDJCGeneData that = (VDJCGeneData) o;

        if (isFunctional != that.isFunctional) return false;
        if (baseSequence != null ? !baseSequence.equals(that.baseSequence) : that.baseSequence != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (chains != null ? !chains.equals(that.chains) : that.chains != null) return false;
        return anchorPoints != null ? anchorPoints.equals(that.anchorPoints) : that.anchorPoints == null;

    }

    @Override
    public int hashCode() {
        int result = baseSequence != null ? baseSequence.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isFunctional ? 1 : 0);
        result = 31 * result + (chains != null ? chains.hashCode() : 0);
        result = 31 * result + (anchorPoints != null ? anchorPoints.hashCode() : 0);
        return result;
    }
}
