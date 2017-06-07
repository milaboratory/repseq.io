package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

@JsonSubTypes({
        @JsonSubTypes.Type(value = CommonNormalGeneTrimmingModel.class, name = "commonNormal"),
        @JsonSubTypes.Type(value = CommonCategoricalGeneTrimmingModel.class, name = "commonCategorical"),
        @JsonSubTypes.Type(value = SeparateCategoricalGeneTrimmingModel.class, name = "separateCategorical")
})
public interface GeneTrimmingModel extends Model {
    GeneTrimmingGenerator create(RandomGenerator random, VDJCGene gene);
}
