package io.repseq.core;

import com.milaboratory.test.TestUtil;
import org.junit.Test;

import java.security.MessageDigest;

import static org.junit.Assert.*;

public class VDJCGeneIdTest {
    @Test
    public void encodeDecodeTest1() throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] checksum = md.digest("ajsdhbsdfkbaskdjfbjhkaw".getBytes());
        VDJCLibraryId libraryId = new VDJCLibraryId("repseqio.v.1.2", 1234, checksum);
        VDJCGeneId geneId = new VDJCGeneId(libraryId, "TRBV10-3*00");
        TestUtil.assertJson(geneId);
    }
}