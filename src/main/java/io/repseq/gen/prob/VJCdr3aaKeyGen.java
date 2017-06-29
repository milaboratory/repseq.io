package io.repseq.gen.prob;

import io.repseq.gen.GClone;

/**
 * Created by mikesh on 6/29/17.
 */
public class VJCdr3aaKeyGen implements CloneKeyGen<VJCdr3aaKey> {
    private final String gene;

    public VJCdr3aaKeyGen(String gene) {
        this.gene = gene;
    }

    @Override
    public VJCdr3aaKey create(GClone gClone) {
        return new VJCdr3aaKey(gClone, gene);
    }
}
