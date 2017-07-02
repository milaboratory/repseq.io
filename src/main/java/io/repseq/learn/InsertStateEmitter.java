package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikesh on 7/2/17.
 */
public class InsertStateEmitter implements HmmStateEmitter {
    private final HmmStateFamily hmmStateFamily;
    private final double[] baseProbs;
    private final int offset, maxInserts;

    public InsertStateEmitter(HmmStateFamily hmmStateFamily, double[] baseProbs, int offset, int maxInserts) {
        this.hmmStateFamily = hmmStateFamily;
        this.baseProbs = baseProbs;
        this.offset = offset;
        this.maxInserts = maxInserts;
    }

    @Override
    public HmmStateFamily getStateType() {
        return hmmStateFamily;
    }

    @Override
    public double getEmissionProbability(NucleotideSequence sequence, int position, int stateIndex) {
        return baseProbs[sequence.codeAt(position - offset)];
    }

    @Override
    public List<HmmState> spawnStates() {
        List<HmmState> states = new ArrayList<>();

        for (int i = 0; i < maxInserts; i++) {
            states.add(new HmmState(hmmStateFamily, i));
        }

        return states;
    }
}
