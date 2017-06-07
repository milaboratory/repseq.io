package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public class CategoricalIndependentIntModel implements IndependentIntModel {
    public final Map<Integer, Double> distribution;

    @JsonCreator
    public CategoricalIndependentIntModel(@JsonProperty("distribution") Map<Integer, Double> distribution) {
        this.distribution = distribution;
    }

    @Override
    public IndependentIntGenerator create(RandomGenerator random) {
        int[] intValues = new int[distribution.size()];
        double[] weights = new double[distribution.size()]; // ~ probability, will be normalized in distribution constructor
        int i = 0;
        for (Map.Entry<Integer, Double> e : distribution.entrySet()) {
            intValues[i] = e.getKey();
            weights[i] = e.getValue();
            ++i;
        }
        final EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(random, intValues, weights);
        return new IndependentIntGenerator() {
            @Override
            public int sample() {
                return dist.sample();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoricalIndependentIntModel)) return false;

        CategoricalIndependentIntModel that = (CategoricalIndependentIntModel) o;

        return distribution.equals(that.distribution);
    }

    @Override
    public int hashCode() {
        return distribution.hashCode();
    }
}
