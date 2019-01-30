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
package io.repseq.core;

import com.milaboratory.core.Range;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

import java.net.URI;

public class BaseSequenceTest {
    @Test
    public void test1() throws Exception {
        //SequenceResolvers.initDefaultResolver(TempFileManager.getTempDir().toPath());
        //SequenceResolver defaultResolver = SequenceResolvers.getDefault();
        //
        //NucleotideSequence seq = defaultResolver.resolve(new SequenceAddress("gi://195360724"))
        //        .getRegion(new Range(10, 30));
        //Assert.assertEquals(new NucleotideSequence("gcgagagcaagcactatggc"), seq);
        //

        //Path context = new File(SingleFastqReaderTest.class.getClassLoader().getResource("sequences/some_fasta.fasta")
        //        .toURI()).toPath().toAbsolutePath().normalize().getParent();
        //
        //seq = defaultResolver.resolve(new SequenceAddress(rootPath, "file://some_fasta.fasta#24.6jsd21.Tut")).getRegion(new Range(10, 30));
        //Assert.assertEquals(new NucleotideSequence("ATCCTGGCTTAGAACTAACG"), seq);

        boolean sout = true;

        BaseSequence seq = new BaseSequence(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), null, null);
        TestUtil.assertJson(seq, sout);

        seq = new BaseSequence(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), new Range[]{}, null);
        TestUtil.assertJson(seq, sout);

        seq = new BaseSequence(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), new Range[]{new Range(10, 30)}, null);
        TestUtil.assertJson(seq, sout);

        seq = new BaseSequence(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), new Range[]{new Range(10, 20), new Range(20, 30)}, null);
        TestUtil.assertJson(seq, sout);

        seq = new BaseSequence(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), new Range[]{new Range(10, 20), new Range(20, 30)},
                Mutations.decode("", NucleotideSequence.ALPHABET));
        TestUtil.assertJson(seq, sout);

        seq = new BaseSequence(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), new Range[]{new Range(10, 20), new Range(20, 30)},
                Mutations.decode("DC2SG5T", NucleotideSequence.ALPHABET));
        TestUtil.assertJson(seq, sout);
    }
}