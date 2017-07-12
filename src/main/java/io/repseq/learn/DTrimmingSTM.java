package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.tree.NeighborhoodIterator;
import com.milaboratory.core.tree.SequenceTreeMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mikesh on 7/12/17.
 */
public class DTrimmingSTM {
    public static double MAX_ERROR_RATE = 0.1;

    private final SequenceTreeMap<NucleotideSequence, Set<DTrimming>> stm = new SequenceTreeMap<>(NucleotideSequence.ALPHABET);

    public DTrimmingSTM(NucleotideSequence ref) {
        for (int i = 0; i < ref.size(); i++) {
            for (int j = i + 1; j < ref.size(); j++) {
                DTrimming dTrimming = new DTrimming(i, j);
                NucleotideSequence seq = ref.getRange(i, j + 1);

                Set<DTrimming> trimmings = stm.get(seq);

                if (trimmings == null) {
                    trimmings = new HashSet<>();
                }

                trimmings.add(dTrimming);

                stm.put(seq, trimmings);
            }
        }
    }

    public DTrimmingMatch getTrimmings(NucleotideSequence seq) {
        int maxErrors = (int) (seq.size() * MAX_ERROR_RATE);
        NeighborhoodIterator<NucleotideSequence, Set<DTrimming>> ni = stm.getNeighborhoodIterator(seq,
                maxErrors, 0, 0, maxErrors);

        Set<DTrimming> dTrimmings = ni.next();

        return new DTrimmingMatch(dTrimmings, ni.getMismatches(), seq.size());
    }
}
