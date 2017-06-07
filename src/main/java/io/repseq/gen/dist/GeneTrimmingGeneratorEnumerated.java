package io.repseq.gen.dist;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public final class GeneTrimmingGeneratorEnumerated implements GeneTrimmingGenerator {
    final EnumeratedIntegerDistribution distribution;

    public GeneTrimmingGeneratorEnumerated(EnumeratedIntegerDistribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public int sample() {
        return distribution.sample();
    }
}
