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

import com.milaboratory.util.IntArrayList;
import io.repseq.util.Doc;
import org.apache.commons.math3.random.Well44497a;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static io.repseq.core.GeneFeature.*;
import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GeneFeatureTest {

    @Test
    public void test1() throws Exception {
        GeneFeature f1, f2, f3, expected, actual;

        f1 = create(1, 3);
        f2 = create(4, 5);
        expected = create(new int[]{1, 3, 4, 5});
        actual = new GeneFeature(f1, f2);
        assertEquals(expected, actual);


        f1 = create(1, 5);
        f2 = create(5, 6);
        f3 = create(6, 9);
        expected = create(1, 9);
        actual = new GeneFeature(f1, f2, f3);
        assertEquals(expected, actual);


        f1 = create(1, 5);
        f2 = create(6, 7);
        f3 = create(7, 9);
        expected = create(1, 5, 6, 9);
        actual = new GeneFeature(f1, f2, f3);
        assertEquals(expected, actual);

        f1 = create(1, 5);
        f2 = create(8, 10);
        f3 = create(11, 12);
        expected = create(1, 5, 8, 10, 11, 12);
        actual = new GeneFeature(f1, f2, f3);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2() throws Exception {
        GeneFeature f1 = create(1, 5),
                f2 = create(3, 7);
        new GeneFeature(f1, f2);
    }

    @Test
    public void test3() throws Exception {
        GeneFeature f1, f2, f3, expected, actual;

        f1 = createWithOffsets(1, 3, -2, 0);
        f2 = createWithOffsets(3, 5, 1, 1);
        f3 = createWithOffsets(5, 7, 1, 5);
        expected = create(new int[]{1, 3, 3, 7}, new int[]{-2, 0, 1, 5});
        actual = new GeneFeature(f2, f1, f3);
        assertEquals(expected, actual);


        f1 = createWithOffsets(1, 3, -2, 0);
        f2 = createWithOffsets(3, 5, 1, -1);
        f3 = createWithOffsets(5, 7, -1, 5);
        expected = create(new int[]{1, 3, 3, 7}, new int[]{-2, 0, 1, 5});
        actual = new GeneFeature(f2, f1, f3);
        assertEquals(expected, actual);


        f1 = createWithOffsets(1, 3, -2, 0);
        f2 = createWithOffsets(3, 5, 1, -3);
        f3 = createWithOffsets(5, 7, -2, 5);
        expected = create(new int[]{1, 3, 3, 5, 5, 7}, new int[]{-2, 0, 1, -3, -2, 5});
        actual = new GeneFeature(f2, f1, f3);
        assertEquals(expected, actual);
        assertEquals(3, actual.regions.length);
    }

    @Test
    public void test3_1() throws Exception {
        GeneFeature f1 = createWithOffsets(1, 3, -2, 0);
        assertEquals(createWithOffsets(1, 3, -3, 2), new GeneFeature(f1, -1, 2));
    }

    @Test
    public void test3_2() throws Exception {
        GeneFeature f1 = new GeneFeature(
                createWithOffsets(1, 3, -2, 0),
                createWithOffsets(4, 5, -2, 4)
        );

        GeneFeature f2 = new GeneFeature(
                createWithOffsets(1, 3, -6, 0),
                createWithOffsets(4, 5, -2, 2)
        );

        assertEquals(f2, new GeneFeature(f1, -4, -2));
    }

    @Test
    public void testReversed() throws Exception {
        GeneFeature gf = GeneFeature.parse("{FR1Begin:VEnd}+{VEnd:VEnd(-20)}");
        assertEquals(2, gf.size());
        gf = GeneFeature.parse("{FR1Begin:VEnd}").append(GeneFeature.parse("{VEnd:VEnd(-20)}"));
        assertEquals(2, gf.size());
        gf = GeneFeature.VGeneWithP;
        assertEquals(2, gf.size());
    }

    @Test
    public void testIntersection1() throws Exception {
        GeneFeature f1, f2;
        f1 = create(1, 5, 7, 9);
        f2 = create(8, 9);
        Assert.assertEquals(create(8, 9), GeneFeature.intersection(f1, f2));

        f1 = create(1, 5, 7, 10);
        f2 = create(8, 9);
        Assert.assertEquals(create(8, 9), GeneFeature.intersection(f1, f2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntersection2() throws Exception {
        GeneFeature f1, f2;
        f1 = create(1, 5, 7, 9);
        f2 = create(6, 9);
        GeneFeature.intersection(f1, f2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntersection3() throws Exception {
        GeneFeature f1, f2;
        f1 = create(1, 5, 7, 9);
        f2 = create(2, 5, 6, 9);
        GeneFeature.intersection(f1, f2);
    }

    @Test
    public void testIntersection5() throws Exception {
        GeneFeature f1, f2;
        f1 = create(1, 5, 7, 9, 10, 12);
        f2 = create(2, 5, 7, 9, 10, 11);
        Assert.assertEquals(create(2, 5, 7, 9, 10, 11), GeneFeature.intersection(f1, f2));
        Assert.assertEquals(create(2, 5, 7, 9, 10, 11), GeneFeature.intersection(f2, f1));

        f1 = create(2, 5, 7, 9, 10, 12);
        f2 = create(1, 5, 7, 9, 10, 11);
        Assert.assertEquals(create(2, 5, 7, 9, 10, 11), GeneFeature.intersection(f1, f2));
        Assert.assertEquals(create(2, 5, 7, 9, 10, 11), GeneFeature.intersection(f2, f1));

        f1 = create(8, 9, 10, 11);
        f2 = create(1, 5, 7, 9, 10, 12);
        Assert.assertEquals(create(8, 9, 10, 11), GeneFeature.intersection(f1, f2));
        Assert.assertEquals(create(8, 9, 10, 11), GeneFeature.intersection(f2, f1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntersection6() throws Exception {
        GeneFeature f1, f2;
        f1 = create(6, 9, 10, 11);
        f2 = create(1, 5, 7, 9, 10, 12);
        GeneFeature.intersection(f1, f2);
    }

    @Test
    public void testIntersection7() throws Exception {
        GeneFeature f1, f2;
        f1 = create(new int[]{1, 5, 7, 9, 10, 12}, new int[]{-2, 0, 1, -3, -2, 5});
        f2 = create(new int[]{1, 5, 7, 9, 10, 12}, new int[]{-3, 0, 1, -3, -2, 4});
        Assert.assertEquals(create(new int[]{1, 5, 7, 9, 10, 12}, new int[]{-2, 0, 1, -3, -2, 4}),
                GeneFeature.intersection(f2, f1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntersection8() throws Exception {
        GeneFeature f1, f2;
        f1 = create(new int[]{1, 5, 7, 9, 10, 12}, new int[]{-2, 0, 1, -3, -2, 5});
        f2 = create(new int[]{1, 5, 7, 9, 10, 12}, new int[]{-3, 0, 2, -3, -2, 4});
        GeneFeature.intersection(f2, f1);
    }

    @Test
    public void testIntersection9() throws Exception {
        GeneFeature f1, f2;
        f1 = create(new int[]{7, 9}, new int[]{2, -4});
        f2 = create(new int[]{1, 5, 7, 9, 10, 12}, new int[]{-3, 0, 1, -4, -2, 4});
        Assert.assertEquals(create(new int[]{7, 9}, new int[]{2, -4}),
                GeneFeature.intersection(f2, f1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntersection10() throws Exception {
        GeneFeature f1, f2;
        f1 = create(new int[]{7, 9}, new int[]{0, -3});
        f2 = create(new int[]{1, 5, 7, 9, 10, 12}, new int[]{-3, 0, 1, -3, -2, 4});
        GeneFeature.intersection(f2, f1);
    }

    @Test
    public void testIntersection11() throws Exception {
        GeneFeature f1, f2;
        f1 = create(7, 9);
        f2 = create(1, 5, 7, 9, 10, 12);
        Assert.assertEquals(create(7, 9),
                GeneFeature.intersection(f2, f1));
    }

    @Test
    public void testIntersection12() throws Exception {
        assertNull(GeneFeature.intersection(GeneFeature.CDR3, GeneFeature.CExon1));
        assertNull(GeneFeature.intersection(GeneFeature.CExon1, GeneFeature.CDR3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test4() throws Exception {
        new GeneFeature(create(1, 5), create(1, 7));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test5() throws Exception {
        new GeneFeature(create(1, 7), create(1, 5));
    }

    @Test
    public void testFrameAnchor() throws Exception {
        //Assert.assertEquals(CDR1Begin, GeneFeature.getFrameReference(CDR1));
        //Assert.assertEquals(CDR2Begin, GeneFeature.getFrameReference(CDR2));
        //Assert.assertEquals(CDR3End, GeneFeature.getFrameReference(JRegion));
        Assert.assertEquals(ReferencePoint.L1Begin, GeneFeature.getFrameReference(VTranscriptWithout5UTR));
    }

    @Test
    public void testRandom1() {
        Well44497a rand = new Well44497a();
        int tn = BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS;
        for (int baseBlock = 2; baseBlock < 5; ++baseBlock)
            for (int t = 0; t < 1000; ++t) {
                int[] all = new int[tn];
                ArrayList<GeneFeature> features = new ArrayList<>();
                int begin, end = 0;
                do {
                    begin = end + rand.nextInt(baseBlock);
                    end = begin + 1 + rand.nextInt(baseBlock);
                    if (end >= tn)
                        break;
                    Arrays.fill(all, begin, end, 1);
                    features.add(create(begin, end));
                } while (end < tn);

                IntArrayList expectedPoints = new IntArrayList();
                if (all[0] == 1)
                    expectedPoints.add(0);
                for (int i = 1; i < tn; ++i)
                    if (all[i] != all[i - 1])
                        expectedPoints.add(i);
                if (all[tn - 1] == 1)
                    expectedPoints.add(tn);

                GeneFeature actual = new GeneFeature(features.toArray(new GeneFeature[features.size()]));
                assertEquals(create(expectedPoints.toArray()), actual);
                assertEquals(expectedPoints.size() / 2, actual.regions.length);
            }
    }

    @Test
    public void testCodingSubfeature() throws Exception {
        Assert.assertEquals(VTranscriptWithout5UTR, getCodingGeneFeature(VGene));
        Assert.assertEquals(VTranscriptWithout5UTR, getCodingGeneFeature(VTranscriptWithout5UTR));
        Assert.assertEquals(VDJTranscriptWithout5UTR, getCodingGeneFeature(VDJTranscript));

        Assert.assertEquals(GeneFeature.parse("{DBegin(-20):FR4End}"),
                getCodingGeneFeature(GeneFeature.parse("{DBegin(-20):FR4End(20)}")));
        Assert.assertEquals(GeneFeature.parse("{DBegin(1):FR4End}"),
                getCodingGeneFeature(GeneFeature.parse("{DBegin(1):FR4End(20)}")));

        Assert.assertNull(getCodingGeneFeature(VIntron));
    }

    @Test
    public void testStatic() throws Exception {
        Assert.assertEquals(GeneFeature.JRegion, GeneFeature.parse("JRegion"));
    }

    static final GeneFeature create(int... indexes) {
        assert indexes.length % 2 == 0;
        GeneFeature[] res = new GeneFeature[indexes.length / 2];
        for (int i = 0; i < indexes.length; ) {
            res[i / 2] = new GeneFeature(
                    new ReferencePoint(BasicReferencePoint.getByIndex(indexes[i])),
                    new ReferencePoint(BasicReferencePoint.getByIndex(indexes[i + 1])));
            i += 2;
        }
        return new GeneFeature(res);
    }

    static final GeneFeature createWithOffsets(int index1, int index2, int offset1, int offset2) {
        return create(new int[]{index1, index2}, new int[]{offset1, offset2});
    }

    static final GeneFeature create(int[] indexes, int[] offsets) {
        GeneFeature[] res = new GeneFeature[indexes.length / 2];
        for (int i = 0; i < indexes.length; ) {
            res[i / 2] = new GeneFeature(
                    new ReferencePoint(
                            new ReferencePoint(BasicReferencePoint.getByIndex(indexes[i])), offsets[i]),
                    new ReferencePoint(
                            new ReferencePoint(BasicReferencePoint.getByIndex(indexes[i + 1])), offsets[i + 1]));
            i += 2;
        }
        return new GeneFeature(res);
    }

    @Test
    public void testParse1() throws Exception {
        assertEncode("CDR3");
        assertEncode("CDR3(1, -2)");
        assertEncode("CDR3(-31,-2)");
        assertEncode("CDR1(3, 2)+CDR3(-31,-2)");
    }

    @Test
    public void testParse2() throws Exception {
        assertEncode("{FR1Begin:FR3End}");
        assertEncode("{FR1Begin:FR3End}+JRegion");
        assertEncode("{FR1Begin:FR3End}+JRegion+CExon1(-3,12)");
        assertEncode("{FR1Begin(-33):FR3End(3)}+JRegion+CExon1(-3,12)");
    }

    @Test
    public void testParse3() throws Exception {
        assertEncode("CDR3Begin(0, 10)");
        assertEncode("V5UTRBeginTrimmed(0, 10)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse4() throws Exception {
        GeneFeature.parse("CDR3Begin");
    }

    @Test
    public void testContains1() throws Exception {
        assertTrue(GeneFeature.CDR3.contains(GeneFeature.VJJunction));
        assertTrue(GeneFeature.CDR3.contains(GeneFeature.VDJunction));
        assertTrue(GeneFeature.CDR3.contains(GeneFeature.DJJunction));
        assertTrue(GeneFeature.CDR3.contains(GeneFeature.CDR3));
        assertFalse(GeneFeature.CDR3.contains(GeneFeature.FR2));
        assertFalse(GeneFeature.CDR3.contains(GeneFeature.CDR1));
        assertFalse(GeneFeature.CDR3.contains(GeneFeature.V5UTRGermline));
    }

    @Test
    public void testReverse1() throws Exception {
        Assert.assertEquals(new GeneFeature(ReferencePoint.DEnd, ReferencePoint.DBegin), GeneFeature.DRegion.reverse());
    }

    @Test
    public void testIntersection13() throws Exception {
        Assert.assertEquals(GeneFeature.VRegionWithP, GeneFeature.intersection(GeneFeature.VRegionWithP, GeneFeature.VRegionWithP));
        Assert.assertEquals(GeneFeature.DRegionWithP, GeneFeature.intersection(GeneFeature.DRegionWithP, GeneFeature.DRegionWithP));
    }

    @Test
    public void testIntersection15() throws Exception {
        Assert.assertEquals(GermlineVCDR3Part.append(GermlineVPSegment),
                GeneFeature.intersection(GeneFeature.VRegionWithP, CDR3));
    }

    @Test
    public void testIntersection14() throws Exception {
        GeneFeature aa1 = GeneFeature.VRegion.append(new GeneFeature(ReferencePoint.VEnd, ReferencePoint.VEnd.move(-20)));
        GeneFeature aa2 = GeneFeature.VRegion.append(new GeneFeature(ReferencePoint.VEnd, ReferencePoint.VEnd.move(-15)));
        Assert.assertEquals(aa2, GeneFeature.intersection(aa1, aa2));
        GeneFeature dd1 = new GeneFeature(ReferencePoint.DEnd.move(-3), ReferencePoint.DBegin).append(GeneFeature.DRegion).append(GeneFeature.GermlineDPSegment);
        GeneFeature dd2 = GeneFeature.GermlineDPSegment.append(GeneFeature.DRegion).append(new GeneFeature(ReferencePoint.DEnd, ReferencePoint.DBegin.move(3)));
        GeneFeature dd3 = new GeneFeature(ReferencePoint.DEnd.move(-3), ReferencePoint.DBegin).append(GeneFeature.DRegion).append(new GeneFeature(ReferencePoint.DEnd, ReferencePoint.DBegin.move(3)));
        Assert.assertEquals(dd3, GeneFeature.intersection(dd1, dd2));
    }

    @Test
    public void testIntersection16() throws Exception {
        Assert.assertEquals(GeneFeature.DRegion,
                GeneFeature.intersectionStrict(GeneFeature.DRegionWithP, GeneFeature.DRegion));
        Assert.assertEquals(GeneFeature.DRegionWithP,
                GeneFeature.intersection(GeneFeature.DRegionWithP, GeneFeature.DRegion));
    }

    @Test
    public void testIntersectionTrimmed1() throws Exception {
        assertTrue(GeneFeature.VRegion.contains(GeneFeature.VCDR3Part));
    }

    @Test
    public void testIntersectionTrimmed2() throws Exception {
        ExtendedReferencePointsBuilder builder = new ExtendedReferencePointsBuilder();
        builder.setPosition(ReferencePoint.FR3Begin, 10);
        builder.setPosition(ReferencePoint.CDR3Begin, 22);
        builder.setPosition(ReferencePoint.VEndTrimmed, 27);
        builder.setPosition(ReferencePoint.JBeginTrimmed, 33);
        builder.setPosition(ReferencePoint.CDR3End, 52);
        builder.setPosition(ReferencePoint.FR4End, 64);
        ExtendedReferencePoints points = builder.build();

    }

    @Test
    public void testCoding1() throws Exception {
        GeneFeature input = GeneFeature.parse("{CDR3Begin(-10):CDR3Begin(-1)}");
        GeneFeature cf = GeneFeature.getCodingGeneFeature(input);
        assertEquals(cf, input);
    }

    @Test
    public void testEncode1() throws Exception {
        Collection<GeneFeature> features = GeneFeature.getFeaturesByName().values();
        for (GeneFeature feature : features)
            assertEquals(feature, GeneFeature.parse(GeneFeature.encode(feature)));
    }

    private static void assertEncode(String str) {
        Assert.assertEquals(str.replace(" ", ""), GeneFeature.encode(GeneFeature.parse(str)).replace(" ", ""));
    }

    @Ignore
    @Test
    public void testListForDocumentation() throws Exception {
        GeneFeature.getFeatureByName("sd");
        List<GFT> gfts = new ArrayList<>();
        Field[] declaredFields = GeneFeature.class.getDeclaredFields();
        for (Field field : declaredFields)
            if (Modifier.isStatic(field.getModifiers()) &&
                    field.getType() == GeneFeature.class) {
                GeneFeature value = (GeneFeature) field.get(null);
                String name = field.getName();
                gfts.add(new GFT(value, name, field.getAnnotation(Doc.class).value()));
            }

        Collections.sort(gfts);
        int widthName = 0, widthValue = 0, widthDoc = 0;
        for (GFT gft : gfts) {
            widthName = Math.max(widthName, gft.name.length());
            widthValue = Math.max(widthValue, gft.value.length());
            widthDoc = Math.max(widthDoc, gft.doc.length());
        }

        String sepHeader = "+" + chars(widthName + 2, '=') + "+" + chars(widthValue + 2, '=') +
                "+" + chars(widthDoc + 2, '=') + "+";
        String sep = "+" + chars(widthName + 2, '-') + "+" + chars(widthValue + 2, '-') +
                "+" + chars(widthDoc + 2, '-') + "+";

        System.out.println(sep);
        System.out.println("| " + fixed("Gene Feature Name", widthName) + " | " +
                fixed("Gene feature decomposition", widthValue) + " | " + fixed("Documentation", widthDoc) + " |");
        System.out.println(sepHeader);
        for (GFT gft : gfts) {
            System.out.println("| " + fixed(gft.name, widthName) + " | " +
                    fixed(gft.value, widthValue) + " | " + fixed(gft.doc, widthDoc) + " |");
            System.out.println(sep);
        }
    }

    private static String fixed(String str, int length) {
        return str + chars(length - str.length(), ' ');
    }

    private static String chars(int n, char cc) {
        char[] c = new char[n];
        Arrays.fill(c, cc);
        return String.valueOf(c);
    }

    private static final class GFT implements Comparable<GFT> {
        final GeneFeature feature;
        final String name;
        final String value;
        final String doc;

        public GFT(GeneFeature feature, String name, String doc) {
            this.feature = feature;
            this.name = "``" + name + "``";
            this.value = "``" + GeneFeature.encode(feature, false).replace("+", "`` + ``") + "``";
            this.doc = doc;
        }

        @Override
        public int compareTo(GFT o) {
            return feature.getFirstPoint().compareTo(o.feature.getFirstPoint());
        }
    }
}