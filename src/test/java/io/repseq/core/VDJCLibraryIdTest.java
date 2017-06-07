package io.repseq.core;

import com.milaboratory.test.TestUtil;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.Assert.*;

public class VDJCLibraryIdTest {
    @Test
    public void testDecodeEncode1() throws Exception {
        VDJCLibraryId libraryId = new VDJCLibraryId("repseqio.v.1.2", 1234);
        String str = libraryId.toString();
        VDJCLibraryId decoded = VDJCLibraryId.decode(str);
        assertEquals(libraryId, decoded);
    }

    @Test
    public void testDecodeEncode2() throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] checksum = md.digest("ajsdhbsdfkbaskdjfbjhkaw".getBytes());
        VDJCLibraryId libraryId = new VDJCLibraryId("repseqio.v.1.2", 1234, checksum);
        String str = libraryId.toString();
        VDJCLibraryId decoded = VDJCLibraryId.decode(str);
        assertEquals(libraryId, decoded);
    }

    @Test
    public void testDecodeEncode3() throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] checksum = md.digest("ajsdhbsdfkbaskdjfbjhkaw".getBytes());
        VDJCLibraryId libraryId = new VDJCLibraryId("repseqio.v.1.2", 1234, checksum);
        TestUtil.assertJson(libraryId);
    }
}