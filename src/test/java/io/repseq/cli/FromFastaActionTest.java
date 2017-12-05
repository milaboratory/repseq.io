package io.repseq.cli;

import org.junit.Test;

import java.util.regex.Pattern;

public class FromFastaActionTest {
    @Test
    public void regexpTest() throws Exception {
        Pattern p = Pattern.compile("[\\(\\[]?[Ff].?");
        System.out.println(p.matcher("F").matches());
    }
}