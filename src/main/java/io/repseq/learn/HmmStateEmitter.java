package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.List;

/**
 * Created by mikesh on 7/2/17.
 */
public interface HmmStateEmitter {
    HmmStateFamily getStateType();

    double getEmissionProbability(NucleotideSequence sequence, int position, int stateIndex);

    List<HmmState> spawnStates();
}
