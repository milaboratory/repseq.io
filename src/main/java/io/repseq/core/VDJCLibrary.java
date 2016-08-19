package io.repseq.core;

import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;

import java.nio.file.Path;
import java.util.*;

/**
 * Class represent a single library of V, D, J, C genes from a single species. This class may represent a full set of
 * genes in organism e.g. TRA,B,G,D/IGH,L,K or just a set of segments for a single immune receptor chain.
 *
 * VDJCLibrary can be loaded using {@link VDJCLibraryRegistry}.
 */
public class VDJCLibrary {
    /**
     * Original DTO
     */
    private final VDJCLibraryData libraryData;
    /**
     * Library name
     */
    private final String name;
    /**
     * Each library stores the link to it's parent registry
     */
    private final VDJCLibraryRegistry registry;
    /**
     * Context: e.g. path of file this object was deserialized from. Used to resolve relative paths of fasta files etc.
     */
    private final Path context;
    /**
     * Name -> VDJCGene
     */
    private final Map<String, VDJCGene> genes = new HashMap<>();

    public VDJCLibrary(VDJCLibraryData libraryData, String name, VDJCLibraryRegistry registry, Path context) {
        this.libraryData = libraryData;
        this.name = name;
        this.registry = registry;
        this.context = context;
    }

    private void put(VDJCGene gene) {
        genes.put(gene.getName(), gene);
    }

    /**
     * Return checksum for this library.
     *
     * @return checksum for this library
     */
    public String getChecksum() {
        return "00";
    }

    /**
     * Returns serializable library data
     *
     * @return serializable library data
     */
    public VDJCLibraryData getData() {
        return libraryData;
    }

    /**
     * Returns parent registry
     *
     * @return parent registry
     */
    public VDJCLibraryRegistry getParent() {
        return registry;
    }

    /**
     * Returns library context folder
     *
     * @return library context folder
     */
    public Path getContext() {
        return context;
    }

    /**
     * Returns collection of all genes in this library
     *
     * @return collection of all genes in this library
     */
    public Collection<VDJCGene> getGenes() {
        return genes.values();
    }

    /**
     * Returns collection of all genes in this library with specific chains
     *
     * @return collection of all genes in this library with specific chains
     */
    public Collection<VDJCGene> getGenes(Chains chains) {
        List<VDJCGene> result = new ArrayList<>();
        for (VDJCGene gene : genes.values())
            if (gene.getChains().intersects(chains))
                result.add(gene);
        return result;
    }

    /**
     * Get gene by name
     *
     * @return gene
     */
    public VDJCGene get(String geneName) {
        return genes.get(geneName);
    }

    /**
     * Returns identifier of this library
     *
     * @return identifier of this library
     */
    public VDJCLibraryId getLibraryId() {
        return new VDJCLibraryId(name, libraryData.getTaxonId(), getChecksum());
    }

    /**
     * Return species (taxon id)
     *
     * @return species (taxon id)
     */
    public long getTaxonId() {
        return libraryData.getTaxonId();
    }

    /**
     * Library name
     *
     * @return library name
     */
    public String getName() {
        return name;
    }

    /**
     * Creates full featured VDJCGene object from VDJCGene DTO object, and adds it to VDJCLibrary
     *
     * @param library  library
     * @param geneData gene DTO
     * @return full featured VDJCGene object
     */
    public static VDJCGene addGene(VDJCLibrary library, VDJCGeneData geneData) {
        ReferencePointsBuilder rpBuilder = new ReferencePointsBuilder();
        for (Map.Entry<ReferencePoint, Long> entry : geneData.getAnchorPoints().entrySet())
            // TODO convert base reference point position type to long ?
            rpBuilder.setPosition(entry.getKey(), entry.getValue().intValue());

        VDJCGene gene = new VDJCGene(library, geneData, geneData.getBaseSequence().resolve(library.getContext(),
                library.registry.getSequenceResolver()), rpBuilder.build());
        library.put(gene);
        return gene;
    }
}
