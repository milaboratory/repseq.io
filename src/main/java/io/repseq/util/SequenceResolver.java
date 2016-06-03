package io.repseq.util;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.SequenceProvider;

import java.nio.file.Path;

/**
 * Resolves sequence address to {@link SequenceProvider}.
 */
public interface SequenceResolver {
    /**
     * Resolves address and returns corresponding
     *
     * @param context context of original caller (used for resolution of relative paths)
     * @param address address
     * @return
     */
    SequenceProvider<NucleotideSequence> resolve(Path context, String address);
}
