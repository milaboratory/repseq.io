package io.repseq.learn.param;

import io.repseq.learn.SegmentTuple;

/**
 * Created by mikesh on 04/07/17.
 */
public interface UsageParameters {
    /**
     * Gets the probability of observing a certain sequence of segments in a V(D)J rearrangement.
     * In most general case V(D)J probabilities are stored as P(VDJ) for TRB/IGH and P(VJ) for TRA/IGL/IGK.
     * In case not all segment types are specified in the arguments, a marginal probability, e.g. P(V)=sum_DJ P(VDJ)
     * is returned.
     * @param segments a map of segment_type:segment_name entries
     * @return probability of V(D)J rearrangement
     */
    double getProbability(SegmentTuple segments);
}
