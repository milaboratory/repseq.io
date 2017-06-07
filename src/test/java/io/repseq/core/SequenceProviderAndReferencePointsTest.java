package io.repseq.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class SequenceProviderAndReferencePointsTest {
    @Test
    public void testReversed() throws Exception {
        VDJCGene gene = VDJCLibraryRegistry.getDefaultLibrary("hs").getSafe("TRBV12-3*00");
        assertEquals(gene.getFeature(GeneFeature.FR3), gene.getSPAndRPs().getFeature(GeneFeature.FR3));
        assertEquals(gene.getFeature(GeneFeature.FR3), gene.getSPAndRPs().reverse().getFeature(GeneFeature.FR3));
    }
}