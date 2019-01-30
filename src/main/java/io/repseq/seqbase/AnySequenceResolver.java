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
package io.repseq.seqbase;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;

import java.util.HashMap;

public final class AnySequenceResolver implements OptionalSequenceResolver {
    final HashMap<SequenceAddress, CachedSequenceProvider<NucleotideSequence>> providers = new HashMap<>();

    @Override
    public boolean canResolve(SequenceAddress address) {
        return true;
    }

    @Override
    public synchronized CachedSequenceProvider<NucleotideSequence> resolve(SequenceAddress address) {
        CachedSequenceProvider<NucleotideSequence> provider = providers.get(address);
        if (provider == null)
            providers.put(address, provider = new CachedSequenceProvider<>(NucleotideSequence.ALPHABET, "Can't get sequence for " + address));
        return provider;
    }
}
