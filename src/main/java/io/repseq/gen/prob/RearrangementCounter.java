package io.repseq.gen.prob;

import io.repseq.gen.GClone;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mikesh on 6/29/17.
 */
public class RearrangementCounter {
    private final AtomicLong rearrangementCounter;

    public RearrangementCounter() {
        this.rearrangementCounter = new AtomicLong(0);
    }

    public void update(GClone gClone) {
        rearrangementCounter.incrementAndGet();
    }

    public long getNumberOfRearrangements() {
        return rearrangementCounter.get();
    }
}
