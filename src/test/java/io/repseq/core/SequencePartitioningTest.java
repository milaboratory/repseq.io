package io.repseq.core;

import com.milaboratory.core.Range;
import org.junit.Assert;
import org.junit.Test;

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
    public void test5() throws Exception {
        ReferencePoints refPoints = new ReferencePoints(
                BasicReferencePoint.DBegin.index, new int[]{0, 100});
        Assert.assertEquals(100, refPoints.getRelativePosition(GeneFeature.DRegionWithP, ReferencePoint.DBegin));
        Assert.assertEquals(200, refPoints.getRelativePosition(GeneFeature.DRegionWithP, ReferencePoint.DEnd));
    }
}