package io.repseq.core;

/**
 * Taxon id and library name and optional library checksum
 */
public final class VDJCLibraryId implements Comparable<VDJCLibraryId> {
    private final String libraryName;
    private final long taxonId;
    private final String checksum;

    /**
     * Creates VDJCLibraryId from taxonId and library name
     *
     * @param libraryName library name
     * @param taxonId     taxon id
     */
    public VDJCLibraryId(String libraryName, long taxonId) {
        this.taxonId = taxonId;
        this.libraryName = libraryName;
        this.checksum = null;
    }

    /**
     * Creates VDJCLibraryId from taxonId, library name and checksum
     *
     * @param libraryName library name
     * @param taxonId     taxon id
     * @param checksum    checksum
     */
    public VDJCLibraryId(String libraryName, long taxonId, String checksum) {
        this.taxonId = taxonId;
        this.libraryName = libraryName;
        this.checksum = checksum;
    }

    /**
     * Taxon id
     *
     * @return taxon id
     */
    public long getTaxonId() {
        return taxonId;
    }

    /**
     * Library name
     *
     * @return library name
     */
    public String getLibraryName() {
        return libraryName;
    }

    /**
     * Return checksum
     *
     * @return checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Returns whether this id also contain checksum information
     *
     * @return whether this id also contain checksum information
     */
    public boolean requireChecksumCheck() {
        return checksum != null;
    }

    /**
     * Return new instance of VDJCLibraryId with different library name
     *
     * @param newLibraryName new library name
     * @return new instance of VDJCLibraryId with different library name
     */
    public VDJCLibraryId setLibraryName(String newLibraryName) {
        return new VDJCLibraryId(newLibraryName, taxonId, checksum);
    }

    @Override
    public int compareTo(VDJCLibraryId o) {
        int c;

        if ((c = libraryName.compareTo(o.getLibraryName())) != 0)
            return c;

        return Long.compare(taxonId, o.taxonId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCLibraryId)) return false;

        VDJCLibraryId that = (VDJCLibraryId) o;

        if (taxonId != that.taxonId) return false;
        return libraryName.equals(that.libraryName);

    }

    @Override
    public int hashCode() {
        int result = (int) (taxonId ^ (taxonId >>> 32));
        result = 31 * result + libraryName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return libraryName + ":" + taxonId +
                (checksum == null ? "" : " (" + checksum + ")");
    }
}
