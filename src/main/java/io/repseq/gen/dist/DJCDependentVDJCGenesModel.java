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

public final class DJCDependentVDJCGenesModel implements VDJCGenesModel {
    public final Map<String, Double> v, djc;

    @JsonCreator
    public DJCDependentVDJCGenesModel(@JsonProperty("v") Map<String, Double> v,
                                      @JsonProperty("djc") Map<String, Double> djc) {
        this.v = v;
        this.djc = djc;
    }

    public static EnumeratedDistribution<VDJCGene3> toDistribution3(RandomGenerator random,
                                                                    VDJCLibrary library,
                                                                    Map<String, Double> distMap,
                                                                    GeneType geneType1, GeneType geneType2,
                                                                    GeneType geneType3) {
        List<Pair<VDJCGene3, Double>> ps = new ArrayList<>();
        for (Map.Entry<String, Double> e : distMap.entrySet()) {
            String[] split = e.getKey().split("\\|");
            if (split.length != 3)
                throw new IllegalArgumentException("Wrong key format (expected single '|' symbol): " + e.getKey());

            ps.add(new Pair<>(
                    new VDJCGene3(
                            geneOrNull(library, split[0], geneType1),
                            geneOrNull(library, split[1], geneType2),
                            geneOrNull(library, split[2], geneType3)),
                    e.getValue()));
        }
        return new EnumeratedDistribution<>(random, ps);
    }

    public static final class VDJCGene3 {
        public final VDJCGene gene1, gene2, gene3;

        public VDJCGene3(VDJCGene gene1, VDJCGene gene2, VDJCGene gene3) {
            this.gene1 = gene1;
            this.gene2 = gene2;
            this.gene3 = gene3;
        }

        public VDJCGene get(int index) {
            switch (index) {
                case 0:
                    return gene1;
                case 1:
                    return gene2;
                case 2:
                    return gene3;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }

    @Override
    public VDJCGenesGenerator create(RandomGenerator random, VDJCLibrary library) {
        final EnumeratedDistribution<VDJCGene> vDist = toDistribution(random, library, v, GeneType.Variable);
        final EnumeratedDistribution<VDJCGene3> djcDist = toDistribution3(random, library, djc,
                GeneType.Diversity, GeneType.Joining, GeneType.Constant);
        return new VDJCGenesGenerator() {
            @Override
            public List<VDJCGene> genes(GeneType gt) {
                switch (gt) {
                    case Variable:
                        return IndependentVDJCGenesModel.genes(vDist);
                    case Diversity:
                        return DJCDependentVDJCGenesModel.genes(djcDist, 0);
                    case Joining:
                        return DJCDependentVDJCGenesModel.genes(djcDist, 1);
                    case Constant:
                        return DJCDependentVDJCGenesModel.genes(djcDist, 2);
                }
                throw new IllegalArgumentException();
            }

            @Override
            public VDJCGenes sample() {
                VDJCGene3 djc = djcDist.sample();
                return new VDJCGenes(vDist.sample(), djc.gene1, djc.gene2, djc.gene3);
            }
        };
    }

    public static List<VDJCGene> genes(EnumeratedDistribution<VDJCGene3> d, int index) {
        Set<VDJCGene> result = new HashSet<>();
        for (Pair<VDJCGene3, Double> p : d.getPmf())
            if (p.getFirst() != null)
                result.add(p.getFirst().get(index));
        return new ArrayList<>(result);
    }
}
