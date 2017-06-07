package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.random.RandomGenerator;

public final class FixedRealModel implements IndependentRealModel {
    public final double value;

    @JsonCreator
    public FixedRealModel(@JsonProperty("value") double value) {
        this.value = value;
    }

    @Override
    public IndependentRealGenerator create(RandomGenerator random) {
        return new IndependentRealGenerator() {
            @Override
            public double generate() {
                return value;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FixedRealModel)) return false;

        FixedRealModel that = (FixedRealModel) o;

        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }
}
