package io.repseq.core;

import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.dto.KnownSequenceFragmentData;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.seqbase.SequenceAddress;
import io.repseq.seqbase.SequenceResolver;
import io.repseq.seqbase.SequenceResolvers;

import java.io.IOException;
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
    final Map<String, Long> speciesNames = new HashMap<>();
    final Map<SpeciesAndLibraryName, VDJCLibrary> libraries = new HashMap<>();

    public VDJCLibraryRegistry(SequenceResolver sequenceResolver) {
        this.sequenceResolver = sequenceResolver;
    }

    public SequenceResolver getSequenceResolver() {
        return sequenceResolver == null ? SequenceResolvers.getDefault() : sequenceResolver;
    }

    public long resolveSpecies(String name) {
        Long taxonId = speciesNames.get(name);
        if (taxonId == null)
            throw new IllegalArgumentException("Can't resolve species name: " + name);
        return taxonId;
    }

    public SpeciesAndLibraryName resolveSpecies(SpeciesAndLibraryName sal) {
        return sal.bySpeciesName() ? new SpeciesAndLibraryName(resolveSpecies(sal.getSpeciesName()),
                sal.getLibraryName()) :
                sal;
    }

    public VDJCLibrary registerLibrary(Path context, String name, VDJCLibraryData data) {
        // Loading known sequence fragments from VDJCLibraryData to current SequenceResolver
        SequenceResolver resolver = getSequenceResolver();
        for (KnownSequenceFragmentData fragment : data.getSequenceFragments())
            resolver.resolve(new SequenceAddress(context, fragment.getUri())).setRegion(fragment.getRange(), fragment.getSequence());

        // Creating library object
        VDJCLibrary library = new VDJCLibrary(data, name, this, context);

        if (libraries.containsKey(library.getSpeciesAndLibraryName()))
            throw new RuntimeException("Duplicate library: " + library.getSpeciesAndLibraryName());

        // Adding genes
        for (VDJCGeneData gene : data.getGenes())
            VDJCLibrary.addGene(library, gene);

        // Adding common species names
        Long taxonId = data.getTaxonId();
        for (String s : data.getSpeciesNames())
            if (speciesNames.containsKey(s) && !speciesNames.get(s).equals(taxonId))
                throw new IllegalArgumentException("Mismatch in common species name between several libraries. (Library name = " + name + ").");

        // Adding this library to collection
        libraries.put(library.getSpeciesAndLibraryName(), library);

        return library;
    }

    public void registerLibraries(Path file) {
        file = file.toAbsolutePath();
        String name = file.getFileName().toString();
        name = name.toLowerCase().replaceAll("(?i).json$", "");
        try {
            // Getting libraries from file
            VDJCLibraryData[] libraries = GlobalObjectMappers.ONE_LINE.readValue(file.toFile(), VDJCLibraryData[].class);

            // Registering libraries
            for (VDJCLibraryData library : libraries)
                registerLibrary(file.getParent(), name, library);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
