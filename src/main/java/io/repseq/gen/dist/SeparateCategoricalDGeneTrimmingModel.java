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

import java.util.Map;

public final class SeparateCategoricalDGeneTrimmingModel implements DTrimmingModel {
    public final Map<String, Map<String, Double>> distributions;

    @JsonCreator
    public SeparateCategoricalDGeneTrimmingModel(@JsonProperty("distributions") Map<String, Map<String, Double>> distributions) {
        this.distributions = distributions;
    }

    @Override
    public DTrimmingGenerator create(RandomGenerator random, VDJCGene gene) {
        Map<String, Double> probs = distributions.get(gene.getName());
        if (probs == null)
            throw new IllegalArgumentException("No distribution for " + gene.getName() + ".");
        final EnumeratedDistribution<DTrimming> dist =
                CommonCategoricalDGeneTrimmingModel
                        .createDistribution(random, probs);
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
        if (!(o instanceof SeparateCategoricalDGeneTrimmingModel)) return false;

        SeparateCategoricalDGeneTrimmingModel that = (SeparateCategoricalDGeneTrimmingModel) o;

        return distributions.equals(that.distributions);
    }

    @Override
    public int hashCode() {
        return distributions.hashCode();
    }
}
