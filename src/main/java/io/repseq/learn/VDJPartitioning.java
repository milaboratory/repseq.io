package io.repseq.learn;

/**
 * Created by mikesh on 7/12/17.
 */
public class VDJPartitioning {
    private final int vEnd, dStart, dEnd, jStart;

    public VDJPartitioning(int vEnd, int jStart, int dStart, int dEnd) {
        this.vEnd = vEnd;
        this.jStart = jStart;
        this.dStart = dStart;
        this.dEnd = dEnd;
    }

    public VDJPartitioning(int vEnd, int jStart) {
        this(vEnd, jStart, -1, -1);
    }

    public int getvEnd() {
        return vEnd;
    }

    public int getdStart() {
        return dStart;
    }

    public int getdEnd() {
        return dEnd;
    }

    public int getjStart() {
        return jStart;
    }
}
