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
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Represents information on number of nucleotides deleted from 3' V, 5' J and both sides of D gene, if present;
 * or sizes of corresponding P-segments. Negative values represent deletions, positive values represent length
 * of P-segment.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class VDJTrimming {
    public final int vTrimming, jTrimming;
    @JsonUnwrapped
    @JsonProperty(access = READ_ONLY)
    public final DTrimming dTrimming;

    public VDJTrimming(int vTrimming, int jTrimming, DTrimming dTrimming) {
        this.vTrimming = vTrimming;
        this.jTrimming = jTrimming;
        this.dTrimming = dTrimming;
    }

    public VDJTrimming(int vTrimming, int jTrimming) {
        this(vTrimming, jTrimming, null);
    }

    public VDJTrimming(int vTrimming, int jTrimming, int d5Trimming, int d3Trimming) {
        this(vTrimming, jTrimming, new DTrimming(d5Trimming, d3Trimming));
    }

    @JsonCreator
    public VDJTrimming(@JsonProperty("vTrimming") int vTrimming,
                       @JsonProperty("jTrimming") int jTrimming,
                       @JsonProperty("d5Trimming") Integer d5Trimming,
                       @JsonProperty("d3Trimming") Integer d3Trimming) {
        this(vTrimming, jTrimming,
                d5Trimming != null && d3Trimming != null ?
                        new DTrimming(d5Trimming, d3Trimming) :
                        null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VDJTrimming that = (VDJTrimming) o;

        if (vTrimming != that.vTrimming) return false;
        if (jTrimming != that.jTrimming) return false;
        return dTrimming != null ? dTrimming.equals(that.dTrimming) : that.dTrimming == null;
    }

    @Override
    public int hashCode() {
        int result = vTrimming;
        result = 31 * result + jTrimming;
        result = 31 * result + (dTrimming != null ? dTrimming.hashCode() : 0);
        return result;
    }
}
