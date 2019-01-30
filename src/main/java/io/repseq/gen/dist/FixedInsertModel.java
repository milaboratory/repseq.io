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
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.core.VDJCGene;
import io.repseq.gen.GGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

public final class FixedInsertModel implements InsertModel {
    public final NucleotideSequence sequence;

    @JsonCreator
    public FixedInsertModel(@JsonProperty("sequence") NucleotideSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public InsertGenerator create(RandomGenerator random, boolean v, List<VDJCGene> vGenes, List<VDJCGene> dGenes,
                                  List<VDJCGene> jGenes, List<VDJCGene> cGenes) {
        return new InsertGenerator() {
            @Override
            public NucleotideSequence generate(GGene gene) {
                return sequence;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FixedInsertModel that = (FixedInsertModel) o;

        return sequence.equals(that.sequence);
    }

    @Override
    public int hashCode() {
        return sequence.hashCode();
    }
}
