package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class VDJCLibraryData implements Comparable<VDJCLibraryData> {
    private final long taxonId;
    private final List<String> speciesNames;
    private final List<VDJCGeneData> genes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<VDJCLibraryNote> notes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<KnownSequenceFragmentData> sequenceFragments;

    /**
     * Creates VDJCLibraryData object from other VDJCLibraryData given new sed of genes
     *
     * @param other VDJCLibraryData to clone properties from
     * @param genes new gene list
     */
    public VDJCLibraryData(VDJCLibraryData other, List<VDJCGeneData> genes) {
        this.taxonId = other.taxonId;
        this.speciesNames = new ArrayList<>(other.speciesNames); // clone just in case
        this.genes = genes;
        this.sequenceFragments = new ArrayList<>(other.sequenceFragments); // clone just in case
        this.notes = new ArrayList<>(other.notes); // clone just in case
    }

    @JsonCreator
    public VDJCLibraryData(@JsonProperty("taxonId") long taxonId,
                           @JsonProperty("speciesNames") List<String> speciesNames,
                           @JsonProperty("genes") List<VDJCGeneData> genes,
                           @JsonProperty("notes") List<VDJCLibraryNote> notes,
                           @JsonProperty("sequenceFragments") List<KnownSequenceFragmentData> sequenceFragments) {
        this.taxonId = taxonId;
        this.speciesNames = speciesNames == null ? Collections.EMPTY_LIST : speciesNames;
        this.genes = genes;
        this.notes = notes == null ? Collections.EMPTY_LIST : notes;
        this.sequenceFragments = sequenceFragments == null ? Collections.EMPTY_LIST : sequenceFragments;
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

    public List<VDJCLibraryNote> getNotes() {
        return notes;
    }

    /**
     * Return notes of a particular type
     *
     * @param type comment type
     * @return list of notes of a particular type
     */
    public List<VDJCLibraryNote> getComments(VDJCLibraryNoteType type) {
        List<VDJCLibraryNote> ret = new ArrayList<>();
        for (VDJCLibraryNote c : notes)
            if (c.getType() == type)
                ret.add(c);
        return ret;
    }

    public List<KnownSequenceFragmentData> getSequenceFragments() {
        return sequenceFragments;
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
        if (speciesNames != null ? !speciesNames.equals(that.speciesNames) : that.speciesNames != null) return false;
        if (genes != null ? !genes.equals(that.genes) : that.genes != null) return false;
        if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
        return sequenceFragments != null ? sequenceFragments.equals(that.sequenceFragments) : that.sequenceFragments == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (taxonId ^ (taxonId >>> 32));
        result = 31 * result + (speciesNames != null ? speciesNames.hashCode() : 0);
        result = 31 * result + (genes != null ? genes.hashCode() : 0);
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        result = 31 * result + (sequenceFragments != null ? sequenceFragments.hashCode() : 0);
        return result;
    }
}
