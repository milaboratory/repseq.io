package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

/**
 * Created by mikesh on 7/13/17.
 */
public class VDJHmmTransitions extends VJHmmTransitions {
    private final NucleotideSequence dRef;
    final DFactor[][] gamma;

    public VDJHmmTransitions(SegmentTuple segments, NucleotideSequence query,
                             NucleotideSequence vRef, NucleotideSequence jRef,
                             NucleotideSequence dRef,
                             double[][] alpha, double[][] beta,
                             DFactor[][] gamma) {
        super(segments, query, vRef, jRef, alpha, beta);
        this.dRef = dRef;
        this.gamma = gamma;
    }
}
