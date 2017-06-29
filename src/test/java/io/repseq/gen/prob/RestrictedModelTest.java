package io.repseq.gen.prob;

import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.gen.dist.GCloneModel;
import io.repseq.gen.dist.GModels;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by mikesh on 6/30/17.
 */
public class RestrictedModelTest {
    @Test
    public void test() throws Exception {
        long sampleSize = 50000;

        GCloneModel model = GModels.getGCloneModelByName("murugan_vjcorr");

        RearrangementAccumulator rearrangementAccumulator1 = new RearrangementAccumulator(new VJCdr3aaKeyGen("TRB")),
                rearrangementAccumulator2 = new RearrangementAccumulator(new VJCdr3aaKeyGen("TRB"));

        rearrangementAccumulator1.update(model.create(new Well19937c(51102),
                VDJCLibraryRegistry.getDefault()), sampleSize);

        RestrictedModel<GCloneModel> restrictedModel = RestrictedModelUtil.restrictIndependent(model,
                new HashSet<>(Collections.singletonList("TRBV21-1*00")),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>());

        rearrangementAccumulator2.update(restrictedModel.getModel().create(new Well19937c(51102),
                VDJCLibraryRegistry.getDefault()), sampleSize);

        for (Object key : rearrangementAccumulator2.getRearrangements().keySet()) {
            Assert.assertEquals("TRBV21-1*00", ((VJCdr3aaKey) key).getV().getName());
        }

        double weight = 0;

        for (Object key : rearrangementAccumulator1.getRearrangements().keySet()) {
            if (Objects.equals(((VJCdr3aaKey) key).getV().getName(), "TRBV21-1*00")) {
                weight++;
            }
        }

        weight /= rearrangementAccumulator1.getTotalCounter().getNumberOfRearrangements();

        Assert.assertTrue(Math.abs(weight - restrictedModel.getWeight()) < 0.01);
    }
}
