package io.repseq.gen.dist;

import io.repseq.gen.VDJCGenes;
import io.repseq.gen.VDJTrimming;

public interface VDJTrimmingGenerator {
    /**
     * Generate trimmings, given v, d, j, c genes.
     *
     * @param genes v, d, j, c genes
     * @return trimmings
     */
    VDJTrimming sample(VDJCGenes genes);
}
