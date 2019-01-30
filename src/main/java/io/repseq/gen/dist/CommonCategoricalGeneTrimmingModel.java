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
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public class CommonCategoricalGeneTrimmingModel extends CategoricalIndependentIntModel
        implements GeneTrimmingModel {
    @JsonCreator
    public CommonCategoricalGeneTrimmingModel(@JsonProperty("distribution") Map<Integer, Double> values) {
        super(values);
    }

    @Override
    public GeneTrimmingGenerator create(RandomGenerator random, VDJCGene gene) {
        final IndependentIntGenerator im = create(random);
        return new GeneTrimmingGenerator() {
            @Override
            public int sample() {
                return im.sample();
            }
        };
    }
}
