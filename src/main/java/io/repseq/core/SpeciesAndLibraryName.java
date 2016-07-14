package io.repseq.core;

/**
 * Pair of species (taxon id) and library name
 *
 * Species can be specified either by taxon id or by species name
 */
public final class SpeciesAndLibraryName {
    private final String speciesName;
    private final long taxonId;
    private final String libraryName;

    /**
     * Creates SpeciesAndLibraryName from speciesName and library name
     *
     * @param speciesName species name
     * @param libraryName library name
     */
    public SpeciesAndLibraryName(String speciesName, String libraryName) {
        this.speciesName = speciesName;
        this.taxonId = -1;
        this.libraryName = libraryName;
    }

    /**
     * Creates SpeciesAndLibraryName from taxonId and library name
     *
     * @param taxonId     taxon id
     * @param libraryName library name
     */
    public SpeciesAndLibraryName(long taxonId, String libraryName) {
        this.speciesName = null;
        this.taxonId = taxonId;
        this.libraryName = libraryName;
    }

    /**
     * Returns true if species is specified by name
     *
     * @return true if species is specified by name
     */
    public boolean bySpeciesName() {
        return speciesName != null;
    }

    /**
     * String species name
     *
     * @return string species name
     */
    public String getSpeciesName() {
        if (!bySpeciesName())
            throw new IllegalArgumentException("No species name specified");
        return speciesName;
    }

    /**
     * Taxon id
     *
     * @return taxon id
     */
    public long getTaxonId() {
        if (bySpeciesName())
            throw new IllegalArgumentException("No taxon id specified");
        return taxonId;
    }

    public String getLibraryName() {
        return libraryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpeciesAndLibraryName)) return false;

        SpeciesAndLibraryName that = (SpeciesAndLibraryName) o;

        if (taxonId != that.taxonId) return false;
        if (speciesName != null ? !speciesName.equals(that.speciesName) : that.speciesName != null) return false;
        return libraryName != null ? libraryName.equals(that.libraryName) : that.libraryName == null;

    }

    @Override
    public int hashCode() {
        int result = speciesName != null ? speciesName.hashCode() : 0;
        result = 31 * result + (int) (taxonId ^ (taxonId >>> 32));
        result = 31 * result + (libraryName != null ? libraryName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SpeciesAndLibraryName{" +
                "speciesName='" + speciesName + '\'' +
                ", taxonId=" + taxonId +
                ", libraryName='" + libraryName + '\'' +
                '}';
    }
}
