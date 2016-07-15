package io.repseq.cli;

import com.milaboratory.core.io.sequence.fasta.FastaWriter;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.io.IOException;

public final class CLIUtils {
    private CLIUtils() {
    }

    public static FastaWriter<NucleotideSequence> createSingleFastaWriter(String fileName) throws IOException {
        if (fileName.equals("."))
            return new FastaWriter<>(System.out);
        return new FastaWriter<>(fileName);
    }
}
