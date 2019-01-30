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