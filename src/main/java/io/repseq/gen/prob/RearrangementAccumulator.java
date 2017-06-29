package io.repseq.gen.prob;

import io.repseq.gen.GClone;
import io.repseq.gen.dist.GCloneGenerator;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class RearrangementAccumulator<T extends CloneKey> {
    private final ConcurrentHashMap<T, RearrangementCounter> accumulatorMap = new ConcurrentHashMap<>();
    private final CloneKeyGen<T> cloneKeyGen;
    private final RearrangementCounter totalCounter = new RearrangementCounter();

    public RearrangementAccumulator(CloneKeyGen<T> cloneKeyGen) {
        this.cloneKeyGen = cloneKeyGen;
    }

    public void update(GClone gClone) {
        T cloneKey = cloneKeyGen.create(gClone);

        accumulatorMap.computeIfAbsent(cloneKey, t -> new RearrangementCounter()).update(gClone);
        totalCounter.update(gClone);
    }

    public void update(GCloneGenerator generator, long sampleSize) {

        Stream.generate(generator).limit(sampleSize).parallel().forEach(this::update);
    }

    public RearrangementCounter getCounter(T cloneKey) {
        return accumulatorMap.getOrDefault(cloneKey, new RearrangementCounter());
    }

    public Map<T, RearrangementCounter> getRearrangements() {
        return Collections.unmodifiableMap(accumulatorMap);
    }

    public RearrangementCounter getTotalCounter() {
        return totalCounter;
    }
}
