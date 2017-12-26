package io.repseq.core;

import com.milaboratory.core.Range;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.repseq.core.GeneFeature.CDR2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by poslavsky on 12/01/2017.
 */
public class SequencePartitioningTest {
    @Test
    public void test1() throws Exception {
        ReferencePoints refPoints = new ReferencePoints(BasicReferencePoint.JBegin.index, new int[]{1, 100, 200});
        Range r = refPoints.getRelativeRange(GeneFeature.JRegionWithP, GeneFeature.JRegion);
        Assert.assertEquals(new Range(20, 219), r);
        r = refPoints.getRelativeRange(GeneFeature.JRegionWithP, GeneFeature.FR4);
        Assert.assertEquals(new Range(119, 219), r);
        r = refPoints.getRelativeRange(GeneFeature.JRegionWithP, GeneFeature.GermlineJPSegment);
        Assert.assertEquals(new Range(0, 20), r);
    }

    @Test
    public void test2() throws Exception {
        ReferencePoints refPoints = new ReferencePoints(BasicReferencePoint.JBegin.index, new int[]{200, 100, 1});
        Range r = refPoints.getRelativeRange(GeneFeature.JRegion, GeneFeature.FR4);
        Assert.assertEquals(new Range(100, 199), r);
    }

    @Test
    public void test3() throws Exception {
        ReferencePoints refPoints = new ReferencePoints(BasicReferencePoint.JBegin.index, new int[]{199, 100, 0});
        Range r = refPoints.getRelativeRange(GeneFeature.JRegionWithP, GeneFeature.JRegion);
        Assert.assertEquals(new Range(20, 219), r);
        r = refPoints.getRelativeRange(GeneFeature.JRegionWithP, GeneFeature.FR4);
        Assert.assertEquals(new Range(119, 219), r);
        r = refPoints.getRelativeRange(GeneFeature.JRegionWithP, GeneFeature.GermlineJPSegment);
        Assert.assertEquals(new Range(0, 20), r);
    }

    @Test
    public void test4() throws Exception {
        ReferencePoints refPoints = new ReferencePoints(
                BasicReferencePoint.V5UTRBegin.index,
                new int[]{0, 100, 250, 450, 700, 1000, 1400, 1450, 1525, 1700, 1930});
        Range r = refPoints.getRelativeRange(GeneFeature.VGene, GeneFeature.VGeneWithP);
        assertNull(r);

        r = refPoints.getRelativeRange(GeneFeature.VGeneWithP, GeneFeature.VGene);
        assertEquals(new Range(0, 1930), r);

        r = refPoints.getRelativeRange(GeneFeature.VGeneWithP, GeneFeature.GermlineVPSegment);
        assertEquals(new Range(1930, 1950), r);
    }

    @Test
    public void testTouchingGeneFeatures() throws Exception {
        ReferencePoints points2 = new ReferencePoints(0, new int[]{2, 52, 63, 84, 155, 455, 645, 1255, 1255});
        Assert.assertNotNull(points2.getRelativeRange(new GeneFeature(ReferencePoint.FR1Begin, ReferencePoint.FR3Begin), CDR2));
        ReferencePoints points1 = new ReferencePoints(0, new int[]{2142, 1255, 645, 455, 155, 84, 63, 52, 52});
        Assert.assertNotNull(points1.getRelativeRange(new GeneFeature(ReferencePoint.FR1Begin, ReferencePoint.FR3Begin), CDR2));
    }

    @Test
    public void test5() throws Exception {
        ReferencePoints refPoints = new ReferencePoints(
                BasicReferencePoint.DBegin.index, new int[]{0, 100});
        Assert.assertEquals(100, refPoints.getRelativePosition(GeneFeature.DRegionWithP, ReferencePoint.DBegin));
        Assert.assertEquals(200, refPoints.getRelativePosition(GeneFeature.DRegionWithP, ReferencePoint.DEnd));
    }

    @Test
    public void testTranslationRules1() throws Exception {
        ExtendedReferencePointsBuilder builder = new ExtendedReferencePointsBuilder();
        builder.setPosition(ReferencePoint.UTR5Begin, 10);
        builder.setPosition(ReferencePoint.L1Begin, 20);
        builder.setPosition(ReferencePoint.L1End, 30);
        builder.setPosition(ReferencePoint.L2Begin, 40);
        builder.setPosition(ReferencePoint.FR1Begin, 51);
        builder.setPosition(ReferencePoint.CDR1Begin, 60);
        builder.setPosition(ReferencePoint.FR2Begin, 72);
        builder.setPosition(ReferencePoint.CDR2Begin, 78);
        builder.setPosition(ReferencePoint.FR3Begin, 81);
        builder.setPosition(ReferencePoint.CDR3Begin, 90);
        builder.setPosition(ReferencePoint.VEndTrimmed, 94);
        builder.setPosition(ReferencePoint.DBeginTrimmed, 95);
        builder.setPosition(ReferencePoint.DEndTrimmed, 97);
        builder.setPosition(ReferencePoint.JBeginTrimmed, 98);
        builder.setPosition(ReferencePoint.JBegin, 100);
        builder.setPosition(ReferencePoint.CDR3End, 102);
        builder.setPosition(ReferencePoint.FR4End, 112);
        builder.setPosition(ReferencePoint.CBegin, 112);
        ExtendedReferencePoints points = builder.build();

        List<RangeTranslationParameters> translationParameters = points.getTranslationParameters(120);
        List<RangeTranslationParameters> expected = new ArrayList<>();
        expected.add(new RangeTranslationParameters(ReferencePoint.L1Begin, ReferencePoint.L1End,
                new Range(20, 30), new Range(40, 42)));
        expected.add(new RangeTranslationParameters(ReferencePoint.L2Begin, ReferencePoint.CDR3End,
                new Range(40, 102), new Range(29, 30)));
        expected.add(new RangeTranslationParameters(ReferencePoint.CDR3End, null,
                new Range(102, 120)));

        // for (RangeTranslationParameters tp : translationParameters)
        //     System.out.println(tp);

        assertEquals(expected, translationParameters);
    }

    @Test
    public void testTranslationRules2() throws Exception {
        ExtendedReferencePointsBuilder builder = new ExtendedReferencePointsBuilder();
        builder.setPosition(ReferencePoint.UTR5Begin, 20);
        builder.setPosition(ReferencePoint.L1Begin, 30);
        builder.setPosition(ReferencePoint.L1End, 40);
        builder.setPosition(ReferencePoint.L2Begin, 40);
        builder.setPosition(ReferencePoint.FR1Begin, 51);
        builder.setPosition(ReferencePoint.CDR1Begin, 60);
        builder.setPosition(ReferencePoint.FR2Begin, 72);
        builder.setPosition(ReferencePoint.CDR2Begin, 78);
        builder.setPosition(ReferencePoint.FR3Begin, 81);
        builder.setPosition(ReferencePoint.CDR3Begin, 90);
        builder.setPosition(ReferencePoint.VEndTrimmed, 94);
        builder.setPosition(ReferencePoint.DBeginTrimmed, 95);
        builder.setPosition(ReferencePoint.DEndTrimmed, 97);
        builder.setPosition(ReferencePoint.JBeginTrimmed, 98);
        builder.setPosition(ReferencePoint.JBegin, 100);
        builder.setPosition(ReferencePoint.CDR3End, 102);
        builder.setPosition(ReferencePoint.FR4End, 112);
        builder.setPosition(ReferencePoint.CBegin, 112);
        ExtendedReferencePoints points = builder.build();

        List<RangeTranslationParameters> translationParameters = points.getTranslationParameters(120);
        List<RangeTranslationParameters> expected = new ArrayList<>();
        expected.add(new RangeTranslationParameters(ReferencePoint.L1Begin, null,
                new Range(30, 120)));

        // for (RangeTranslationParameters tp : translationParameters)
        //     System.out.println(tp);

        assertEquals(expected, translationParameters);
    }
}