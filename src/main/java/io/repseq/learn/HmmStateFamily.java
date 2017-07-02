package io.repseq.learn;

/**
 * Created by mikesh on 7/2/17.
 */
public enum HmmStateFamily {
    V(0), D(4), J(8),
    Ivd(2), Idj(6), Ivj(2),
    G1(1), G2(3), G3(5), G4(7);

    public final int order;

    HmmStateFamily(int order) {
        this.order = order;
    }
}
