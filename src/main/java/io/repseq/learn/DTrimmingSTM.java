package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.tree.NeighborhoodIterator;
import com.milaboratory.core.tree.SequenceTreeMap;

import java.util.HashSet;

/**
 * Created by mikesh on 7/12/17.
 */
public class DTrimmingSTM {
    public static double MAX_ERROR_RATE = 0.1;

    private boolean initialized = false;

    private final SequenceTreeMap<NucleotideSequence, DTrimmingSet> stm = new SequenceTreeMap<>(NucleotideSequence.ALPHABET);

    public DTrimmingSTM(NucleotideSequence ref) {
        for (int i = 0; i < ref.size(); i++) {
            for (int j = i + 1; j < ref.size(); j++) {
                DTrimming dTrimming = new DTrimming(i, j);
                NucleotideSequence seq = ref.getRange(i, j + 1);

                DTrimmingSet dTrimmingSet = stm.get(seq);

                if (dTrimmingSet == null) {
                    dTrimmingSet = new DTrimmingSet(new HashSet<>());
                }

                dTrimmingSet.getdTrimmings().add(dTrimming);

                stm.put(seq, dTrimmingSet);
            }
        }
    }

    public Iterable<DTrimmingSet> getDTrimmingSets() {
        return stm.values();
    }

    public DTrimmingMatch getTrimmings(NucleotideSequence seq) {
        if (seq.size() < 2) {
            new DTrimmingMatch(DTrimmingSet.EMPTY, 0, seq.size());
        }

        int maxErrors = (int) (seq.size() * MAX_ERROR_RATE);
        NeighborhoodIterator<NucleotideSequence, DTrimmingSet> ni = stm.getNeighborhoodIterator(seq,
                maxErrors, 0, 0, maxErrors);

        DTrimmingSet dTrimmingSet = ni.next();

        return new DTrimmingMatch(dTrimmingSet, ni.getMismatches(), seq.size());
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
