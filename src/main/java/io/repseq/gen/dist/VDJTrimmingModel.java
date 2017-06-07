package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

/**
 * Trimming model, that depends on selected v, d, j, c genes.
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = IndependentVDJTrimmingModel.class, name = "independent")
})
public interface VDJTrimmingModel extends Model {
    VDJTrimmingGenerator create(RandomGenerator random,
                                List<VDJCGene> vGenes, List<VDJCGene> dGenes,
                                List<VDJCGene> jGenes, List<VDJCGene> cGenes);
}
