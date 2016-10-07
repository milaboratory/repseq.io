package io.repseq.core;

import org.junit.Assert;
import org.junit.Test;

public class VDJCLibraryRegistryTest {
    @Test
    public void testLibraryName() throws Exception {
        Assert.assertEquals("imgt.201631-4.sv1", VDJCLibraryRegistry.libraryNameFromFileName("imgt.201631-4.sv1.json.gz"));
        Assert.assertEquals("imgt.201631-4.sv1", VDJCLibraryRegistry.libraryNameFromFileName("imgt.201631-4.sv1.json"));
    }
}