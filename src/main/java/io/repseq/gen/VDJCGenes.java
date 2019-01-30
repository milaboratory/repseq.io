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

import com.fasterxml.jackson.annotation.*;
import io.repseq.core.VDJCGene;

/**
 * Tuple of VDJC genes.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class VDJCGenes {
    /**
     * null represents absence of certain gene
     */
    public final VDJCGene v, d, j, c;

    @JsonCreator
    public VDJCGenes(
            @JsonProperty("v") VDJCGene v,
            @JsonProperty("d") VDJCGene d,
            @JsonProperty("j") VDJCGene j,
            @JsonProperty("c") VDJCGene c
    ) {
        this.v = v;
        this.d = d;
        this.j = j;
        this.c = c;
    }

    public boolean isDDefined() {
        return d != null;
    }

    public boolean isCDefined() {
        return c != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VDJCGenes vdjcGenes = (VDJCGenes) o;

        if (v != null ? !v.equals(vdjcGenes.v) : vdjcGenes.v != null) return false;
        if (d != null ? !d.equals(vdjcGenes.d) : vdjcGenes.d != null) return false;
        if (j != null ? !j.equals(vdjcGenes.j) : vdjcGenes.j != null) return false;
        return c != null ? c.equals(vdjcGenes.c) : vdjcGenes.c == null;
    }

    @Override
    public int hashCode() {
        int result = v != null ? v.hashCode() : 0;
        result = 31 * result + (d != null ? d.hashCode() : 0);
        result = 31 * result + (j != null ? j.hashCode() : 0);
        result = 31 * result + (c != null ? c.hashCode() : 0);
        return result;
    }
}
