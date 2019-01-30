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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.milaboratory.core.Range;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.test.TestUtil;
import com.milaboratory.util.GlobalObjectMappers;
import org.junit.Assert;
import org.junit.Test;

public class ReferencePointsTest {
    @Test
    public void test1() throws Exception {
        new ReferencePoints(0, new int[]{1, 3, 5});
        new ReferencePoints(0, new int[]{1, -1, 5, -1, 7});
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2() throws Exception {
        new ReferencePoints(0, new int[]{1, 3, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2e() throws Exception {
        new ExtendedReferencePoints(3, new int[]{1, 3, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void test3() throws Exception {
        new ReferencePoints(0, new int[]{1, -1, 5, -1, 3});
    }

    @Test(expected = IllegalArgumentException.class)
    public void test5() throws Exception {
        new ReferencePoints(0, new int[]{1, -1, 5, -1, -3});
    }

    @Test(expected = IllegalArgumentException.class)
    public void test6() throws Exception {
        new ReferencePoints(0, new int[]{1, -5, 6, 8});
    }

    @Test(expected = IllegalArgumentException.class)
    public void test7() throws Exception {
        new ReferencePoints(0, new int[]{-3, 5, 6, 8});
        new ReferencePoints(0, new int[]{-3, 5, 6, -10});
    }

    @Test
    public void test4() throws Exception {
        ReferencePoints rp = new ReferencePoints(3, new int[]{1, 2, 4, -1, 5, -1, 7});
        Assert.assertEquals(new Range(2, 4), rp.getRange(GeneFeature.FR1));
        Assert.assertNull(rp.getRange(GeneFeature.CDR1));
        Assert.assertNull(rp.getRange(GeneFeature.FR2));
    }

    @Test
    public void test8() throws Exception {
        ReferencePoints points = new ReferencePoints(0, new int[]{2, 52, 63, 84, 155, 455, 645, 1255, 2142});
        GeneFeature feature = GeneFeatureTest.create(2, 4);
        Assert.assertEquals(21, points.getRelativePosition(feature,
                new ReferencePoint(BasicReferencePoint.getByIndex(3))));

        feature = GeneFeatureTest.create(2, 4, 5, 7);
        Assert.assertEquals(155 - 63 + 645 - 455, points.getRelativePosition(feature,
                new ReferencePoint(BasicReferencePoint.getByIndex(6))));
    }

    @Test
    public void test9() throws Exception {
        ReferencePoints points = new ReferencePoints(0, new int[]{2, 52, 63, 84, 155, 455, 645, 1255, 2142});
        GeneFeature feature = GeneFeatureTest.create(2, 5);
        GeneFeature minor = GeneFeatureTest.create(3, 4);
        Assert.assertEquals(new Range(84 - 63, 155 - 63), points.getRelativeRange(feature, minor));

        feature = GeneFeatureTest.create(2, 5, 6, 8);
        minor = GeneFeatureTest.create(6, 8);
        Assert.assertEquals(new Range(455 - 63, 2142 - 645 + 455 - 63), points.getRelativeRange(feature, minor));
    }


    @Test
    public void test10() throws Exception {
        ReferencePoints points = new ReferencePoints(0, new int[]{-1, 4, 15, 20, 27, 29, 48, 71, 83});
        GeneFeature feature = GeneFeatureTest.create(2, 3);
        Assert.assertEquals(
                new ReferencePoints(2, new int[]{0, 5}),
                points.getRelativeReferencePoints(feature));

        feature = GeneFeatureTest.create(2, 4, 6, 8);
        Assert.assertEquals(
                new ReferencePoints(2, new int[]{0, 5, 12, -1, 12, 35, 47}),
                points.getRelativeReferencePoints(feature));
    }

    @Test
    public void test11() throws Exception {
        ReferencePoints points = new ReferencePoints(0, new int[]{2, 52, 63, 84, 155, 455, 645, 1255, 2142, 12342, 24234234, 234423424});
        GeneFeature feature = GeneFeatureTest.create(3, 5, 7, 9);
        Assert.assertEquals(
                new ReferencePoints(0, new int[]{-1, -1, -1, 0, 71, 371, -1, 371, 1258, 11458, -1, -1, -1, -1, -1, -1, -1, -1, -1}),
                points.getRelativeReferencePoints(feature));
    }

    @Test
    public void test12() throws Exception {
        ReferencePoints points = new ReferencePoints(0, new int[]{-1, 96, 85, 80, 73, 71, 52, 29, 17});
        GeneFeature feature = GeneFeatureTest.create(2, 3);
        Assert.assertEquals(
                new ReferencePoints(2, new int[]{0, 5}),
                points.getRelativeReferencePoints(feature));

        feature = GeneFeatureTest.create(2, 4, 6, 8);
        Assert.assertEquals(
                new ReferencePoints(2, new int[]{0, 5, 12, -1, 12, 35, 47}),
                points.getRelativeReferencePoints(feature));
    }

    @Test
    public void test13() throws Exception {
        ReferencePoints points = new ReferencePoints(0, new int[]{-1, 96, 85, 80, 73, 71, 52, 29, 17});
        Mutations<NucleotideSequence> mutations = Mutations.decode("DA7I15C", NucleotideSequence.ALPHABET);
        GeneFeature feature = GeneFeatureTest.create(2, 4, 6, 8);
        Assert.assertEquals(
                new ReferencePoints(2, new int[]{0, 5, 11, -1, 11, 35, 47}),
                points.getRelativeReferencePoints(feature).applyMutations(mutations));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilders1() throws Exception {
        ReferencePointsBuilder builder = new ReferencePointsBuilder();
        builder.setPosition(ReferencePoint.FR3Begin.move(1), 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilders2() throws Exception {
        ReferencePointsBuilder builder = new ReferencePointsBuilder();
        builder.setPosition(ReferencePoint.V5UTRBeginTrimmed, 12);
    }

    @Test
    public void testBuilders3() throws Exception {
        ReferencePointsBuilder builder = new ReferencePointsBuilder();
        builder.setPosition(ReferencePoint.FR3Begin, 12);
        builder.setPosition(ReferencePoint.JBegin, 17);
        ReferencePoints rp = builder.build();
        Assert.assertEquals(12, rp.getPosition(ReferencePoint.FR3Begin));
        Assert.assertEquals(-1, rp.getPosition(ReferencePoint.DBegin));
        Assert.assertEquals(17, rp.getPosition(ReferencePoint.JBegin));
        ExtendedReferencePointsBuilder ebuilder = new ExtendedReferencePointsBuilder();
        ebuilder.setPositionsFrom(rp);
        ebuilder.setPosition(ReferencePoint.V5UTRBeginTrimmed, 11);
        ExtendedReferencePoints rpe = ebuilder.build();
        Assert.assertEquals(12, rpe.getPosition(ReferencePoint.FR3Begin));
        Assert.assertEquals(-1, rpe.getPosition(ReferencePoint.DBegin));
        Assert.assertEquals(17, rpe.getPosition(ReferencePoint.JBegin));
        Assert.assertEquals(11, rpe.getPosition(ReferencePoint.V5UTRBeginTrimmed));
    }

    @Test
    public void testSerialization1() throws Exception {
        ReferencePoints points = new ReferencePoints(0, new int[]{2, 52, 63, 84, 155, 455, 645, 1255, 2142});
        TestUtil.assertJson(points, true);
    }

    @Test(expected = JsonProcessingException.class)
    public void testSerialization2() throws Exception {
        String str = "[{" +
                "\"UTR5Begin\":2,\n" +
                "\"L1Begin\":52,\n" +
                "\"VIntronBegin\":87,\n" +
                "\"L2Begin\":84,\n" + // <-- here
                "\"FR1Begin\":155,\n" +
                "\"CDR1Begin\":455,\n" +
                "\"FR2Begin\":645,\n" +
                "\"CDR2Begin\":1255,\n" +
                "\"FR3Begin\":2142\n" +
                "}]";
        GlobalObjectMappers.ONE_LINE.readValue(str, ReferencePoints[].class);
    }
}