package io.repseq.seqbase;

import com.milaboratory.core.Range;
import com.milaboratory.core.io.sequence.fastq.SingleFastqReaderTest;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.TempFileManager;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;

public class SequenceResolverTest {
    @Test
    public void test1() throws Exception {
        Path dir = TempFileManager.getTempDir().toPath().toAbsolutePath();

        Path work = dir.resolve("work");
        Files.createDirectories(work);

        Path cache = dir.resolve("cache");
        Files.createDirectories(cache);

        SequenceResolvers.initDefaultResolver(cache);
        SequenceResolver defaultResolver = SequenceResolvers.getDefault();

        NucleotideSequence seq = defaultResolver.resolve(new SequenceAddress("nuccore://EU877942.1"))
                .getRegion(new Range(10, 30).inverse());
        Assert.assertEquals(new NucleotideSequence("gcgagagcaagcactatggc").getReverseComplement(), seq);

        Path someFasta = work.resolve("some_fasta.fasta");
        FileUtils.copyToFile(
                SingleFastqReaderTest.class.getClassLoader().getResourceAsStream("sequences/some_fasta.fasta"),
                someFasta.toFile());

        //Path rootPath = new File(SingleFastqReaderTest.class.getClassLoader().getResource("sequences/some_fasta.fasta")
        //        .toURI()).toPath().toAbsolutePath().normalize().getParent();

        seq = defaultResolver.resolve(
                new SequenceAddress(work,
                        "file://some_fasta.fasta#24.6jsd21.Tut"))
                .getRegion(new Range(10, 30));

        Assert.assertEquals(new NucleotideSequence("ATCCTGGCTTAGAACTAACG"), seq);
    }

    @Test
    public void rawHttpTest1() throws Exception {
        Path dir = TempFileManager.getTempDir().toPath().toAbsolutePath();

        Path work = dir.resolve("work");
        Files.createDirectories(work);

        Path cache = dir.resolve("cache");
        Files.createDirectories(cache);

        SequenceResolvers.initDefaultResolver(cache);
        SequenceResolver defaultResolver = SequenceResolvers.getDefault();

        NucleotideSequence seq = defaultResolver.resolve(new SequenceAddress(
                "http://files.milaboratory.com/test-data/test.fa#testRecord"))
                .getRegion(new Range(10, 30).inverse());
        Assert.assertEquals(new NucleotideSequence("GCTCCACCACAAGACACTCT"), seq);
    }

    @Test
    public void rawHttpTest2() throws Exception {
        Path dir = TempFileManager.getTempDir().toPath().toAbsolutePath();

        Path work = dir.resolve("work");
        Files.createDirectories(work);

        Path cache = dir.resolve("cache");
        Files.createDirectories(cache);

        SequenceResolvers.initDefaultResolver(cache);
        SequenceResolver defaultResolver = SequenceResolvers.getDefault();

        NucleotideSequence seq = defaultResolver.resolve(new SequenceAddress(
                "http://files.milaboratory.com/test-data/test.fa.gz#testRecord"))
                .getRegion(new Range(10, 30).inverse());
        Assert.assertEquals(new NucleotideSequence("GCTCCACCACAAGACACTCT"), seq);
    }
}