package io.repseq.gen.dist;

import org.apache.commons.math3.random.RandomGenerator;

public interface IntDistribution {
    int sample(RandomGenerator random);
}
