package io.repseq.gen.dist;

import io.repseq.core.GeneType;
import io.repseq.core.VDJCGene;
import io.repseq.gen.VDJCGenes;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collection;
import java.util.List;

public interface VDJCGenesGenerator {
    List<VDJCGene> genes(GeneType gt);

    VDJCGenes sample();
}
