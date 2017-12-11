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
    @JsonSerialize(contentUsing = MetaUtils.MetaValueSerializer.class)
    @JsonDeserialize(contentUsing = MetaUtils.MetaValueDeserializer.class)
    final SortedMap<String, List<String>> meta;
    @JsonDeserialize(keyUsing = ReferencePoint.JsonKeyDeserializer.class)
    @JsonSerialize(keyUsing = ReferencePoint.JsonKeySerializer.class)
    final SortedMap<ReferencePoint, Long> anchorPoints;

    @JsonCreator
    public VDJCGeneData(@JsonProperty("baseSequence") BaseSequence baseSequence,
                        @JsonProperty("name") String name,
                        @JsonProperty("geneType") GeneType geneType,
                        @JsonProperty("isFunctional") boolean isFunctional,
                        @JsonProperty("chains") Chains chains,
                        @JsonProperty("meta") SortedMap<String, List<String>> meta,
                        @JsonProperty("anchorPoints") SortedMap<ReferencePoint, Long> anchorPoints) {
        this.baseSequence = baseSequence;
        this.name = name;
        this.geneType = geneType;
        this.isFunctional = isFunctional;
        this.chains = chains;
        this.meta = meta == null ? new TreeMap<String, List<String>>() : meta;
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

    /**
     * Gene type (V / D/ J / C)
     */
    public GeneType getGeneType() {
        return geneType;
    }

    /**
     * Returns true if this gene is marked as functional in the library file
     */
    public boolean isFunctional() {
        return isFunctional;
    }

    /**
     * Chains of immunological receptors that this segment can be a part of
     */
    public Chains getChains() {
        return chains;
    }

    /**
     * Map of anchor points
     */
    public Map<ReferencePoint, Long> getAnchorPoints() {
        return anchorPoints;
    }

    /**
     * Free form meta information for the gene, raw meta map
     */
    public SortedMap<String, List<String>> getMeta() {
        return meta;
    }

    /**
     * Returns list of values associated with the key from meta section of the gene record
     *
     * @param key key
     */
    public List<String> getMetaList(String key) {
        List<String> vals = meta.get(key);
        return vals == null ? Collections.EMPTY_LIST : vals;
    }

    /**
     * Returns single value associated with the key from meta section of the gene record
     *
     * @param key key
     */
    public String getMetaValue(String key) {
        List<String> vals = meta.get(key);
        if (vals == null)
            return null;
        else if (vals.size() > 1)
            throw new RuntimeException("More then one value associated with the key \"" + key + "\"");
        else
            return vals.get(0);
    }

    /**
     * Overrides value of field with the specified key. All previous values (even if several were associated with the
     * field) will be dropped.
     *
     * @param key      key
     * @param newValue new value
     */
    public VDJCGeneData setMetaValue(String key, String newValue) {
        List<String> values = new ArrayList<>();
        values.add(newValue);
        meta.put(key, values);
        return this;
    }

    /**
     * Add value to the list of values associated with the key.
     *
     * @param key   key
     * @param value value
     */
    public VDJCGeneData addMetaValue(String key, String value) {
        List<String> values = meta.get(key);
        if (value == null)
            meta.put(key, values = new ArrayList<>());
        values.add(value);
        return this;
    }

    /**
     * Clone this object
     */
    public VDJCGeneData clone() {
        return new VDJCGeneData(baseSequence, name, geneType, isFunctional,
                chains, MetaUtils.deepCopy(meta), new TreeMap<>(anchorPoints));
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
        if (!baseSequence.equals(that.baseSequence)) return false;
        if (!name.equals(that.name)) return false;
        if (geneType != that.geneType) return false;
        if (!chains.equals(that.chains)) return false;
        if (!meta.equals(that.meta)) return false;
        return anchorPoints.equals(that.anchorPoints);
    }

    @Override
    public int hashCode() {
        int result = baseSequence.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + geneType.hashCode();
        result = 31 * result + (isFunctional ? 1 : 0);
        result = 31 * result + chains.hashCode();
        result = 31 * result + meta.hashCode();
        result = 31 * result + anchorPoints.hashCode();
        return result;
    }
}
