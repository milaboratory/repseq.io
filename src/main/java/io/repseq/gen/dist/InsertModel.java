package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedInsertModel.class, name = "fixed"),
        @JsonSubTypes.Type(value = MarkovInsertModel.Model5.class, name = "5'markov"),
        @JsonSubTypes.Type(value = MarkovInsertModel.Model3.class, name = "3'markov")
})
public interface InsertModel extends Model {
    /**
     * Initialize insert generator
     *
     * @param random source of random data
     * @param v      {@literal true} for V(J/D) insert, {@literal false} for DJ insert
     * @return insert generator
     */
    InsertGenerator create(RandomGenerator random, boolean v,
                           List<VDJCGene> vGenes, List<VDJCGene> dGenes,
                           List<VDJCGene> jGenes, List<VDJCGene> cGenes);
}
