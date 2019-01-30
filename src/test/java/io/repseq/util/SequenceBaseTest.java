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
package io.repseq.util;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SequenceBaseTest {
    @Test(expected = IllegalArgumentException.class)
    public void test1e() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 20, new NucleotideSequence("ATT"));
    }

    @Test
    public void test1() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 30, new NucleotideSequence("ATTACACA"));
        base.put("A2", 0, new NucleotideSequence("TATAGACATAAGCA"));
        assertNull(base.get("A1", new Range(21, 24)));
        assertNull(base.get("A1", new Range(29, 32)));
        assertNull(base.get("A3", new Range(29, 32)));
        assertEquals(new NucleotideSequence("TAGA"), base.get("A1", new Range(12, 16)));
        assertEquals(new NucleotideSequence("TCTA"), base.get("A1", new Range(16, 12)));
        assertEquals(new NucleotideSequence("GACA"), base.get("A2", new Range(4, 8)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2e() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 20, new NucleotideSequence("TACATA"));
    }

    @Test
    public void test2() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 20, new NucleotideSequence("CACATA"));
        assertEquals(new NucleotideSequence("ACACA"), base.get("A1", new Range(19, 24)));
    }

    @Test
    public void test3() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 20, new NucleotideSequence("CACATA"));
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        assertEquals(new NucleotideSequence("ACACA"), base.get("A1", new Range(19, 24)));
    }

    @Test
    public void test4() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 18, new NucleotideSequence("CAC"));
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        assertEquals(new NucleotideSequence("ATTAGACACAC"), base.get("A1", new Range(10, 21)));
    }

    @Test
    public void test5() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 18, new NucleotideSequence("CAC"));
        assertEquals(new NucleotideSequence("ATTAGACACAC"), base.get("A1", new Range(10, 21)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2em() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 100, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 20, new NucleotideSequence("TACATA"));
    }

    @Test
    public void test2m() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 100, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 20, new NucleotideSequence("CACATA"));
        assertEquals(new NucleotideSequence("ACACA"), base.get("A1", new Range(19, 24)));
        assertEquals(new NucleotideSequence("TTAG"), base.get("A1", new Range(101, 105)));
    }

    @Test
    public void test3m() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 100, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 20, new NucleotideSequence("CACATA"));
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        assertEquals(new NucleotideSequence("ACACA"), base.get("A1", new Range(19, 24)));
        assertEquals(new NucleotideSequence("TTAG"), base.get("A1", new Range(101, 105)));
    }

    @Test
    public void test4m() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 100, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 18, new NucleotideSequence("CAC"));
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        assertEquals(new NucleotideSequence("ATTAGACACAC"), base.get("A1", new Range(10, 21)));
        assertEquals(new NucleotideSequence("TTAG"), base.get("A1", new Range(101, 105)));
    }

    @Test
    public void test5m() throws Exception {
        SequenceBase base = new SequenceBase();
        base.put("A1", 100, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 10, new NucleotideSequence("ATTAGACACACAC"));
        base.put("A1", 18, new NucleotideSequence("CAC"));
        assertEquals(new NucleotideSequence("ATTAGACACAC"), base.get("A1", new Range(10, 21)));
        assertEquals(new NucleotideSequence("TTAG"), base.get("A1", new Range(101, 105)));
    }
}
