package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.repseq.core.BaseSequence;
import io.repseq.core.Chains;
import io.repseq.core.GeneType;
import io.repseq.core.ReferencePoint;

import java.util.*;

/**
 * DTO for VDJC Gene
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public class VDJCGeneData implements Comparable<VDJCGeneData> {
    final BaseSequence baseSequence;
    final String name;
    final GeneType geneType;
    final boolean isFunctional;
    final Chains chains;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    final String note;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    final EnumSet<GeneTag> tags;
    @JsonDeserialize(keyUsing = ReferencePoint.JsonKeyDeserializer.class)
    @JsonSerialize(keyUsing = ReferencePoint.JsonKeySerializer.class)
    final SortedMap<ReferencePoint, Long> anchorPoints;

    @JsonCreator
    public VDJCGeneData(@JsonProperty("baseSequence") BaseSequence baseSequence,
                        @JsonProperty("name") String name,
                        @JsonProperty("geneType") GeneType geneType,
                        @JsonProperty("isFunctional") boolean isFunctional,
                        @JsonProperty("chains") Chains chains,
                        @JsonProperty("note") String note,
                        @JsonProperty("tags") Set<GeneTag> tags,
                        @JsonProperty("anchorPoints") SortedMap<ReferencePoint, Long> anchorPoints) {
        this.baseSequence = baseSequence;
        this.name = name;
        this.geneType = geneType;
        this.isFunctional = isFunctional;
        this.chains = chains;
        this.note = note == null ? "" : note;
        this.tags = tags == null ? EnumSet.noneOf(GeneTag.class) : EnumSet.copyOf(tags);
        this.anchorPoints = anchorPoints;
    }

    public BaseSequence getBaseSequence() {
        return baseSequence;
    }

    /**
     * Full gene name
     *
     * @return full gene name
     */
    public String getName() {
        return name;
    }

    /**
     * Name without allele index (e.g. TRBV12-3 for TRBV12-3*01).
     *
     * @return without allele index (e.g. TRBV12-3 for TRBV12-3*01)
     */
    public String getGeneName() {
        int i = name.lastIndexOf('*');
        if (i < 0)
            return name;
        return name.substring(0, i);
    }

    /**
     * Gene family name (e.g. TRBV12 for TRBV12-3*01).
     *
     * @return gene family name (e.g. TRBV12 for TRBV12-3*01)
     */
    public String getFamilyName() {
        String name = getGeneName();
        int i = name.indexOf('-');
        if (i > 0)
            name = name.substring(0, i);
        return name;
    }

    public GeneType getGeneType() {
        return geneType;
    }

    public boolean isFunctional() {
        return isFunctional;
    }

    public Chains getChains() {
        return chains;
    }

    public EnumSet<GeneTag> getTags() {
        return tags;
    }

    public Map<ReferencePoint, Long> getAnchorPoints() {
        return anchorPoints;
    }

    public VDJCGeneData clone() {
        return new VDJCGeneData(baseSequence, name, geneType, isFunctional,
                chains, note, EnumSet.copyOf(tags), new TreeMap<>(anchorPoints));
    }

    /**
     * Used for sorting
     */
    private String lowestChain() {
        String chain = null;
        for (String c : chains)
            if (chain == null || chain.compareTo(c) > 0)
                chain = c;
        return chain;
    }

    @Override
    public int compareTo(VDJCGeneData o) {
        int c;

        if ((c = lowestChain().compareTo(o.lowestChain())) != 0)
            return c;

        if ((c = geneType.compareTo(o.geneType)) != 0)
            return c;

        return VDJCDataUtils.smartCompare(this.getName(), o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCGeneData)) return false;

        VDJCGeneData that = (VDJCGeneData) o;

        if (isFunctional != that.isFunctional) return false;
        if (baseSequence != null ? !baseSequence.equals(that.baseSequence) : that.baseSequence != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (geneType != that.geneType) return false;
        if (chains != null ? !chains.equals(that.chains) : that.chains != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        return anchorPoints != null ? anchorPoints.equals(that.anchorPoints) : that.anchorPoints == null;
    }

    @Override
    public int hashCode() {
        int result = baseSequence != null ? baseSequence.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (geneType != null ? geneType.hashCode() : 0);
        result = 31 * result + (isFunctional ? 1 : 0);
        result = 31 * result + (chains != null ? chains.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (anchorPoints != null ? anchorPoints.hashCode() : 0);
        return result;
    }
}
