package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

/**
 * Created by mikesh on 7/13/17.
 */
public interface TransitionGenerator<T extends HmmTransitions> {
    T generate(SegmentTuple segments,
                            NucleotideSequence query);
}
