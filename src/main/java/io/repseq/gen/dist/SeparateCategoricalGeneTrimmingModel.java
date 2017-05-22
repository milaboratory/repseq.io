package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public final class SeparateCategoricalGeneTrimmingModel implements GeneTrimmingModel {
    public final Map<String, Map<Integer, Double>> distributions;

    @JsonCreator
    public SeparateCategoricalGeneTrimmingModel(@JsonProperty("distributions") Map<String, Map<Integer, Double>> distributions) {
        this.distributions = distributions;
    }

    @Override
    public GeneTrimmingGenerator create(RandomGenerator random, VDJCGene gene) {
        Map<Integer, Double> probs = distributions.get(gene.getName());
        if (probs == null)
            throw new IllegalArgumentException("No trimming distribution for " + gene.getName() + ".");
        final IndependentIntGenerator gen = new CategoricalIndependentIntModel(probs).create(random);
        return new GeneTrimmingGenerator() {
            @Override
            public int sample() {
                return gen.sample();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeparateCategoricalGeneTrimmingModel)) return false;

        SeparateCategoricalGeneTrimmingModel that = (SeparateCategoricalGeneTrimmingModel) o;

        return distributions.equals(that.distributions);
    }

    @Override
    public int hashCode() {
        return distributions.hashCode();
    }
}
