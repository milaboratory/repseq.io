package io.repseq.gen.dist;

import com.milaboratory.test.TestUtil;
import org.junit.Test;

public class CommonNormalGeneTrimmingModelTest {
    @Test
    public void serDeserTest1() throws Exception {
        CommonNormalGeneTrimmingModel m = new CommonNormalGeneTrimmingModel(12, 3, 14);
        TestUtil.assertJson(m);
    }
}