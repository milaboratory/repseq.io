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
package io.repseq.core;

import com.milaboratory.test.TestUtil;
import org.junit.Assert;
import org.junit.Test;

public class ChainsTest {
    @Test
    public void serializationDeserialization1() throws Exception {
        Chains c = new Chains("TRB", "TRA");
        TestUtil.assertJson(c);
    }

    @Test
    public void test1() throws Exception {
        Assert.assertTrue(Chains.ALL.intersects(Chains.ALL));
        Assert.assertTrue(Chains.ALL.intersects(Chains.TRA));
        Assert.assertTrue(Chains.TRA.intersects(Chains.ALL));

        Assert.assertFalse(Chains.TRA.intersects(Chains.EMPTY));
        Assert.assertFalse(Chains.EMPTY.intersects(Chains.TRA));
        Assert.assertFalse(Chains.ALL.intersects(Chains.EMPTY));
        Assert.assertFalse(Chains.EMPTY.intersects(Chains.ALL));
    }

    @Test
    public void testParse() throws Exception {
        Assert.assertEquals(Chains.parse("IG, TRB"), Chains.IG.merge(Chains.TRB));
    }
}