/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
