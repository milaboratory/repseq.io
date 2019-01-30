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
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;

public final class ParetoModel implements IndependentRealModel {
    public final double xm, alpha;

    @JsonCreator
    public ParetoModel(@JsonProperty("xm") double xm, @JsonProperty("alpha")double alpha) {
        this.xm = xm;
        this.alpha = alpha;
    }

    @Override
    public IndependentRealGenerator create(RandomGenerator random) {
        final ParetoDistribution dist = new ParetoDistribution(random, xm, alpha);
        return new IndependentRealGenerator() {
            @Override
            public double generate() {
                return dist.sample();
            }
        };
    }
}
