package io.repseq.cli;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainTest {
    @Test
    public void name() throws Exception {
        Main.main(new String[]{"-h"});
    }
}