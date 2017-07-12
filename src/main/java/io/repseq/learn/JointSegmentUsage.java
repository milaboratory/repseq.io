package io.repseq.learn;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by mikesh on 7/9/17.
 */
public class JointSegmentUsage implements SegmentUsage {
    private final Map<SegmentTuple, Double> segmentUsage;

    public JointSegmentUsage(Map<SegmentTuple, Double> segmentUsage) {
        this.segmentUsage = segmentUsage;
    }

    @Override
    public double getProbability(SegmentTuple segments) {
        return segmentUsage.getOrDefault(segments, 0.0);
    }

    @Override
    public Set<SegmentTuple> getUniqueSegmentTuples() {
        return Collections.unmodifiableSet(segmentUsage.keySet());
    }
}
