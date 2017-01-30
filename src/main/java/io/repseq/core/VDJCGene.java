package io.repseq.core;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import com.milaboratory.primitivio.annotations.Serializable;
import io.repseq.dto.VDJCGeneData;

/**
 * This class represents the same entity as Allele class in previous abstraction version.
 */
@Serializable(by = IO.VDJCGeneSerializer.class)
public final class VDJCGene extends PartitionedSequenceCached<NucleotideSequence>
        implements Comparable<VDJCGene> {
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
     * Returns sequence provider associated with this gene
     *
     * @return sequence provider associated with this gene
     */
    public SequenceProvider<NucleotideSequence> getSequenceProvider() {
        return sequenceProvider;
    }

    /**
     * Returns serializable gene data
     *
     * @return serializable gene data
     */
    public VDJCGeneData getData() {
        return data;
    }

    /**
     * Returns parent VDJCLibrary
     *
     * @return parent VDJCLibrary
     */
    public VDJCLibrary getParentLibrary() {
        return parentLibrary;
    }

    /**
     * Returns global gene identifier including library id
     *
     * @return global gene identifier including library id
     */
    public VDJCGeneId getId() {
        return new VDJCGeneId(parentLibrary.getLibraryId(), getName());
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
     * Name without allele index (e.g. TRBV12-3 for TRBV12-3*01).
     *
     * @return without allele index (e.g. TRBV12-3 for TRBV12-3*01)
     */
    public String getGeneName() {
        return data.getGeneName();
    }

    /**
     * Gene family name (e.g. TRBV12 for TRBV12-3*01).
     *
     * @return gene family name (e.g. TRBV12 for TRBV12-3*01)
     */
    public String getFamilyName() {
        return data.getFamilyName();
    }

    /**
     * Returns gene type
     *
     * @return gene type
     */
    public GeneType getGeneType() {
        return data.getGeneType();
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
    public Chains getChains() {
        return data.getChains();
    }

    @Override
    protected NucleotideSequence getSequence(Range range) {
        return sequenceProvider.getRegion(range);
    }

    /**
     * Returns reference point object for this gene
     *
     * @return reference point object for this gene
     */
    @Override
    public ReferencePoints getPartitioning() {
        return referencePoints;
    }

    @Override
    public int compareTo(VDJCGene o) {
        return getId().compareTo(o.getId());
    }
}
