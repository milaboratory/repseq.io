package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikesh on 7/2/17.
 */
public class GermlineStateEmitter implements HmmStateEmitter {
    private final double errorProb;
    private final NucleotideSequence reference; /* with P*/
    private final HmmStateFamily hmmStateFamily;
    private final int offset;

    public GermlineStateEmitter(double errorProb,
                                NucleotideSequence reference,
                                HmmStateFamily hmmStateFamily,
                                int offset) {
        this.errorProb = errorProb;
        this.reference = reference;
        this.hmmStateFamily = hmmStateFamily;
        this.offset = offset;
    }

    @Override
    public HmmStateFamily getStateType() {
        return hmmStateFamily;
    }

    @Override
    public double getEmissionProbability(NucleotideSequence sequence, int position, int stateIndex) {
        return reference.codeAt(stateIndex) == sequence.codeAt(position - offset) ?
                (1.0d - errorProb) : (errorProb / 3.0d);
    }

    @Override
    public List<HmmState> spawnStates() {
        List<HmmState> states = new ArrayList<>();

        for (int i = 0; i < reference.size(); i++) {
            states.add(new HmmState(hmmStateFamily, i));
        }

        return states;
    }
}
