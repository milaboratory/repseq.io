package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.Map;

/**
 * Created by mikesh on 7/11/17.
 */
public class HashedGermlineSequenceProvider implements GermlineSequenceProvider {
    private final Map<String, NucleotideSequence> segmentMap;

    public HashedGermlineSequenceProvider(Map<String, NucleotideSequence> segmentMap) {
        this.segmentMap = segmentMap;
    }

    @Override
    public NucleotideSequence getFullSequenceWithP(String id) {
        return segmentMap.get(id);
    }
}
