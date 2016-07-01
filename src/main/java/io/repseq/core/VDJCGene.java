package io.repseq.core;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import io.repseq.dto.VDJCGeneData;
import io.repseq.reference.ReferencePoints;

import java.util.Set;

public class VDJCGene extends PartitionedSequenceCached<NucleotideSequence> {
    /**
     * Any gene stores a reference to it's parent library
     */
    private final VDJCLibrary parentLibrary;
    /**
     * Data from original DTO
     */
    private final VDJCGeneData data;
    /**
     * Sequence provider
     */
    private final SequenceProvider<NucleotideSequence> sequenceProvider;
    /**
     * Sequence partitioning
     */
    private final ReferencePoints referencePoints;

    /**
     * Use {@link VDJCLibrary#addGene(VDJCLibrary, VDJCGeneData)} to create instances of this object.
     */
    public VDJCGene(VDJCLibrary parentLibrary, VDJCGeneData data,
                    SequenceProvider<NucleotideSequence> sequenceProvider,
                    ReferencePoints referencePoints) {
        this.parentLibrary = parentLibrary;
        this.data = data;
        this.sequenceProvider = sequenceProvider;
        this.referencePoints = referencePoints;
    }

    /**
     * Returns gene name (e.g. TRBV12-2*01)
     *
     * @return gene name (e.g. TRBV12-2*01)
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Returns true if this gene marked as functional
     *
     * @return true if this gene marked as functional
     */
    public boolean isFunctional() {
        return data.isFunctional();
    }

    /**
     * Returns set of chains (e.g. TRB, IGH, IGL, etc...) in which this gene can be used. For some genes set can
     * contain several entries (e.g. TRA and TRD for TRAV23DV6)
     *
     * @return set of chains (e.g. TRA, TRD) in which this gene can be used.
     */
    public Set<String> getChains() {
        return data.getChains();
    }
    
    @Override
    protected NucleotideSequence getSequence(Range range) {
        return sequenceProvider.getRegion(range);
    }

    @Override
    protected SequencePartitioning getPartitioning() {
        return referencePoints;
    }
}
