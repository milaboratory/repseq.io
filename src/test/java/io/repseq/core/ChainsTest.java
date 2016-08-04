package io.repseq.core;

import com.milaboratory.test.TestUtil;
import org.junit.Test;

public class ChainsTest {
    @Test
    public void serializationDeserialization1() throws Exception {
        Chains c = new Chains("TRB", "TRA");
        TestUtil.assertJson(c, true);
    }
}