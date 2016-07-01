package io.repseq.core;

import io.repseq.dto.KnownSequenceFragmentData;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.seqbase.SequenceAddress;
import io.repseq.seqbase.SequenceResolver;
import io.repseq.seqbase.SequenceResolvers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VDJCLibraryRegistry {
    /**
     * If null - default sequence resolver is used
     */
    final SequenceResolver sequenceResolver;
    final List<Path> searchPath = new ArrayList<>();
    final Map<String, VDJCLibrary> libraries = new HashMap<>();

    public VDJCLibraryRegistry(SequenceResolver sequenceResolver) {
        this.sequenceResolver = sequenceResolver;
    }

    public SequenceResolver getSequenceResolver() {
        return sequenceResolver == null ? SequenceResolvers.getDefault() : sequenceResolver;
    }

    public VDJCLibrary registerLibrary(Path context, String name, VDJCLibraryData data) {
        // Loading known sequence fragments from VDJCLibraryData to current SequenceResolver
        SequenceResolver resolver = getSequenceResolver();
        for (KnownSequenceFragmentData fragment : data.getSequenceFragments())
            resolver.resolve(new SequenceAddress(context, fragment.getUri())).setRegion(fragment.getRange(), fragment.getSequence());

        // Creating library object
        VDJCLibrary library = new VDJCLibrary(data, this, context);

        // Adding genes
        for (VDJCGeneData gene : data.getGenes())
            VDJCLibrary.addGene(library, gene);

        return library;
    }
}
