package io.repseq.gen.dist;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleTensorTest {
    @Test
    public void test1() throws Exception {
        DoubleTensor dd = new DoubleTensor(1, 2, 3, 4);
        dd.set(12, 0, 1, 2, 3);
        dd.set(7, 0, 0, 0, 0);
        assertEquals(12.0, dd.get(0, 1, 2, 3), 0.01);
        assertEquals(7.0, dd.get(0, 0, 0, 0), 0.01);
    }

    @Test
    public void testIdx1() throws Exception {
        int[] dims = new int[]{12, 4, 3, 1, 17};
        for (int i = 0; i < 12 * 4 * 3 * 17; i++)
            assertEquals(i, DoubleTensor.idx(dims, DoubleTensor.invIdx(dims, i)));
    }
}