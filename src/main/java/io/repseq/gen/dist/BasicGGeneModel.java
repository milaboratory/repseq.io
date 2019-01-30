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
import io.repseq.core.GeneType;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.gen.GGene;
import io.repseq.gen.VDJCGenes;
import io.repseq.gen.VDJTrimming;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

public final class BasicGGeneModel implements GGeneModel {
    public final VDJCGenesModel vdjcGenesModel;
    public final VDJTrimmingModel trimmingModel;
    public final InsertModel vInsertModel, djInsertModel;

    @JsonCreator
    public BasicGGeneModel(@JsonProperty("vdjcGenesModel") VDJCGenesModel vdjcGenesModel,
                           @JsonProperty("trimmingModel") VDJTrimmingModel trimmingModel,
                           @JsonProperty("vInsertModel") InsertModel vInsertModel,
                           @JsonProperty("djInsertModel") InsertModel djInsertModel) {
        this.vdjcGenesModel = vdjcGenesModel;
        this.trimmingModel = trimmingModel;
        this.vInsertModel = vInsertModel;
        this.djInsertModel = djInsertModel;
    }

    @Override
    public GGeneGenerator create(RandomGenerator random, VDJCLibrary library) {
        final VDJCGenesGenerator vdjcGenesGenerator = vdjcGenesModel.create(random, library);
        List<VDJCGene> vGenes = vdjcGenesGenerator.genes(GeneType.Variable);
        List<VDJCGene> dGenes = vdjcGenesGenerator.genes(GeneType.Diversity);
        List<VDJCGene> jGenes = vdjcGenesGenerator.genes(GeneType.Joining);
        List<VDJCGene> cGenes = vdjcGenesGenerator.genes(GeneType.Constant);
        final VDJTrimmingGenerator trimmingGenerator = trimmingModel.create(random, vGenes, dGenes, jGenes, cGenes);
        final InsertGenerator vInsertGenerator = vInsertModel.create(random, true, vGenes, dGenes, jGenes, cGenes);
        final InsertGenerator djInsertGenerator = dGenes.isEmpty() ? null : djInsertModel.create(random, false, vGenes, dGenes, jGenes, cGenes);
        return new GGeneGenerator() {
            @Override
            public GGene generate() {
                VDJCGenes vdjcGenes = vdjcGenesGenerator.sample();
                VDJTrimming trimming = trimmingGenerator.sample(vdjcGenes);
                assert !vdjcGenes.isDDefined() || (vdjcGenes.isDDefined() && djInsertGenerator != null);
                NucleotideSequence vInsert;
                NucleotideSequence djInsert;
                if (vdjcGenes.isDDefined()) {
                    assert djInsertGenerator != null;
                    GGene tempGene = new GGene(null, vdjcGenes, trimming, NucleotideSequence.EMPTY,
                            NucleotideSequence.EMPTY);
                    vInsert = vInsertGenerator.generate(tempGene);
                    djInsert = djInsertGenerator.generate(tempGene);
                } else {
                    GGene tempGene = new GGene(null, vdjcGenes, trimming, NucleotideSequence.EMPTY, null);
                    vInsert = vInsertGenerator.generate(tempGene);
                    djInsert = null;
                }
                return new GGene(null, vdjcGenes, trimming, vInsert, djInsert);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicGGeneModel)) return false;

        BasicGGeneModel that = (BasicGGeneModel) o;

        if (!vdjcGenesModel.equals(that.vdjcGenesModel)) return false;
        if (!trimmingModel.equals(that.trimmingModel)) return false;
        if (!vInsertModel.equals(that.vInsertModel)) return false;
        return djInsertModel != null ? djInsertModel.equals(that.djInsertModel) : that.djInsertModel == null;
    }

    @Override
    public int hashCode() {
        int result = vdjcGenesModel.hashCode();
        result = 31 * result + trimmingModel.hashCode();
        result = 31 * result + vInsertModel.hashCode();
        result = 31 * result + (djInsertModel != null ? djInsertModel.hashCode() : 0);
        return result;
    }
}
