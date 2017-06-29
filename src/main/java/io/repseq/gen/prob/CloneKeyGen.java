package io.repseq.gen.prob;

import io.repseq.gen.GClone;

/**
 * Created by mikesh on 6/29/17.
 */
public interface CloneKeyGen<T extends CloneKey> {
    T create(GClone gClone);
}
