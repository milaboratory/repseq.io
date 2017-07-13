package io.repseq.learn.param;

/**
 * Created by mikesh on 7/5/17.
 */
public interface SegmentTrimmingParameters {
    double getTrimmingProb(int pos5, int pos3);

    double getZeroLengthProb();
}
