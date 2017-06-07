package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;

public final class ParetoModel implements IndependentRealModel {
    public final double xm, alpha;

    @JsonCreator
    public ParetoModel(@JsonProperty("xm") double xm, @JsonProperty("alpha")double alpha) {
        this.xm = xm;
        this.alpha = alpha;
    }

    @Override
    public IndependentRealGenerator create(RandomGenerator random) {
        final ParetoDistribution dist = new ParetoDistribution(random, xm, alpha);
        return new IndependentRealGenerator() {
            @Override
            public double generate() {
                return dist.sample();
            }
        };
    }
}
