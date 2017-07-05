package io.repseq.learn;

import io.repseq.learn.param.SegmentTrimmingParameters;

/**
 * Created by mikesh on 7/5/17.
 */
public interface SegmentTrimmingParameterProvider {
    SegmentTrimmingParameters get(SegmentType segmentType, String id);
}
