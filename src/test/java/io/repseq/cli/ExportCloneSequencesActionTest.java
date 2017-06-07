package io.repseq.cli;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExportCloneSequencesActionTest {
    @Test
    public void randomRoundTest() throws Exception {
        double value = 12.34;
        double sum = 0.0;
        RandomGenerator random = new Well19937c(1232434);
        for (int i = 0; i < 100000; i++)
            sum += ExportCloneSequencesAction.randomizedRound(value, random);
        assertEquals(value, sum / 100000, 0.1);
    }
}