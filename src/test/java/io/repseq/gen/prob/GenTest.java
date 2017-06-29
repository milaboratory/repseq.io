package io.repseq.gen.prob;

import io.repseq.core.*;
import io.repseq.gen.GClone;
import io.repseq.gen.GGene;
import io.repseq.gen.dist.*;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mikesh on 6/29/17.
 */
public class GenTest {
    @Test
    public void mdlTest() throws Exception {
        GCloneModel model = GModels.getGCloneModelByName("murugan_vjcorr");

        GCloneGenerator generator = model.create(new Well19937c(51102),
                VDJCLibraryRegistry.getDefault());

        Assert.assertNotNull(VDJCLibraryRegistry.getDefault().getLibrary(model.libraryId()));

        for (int i = 0; i < 1000; i++) {
            GClone clone = generator.get();

            for (GGene g : clone.genes.values()) {
                Assert.assertNotNull(g.getFeature(GeneFeature.CDR3));
                Assert.assertNotNull(g.vdjcGenes.v);
                Assert.assertNotNull(g.vdjcGenes.d);
                Assert.assertNotNull(g.vdjcGenes.j);
            }
        }
    }
}
