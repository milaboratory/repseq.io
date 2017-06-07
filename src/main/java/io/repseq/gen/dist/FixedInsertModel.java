package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.core.VDJCGene;
import io.repseq.gen.GGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

public final class FixedInsertModel implements InsertModel {
    public final NucleotideSequence sequence;

    @JsonCreator
    public FixedInsertModel(@JsonProperty("sequence") NucleotideSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public InsertGenerator create(RandomGenerator random, boolean v, List<VDJCGene> vGenes, List<VDJCGene> dGenes,
                                  List<VDJCGene> jGenes, List<VDJCGene> cGenes) {
        return new InsertGenerator() {
            @Override
            public NucleotideSequence generate(GGene gene) {
                return sequence;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FixedInsertModel that = (FixedInsertModel) o;

        return sequence.equals(that.sequence);
    }

    @Override
    public int hashCode() {
        return sequence.hashCode();
    }
}
