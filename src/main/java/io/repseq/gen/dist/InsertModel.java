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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.repseq.core.VDJCGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedInsertModel.class, name = "fixed"),
        @JsonSubTypes.Type(value = MarkovInsertModel.Model5.class, name = "5'markov"),
        @JsonSubTypes.Type(value = MarkovInsertModel.Model3.class, name = "3'markov")
})
public interface InsertModel extends Model {
    /**
     * Initialize insert generator
     *
     * @param random source of random data
     * @param v      {@literal true} for V(J/D) insert, {@literal false} for DJ insert
     * @return insert generator
     */
    InsertGenerator create(RandomGenerator random, boolean v,
                           List<VDJCGene> vGenes, List<VDJCGene> dGenes,
                           List<VDJCGene> jGenes, List<VDJCGene> cGenes);
}
