package io.repseq.dto;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

import java.net.URI;

public class KnownSequenceFragmentDataTest {
    @Test
    public void test1() throws Exception {
        KnownSequenceFragmentData data = new KnownSequenceFragmentData(
                URI.create("file://some_fasta.fasta#24.6jsd21.Tut"),
                new Range(10, 30),
                new NucleotideSequence("ATCCTGGCTTAGAACTAACG"));
        TestUtil.assertJson(data);
    }
}