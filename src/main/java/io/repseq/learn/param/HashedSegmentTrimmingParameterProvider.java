package io.repseq.learn.param;

import java.util.Map;

/**
 * Created by mikesh on 7/9/17.
 */
public class HashedSegmentTrimmingParameterProvider implements SegmentTrimmingParameterProvider {
    private final Map<String, SegmentTrimmingParameters> segmentTrimmingParametersMap;

    public HashedSegmentTrimmingParameterProvider(Map<String, SegmentTrimmingParameters> segmentTrimmingParametersMap) {
        this.segmentTrimmingParametersMap = segmentTrimmingParametersMap;
    }

    @Override
    public SegmentTrimmingParameters get(String id) {
        return segmentTrimmingParametersMap.get(id);
    }
}
