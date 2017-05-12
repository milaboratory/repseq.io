package io.repseq.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.milaboratory.primitivio.annotations.Serializable;

@Serializable(by = IO.VDJCGeneIdSerializer.class)
@JsonSerialize(using = IO.VDJCGeneIdJSONSerializer.class)
@JsonDeserialize(using = IO.VDJCGeneIdJSONDeserializer.class)
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

    /**
     * Return gene name. Like: TRBV12-3*00, etc...
     */
    public String getName() {
        return geneName;
    }

    /**
     * Return full gene name.
     *
     * Can be decoded using {@link #decode(String)}
     *
     * Format:
     * {@code libraryName:taxonId[:checksum]/geneName}
     */
    public String getFullName() {
        return libraryId + "/" + geneName;
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
        return getFullName();
    }

    /**
     * Decode string representation returned by {@link #getFullName()} into VDJCGeneId object
     *
     * Format:
     * {@code libraryName:taxonId[:checksum]/geneName}
     *
     * @param str string representation
     * @return VDJCGeneId object
     */
    public static VDJCGeneId decode(String str) {
        String[] split = str.split("/");
        if (split.length != 2)
            throw new IllegalArgumentException("Wrong format: " + str);
        return new VDJCGeneId(VDJCLibraryId.decode(split[0]), split[1]);
    }
}
