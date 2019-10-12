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
package io.repseq.seqbase;

import com.milaboratory.core.Range;
import com.milaboratory.core.io.sequence.fastq.SingleFastqReaderTest;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.TempFileManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

public class SequenceResolverTest {
    @Test
    public void test1() throws Exception {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");

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