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
package io.repseq.dto;

import org.junit.Assert;
import org.junit.Test;

public class VDJCDataUtilsTest {
    @Test
    public void test1() throws Exception {
        smartCompareAssert("A", "B");
        smartCompareAssert("A1", "B");
        smartCompareAssert("A", "A1");
        smartCompareAssert("A1", "AT");
        smartCompareAssert("A1", "A2");
        smartCompareAssert("A1ZZZ", "A123");
        smartCompareAssert("A123ZZZ12", "A123ZZZ14");
        smartCompareAssert("A123ZZZ12", "A123ZZZ12d");
        smartCompareAssert("A123ZZZ12d", "AZZZ12");
    }

    public void smartCompareAssert(String lower, String higher) {
        Assert.assertTrue(lower + " < " + higher, VDJCDataUtils.smartCompare(lower, higher) < 0);
        Assert.assertTrue(higher + " > " + lower, VDJCDataUtils.smartCompare(higher, lower) > 0);
        Assert.assertEquals(higher + " == " + higher, 0, VDJCDataUtils.smartCompare(higher, higher));
        Assert.assertEquals(lower + " == " + lower, 0, VDJCDataUtils.smartCompare(lower, lower));
    }
}