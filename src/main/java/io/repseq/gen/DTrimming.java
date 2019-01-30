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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents information on number of nucleotides deleted from 5' and 3' D; or sizes of corresponding
 * P-segments. Negative values represent deletions, positive values represent length of P-segment.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class DTrimming {
    public final int d5Trimming, d3Trimming;

    @JsonCreator
    public DTrimming(
            @JsonProperty("d5Trimming") int d5Trimming,
            @JsonProperty("d3Trimming") int d3Trimming) {
        this.d5Trimming = d5Trimming;
        this.d3Trimming = d3Trimming;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DTrimming dTrimming = (DTrimming) o;

        if (d5Trimming != dTrimming.d5Trimming) return false;
        return d3Trimming == dTrimming.d3Trimming;
    }

    @Override
    public int hashCode() {
        int result = d5Trimming;
        result = 31 * result + d3Trimming;
        return result;
    }
}
