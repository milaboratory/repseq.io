package io.repseq.gen;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceBuilder;
import com.milaboratory.test.TestUtil;
import io.repseq.core.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

public class GGeneTest {
    @Test
    public void serializationDeserializationTest() throws Exception {
        VDJCLibrary library = VDJCLibraryRegistry
                .getDefaultLibrary("hs");
        VDJCGene v = library.getSafe("TRBV12-3*00");
        VDJCGene d = library.getSafe("TRBD1*00");
        VDJCGene j = library.getSafe("TRBJ1-2*00");
        VDJCGene c = library.getSafe("TRBC1*00");

        GGene rWithD = new GGene(null, new VDJCGenes(v, d, j, c),
                new VDJTrimming(-3, 1, new DTrimming(2, 3)),
                new NucleotideSequence("ATAAG"), new NucleotideSequence("GACAT"));

        GGene rWithoutD = new GGene(null, new VDJCGenes(v, null, j, c),
                new VDJTrimming(-3, 1),
                new NucleotideSequence("ATAAG"), null);

        TestUtil.assertJson(rWithD);
        TestUtil.assertJson(rWithoutD);
    }

    @Test
    public void test1() throws Exception {
        VDJCLibrary library = VDJCLibraryRegistry
                .getDefaultLibrary("hs");
        VDJCGene v = library.getSafe("TRBV12-3*00");
        VDJCGene d = library.getSafe("TRBD1*00");
        VDJCGene j = library.getSafe("TRBJ1-2*00");
        VDJCGene c = library.getSafe("TRBC1*00");

        NucleotideSequence s0 = new NucleotideSequence("ATAAG");
        NucleotideSequence s1 = new NucleotideSequence("GACAT");
        GGene rg;

        // Custom 1
        rg = new GGene(null, new VDJCGenes(v, d, j, c),
                new VDJTrimming(-3, 1, 2, 3),
                s0, s1);
        assertSequences(rg.getFeature(GeneFeature.CDR3),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, -3)),
                s0,
                d.getFeature(new GeneFeature(ReferencePoint.DBegin, 2, 0)),
                d.getFeature(new GeneFeature(GeneFeature.DRegion, 0, 0)),
                d.getFeature(new GeneFeature(ReferencePoint.DEnd, 0, -3)),
                s1,
                j.getFeature(new GeneFeature(ReferencePoint.JBegin, 1, 0)),
                j.getFeature(new GeneFeature(GeneFeature.GermlineJCDR3Part, 0, 0)));
        assertSequences(rg.getFeature(GeneFeature.CDR3.append(GeneFeature.FR4).append(GeneFeature.CExon1)),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, -3)),
                s0,
                d.getFeature(new GeneFeature(ReferencePoint.DBegin, 2, 0)),
                d.getFeature(new GeneFeature(GeneFeature.DRegion, 0, 0)),
                d.getFeature(new GeneFeature(ReferencePoint.DEnd, 0, -3)),
                s1,
                j.getFeature(new GeneFeature(ReferencePoint.JBegin, 1, 0)),
                j.getFeature(new GeneFeature(GeneFeature.JRegion, 0, 0)),
                c.getFeature(GeneFeature.CExon1));

        // All P segments 1
        rg = new GGene(null, new VDJCGenes(v, d, j, c),
                new VDJTrimming(3, 1, 2, 3),
                s0, s1);
        assertSequences(rg.getFeature(GeneFeature.CDR3),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, 0)),
                v.getFeature(new GeneFeature(ReferencePoint.VEnd, 0, -3)),
                s0,
                d.getFeature(new GeneFeature(ReferencePoint.DBegin, 2, 0)),
                d.getFeature(new GeneFeature(GeneFeature.DRegion, 0, 0)),
                d.getFeature(new GeneFeature(ReferencePoint.DEnd, 0, -3)),
                s1,
                j.getFeature(new GeneFeature(ReferencePoint.JBegin, 1, 0)),
                j.getFeature(new GeneFeature(GeneFeature.GermlineJCDR3Part, 0, 0)));
        assertSequences(rg.getFeature(GeneFeature.CDR3.append(GeneFeature.FR4).append(GeneFeature.CExon1)),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, 0)),
                v.getFeature(new GeneFeature(ReferencePoint.VEnd, 0, -3)),
                s0,
                d.getFeature(new GeneFeature(ReferencePoint.DBegin, 2, 0)),
                d.getFeature(new GeneFeature(GeneFeature.DRegion, 0, 0)),
                d.getFeature(new GeneFeature(ReferencePoint.DEnd, 0, -3)),
                s1,
                j.getFeature(new GeneFeature(ReferencePoint.JBegin, 1, 0)),
                j.getFeature(new GeneFeature(GeneFeature.JRegion, 0, 0)),
                c.getFeature(GeneFeature.CExon1));

        // No P segments 1
        rg = new GGene(null, new VDJCGenes(v, d, j, c),
                new VDJTrimming(-4, -5, -2, -4),
                s0, s1);
        assertSequences(rg.getFeature(GeneFeature.CDR3),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, -4)),
                s0,
                d.getFeature(new GeneFeature(GeneFeature.DRegion, 2, -4)),
                s1,
                j.getFeature(new GeneFeature(GeneFeature.GermlineJCDR3Part, 5, 0)));
        assertSequences(rg.getFeature(GeneFeature.CDR3.append(GeneFeature.FR4).append(GeneFeature.CExon1)),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, -4)),
                s0,
                d.getFeature(new GeneFeature(GeneFeature.DRegion, 2, -4)),
                s1,
                j.getFeature(new GeneFeature(GeneFeature.JRegion, 5, 0)),
                c.getFeature(GeneFeature.CExon1));

        // No D gene segments 1
        rg = new GGene(null, new VDJCGenes(v, null, j, c),
                new VDJTrimming(-4, -5),
                s0, null);
        assertSequences(rg.getFeature(GeneFeature.CDR3),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, -4)),
                s0,
                j.getFeature(new GeneFeature(GeneFeature.GermlineJCDR3Part, 5, 0)));
        assertSequences(rg.getFeature(GeneFeature.CDR3.append(GeneFeature.FR4).append(GeneFeature.CExon1)),
                v.getFeature(new GeneFeature(GeneFeature.GermlineVCDR3Part, 0, -4)),
                s0,
                j.getFeature(new GeneFeature(GeneFeature.JRegion, 5, 0)),
                c.getFeature(GeneFeature.CExon1));
    }

    void assertSequences(NucleotideSequence expected, NucleotideSequence... toConcat) {
        SequenceBuilder<NucleotideSequence> b = NucleotideSequence.ALPHABET.createBuilder();
        for (NucleotideSequence ns : toConcat)
            b.append(ns);
        assertEquals(expected, b.createAndDestroy());
    }
}