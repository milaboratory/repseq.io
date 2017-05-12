package io.repseq.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

/**
 * Taxon id and library name and optional library checksum
 */
@JsonSerialize(using = IO.VDJCLibraryIdJSONSerializer.class)
@JsonDeserialize(using = IO.VDJCLibraryIdJSONDeserializer.class)
public final class VDJCLibraryId implements Comparable<VDJCLibraryId> {
    private final String libraryName;
    private final long taxonId;
    private final byte[] checksum;

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
    public VDJCLibraryId(String libraryName, long taxonId, byte[] checksum) {
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
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * Returns copy of this VDJCLibraryId with checksum set to null
     *
     * @return copy of this VDJCLibraryId with checksum set to null
     */
    public VDJCLibraryId withoutChecksum() {
        if (checksum == null)
            // If checksum already null return this
            return this;

        return new VDJCLibraryId(libraryName, taxonId, null);
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

        if ((c = Long.compare(taxonId, o.taxonId)) != 0)
            return c;

        if (checksum == null && o.checksum == null)
            return 0;

        if (checksum == null)
            return -1;

        if (o.checksum == null)
            return 1;

        for (int i = 0; i < checksum.length; i++)
            if ((c = Byte.compare(checksum[i], o.checksum[i])) != 0)
                return c;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCLibraryId)) return false;

        VDJCLibraryId libraryId = (VDJCLibraryId) o;

        if (taxonId != libraryId.taxonId) return false;
        if (libraryName != null ? !libraryName.equals(libraryId.libraryName) : libraryId.libraryName != null)
            return false;
        return Arrays.equals(checksum, libraryId.checksum);

    }

    @Override
    public int hashCode() {
        int result = libraryName != null ? libraryName.hashCode() : 0;
        result = 31 * result + (int) (taxonId ^ (taxonId >>> 32));
        result = 31 * result + Arrays.hashCode(checksum);
        return result;
    }

    @Override
    public String toString() {
        return libraryName + ":" + taxonId +
                (checksum == null ? "" : ":" + new String(Hex.encodeHex(checksum)) + "");
    }

    /**
     * Return VDJCLibraryId object from string representation returned by {@link #toString()}.
     *
     * Format:
     * {@code libraryName:taxonId[:checksum]}
     *
     * @param str string representation
     * @return VDJCLibraryId object
     * @throws IllegalArgumentException if string has wrong format
     */
    public static VDJCLibraryId decode(String str) {
        String[] split = str.split(":");
        if (split.length > 3 || split.length < 2)
            throw new IllegalArgumentException("Wrong string format: " + str);
        else if (split.length == 2)
            return new VDJCLibraryId(split[0], Long.parseLong(split[1]));
        else
            try {
                return new VDJCLibraryId(split[0], Long.parseLong(split[1]), Hex.decodeHex(split[2].toCharArray()));
            } catch (DecoderException e) {
                throw new IllegalArgumentException("Wrong string format:" + str + ". Can't decode checksum.");
            }
    }
}
