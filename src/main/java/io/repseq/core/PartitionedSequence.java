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
package io.repseq.core;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.Seq;
import com.milaboratory.core.sequence.SeqBuilder;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class PartitionedSequence<S extends Seq<S>> {
    protected abstract S getSequence(Range range);

    protected abstract SequencePartitioning getPartitioning();

    public S getFeature(GeneFeature feature) {
        if (!feature.isComposite()) {
            Range range = getPartitioning().getRange(feature);
            if (range == null)
                return null;
            return getSequence(range);
        }
        Range[] ranges = getPartitioning().getRanges(feature);
        if (ranges == null)
            return null;
        if (ranges.length == 1)
            return getSequence(ranges[0]);
        int size = 0;
        for (Range range : ranges)
            size += range.length();
        S seq0 = getSequence(ranges[0]);
        SeqBuilder<S> builder = seq0.getBuilder().ensureCapacity(size).append(seq0);
        for (int i = 1; i < ranges.length; ++i)
            builder.append(getSequence(ranges[i]));
        return builder.createAndDestroy();
    }
}
