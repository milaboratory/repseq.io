package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

/**
 * Created by mikesh on 7/13/17.
 */
public interface HmmTransitions {
    NucleotideSequence getQuery();

    SegmentTuple getSegments();

    double computePartialProbability();

    VDJPartitioning computeBestPartitioning();
}
