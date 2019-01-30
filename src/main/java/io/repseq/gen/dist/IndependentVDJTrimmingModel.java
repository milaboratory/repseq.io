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
import io.repseq.gen.VDJCGenes;
import io.repseq.gen.VDJTrimming;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IndependentVDJTrimmingModel implements VDJTrimmingModel {
    public final GeneTrimmingModel v;
    public final DTrimmingModel d;
    public final GeneTrimmingModel j;

    @JsonCreator
    public IndependentVDJTrimmingModel(@JsonProperty("v") GeneTrimmingModel v,
                                       @JsonProperty("d") DTrimmingModel d,
                                       @JsonProperty("j") GeneTrimmingModel j) {
        this.v = v;
        this.d = d;
        this.j = j;
    }

    @Override
    public VDJTrimmingGenerator create(RandomGenerator random, List<VDJCGene> vGenes, List<VDJCGene> dGenes, List<VDJCGene> jGenes, List<VDJCGene> cGenes) {
        final Map<VDJCGene, GeneTrimmingGenerator> vGenerators = new HashMap<>();
        final Map<VDJCGene, DTrimmingGenerator> dGenerators = new HashMap<>();
        final Map<VDJCGene, GeneTrimmingGenerator> jGenerators = new HashMap<>();

        for (VDJCGene gene : vGenes)
            vGenerators.put(gene, v.create(random, gene));

        for (VDJCGene gene : dGenes)
            dGenerators.put(gene, d.create(random, gene));

        for (VDJCGene gene : jGenes)
            jGenerators.put(gene, j.create(random, gene));

        return new VDJTrimmingGenerator() {
            @Override
            public VDJTrimming sample(VDJCGenes genes) {
                if (genes.d == null)
                    return new VDJTrimming(
                            vGenerators.get(genes.v).sample(), jGenerators.get(genes.j).sample());
                else
                    return new VDJTrimming(
                            vGenerators.get(genes.v).sample(), jGenerators.get(genes.j).sample(),
                            dGenerators.get(genes.d).sample());
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndependentVDJTrimmingModel)) return false;

        IndependentVDJTrimmingModel that = (IndependentVDJTrimmingModel) o;

        if (!v.equals(that.v)) return false;
        if (d != null ? !d.equals(that.d) : that.d != null) return false;
        return j.equals(that.j);
    }

    @Override
    public int hashCode() {
        int result = v.hashCode();
        result = 31 * result + (d != null ? d.hashCode() : 0);
        result = 31 * result + j.hashCode();
        return result;
    }
}
