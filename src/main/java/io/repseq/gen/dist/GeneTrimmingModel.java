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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

@JsonSubTypes({
        @JsonSubTypes.Type(value = CommonNormalGeneTrimmingModel.class, name = "commonNormal"),
        @JsonSubTypes.Type(value = CommonCategoricalGeneTrimmingModel.class, name = "commonCategorical"),
        @JsonSubTypes.Type(value = SeparateCategoricalGeneTrimmingModel.class, name = "separateCategorical")
})
public interface GeneTrimmingModel extends Model {
    GeneTrimmingGenerator create(RandomGenerator random, VDJCGene gene);
}
