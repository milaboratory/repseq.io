package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

@JsonSubTypes({
        @JsonSubTypes.Type(value = CommonNormalDTrimmingModel.class, name = "commonNormal"),
        @JsonSubTypes.Type(value = CommonCategoricalDGeneTrimmingModel.class, name = "commonCategorical"),
        @JsonSubTypes.Type(value = SeparateCategoricalDGeneTrimmingModel.class, name = "separateCategorical")
})
public interface DTrimmingModel extends Model {
    DTrimmingGenerator create(RandomGenerator random, VDJCGene gene);
}
