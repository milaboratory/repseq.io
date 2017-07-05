package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by mikesh on 05/07/17.
 */
public class GermlineSequenceProvider {
    private final EnumMap<SegmentType, Map<String, NucleotideSequence>> sequenceMap;

    public GermlineSequenceProvider(EnumMap<SegmentType, Map<String, NucleotideSequence>> sequenceMap) {
        this.sequenceMap = sequenceMap;
    }

    public NucleotideSequence getFullSequenceWithP(SegmentType segmentType, String id) {
        // TODO: handle NPE
        return sequenceMap.get(segmentType).get(id);
    }
}
