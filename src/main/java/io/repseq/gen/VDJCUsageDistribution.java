package io.repseq.gen;

import io.repseq.core.VDJCLibrary;

/**
 * Represents "abstract" distribution of V, D, J, C genes. E.g. uniform, etc... It is a factory object for actual
 * distribution (see {@link VDJCUsageDistributionInstance}).
 */
public interface VDJCUsageDistribution {
    VDJCUsageDistributionInstance create(VDJCLibrary library);
}
