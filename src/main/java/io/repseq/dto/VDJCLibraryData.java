/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.repseq.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(value = {"notes"})
public final class VDJCLibraryData implements Comparable<VDJCLibraryData> {
    private final long taxonId;
    private final List<String> speciesNames;
    private final List<VDJCGeneData> genes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(contentUsing = MetaUtils.MetaValueSerializer.class)
    @JsonDeserialize(contentUsing = MetaUtils.MetaValueDeserializer.class)
    private final SortedMap<String, SortedSet<String>> meta;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<KnownSequenceFragmentData> sequenceFragments;

    /**
     * Creates VDJCLibraryData object from other VDJCLibraryData given new set of genes
     *
     * @param other VDJCLibraryData to clone properties from
     * @param genes new gene list
     */
    public VDJCLibraryData(VDJCLibraryData other, List<VDJCGeneData> genes) {
        this.taxonId = other.taxonId;
        this.speciesNames = new ArrayList<>(other.speciesNames); // clone just in case
        this.genes = genes;
        this.meta = new TreeMap<>(other.meta); // clone just in case
        this.sequenceFragments = new ArrayList<>(other.sequenceFragments); // clone just in case
    }

    @JsonCreator
    public VDJCLibraryData(@JsonProperty("taxonId") long taxonId,
                           @JsonProperty("speciesNames") List<String> speciesNames,
                           @JsonProperty("genes") List<VDJCGeneData> genes,
                           @JsonProperty("meta") SortedMap<String, SortedSet<String>> meta,
                           @JsonProperty("sequenceFragments") List<KnownSequenceFragmentData> sequenceFragments) {
        this.taxonId = taxonId;
        this.speciesNames = speciesNames == null ? Collections.EMPTY_LIST : speciesNames;
        this.genes = genes;
        this.sequenceFragments = sequenceFragments == null ? Collections.EMPTY_LIST : sequenceFragments;
        this.meta = meta == null ? new TreeMap<String, SortedSet<String>>() : meta;
    }

    public long getTaxonId() {
        return taxonId;
    }

    public List<String> getSpeciesNames() {
        return speciesNames;
    }

    public List<VDJCGeneData> getGenes() {
        return genes;
    }

    public List<KnownSequenceFragmentData> getSequenceFragments() {
        return sequenceFragments;
    }

    /**
     * Set of citations specified for this library. E.g. can be used to remind academic users to cite specific paper,
     * that this library was published with.
     */
    public SortedSet<String> getCitations() {
        return getMetaValueSet(KnownVDJCLibraryMetaFields.CITATIONS);
    }

    /**
     * Warnings, associated with the library. E.g. it is not finished yet (beta release).
     */
    public SortedSet<String> getWarnings() {
        return getMetaValueSet(KnownVDJCLibraryMetaFields.WARNINGS);
    }

    /**
     * All comment blocks associated with the library
     */
    public SortedSet<String> getComments() {
        return getMetaValueSet(KnownVDJCLibraryMetaFields.COMMENTS);
    }

    /**
     * Free form meta information for the library, raw meta map
     */
    public SortedMap<String, SortedSet<String>> getMeta() {
        return meta;
    }

    /**
     * Returns list of values associated with the key from meta section of the library document
     *
     * @param key key
     */
    public SortedSet<String> getMetaValueSet(String key) {
        SortedSet<String> values = meta.get(key);
        return values == null ? new TreeSet<String>() : values;
    }

    /**
     * Returns single value associated with the key from meta section of the library document
     *
     * @param key key
     */
    public String getMetaValue(String key) {
        SortedSet<String> values = meta.get(key);
        if (values == null || values.isEmpty())
            return null;
        else if (values.size() > 1)
            throw new RuntimeException("More then one value associated with the key \"" + key + "\"");
        else
            return values.first();
    }

    /**
     * Overrides value of field with the specified key. All previous values (even if several were associated with the
     * field) will be dropped.
     *
     * @param key      key
     * @param newValue new value
     */
    public VDJCLibraryData setMetaValue(String key, String newValue) {
        SortedSet<String> values = new TreeSet<>();
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
    public VDJCLibraryData addMetaValue(String key, String value) {
        SortedSet<String> values = meta.get(key);
        if (values == null)
            meta.put(key, values = new TreeSet<>());
        values.add(value);
        return this;
    }

    public VDJCLibraryData clone() {
        return new VDJCLibraryData(taxonId,
                new ArrayList<>(speciesNames),
                new ArrayList<>(genes),
                MetaUtils.deepCopy(meta),
                new ArrayList<>(sequenceFragments));
    }

    @Override
    public int compareTo(VDJCLibraryData o) {
        return Long.compare(this.getTaxonId(), o.getTaxonId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCLibraryData)) return false;

        VDJCLibraryData that = (VDJCLibraryData) o;

        if (taxonId != that.taxonId) return false;
        if (!speciesNames.equals(that.speciesNames)) return false;
        if (!genes.equals(that.genes)) return false;
        if (!meta.equals(that.meta)) return false;
        return sequenceFragments.equals(that.sequenceFragments);
    }

    @Override
    public int hashCode() {
        int result = (int) (taxonId ^ (taxonId >>> 32));
        result = 31 * result + speciesNames.hashCode();
        result = 31 * result + genes.hashCode();
        result = 31 * result + meta.hashCode();
        result = 31 * result + sequenceFragments.hashCode();
        return result;
    }
}
