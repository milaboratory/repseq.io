package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.repseq.core.VDJCGene;
import io.repseq.gen.DTrimming;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public final class SeparateCategoricalDGeneTrimmingModel implements DTrimmingModel {
    public final Map<String, Map<String, Double>> distributions;

    @JsonCreator
    public SeparateCategoricalDGeneTrimmingModel(@JsonProperty("distributions") Map<String, Map<String, Double>> distributions) {
        this.distributions = distributions;
    }

    @Override
    public DTrimmingGenerator create(RandomGenerator random, VDJCGene gene) {
        Map<String, Double> probs = distributions.get(gene.getName());
        if (probs == null)
            throw new IllegalArgumentException("No distribution for " + gene.getName() + ".");
        final EnumeratedDistribution<DTrimming> dist =
                CommonCategoricalDGeneTrimmingModel
                        .createDistribution(random, probs);
        return new DTrimmingGenerator() {
            @Override
            public DTrimming sample() {
                return dist.sample();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeparateCategoricalDGeneTrimmingModel)) return false;

        SeparateCategoricalDGeneTrimmingModel that = (SeparateCategoricalDGeneTrimmingModel) o;

        return distributions.equals(that.distributions);
    }

    @Override
    public int hashCode() {
        return distributions.hashCode();
    }
}
