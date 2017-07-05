package io.repseq.learn;

/**
 * Created by mikesh on 7/5/17.
 */
public class VJSegmentPair {
    private final String v, j;

    public VJSegmentPair(String v, String j) {
        this.v = v;
        this.j = j;
    }

    public String getV() {
        return v;
    }

    public String getJ() {
        return j;
    }
}
