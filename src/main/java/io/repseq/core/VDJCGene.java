package io.repseq.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import com.milaboratory.primitivio.annotations.Serializable;
import io.repseq.dto.VDJCGeneData;

/**
 * This class represents the same entity as Allele class in previous abstraction version.
 */
@JsonSerialize(using = IO.VDJCGeneJSONSerializer.class)
@JsonDeserialize(using = IO.VDJCGeneJSONDeserializer.class)
@Serializable(by = IO.VDJCGeneSerializer.class)
public final class VDJCGene extends PartitionedSequenceCached<NucleotideSequence>
        implements Comparable<VDJCGene> {
    /**
     * Attribute key used to set current VDJCLibrary during serialization / deserialization. If not set full gene names
     * will be written to the output file, and global VDJCLibraryRegistry will be used for gene lookup.
     */
    public static final String JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY = "currentLibrary";
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
     * Return full gene name, including library id (e.g. repseqio.v.1.2:9060:6dc0513f4400b2abd19487474154a77e/TRBV12-3*00)
     *
     * @return full gene name, including library id (e.g. repseqio.v.1.2:9060:6dc0513f4400b2abd19487474154a77e/TRBV12-3*00)
     */
    public String getFullName() {
        return getId().getFullName();
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

    /**
     * Returns tuple of {@link com.milaboratory.core.sequence.provider.SequenceProvider} and {@link ReferencePoints}
     * with useful methods
     *
     * @return tuple of {@link com.milaboratory.core.sequence.provider.SequenceProvider} and {@link ReferencePoints}
     * with useful methods
     */
    public SequenceProviderAndReferencePoints getSPAndRPs() {
        return new SequenceProviderAndReferencePoints(sequenceProvider, referencePoints);
    }

    @Override
    protected NucleotideSequence getSequence(Range range) {
        return sequenceProvider.getRegion(range);
    }

    /**
     * Returns whether all reference points for this gene are defined
     *
     * @return whether all reference points for this gene are defined
     */
    public boolean isComplete() {
        return getGeneType().getCompleteNumberOfReferencePoints() == referencePoints.numberOfDefinedPoints();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VDJCGene vdjcGene = (VDJCGene) o;

        return vdjcGene.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
