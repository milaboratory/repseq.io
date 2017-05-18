package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class NormalDistributionParameters {
    public final double mu, sigma;

    @JsonCreator
    public NormalDistributionParameters(@JsonProperty("mu") double mu,
                                        @JsonProperty("sigma") double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    /**
     * Both boundaries are inclusive.
     */
    public EnumeratedIntegerDistribution truncatedDistribution(RandomGenerator random, int from, int to) {
        int count = to - from + 1;
        int[] values = new int[count];
        double[] weights = new double[count]; // ~ probability, will be normalized in distribution constructor
        for (int i = 0, t = from; i < count; i++, t++) {
            values[i] = t;
            weights[i] = Math.exp(-(t - mu) * (t - mu) / (2 * sigma * sigma));
        }
        return new EnumeratedIntegerDistribution(random, values, weights);
    }

    @Override
    public String toString() {
        return "NormalDistributionParameters{" +
                "mu=" + mu +
                ", sigma=" + sigma +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NormalDistributionParameters that = (NormalDistributionParameters) o;

        if (Double.compare(that.mu, mu) != 0) return false;
        return Double.compare(that.sigma, sigma) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(mu);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sigma);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
