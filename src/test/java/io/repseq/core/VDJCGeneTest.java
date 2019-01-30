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

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.milaboratory.util.GlobalObjectMappers;
import org.junit.Test;

import static org.junit.Assert.*;

public class VDJCGeneTest {
    @Test
    public void jsonTestCurrentLibrary1() throws Exception {
        VDJCLibrary library = VDJCLibraryRegistry
                .getDefaultLibrary("hs");
        VDJCGene gene = library
                .getSafe("TRBV12-3*00");

        ObjectWriter writer = GlobalObjectMappers.PRETTY
                .writerFor(VDJCGene.class)
                .withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library);

        ObjectReader reader = GlobalObjectMappers.PRETTY
                .readerFor(VDJCGene.class)
                .withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library);

        String str = writer.writeValueAsString(gene);

        assertEquals("\"TRBV12-3*00\"", str);

        Object geneDeserialized = reader.readValue(str);

        assertTrue(geneDeserialized == gene);
    }

    @Test
    public void jsonTestNoCurrentLibrary1() throws Exception {
        VDJCLibrary library = VDJCLibraryRegistry
                .getDefaultLibrary("hs");
        VDJCGene gene = library
                .getSafe("TRBV12-3*00");

        ObjectWriter writer = GlobalObjectMappers.PRETTY
                .writerFor(VDJCGene.class);

        ObjectReader reader = GlobalObjectMappers.PRETTY
                .readerFor(VDJCGene.class);

        String str = writer.writeValueAsString(gene);

        assertTrue(str.endsWith("/TRBV12-3*00\""));

        Object geneDeserialized = reader.readValue(str);

        assertTrue(geneDeserialized == gene);
    }
}