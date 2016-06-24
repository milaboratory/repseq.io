package io.repseq.seqbase;

import java.net.URI;
import java.nio.file.Path;

/**
 * Contextual sequence address
 */
public final class SequenceAddress {
    /**
     * Address of source file. Used to build relative paths.
     */
    final Path context;
    /**
     * Address
     */
    final URI uri;

    public SequenceAddress(String uri) {
        this(null, URI.create(uri));
    }

    public SequenceAddress(URI uri) {
        this(null, uri);
    }

    public SequenceAddress(Path context, String uri) {
        this(context, URI.create(uri));
    }

    public SequenceAddress(Path context, URI uri) {
        this.context = context == null ? null : context.normalize();
        this.uri = uri;
    }

    public Path getContext() {
        return context;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri.toString() + " (rel. " + context + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceAddress)) return false;

        SequenceAddress that = (SequenceAddress) o;

        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        return uri.equals(that.uri);

    }

    @Override
    public int hashCode() {
        int result = context != null ? context.hashCode() : 0;
        result = 31 * result + uri.hashCode();
        return result;
    }
}
