/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.repseq.gen;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.core.sequence.SequenceBuilder;
import com.milaboratory.core.sequence.provider.SequenceProvider;

public final class ConcatenatedLazySequence<S extends Sequence<S>> implements SequenceProvider<S> {
    private final SequenceProvider<S>[] providers;
    private int size = -1;

    public ConcatenatedLazySequence(SequenceProvider<S>[] providers) {
        this.providers = providers;
    }

    @Override
    public void forceInitialize() {
        for (SequenceProvider<S> provider : providers)
            provider.forceInitialize();
    }

    @Override
    public int size() {
        if (size == -1) {
            int totalLength = 0;
            for (SequenceProvider<S> p : providers)
                totalLength += p.size();
            size = totalLength;
        }
        return size;
    }

    @Override
    public S getRegion(Range range) {
        if (range.getUpper() > size())
            throw new IllegalArgumentException("Can't get sequence outside defined region.");
        Range direct = range.isReverse() ? new Range(range.getLower(), range.getUpper()) : range;
        SequenceBuilder<S> seq = null;
        for (SequenceProvider<S> provider : providers) {
            if (provider.size() <= direct.getLower())
                direct = direct.move(-provider.size());
            else {
                Range targetRange;
                if (direct.getUpper() <= provider.size()) {
                    targetRange = direct;
                    direct = null;
                } else {
                    Range r = new Range(0, provider.size());
                    targetRange = direct.intersection(r);
                    assert targetRange != null;
                    direct = new Range(0, direct.getUpper() - provider.size());
                }
                S currentSeq = provider.getRegion(targetRange);
                if (seq == null)
                    seq = currentSeq.getBuilder().ensureCapacity(range.length()).append(currentSeq);
                else
                    seq.append(currentSeq);
                if (direct == null)
                    break;
            }
        }
        assert seq != null;
        S result = seq.createAndDestroy();
        return range.isReverse() ? result.getRange(result.size(), 0) : result;
    }
}
