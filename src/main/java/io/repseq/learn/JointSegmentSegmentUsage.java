package io.repseq.learn;

import io.repseq.learn.SegmentTuple;
import io.repseq.learn.SegmentUsage;

import java.util.Map;

/**
 * Created by mikesh on 7/9/17.
 */
public class JointSegmentSegmentUsage implements SegmentUsage {
    private final Map<SegmentTuple, Double> segmentUsage;

    public JointSegmentSegmentUsage(Map<SegmentTuple, Double> segmentUsage) {
        this.segmentUsage = segmentUsage;
    }

    @Override
    public double getProbability(SegmentTuple segments) {
        return segmentUsage.getOrDefault(segments, 0.0);
    }
}
