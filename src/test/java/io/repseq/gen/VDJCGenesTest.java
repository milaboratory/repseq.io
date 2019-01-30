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
package io.repseq.gen;

import com.milaboratory.test.TestUtil;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import org.junit.Test;

import static org.junit.Assert.*;

public class VDJCGenesTest {
    @Test
    public void serializationDeserializationTest() throws Exception {
        VDJCLibrary library = VDJCLibraryRegistry
                .getDefaultLibrary("hs");
        VDJCGene v = library.getSafe("TRBV12-3*00");
        VDJCGene d = library.getSafe("TRBD1*00");
        VDJCGene j = library.getSafe("TRBJ1-2*00");
        VDJCGene c = library.getSafe("TRBC1*00");
        TestUtil.assertJson(new VDJCGenes(v, d, j, c));
        TestUtil.assertJson(new VDJCGenes(v, null, j, c));
    }
}