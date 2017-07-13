package io.repseq.learn.param;

/**
 * Created by mikesh on 7/5/17.
 */
public interface SegmentTrimmingParameterProvider {
    SegmentTrimmingParameters get(String id);
}
