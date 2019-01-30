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
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProviderUtils;

/**
 * Tuple of {@link com.milaboratory.core.sequence.provider.SequenceProvider} and {@link ReferencePoints} with useful methods.
 */
public final class SequenceProviderAndReferencePoints extends PartitionedSequenceCached<NucleotideSequence> {
    public final SequenceProvider<NucleotideSequence> sequenceProvider;
    public final ReferencePoints referencePoints;

    public SequenceProviderAndReferencePoints(SequenceProvider<NucleotideSequence> sequenceProvider,
                                              ReferencePoints referencePoints) {
        this.sequenceProvider = sequenceProvider;
        this.referencePoints = referencePoints;
    }

    @Override
    protected NucleotideSequence getSequence(Range range) {
        return sequenceProvider.getRegion(range);
    }

    @Override
    protected SequencePartitioning getPartitioning() {
        return referencePoints;
    }

    public SequenceProviderAndReferencePoints reverse() {
        return new SequenceProviderAndReferencePoints(SequenceProviderUtils.reversedProvider(sequenceProvider),
                referencePoints.relative(new Range(sequenceProvider.size(), 0)));
    }

    public SequenceProviderAndReferencePoints nonReversedView() {
        if (referencePoints.isReversed())
            return reverse();
        else
            return this;
    }
}
