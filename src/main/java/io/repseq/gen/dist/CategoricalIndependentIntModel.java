/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
