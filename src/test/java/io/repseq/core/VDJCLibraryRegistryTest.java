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