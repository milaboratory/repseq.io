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
}