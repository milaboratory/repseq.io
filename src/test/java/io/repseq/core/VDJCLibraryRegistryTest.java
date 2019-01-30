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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.util.Set;
import java.util.regex.Pattern;

public class VDJCLibraryRegistryTest {
    @Test
    public void testLibraryName() throws Exception {
        Assert.assertEquals("imgt.201631-4.sv1", VDJCLibraryRegistry.libraryNameFromFileName("imgt.201631-4.sv1.json.gz"));
        Assert.assertEquals("imgt.201631-4.sv1", VDJCLibraryRegistry.libraryNameFromFileName("imgt.201631-4.sv1.json"));
    }

    @Ignore
    @Test
    public void name() throws Exception {
        Reflections reflections = new Reflections("libraries", new ResourcesScanner());
        Set<String> resources = reflections.getResources(Pattern.compile(".*\\.json"));
        System.out.println(resources);
    }
}