package io.repseq.learn.param;

import io.repseq.learn.SegmentTuple;

import java.util.Map;

/**
 * Created by mikesh on 7/9/17.
 */
public class JointSegmentUsageParameters implements UsageParameters {
    private final Map<SegmentTuple, Double> segmentUsage;

    public JointSegmentUsageParameters(Map<SegmentTuple, Double> segmentUsage) {
        this.segmentUsage = segmentUsage;
    }

    @Override
    public double getProbability(SegmentTuple segments) {
        return segmentUsage.getOrDefault(segments, 0.0);
    }
}
