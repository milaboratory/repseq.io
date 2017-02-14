package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents note block
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class VDJCLibraryNote implements Comparable<VDJCLibraryNote> {
    final VDJCLibraryNoteType type;
    final String text;

    @JsonCreator
    public VDJCLibraryNote(@JsonProperty("type") VDJCLibraryNoteType type,
                           @JsonProperty("text") String text) {
        if (type == null || text == null)
            throw new NullPointerException("No fields can be null.");
        this.type = type;
        this.text = text;
    }

    public VDJCLibraryNoteType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public int compareTo(VDJCLibraryNote o) {
        int c;

        if ((c = this.type.compareTo(o.type)) != 0)
            return c;

        if ((c = this.text.compareTo(o.text)) != 0)
            return c;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCLibraryNote)) return false;

        VDJCLibraryNote that = (VDJCLibraryNote) o;

        if (type != that.type) return false;
        return text.equals(that.text);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}
