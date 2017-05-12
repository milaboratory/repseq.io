package io.repseq.gen;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProviderUtils;
import com.milaboratory.test.TestUtil;
import org.apache.commons.math3.random.Well44497b;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ConcatenatedLazySequenceTest {
    @Test
    public void testRandom0() throws Exception {
        for (int i = 0; i < 100; i++) {
            testRandom0(NucleotideSequence.ALPHABET);
            testRandom0(AminoAcidSequence.ALPHABET);
        }
    }

    public <S extends Sequence<S>> void testRandom0(Alphabet<S> alphabet) throws Exception {
        Well44497b w = new Well44497b();
        S sequence = alphabet.getEmptySequence();
        SequenceProvider<S>[] providers = new SequenceProvider[100];
        for (int i = 0; i < 100; i++) {
            final S seq = TestUtil.randomSequence(alphabet, 0, 200);
            sequence = sequence.concatenate(seq);
            providers[i] = SequenceProviderUtils.fromSequence(seq);

            ConcatenatedLazySequence<S> sProvider = new ConcatenatedLazySequence<>(Arrays.copyOf(providers, i + 1));

            if (sequence.size() == 0)
                continue;

            for (int j = 0; j < 10; j++) {
                int from = sequence.size() == 1 ? 0 : w.nextInt(sequence.size() - 1);
                int to = (sequence.size() - from) == 0 ? from : from + w.nextInt(sequence.size() - from);
                Range r = new Range(from, to);
                if (r.isEmpty())
                    continue;
                assertEquals(sequence.getRange(r), sProvider.getRegion(r));
            }
        }
    }
}