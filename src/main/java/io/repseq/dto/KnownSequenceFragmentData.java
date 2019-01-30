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
package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.net.URI;

/**
 * Represents "known sequence fragment" from VDJCLibrary file
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class KnownSequenceFragmentData implements Comparable<KnownSequenceFragmentData> {
    private final URI uri;
    private final Range range;
    private final NucleotideSequence sequence;

    @JsonCreator
    public KnownSequenceFragmentData(@JsonProperty("uri") URI uri,
                                     @JsonProperty("range") Range range,
                                     @JsonProperty("sequence") NucleotideSequence sequence) {
        this.uri = uri;
        this.range = range;
        this.sequence = sequence;
    }

    public URI getUri() {
        return uri;
    }

    public Range getRange() {
        return range;
    }

    public NucleotideSequence getSequence() {
        return sequence;
    }

    @Override
    public int compareTo(KnownSequenceFragmentData o) {
        int cmp;

        if ((cmp = getUri().compareTo(o.getUri())) != 0)
            return cmp;

        if ((cmp = getRange().compareTo(o.getRange())) != 0)
            return cmp;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KnownSequenceFragmentData)) return false;

        KnownSequenceFragmentData that = (KnownSequenceFragmentData) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (range != null ? !range.equals(that.range) : that.range != null) return false;
        return sequence != null ? sequence.equals(that.sequence) : that.sequence == null;

    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (range != null ? range.hashCode() : 0);
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        return result;
    }
}
