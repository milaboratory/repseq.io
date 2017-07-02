package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mikesh on 7/2/17.
 */
public class GhostStateEmitter implements HmmStateEmitter {
    private final HmmStateFamily hmmStateFamily;

    public GhostStateEmitter(HmmStateFamily hmmStateFamily) {
        this.hmmStateFamily = hmmStateFamily;
    }

    @Override
    public HmmStateFamily getStateType() {
        return hmmStateFamily;
    }

    @Override
    public double getEmissionProbability(NucleotideSequence sequence, int position, int stateIndex) {
        return 1.0;
    }

    @Override
    public List<HmmState> spawnStates() {
        return Arrays.asList(new HmmState(hmmStateFamily, 0));
    }
}
