package io.repseq.gen.dist;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.gen.GGene;

public interface InsertGenerator {
    /**
     * Generates insert, using information from GGene with empty sequence V and DJ insertions.
     *
     * @param gene rearranged gene with empty sequence V and DJ insertions
     * @return insert
     */
    NucleotideSequence generate(GGene gene);
}
