package io.repseq.reference;

import com.milaboratory.core.Range;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

public class SequenceRefTest {
    @Test
    public void test1() throws Exception {
        SequenceRef ref = new SequenceRef("file://asdasd.fastq");
        TestUtil.assertJson(ref);

        ref = new SequenceRef("file://asdasd.fastq", new Range[]{new Range(1, 2)}, Mutations.decodeNuc("SA0T"));
        TestUtil.assertJson(ref);

        ref = new SequenceRef("file://asdasd.fastq", null, Mutations.decodeNuc("SA0T"));
        TestUtil.assertJson(ref);

        ref = new SequenceRef("file://asdasd.fastq", null, Mutations.EMPTY_NUCLEOTIDE_MUTATIONS);
        TestUtil.assertJson(ref);
    }
}