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