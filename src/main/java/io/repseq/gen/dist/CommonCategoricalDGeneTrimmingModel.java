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
import io.repseq.core.VDJCGene;
import io.repseq.gen.DTrimming;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Map;

public final class CommonCategoricalDGeneTrimmingModel implements DTrimmingModel {
    public final Map<String, Double> distribution;

    @JsonCreator
    public CommonCategoricalDGeneTrimmingModel(@JsonProperty("distribution") Map<String, Double> distribution) {
        this.distribution = distribution;
    }

    public static EnumeratedDistribution<DTrimming> createDistribution(RandomGenerator random, Map<String, Double> probabilities) {
        ArrayList<Pair<DTrimming, Double>> points = new ArrayList<>(probabilities.size());
        for (Map.Entry<String, Double> p : probabilities.entrySet()) {
            String[] split = p.getKey().split("\\|");
            if (split.length != 2)
                throw new IllegalArgumentException("Illegal key for D trimming distribution. Found: \"" + p.getKey() +
                        "\" expected something like (\"-2|1\" == \"5'deletions|3'deletions\").");
            try {
                points.add(new Pair<>(new DTrimming(Integer.parseInt(split[0]), Integer.parseInt(split[1])),
                        p.getValue()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Exception during parsing " + p.getKey(), e);
            }
        }
        return new EnumeratedDistribution<>(random, points);
    }

    @Override
    public DTrimmingGenerator create(RandomGenerator random, VDJCGene gene) {
        final EnumeratedDistribution<DTrimming> dist = createDistribution(random, distribution);
        return new DTrimmingGenerator() {
            @Override
            public DTrimming sample() {
                return dist.sample();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonCategoricalDGeneTrimmingModel)) return false;

        CommonCategoricalDGeneTrimmingModel that = (CommonCategoricalDGeneTrimmingModel) o;

        return distribution.equals(that.distribution);
    }

    @Override
    public int hashCode() {
        return distribution.hashCode();
    }
}
