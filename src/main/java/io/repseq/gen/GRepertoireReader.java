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

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPortCloseable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryId;
import io.repseq.core.VDJCLibraryRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GRepertoireReader implements OutputPortCloseable<GClone> {
    final VDJCLibrary library;
    final BufferedReader reader;
    final ObjectReader objectReader;

    public GRepertoireReader(BufferedReader reader) throws IOException {
        this.reader = reader;
        String libraryIdStr = reader.readLine();
        VDJCLibraryId libraryId = GlobalObjectMappers.ONE_LINE.readValue(libraryIdStr, VDJCLibraryId.class);
        this.library = VDJCLibraryRegistry.getDefault().getLibrary(libraryId);
        this.objectReader = GlobalObjectMappers.ONE_LINE.readerFor(new TypeReference<GClone>() {
        }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, this.library);
    }

    public VDJCLibrary getLibrary() {
        return library;
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized GClone take() {
        try {
            String line = reader.readLine();
            if (line == null)
                return null;
            else if (line.startsWith("#"))
                // Skipping comment lines
                return take(); // tailrec
            else if (line.trim().length() == 0)
                // Skipping empty lines
                return take(); // tailrec
            else
                return objectReader.readValue(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GRepertoire readeFully() {
        List<GClone> clones = new ArrayList<>();
        for (GClone clone : CUtils.it(this))
            clones.add(clone);
        return new GRepertoire(clones);
    }
}
