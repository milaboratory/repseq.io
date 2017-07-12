package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by mikesh on 7/11/17.
 */
public class EmissionProbabilityUtilTest {
    @Test
    public void gemlineFactorTest1() {
        GermlineMatchParameters matchParams = new SimpleGermlineMatchParameters();

        NucleotideSequence query = new NucleotideSequence("ATCAGCCATGCA" + "CGGCGAATTATGC"),
                vRef = new NucleotideSequence("ATCAGCCATGCA"),
                jRef = new NucleotideSequence("CGGCGAATTATGC"),
                jRef2 = new NucleotideSequence("AGGCGAATTATGC");

        double[] resV = EmissionProbabilityUtil.getLogVFactors(matchParams, vRef, query),
                resJ = EmissionProbabilityUtil.getLogJFactors(matchParams, jRef, query),
                resJ2 = EmissionProbabilityUtil.getLogJFactors(matchParams, jRef2, query);

        for (int i = 0; i < resV.length; i++) {
            Assert.assertTrue(resV[i] == 0);
        }

        for (int i = 0; i < resJ.length; i++) {
            Assert.assertTrue(resJ[i] == 0);
        }

        for (int i = 1; i < resJ2.length; i++) {
            Assert.assertTrue(resJ2[i] == 0);
        }

        Assert.assertTrue(resJ2[0] == ProbabilityUtil.LOG_MIN_PROB);
    }

    @Test
    public void gemlineFactorTest2() {
        double errorProb = 1e-3;

        GermlineMatchParameters matchParams = new UniformGermlineMatchParameters(errorProb);

        NucleotideSequence query = new NucleotideSequence("ATGCTACGCATGACTACGACTACGAC"),
                vRef = new NucleotideSequence("ATGCTACGCATGA"),
                jRef = new NucleotideSequence("CTACGACTACGAC"),
                vRef2 = new NucleotideSequence("ATGCTACGCATGC"),
                jRef2 = new NucleotideSequence("ATACGACTACGAC");

        double[] res1 = EmissionProbabilityUtil.getLogVFactors(matchParams, vRef, query),
                res2 = EmissionProbabilityUtil.getLogVFactors(matchParams, vRef2, query),
                res3 = EmissionProbabilityUtil.getLogJFactors(matchParams, jRef, query),
                res4 = EmissionProbabilityUtil.getLogJFactors(matchParams, jRef2, query);

        System.out.println(Arrays.toString(res1));
        System.out.println(Arrays.toString(res2));

        for (int i = 0; i < vRef.size(); i++) {
            Assert.assertEquals((i + 1) * Math.log(1 - errorProb), res1[i], 1e-10);
        }

        for (int i = 0; i < jRef.size(); i++) {
            Assert.assertEquals((i + 1) * Math.log(1 - errorProb), res3[jRef.size() - i - 1], 1e-10);
        }

        Assert.assertEquals(vRef.size(), res1.length);
        Assert.assertEquals(vRef.size(), res2.length);

        Assert.assertEquals(res2[0], res1[0], 1e-10);
        Assert.assertEquals(res2[vRef.size() - 1] - Math.log(errorProb / 3),
                res1[vRef.size() - 1] - Math.log(1 - errorProb), 1e-10);

        System.out.println(Arrays.toString(res3));
        System.out.println(Arrays.toString(res4));

        Assert.assertEquals(jRef.size(), res3.length);
        Assert.assertEquals(jRef.size(), res4.length);

        Assert.assertEquals(res4[jRef.size() - 1], res3[jRef.size() - 1], 1e-10);
        Assert.assertEquals(res4[0] - Math.log(errorProb / 3),
                res3[0] - Math.log(1 - errorProb), 1e-10);

        NucleotideSequence vRef3 = new NucleotideSequence("ATGCTACGCATGACTACGACTACGACAAAAAA"),
                jRef3 = new NucleotideSequence("AAAAAAAATGCTACGCATGACTACGACTACGAC");

        double[] res5 = EmissionProbabilityUtil.getLogVFactors(matchParams, vRef3, query),
                res6 = EmissionProbabilityUtil.getLogJFactors(matchParams, jRef3, query);

        System.out.println(Arrays.toString(res5));
        System.out.println(Arrays.toString(res6));

        Assert.assertEquals(query.size(), res5.length);
        Assert.assertEquals(query.size(), res6.length);
    }

    @Test
    public void insertFactorTest1() {
        InsertionParameters params = new SimpleInsertionParameters(new double[0]);

        NucleotideSequence query = new NucleotideSequence("ATGCTACGCATGACTACGACTACGAC");

        double[][] insertFactors = EmissionProbabilityUtil.getLogInsertFactors(params,
                query);

        for (int i = 0; i < query.size(); i++) {
            for (int j = i + 2; j < query.size(); j++) {
                Assert.assertEquals(Math.log(Math.pow(4, -Math.abs(j - i - 1))), insertFactors[i][j], 1e-10);
            }
        }

        insertFactors = EmissionProbabilityUtil.getLogInsertFactorsRev(params,
                query);

        for (int i = 0; i < query.size(); i++) {
            for (int j = i + 2; j < query.size(); j++) {
                Assert.assertEquals(Math.log(Math.pow(4, -Math.abs(j - i - 1))), insertFactors[i][j], 1e-10);
            }
        }
    }
}
