package io.repseq.gen;

import com.fasterxml.jackson.annotation.*;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import io.repseq.core.*;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.milaboratory.core.sequence.provider.SequenceProviderUtils.fromSequence;
import static com.milaboratory.core.sequence.provider.SequenceProviderUtils.subProvider;

/**
 * Represents rearranged TCR/IG gene / sequence.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class RearrangedGene extends PartitionedSequenceCached<NucleotideSequence> {
    /**
     * Gene region where this gene is defined. Access to sequences outside this region is forbidden.
     */
    public final GeneFeature definedIn;
    /**
     * V, D, J, C genes.
     */
    @JsonUnwrapped
    @JsonProperty(access = READ_ONLY)
    public final VDJCGenes vdjcGenes;
    /**
     * V, J genes trimming.
     */
    @JsonUnwrapped
    @JsonProperty(access = READ_ONLY)
    public final VJTrimming vjTrimming;
    /**
     * V, J genes trimming.
     */
    @JsonUnwrapped
    @JsonProperty(access = READ_ONLY)
    public final DTrimming dTrimming;
    /**
     * Insert between V and D (if present) or J (if D gene is not present) genes
     */
    public final NucleotideSequence vInsert;
    /**
     * Insert between D or J genes. Only if D gene is present.
     */
    public final NucleotideSequence djInsert;

    //Internal
    private final transient ConcatenatedLazySequence<NucleotideSequence> baseSequence;
    private final transient ExtendedReferencePoints referencePoints;

    @SuppressWarnings("unchecked")
    public RearrangedGene(GeneFeature definedIn, VDJCGenes vdjcGenes, VJTrimming vjTrimming,
                          DTrimming dTrimming, NucleotideSequence vInsert, NucleotideSequence djInsert) {
        this.definedIn = definedIn;
        this.vdjcGenes = vdjcGenes;
        this.vjTrimming = vjTrimming;
        this.dTrimming = dTrimming;
        this.vInsert = vInsert;
        this.djInsert = djInsert;

        // Composing sequence and reference points
        ExtendedReferencePointsBuilder pointsBuilder = new ExtendedReferencePointsBuilder();
        List<SequenceProvider<NucleotideSequence>> sProviders = new ArrayList<>();
        // Variable follows total length of all sProviders
        int currentLength = 0;

        if (vdjcGenes.v != null) {
            // Case with normal rearrangement
            // Working with non-reversed V gene view (in case V gene is on antisense chromosome strand)
            SequenceProviderAndReferencePoints vsprp = vdjcGenes.v.getSPAndRPs().nonReversedView();
            pointsBuilder.setPositionsFrom(vsprp.referencePoints);
            pointsBuilder.setPosition(ReferencePoint.V5UTRBeginTrimmed, vsprp.referencePoints.getPosition(ReferencePoint.UTR5Begin));
            int vEndPosition = vsprp.referencePoints.getPosition(ReferencePoint.VEnd);
            currentLength += vjTrimming.vTrimming + vEndPosition;
            if (vjTrimming.vTrimming >= 0) { // With P-segment
                // V gene
                // Adding whole sequence to the left of V gene
                // (e.g. whole chromosome sequence to the left of V gene also included)
                sProviders.add(subProvider(vsprp.sequenceProvider, new Range(0, vEndPosition)));
                // V-P-segment
                if (vjTrimming.vTrimming != 0)
                    sProviders.add(fromSequence(
                            vsprp.getFeature(new GeneFeature(ReferencePoint.VEnd, 0, -vjTrimming.vTrimming))
                    ));
            } else { // without P-segment
                // V gene without deleted nucleotides
                // Adding whole sequence to the left of V gene
                // (e.g. whole chromosome sequence to the left of V gene also included)
                sProviders.add(subProvider(vsprp.sequenceProvider, new Range(0, vEndPosition + vjTrimming.vTrimming)));
                // VEnd not covered, removing from builder
                pointsBuilder.setPosition(ReferencePoint.VEnd, -1);
            }
            pointsBuilder.setPosition(ReferencePoint.VEndTrimmed, currentLength);
            sProviders.add(fromSequence(vInsert));
            currentLength += vInsert.size();

            // Adding D gene, if present
            if (vdjcGenes.d != null) {
                pointsBuilder.setPosition(ReferencePoint.DBeginTrimmed, currentLength);
                // Adding reference points from D gene
                pointsBuilder.setPositionsFrom(
                        vdjcGenes.d.getPartitioning()
                                .move(currentLength + dTrimming.d5Trimming
                                        - vdjcGenes.d.getPartitioning().getPosition(ReferencePoint.DBegin)));
                // D-5'-P-segment
                if (dTrimming.d5Trimming > 0) {
                    sProviders.add(fromSequence(
                            vdjcGenes.d.getFeature(new GeneFeature(ReferencePoint.DBegin,
                                    dTrimming.d5Trimming, 0))
                    ));
                    currentLength += dTrimming.d5Trimming;
                }
                // If deletions on the 5' side of D gene, DBegin not covered
                if (dTrimming.d5Trimming < 0)
                    pointsBuilder.setPosition(ReferencePoint.DBegin, -1);
                // D gene body
                NucleotideSequence dGene = vdjcGenes.d.getFeature(
                        new GeneFeature(GeneFeature.DRegion,
                                -Math.min(dTrimming.d5Trimming, 0), Math.min(dTrimming.d3Trimming, 0)));
                sProviders.add(fromSequence(dGene));
                currentLength += dGene.size();
                // If deletions on the 3' side of D gene, DEnd not covered
                if (dTrimming.d3Trimming < 0)
                    pointsBuilder.setPosition(ReferencePoint.DEnd, -1);
                // D-5'-P-segment
                if (dTrimming.d3Trimming > 0) {
                    sProviders.add(fromSequence(
                            vdjcGenes.d.getFeature(new GeneFeature(ReferencePoint.DEnd,
                                    0, -dTrimming.d3Trimming))
                    ));
                    currentLength += dTrimming.d3Trimming;
                }
                pointsBuilder.setPosition(ReferencePoint.DEndTrimmed, currentLength);
                // Adding D-J insert
                sProviders.add(fromSequence(djInsert));
                currentLength += djInsert.size();
            }
        } else {
            throw new RuntimeException("D-J-only recombinations not implemented yet.");
        }

        // Adding J gene
        pointsBuilder.setPosition(ReferencePoint.JBeginTrimmed, currentLength);

        // Working with non-reversed J gene view (in case J gene is on antisense chromosome strand)
        SequenceProviderAndReferencePoints jsprp = vdjcGenes.j.getSPAndRPs().nonReversedView();

        // Adding J gene reference points
        int jBeginPosition = jsprp.referencePoints.getPosition(ReferencePoint.JBegin);
        int jBaseSequenceOffset = currentLength + vjTrimming.jTrimming - jBeginPosition;
        pointsBuilder.setPositionsFrom(jsprp.referencePoints.move(jBaseSequenceOffset));

        // J-P-segment
        if (vjTrimming.jTrimming > 0) {
            sProviders.add(fromSequence(
                    vdjcGenes.j.getFeature(new GeneFeature(ReferencePoint.JBegin,
                            vjTrimming.jTrimming, 0))
            ));
        }

        // If J has deletions, JBegin - not covered
        if (vjTrimming.jTrimming < 0)
            pointsBuilder.setPosition(ReferencePoint.JBegin, -1);

        // Adding whole sequence to the right of JBegin with appropriate offset
        // (e.g. whole chromosome sequence to the right of J gene)
        sProviders.add(subProvider(jsprp.sequenceProvider,
                new Range(jBeginPosition - Math.min(vjTrimming.jTrimming, 0),
                        jsprp.sequenceProvider.size())));

        currentLength += vjTrimming.jTrimming + jsprp.sequenceProvider.size() - jBeginPosition;

        if (vdjcGenes.c != null) {
            // Non-reversed view on C gene base sequence
            SequenceProviderAndReferencePoints csprp = vdjcGenes.c.getSPAndRPs().nonReversedView();

            // Checking if J and C genes are on the same strand of the same chromosome (sequence)
            if (vdjcGenes.j.getSequenceProvider() == vdjcGenes.c.getSequenceProvider() &&
                    vdjcGenes.j.getPartitioning().isReversed() == vdjcGenes.c.getPartitioning().isReversed()) {
                // If so, sequence of C gene is already inside our concatenated provider,
                // we only need to add C gene reference points
                // in this case csprp.sequenceProvider equals to jsprp.sequenceProvider
                pointsBuilder.setPositionsFrom(csprp.referencePoints.move(jBaseSequenceOffset));
            } else {
                // If not - adding separate sequence provider for C gene
                int cBegin = csprp.referencePoints.getPosition(ReferencePoint.CBegin);
                sProviders.add(subProvider(csprp.sequenceProvider, new Range(cBegin, csprp.sequenceProvider.size())));
                pointsBuilder.setPositionsFrom(csprp.referencePoints.move(currentLength - cBegin));
            }
        }

        baseSequence = new ConcatenatedLazySequence<>(sProviders.toArray(new SequenceProvider[sProviders.size()]));
        referencePoints = pointsBuilder.build();
    }

    @JsonCreator
    public RearrangedGene(@JsonProperty("definedIn") GeneFeature definedIn,
                          @JsonProperty("v") VDJCGene v,
                          @JsonProperty("d") VDJCGene d,
                          @JsonProperty("j") VDJCGene j,
                          @JsonProperty("c") VDJCGene c,
                          @JsonProperty("vTrimming") int vTrimming,
                          @JsonProperty("jTrimming") int jTrimming,
                          @JsonProperty("d5Trimming") Integer d5Trimming,
                          @JsonProperty("d3Trimming") Integer d3Trimming,
                          @JsonProperty("vInsert") NucleotideSequence vInsert,
                          @JsonProperty("djInsert") NucleotideSequence djInsert) {
        this(definedIn, new VDJCGenes(v, d, j, c), new VJTrimming(vTrimming, jTrimming),
                d3Trimming != null ? new DTrimming(d5Trimming, d3Trimming) : null, vInsert, djInsert);
    }

    @Override
    protected NucleotideSequence getSequence(Range range) {
        return baseSequence.getRegion(range);
    }

    @Override
    protected SequencePartitioning getPartitioning() {
        return referencePoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RearrangedGene that = (RearrangedGene) o;

        if (definedIn != null ? !definedIn.equals(that.definedIn) : that.definedIn != null) return false;
        if (vdjcGenes != null ? !vdjcGenes.equals(that.vdjcGenes) : that.vdjcGenes != null) return false;
        if (vjTrimming != null ? !vjTrimming.equals(that.vjTrimming) : that.vjTrimming != null) return false;
        if (dTrimming != null ? !dTrimming.equals(that.dTrimming) : that.dTrimming != null) return false;
        if (vInsert != null ? !vInsert.equals(that.vInsert) : that.vInsert != null) return false;
        return djInsert != null ? djInsert.equals(that.djInsert) : that.djInsert == null;
    }

    @Override
    public int hashCode() {
        int result = definedIn != null ? definedIn.hashCode() : 0;
        result = 31 * result + (vdjcGenes != null ? vdjcGenes.hashCode() : 0);
        result = 31 * result + (vjTrimming != null ? vjTrimming.hashCode() : 0);
        result = 31 * result + (dTrimming != null ? dTrimming.hashCode() : 0);
        result = 31 * result + (vInsert != null ? vInsert.hashCode() : 0);
        result = 31 * result + (djInsert != null ? djInsert.hashCode() : 0);
        return result;
    }
}
