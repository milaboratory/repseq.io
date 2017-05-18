package io.repseq.gen.dist;

import com.milaboratory.test.TestUtil;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalDistributionParametersTest {
    @Test
    public void distTest1() throws Exception {
        NormalDistributionParameters p = new NormalDistributionParameters(12, 2);
        TestUtil.assertJson(p);
        EnumeratedIntegerDistribution ee = p.truncatedDistribution(new Well19937c(), -100, +100);
        int c12 = 0;
        int c10 = 0;
        for (int i = 0; i < 100000; i++) {
            int v = ee.sample();
            if (v == 12)
                c12++;
            if (v == 10)
                c10++;
        }
        assertEquals(Math.exp(0.5), 1.0 * c12 / c10, 0.1);
    }
}