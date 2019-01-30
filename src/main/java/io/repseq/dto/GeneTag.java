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

import io.repseq.core.GeneType;
import io.repseq.util.Doc;

/**
 * Manually or automatically attached gene annotation tag.
 */
public enum GeneTag {
    @Doc("Pseudo-gene. Some essential feature of gene sequence is absent or broken: " +
            "stop codon in reading frame, absent recombination or splicing site, etc...")
    Pseudo(GeneType.Variable, GeneType.Diversity, GeneType.Joining, GeneType.Constant),
    @Doc("Stop codon in reading frame.")
    Stops(GeneType.Variable, GeneType.Joining, GeneType.Constant),
    @Doc("Broken conserved Phe/Trp in J gene.")
    NoTrpPhe(GeneType.Joining),
    @Doc("Broken conserved Cys in V gene.")
    NoCys(GeneType.Variable),
    @Doc("Some anchor points are not defined for the gene.")
    Incomplete(GeneType.Variable, GeneType.Diversity, GeneType.Joining, GeneType.Constant);

    private final GeneType[] targetGeneTypes;

    GeneTag(GeneType... targetGeneTypes) {
        this.targetGeneTypes = targetGeneTypes;
    }

    public boolean isSupportedGeneType(GeneType geneType) {
        for (GeneType gt : targetGeneTypes)
            if (geneType == gt)
                return true;
        return false;
    }
}
