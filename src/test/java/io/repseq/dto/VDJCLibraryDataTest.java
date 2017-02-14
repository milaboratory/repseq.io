package io.repseq.dto;

import com.milaboratory.test.TestUtil;
import com.milaboratory.util.GlobalObjectMappers;
import org.junit.Test;

import java.io.InputStream;

public class VDJCLibraryDataTest {
    @Test
    public void test1() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/testdata/example_0.json")) {
            VDJCLibraryData[] lib = GlobalObjectMappers.PRETTY.readValue(stream, VDJCLibraryData[].class);
            TestUtil.assertJson(lib[0]);
        }
    }
}