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
package io.repseq.util;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.RangeMap;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.Entry;

/**
 * Storage of fragmented sequences.
 */
public final class SequenceBase {
    private final Map<String, SequenceContainer> cache = new HashMap<>();

    public void put(String accession, int from, NucleotideSequence sequence) {
        SequenceContainer container = cache.get(accession);
        if (container == null)
            cache.put(accession, new SequenceContainer(from, sequence));
        else
            container.put(from, sequence);
    }

    public NucleotideSequence get(String accession, Range range) {
        SequenceContainer c = cache.get(accession);
        if (c == null)
            return null;
        return c.get(range);
    }

    public Range getAvailableRange(String accession, Range range) {
        SequenceContainer c = cache.get(accession);
        if (c == null)
            return null;
        return c.getAvailableRange(range);
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    private static final class SequenceContainer {
        private Range singleRange;
        private NucleotideSequence singleSequence;
        private RangeMap<NucleotideSequence> map;

        SequenceContainer(int begin, NucleotideSequence seq) {
            singleRange = new Range(begin, begin + seq.size());
            singleSequence = seq;
        }

        void put(int begin, NucleotideSequence sequence) {
            Range r = new Range(begin, begin + sequence.size());
            if (singleRange != null) {
                if (singleRange.intersectsWith(r)) {
                    // Checking
                    Range intersection = singleRange.intersection(r);
                    NucleotideSequence intersectionSequence = get(intersection);
                    if (!intersectionSequence.equals(sequence.getRange(intersection.move(-begin))))
                        throw new IllegalArgumentException();

                    // Merging
                    if (singleRange.contains(r))
                        return;

                    if (r.contains(singleRange)) {
                        singleRange = r;
                        singleSequence = sequence;
                        return;
                    }

                    if (begin < singleRange.getLower())
                        singleSequence = sequence.getRange(0, singleRange.getLower() - begin).concatenate(singleSequence);
                    else
                        singleSequence = singleSequence.getRange(0, begin - singleRange.getLower()).concatenate(sequence);
                    singleRange = singleRange.tryMerge(r);

                    return;
                }
                map = new RangeMap<>();
                map.put(singleRange, singleSequence);
                singleRange = null;
                singleSequence = null;
            }

            Entry<Range, NucleotideSequence> intersectingSeq = map.findSingleIntersection(r);
            if (intersectingSeq == null) {
                map.put(new Range(begin, begin + sequence.size()), sequence);
            } else {
                // Checking
                Range intersection = intersectingSeq.getKey().intersection(r);
                NucleotideSequence intersectionSequence = get(intersection);

                if (!intersectionSequence.equals(sequence.getRange(intersection.move(-begin))))
                    throw new IllegalArgumentException();

                // Merging
                if (intersectingSeq.getKey().contains(r))
                    return;

                // Removing this entry form map
                map.remove(intersectingSeq.getKey());

                if (r.contains(intersectingSeq.getKey())) {
                    map.put(r, sequence);
                    return;
                }

                NucleotideSequence s;
                if (begin < intersectingSeq.getKey().getLower())
                    s = sequence.getRange(0, intersectingSeq.getKey().getLower() - begin).concatenate(intersectingSeq.getValue());
                else
                    s = intersectingSeq.getValue().getRange(0, begin - intersectingSeq.getKey().getLower()).concatenate(sequence);

                map.put(intersectingSeq.getKey().tryMerge(r), s);
            }
        }

        NucleotideSequence get(Range range) {
            if (singleRange != null)
                if (singleRange.contains(range))
                    return singleSequence.getRange(range.move(-singleRange.getLower()));
                else return null;
            Entry<Range, NucleotideSequence> entry = map.findContaining(range);
            if (entry == null)
                return null;
            return entry.getValue().getRange(range.move(-entry.getKey().getLower()));
        }

        Range getAvailableRange(Range range) {
            if (singleRange != null)
                if (singleRange.contains(range))
                    return singleRange;
                else return null;
            Entry<Range, NucleotideSequence> entry = map.findContaining(range);
            if (entry == null)
                return null;
            return entry.getKey();
        }
    }
}
