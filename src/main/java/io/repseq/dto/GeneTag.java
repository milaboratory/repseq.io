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
