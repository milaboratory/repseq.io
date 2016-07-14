package io.repseq.seqbase;

import com.milaboratory.core.Range;
import com.milaboratory.core.io.sequence.fastq.SingleFastqReaderTest;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.TempFileManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

public class SequenceResolverTest {
    @Test
    public void test1() throws Exception {
        SequenceResolvers.initDefaultResolver(TempFileManager.getTempDir().toPath());
        SequenceResolver defaultResolver = SequenceResolvers.getDefault();

        NucleotideSequence seq = defaultResolver.resolve(new SequenceAddress("nuccore://EU877942.1"))
                .getRegion(new Range(10, 30));
        Assert.assertEquals(new NucleotideSequence("gcgagagcaagcactatggc"), seq);

        Path rootPath = new File(SingleFastqReaderTest.class.getClassLoader().getResource("sequences/some_fasta.fasta")
                .toURI()).toPath().toAbsolutePath().normalize().getParent();

        seq = defaultResolver.resolve(new SequenceAddress(rootPath, "file://some_fasta.fasta#24.6jsd21.Tut")).getRegion(new Range(10, 30));
        Assert.assertEquals(new NucleotideSequence("ATCCTGGCTTAGAACTAACG"), seq);
    }
}