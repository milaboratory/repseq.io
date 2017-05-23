package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.*;
import io.repseq.core.GeneType;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.gen.VDJCGenes;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class IndependentVDJCGenesModel implements VDJCGenesModel {
    public final Map<String, Double> v, d, j, c;

    @JsonCreator
    public IndependentVDJCGenesModel(@JsonProperty("v") Map<String, Double> v,
                                     @JsonProperty("d") Map<String, Double> d,
                                     @JsonProperty("j") Map<String, Double> j,
                                     @JsonProperty("c") Map<String, Double> c) {
        this.v = v;
        this.d = d == null ? Collections.EMPTY_MAP : d;
        this.j = j;
        this.c = c == null ? Collections.EMPTY_MAP : c;
    }

    public static VDJCGene geneOrNull(VDJCLibrary library, String name, GeneType expectedGeneType) {
        if (name.equals(""))
            return null;
        else {
            VDJCGene gene = library.getSafe(name);
            if (gene != null && gene.getGeneType() != expectedGeneType)
                throw new IllegalArgumentException("Wrong gene type of " + name + ". Expected: " + expectedGeneType);
            return gene;
        }
    }

    public static EnumeratedDistribution<VDJCGene> toDistribution(RandomGenerator random, VDJCLibrary library,
                                                                  Map<String, Double> distMap, GeneType geneType) {
        List<Pair<VDJCGene, Double>> ps = new ArrayList<>();
        if (distMap == null)
            ps.add(new Pair<VDJCGene, Double>(null, 1.0));
        else
            for (Map.Entry<String, Double> e : distMap.entrySet())
                ps.add(new Pair<>(geneOrNull(library, e.getKey(), geneType), e.getValue()));
        return new EnumeratedDistribution<>(random, ps);
    }

    public static List<VDJCGene> genes(EnumeratedDistribution<VDJCGene> d) {
        List<VDJCGene> result = new ArrayList<>();
        for (Pair<VDJCGene, Double> p : d.getPmf())
            if (p.getFirst() != null)
                result.add(p.getFirst());
        return result;
    }

    @Override
    public VDJCGenesGenerator create(RandomGenerator random, VDJCLibrary library) {
        final EnumeratedDistribution<VDJCGene> vDist = toDistribution(random, library, v, GeneType.Variable);
        final EnumeratedDistribution<VDJCGene> dDist = toDistribution(random, library, d, GeneType.Diversity);
        final EnumeratedDistribution<VDJCGene> jDist = toDistribution(random, library, j, GeneType.Joining);
        final EnumeratedDistribution<VDJCGene> cDist = toDistribution(random, library, c, GeneType.Constant);
        return new VDJCGenesGenerator() {
            @Override
            public List<VDJCGene> genes(GeneType gt) {
                switch (gt) {
                    case Variable:
                        return IndependentVDJCGenesModel.genes(vDist);
                    case Diversity:
                        return IndependentVDJCGenesModel.genes(dDist);
                    case Joining:
                        return IndependentVDJCGenesModel.genes(jDist);
                    case Constant:
                        return IndependentVDJCGenesModel.genes(cDist);
                }
                throw new IllegalArgumentException();
            }

            @Override
            public VDJCGenes sample() {
                return new VDJCGenes(vDist.sample(), dDist.sample(), jDist.sample(), cDist.sample());
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndependentVDJCGenesModel)) return false;

        IndependentVDJCGenesModel that = (IndependentVDJCGenesModel) o;

        if (!v.equals(that.v)) return false;
        if (!d.equals(that.d)) return false;
        if (!j.equals(that.j)) return false;
        return c.equals(that.c);
    }

    @Override
    public int hashCode() {
        int result = v.hashCode();
        result = 31 * result + d.hashCode();
        result = 31 * result + j.hashCode();
        result = 31 * result + c.hashCode();
        return result;
    }
}
