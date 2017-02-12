package io.repseq.core;

import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryComment;
import io.repseq.dto.VDJCLibraryCommentType;
import io.repseq.dto.VDJCLibraryData;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Class represent a single library of V, D, J, C genes from a single species. This class may represent a full set of
 * genes in organism e.g. TRA,B,G,D/IGH,L,K or just a set of segments for a single immune receptor chain.
 *
 * VDJCLibrary can be loaded using {@link VDJCLibraryRegistry}.
 */
public class VDJCLibrary implements Comparable<VDJCLibrary> {
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
    /**
     * Cached checksum value
     */
    private volatile byte[] checksum;

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
    public byte[] getChecksum() {
        if (checksum == null)
            synchronized (this) {
                if (checksum == null) {
                    List<VDJCGene> genes = new ArrayList<>(getGenes());
                    Collections.sort(genes, new Comparator<VDJCGene>() {
                        @Override
                        public int compare(VDJCGene o1, VDJCGene o2) {
                            return o1.getData().compareTo(o2.getData());
                        }
                    });

                    StringBuilder bigSeqBuilder = new StringBuilder();
                    for (VDJCGene gene : genes)
                        bigSeqBuilder.append(gene.getFeature(gene.getPartitioning().getWrappingGeneFeature()).toString());

                    try {
                        byte[] bytes = bigSeqBuilder.toString().getBytes(StandardCharsets.UTF_8);
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        checksum = md.digest(bytes);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        return checksum;
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
     * Get gene by name. Returns VDJCGene or null if gene with provided name is not found
     *
     * @return VDJCGene or null if gene with provided name is not found
     */
    public VDJCGene get(String geneName) {
        return genes.get(geneName);
    }

    /**
     * Get gene by name. Returns VDJCGene or throws {@link GeneNotFoundException} if gene not found.
     *
     * @return gene
     * @throws GeneNotFoundException
     */
    public VDJCGene getSafe(String geneName) {
        VDJCGene gene = genes.get(geneName);
        if (gene == null)
            throw new GeneNotFoundException(geneName, this);
        return gene;
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
     * Return library id with null checksum. For usage as map key.
     *
     * @return library id with null checksum. For usage as map key.
     */
    VDJCLibraryId getLibraryIdWithoutChecksum() {
        return new VDJCLibraryId(name, libraryData.getTaxonId());
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
     * All comment blocks associated with the library
     *
     * @return all comment blocks associated with the library
     */
    public List<VDJCLibraryComment> getComments() {
        return libraryData.getComments();
    }

    /**
     * Return comments of a particular type
     *
     * @param type comment type
     * @return list of comments of a particular type
     */
    public List<VDJCLibraryComment> getComments(VDJCLibraryCommentType type) {
        return libraryData.getComments(type);
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
            try {
                rpBuilder.setPosition(entry.getKey(), entry.getValue().intValue());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error parsing gene: " + geneData.getName(), e);
            }

        VDJCGene gene = new VDJCGene(library, geneData, geneData.getBaseSequence().resolve(library.getContext(),
                library.registry.getSequenceResolver()), rpBuilder.build());
        library.put(gene);
        return gene;
    }

    @Override
    public int compareTo(VDJCLibrary o) {
        return getLibraryId().compareTo(o.getLibraryId());
    }
}
