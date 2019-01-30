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
import io.repseq.core.GeneType;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.gen.VDJCGenes;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.*;

import static io.repseq.gen.dist.IndependentVDJCGenesModel.geneOrNull;
import static io.repseq.gen.dist.IndependentVDJCGenesModel.toDistribution;

public final class DJDependentVDJCGenesModel implements VDJCGenesModel {
    final Map<String, Double> v, dj, c;

    @JsonCreator
    public DJDependentVDJCGenesModel(@JsonProperty("v") Map<String, Double> v,
                                     @JsonProperty("dj") Map<String, Double> dj,
                                     @JsonProperty("c") Map<String, Double> c) {
        this.v = v;
        this.dj = dj;
        this.c = c;
    }

    public static EnumeratedDistribution<VDJCGene2> toDistribution2(RandomGenerator random,
                                                                    VDJCLibrary library,
                                                                    Map<String, Double> distMap,
                                                                    GeneType geneType1, GeneType geneType2) {
        List<Pair<VDJCGene2, Double>> ps = new ArrayList<>();
        for (Map.Entry<String, Double> e : distMap.entrySet()) {
            String[] split = e.getKey().split("\\|");
            if (split.length != 2)
                throw new IllegalArgumentException("Wrong key format (expected single '|' symbol): " + e.getKey());

            ps.add(new Pair<>(new VDJCGene2(geneOrNull(library, split[0], geneType1),
                    geneOrNull(library, split[1], geneType2)), e.getValue()));
        }
        return new EnumeratedDistribution<>(random, ps);
    }

    public static final class VDJCGene2 {
        public final VDJCGene gene1, gene2;

        public VDJCGene2(VDJCGene gene1, VDJCGene gene2) {
            this.gene1 = gene1;
            this.gene2 = gene2;
        }

        public VDJCGene get(int index) {
            switch (index) {
                case 0:
                    return gene1;
                case 1:
                    return gene2;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }

    @Override
    public VDJCGenesGenerator create(RandomGenerator random, VDJCLibrary library) {
        final EnumeratedDistribution<VDJCGene> vDist = toDistribution(random, library, v, GeneType.Variable);
        final EnumeratedDistribution<VDJCGene2> djDist = toDistribution2(random, library, dj,
                GeneType.Diversity, GeneType.Joining);
        final EnumeratedDistribution<VDJCGene> cDist = toDistribution(random, library, c, GeneType.Constant);
        return new VDJCGenesGenerator() {
            @Override
            public List<VDJCGene> genes(GeneType gt) {
                switch (gt) {
                    case Variable:
                        return IndependentVDJCGenesModel.genes(vDist);
                    case Diversity:
                        return DJDependentVDJCGenesModel.genes(djDist, 0);
                    case Joining:
                        return DJDependentVDJCGenesModel.genes(djDist, 1);
                    case Constant:
                        return IndependentVDJCGenesModel.genes(cDist);
                }
                throw new IllegalArgumentException();
            }

            @Override
            public VDJCGenes sample() {
                VDJCGene2 dj = djDist.sample();
                return new VDJCGenes(vDist.sample(), dj.gene1, dj.gene2, cDist.sample());
            }
        };
    }

    public static List<VDJCGene> genes(EnumeratedDistribution<VDJCGene2> d, int index) {
        Set<VDJCGene> result = new HashSet<>();
        for (Pair<VDJCGene2, Double> p : d.getPmf())
            if (p.getFirst() != null)
                result.add(p.getFirst().get(index));
        return new ArrayList<>(result);
    }
}
