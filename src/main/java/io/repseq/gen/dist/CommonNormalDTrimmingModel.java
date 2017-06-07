package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.repseq.core.GeneFeature;
import io.repseq.core.VDJCGene;
import io.repseq.gen.DTrimming;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

public final class CommonNormalDTrimmingModel implements DTrimmingModel {
    @JsonUnwrapped
    @JsonProperty(access = READ_ONLY)
    public final NormalDistributionParameters parameters5, parameters3;
    public final int maxPLength5, maxPLength3;

    @JsonCreator
    public CommonNormalDTrimmingModel(@JsonProperty("parameters5") NormalDistributionParameters parameters5,
                                      @JsonProperty("parameters3") NormalDistributionParameters parameters3,
                                      @JsonProperty("maxPLength5") int maxPLength5,
                                      @JsonProperty("maxPLength3") int maxPLength3) {
        this.parameters5 = parameters5;
        this.parameters3 = parameters3;
        this.maxPLength5 = maxPLength5;
        this.maxPLength3 = maxPLength3;
    }

    @Override
    public DTrimmingGenerator create(RandomGenerator random, VDJCGene gene) {
        final int dLength = gene.getPartitioning().getLength(GeneFeature.DRegion);
        final EnumeratedIntegerDistribution dist3 = parameters3.truncatedDistribution(random, -dLength,
                Math.min(dLength, maxPLength3));
        final EnumeratedIntegerDistribution dist5 = parameters5.truncatedDistribution(random, -dLength,
                Math.min(dLength, maxPLength5));
        return new DTrimmingGenerator() {
            @Override
            public DTrimming sample() {
                int trimming3 = dist3.sample();
                int trimming5 = dist5.sample();
                if (trimming3 + trimming5 < -dLength) {
                    int excess = dLength - trimming3 - trimming5;
                    trimming3 += excess / 2;
                    trimming5 += excess - (excess / 2);
                }
                return new DTrimming(trimming3, trimming5);
            }
        };
    }
}
