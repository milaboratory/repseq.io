package io.repseq.gen.dist;

import io.repseq.gen.GClone;

import java.util.function.Supplier;

public interface GCloneGenerator extends Supplier<GClone> {
    GClone get();
}
