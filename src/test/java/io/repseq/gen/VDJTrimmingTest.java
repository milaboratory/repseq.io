package io.repseq.gen;

import com.milaboratory.test.TestUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class VDJTrimmingTest {
    @Test
    public void serializationDeserializationTest1() throws Exception {
        TestUtil.assertJson(new VDJTrimming(1, 2));
        TestUtil.assertJson(new VDJTrimming(1, 2, 1, 3));
    }
}