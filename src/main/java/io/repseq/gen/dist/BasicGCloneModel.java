package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryId;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.gen.GClone;
import io.repseq.gen.GGene;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.Map;

public final class BasicGCloneModel implements GCloneModel {
    public final VDJCLibraryId vdjcLibrary;
    public final IndependentRealModel abundanceModel;
    public final Map<String, GGeneModel> geneModels;

    @JsonCreator
    public BasicGCloneModel(@JsonProperty("library") VDJCLibraryId vdjcLibrary,
                            @JsonProperty("abundanceModel") IndependentRealModel abundanceModel,
                            @JsonProperty("geneModels") Map<String, GGeneModel> geneModels) {
        this.vdjcLibrary = vdjcLibrary;
        this.abundanceModel = abundanceModel;
        this.geneModels = geneModels;
    }

    @Override
    public VDJCLibraryId libraryId() {
        return vdjcLibrary;
    }

    @Override
    public GCloneGenerator create(RandomGenerator random, VDJCLibraryRegistry registry) {
        VDJCLibrary library = registry.getLibrary(vdjcLibrary);
        final IndependentRealGenerator abundanceGenerator = abundanceModel.create(random);
        final Map<String, GGeneGenerator> geneGenerators = new HashMap<>();
        for (Map.Entry<String, GGeneModel> e : geneModels.entrySet())
            geneGenerators.put(e.getKey(), e.getValue().create(random, library));
        return new GCloneGenerator() {
            @Override
            public GClone get() {
                double abundance = abundanceGenerator.generate();
                Map<String, GGene> genes = new HashMap<>();
                for (Map.Entry<String, GGeneGenerator> e : geneGenerators.entrySet())
                    genes.put(e.getKey(), e.getValue().generate());
                return new GClone(abundance, genes);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicGCloneModel)) return false;

        BasicGCloneModel that = (BasicGCloneModel) o;

        if (!vdjcLibrary.equals(that.vdjcLibrary)) return false;
        if (!abundanceModel.equals(that.abundanceModel)) return false;
        return geneModels.equals(that.geneModels);
    }

    @Override
    public int hashCode() {
        int result = vdjcLibrary.hashCode();
        result = 31 * result + abundanceModel.hashCode();
        result = 31 * result + geneModels.hashCode();
        return result;
    }
}
