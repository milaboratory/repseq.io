package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.Collections;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class VDJCLibraryData {
    final long taxonId;
    final List<String> speciesNames;
    final List<VDJCGeneData> genes;
    final List<KnownSequenceFragmentData> sequenceFragments;

    public VDJCLibraryData(long taxonId,
                           List<String> speciesNames,
                           List<VDJCGeneData> genes,
                           List<KnownSequenceFragmentData> sequenceFragments) {
        this.taxonId = taxonId;
        this.speciesNames = speciesNames;
        this.genes = genes;
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

    public List<KnownSequenceFragmentData> getSequenceFragments() {
        return sequenceFragments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCLibraryData)) return false;

        VDJCLibraryData that = (VDJCLibraryData) o;

        if (taxonId != that.taxonId) return false;
        if (speciesNames != null ? !speciesNames.equals(that.speciesNames) : that.speciesNames != null) return false;
        if (genes != null ? !genes.equals(that.genes) : that.genes != null) return false;
        return sequenceFragments != null ? sequenceFragments.equals(that.sequenceFragments) : that.sequenceFragments == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (taxonId ^ (taxonId >>> 32));
        result = 31 * result + (speciesNames != null ? speciesNames.hashCode() : 0);
        result = 31 * result + (genes != null ? genes.hashCode() : 0);
        result = 31 * result + (sequenceFragments != null ? sequenceFragments.hashCode() : 0);
        return result;
    }
}
