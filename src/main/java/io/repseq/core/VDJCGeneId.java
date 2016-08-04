package io.repseq.core;

public final class VDJCGeneId implements Comparable<VDJCGeneId> {
    final VDJCLibraryId libraryId;
    final String geneName;

    public VDJCGeneId(VDJCLibraryId libraryId, String geneName) {
        if (libraryId == null || geneName == null)
            throw new NullPointerException();
        this.libraryId = libraryId;
        this.geneName = geneName;
    }

    public VDJCLibraryId getLibraryId() {
        return libraryId;
    }

    public String getGeneName() {
        return geneName;
    }

    @Override
    public int compareTo(VDJCGeneId o) {
        int c;

        if ((c = libraryId.compareTo(o.libraryId)) != 0)
            return c;

        return geneName.compareTo(o.geneName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCGeneId)) return false;

        VDJCGeneId that = (VDJCGeneId) o;

        if (!libraryId.equals(that.libraryId)) return false;
        return geneName.equals(that.geneName);

    }

    @Override
    public int hashCode() {
        int result = libraryId.hashCode();
        result = 31 * result + geneName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VDJCGeneId{" +
                "libraryId=" + libraryId +
                ", geneName='" + geneName + '\'' +
                '}';
    }
}
