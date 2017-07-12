package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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

    @Override
    public Map<String, NucleotideSequence> asMap() {
        return Collections.unmodifiableMap(segmentMap);
    }
}
