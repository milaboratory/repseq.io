package io.repseq.gen;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represents actual distribution of V, D, J, C genes. Created with {@link VDJCUsageDistribution}.
 */
public interface VDJCUsageDistributionInstance {
    VDJCGenes generate(RandomGenerator random);
}
