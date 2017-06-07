package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public class CommonCategoricalGeneTrimmingModel extends CategoricalIndependentIntModel
        implements GeneTrimmingModel {
    @JsonCreator
    public CommonCategoricalGeneTrimmingModel(@JsonProperty("distribution") Map<Integer, Double> values) {
        super(values);
    }

    @Override
    public GeneTrimmingGenerator create(RandomGenerator random, VDJCGene gene) {
        final IndependentIntGenerator im = create(random);
        return new GeneTrimmingGenerator() {
            @Override
            public int sample() {
                return im.sample();
            }
        };
    }
}
