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

import com.milaboratory.util.LongProcessReporter;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LocalFileResolver extends AbstractRAFastaResolver implements OptionalSequenceResolver {
    public LocalFileResolver() {
        super(false);
    }

    @Override
    protected String resolveRecordId(URI address) {
        String recordId = address.getFragment();
        if (recordId == null)
            throw new IllegalArgumentException("No record id specified in: " + address);
        return recordId;
    }

    @Override
    protected String resolveReaderId(SequenceAddress address) {
        return getFASTAFile(address).toString();
    }

    @Override
    protected LongProcessReporter getReporter() {
        return LongProcessReporter.DefaultLongProcessReporter.INSTANCE;
    }

    @Override
    protected Path getFASTAFile(SequenceAddress address) {
        Path path = Paths.get(address.getUri().getRawSchemeSpecificPart().replaceAll("^//", "")).normalize();
        if (path.isAbsolute())
            return path;
        else
            return address.getContext().resolve(path).normalize();
    }

    @Override
    public boolean canResolve(SequenceAddress address) {
        return "file".equalsIgnoreCase(address.uri.getScheme());
    }
}
