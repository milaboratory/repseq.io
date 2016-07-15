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
    }

    public void smartCompareAssert(String lower, String higher) {
        Assert.assertTrue(lower + " < " + higher, VDJCDataUtils.smartCompare(lower, higher) < 0);
        Assert.assertTrue(higher + " > " + lower, VDJCDataUtils.smartCompare(higher, lower) > 0);
        Assert.assertEquals(higher + " == " + higher, 0, VDJCDataUtils.smartCompare(higher, higher));
        Assert.assertEquals(lower + " == " + lower, 0, VDJCDataUtils.smartCompare(lower, lower));
    }
}