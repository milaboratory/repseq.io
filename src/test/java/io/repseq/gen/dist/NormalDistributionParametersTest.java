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