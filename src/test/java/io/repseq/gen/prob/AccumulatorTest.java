package io.repseq.gen.prob;

import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.gen.dist.GCloneGenerator;
import io.repseq.gen.dist.GCloneModel;
import io.repseq.gen.dist.GModels;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mikesh on 6/29/17.
 */
public class AccumulatorTest {
    @Test
    public void test() throws Exception {
        GCloneModel model = GModels.getGCloneModelByName("murugan_vjcorr");

        GCloneGenerator generator = model.create(new Well19937c(51102),
                VDJCLibraryRegistry.getDefault());

        RearrangementAccumulator rearrangementAccumulator = new RearrangementAccumulator(new VJCdr3aaKeyGen("TRB"));

        long sampleSize = 10000;
        rearrangementAccumulator.update(generator, sampleSize);

        Assert.assertEquals(sampleSize,
                rearrangementAccumulator.getTotalCounter().getNumberOfRearrangements());
    }
}
