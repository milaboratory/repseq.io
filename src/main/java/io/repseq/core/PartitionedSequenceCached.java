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

import com.milaboratory.core.sequence.Seq;

import java.util.concurrent.ConcurrentHashMap;

public abstract class PartitionedSequenceCached<S extends Seq<S>> extends PartitionedSequence<S> {
    private static final Object NULL_SEQUENCE = new Object();
    final ConcurrentHashMap<GeneFeature, Object> cache = new ConcurrentHashMap<>();

    @Override
    public synchronized S getFeature(GeneFeature feature) {
        Object seq;
        // (IMPORTANT) Exactly the same reference must be returned for the same input for correct serialization/deserialization
        if ((seq = cache.get(feature)) == null && !cache.containsKey(feature))
            cache.put(feature, (seq = super.getFeature(feature)) == null ? NULL_SEQUENCE : seq);
        return seq == NULL_SEQUENCE ? null : (S) seq;
    }
}

