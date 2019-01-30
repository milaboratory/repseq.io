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