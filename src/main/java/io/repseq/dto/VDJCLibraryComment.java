package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents comment library block
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public final class VDJCLibraryComment {
    final VDJCLibraryCommentType type;
    final String text;

    @JsonCreator
    public VDJCLibraryComment(@JsonProperty("type") VDJCLibraryCommentType type,
                              @JsonProperty("text") String text) {
        if (type == null || text == null)
            throw new NullPointerException("No fields can be null.");
        this.type = type;
        this.text = text;
    }

    public VDJCLibraryCommentType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VDJCLibraryComment)) return false;

        VDJCLibraryComment that = (VDJCLibraryComment) o;

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
