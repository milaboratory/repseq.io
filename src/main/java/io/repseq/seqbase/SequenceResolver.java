package io.repseq.seqbase;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;

/**
 * Resolves sequence address to {@link CachedSequenceProvider}.
 */
public interface SequenceResolver {
    /**
     * Resolves address and returns corresponding
     *
     * @param address address
     * @return record
     */
    CachedSequenceProvider<NucleotideSequence> resolve(SequenceAddress address);
}
