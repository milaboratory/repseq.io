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

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class HTTPRedirectSequenceResolver implements OptionalSequenceResolver, ParentAwareResolver {
    private static final Charset cahceCharset = StandardCharsets.UTF_8;
    /**
     * Resolver context
     */
    final HTTPResolversContext context;
    /**
     * Link to parent resolver
     */
    private final AtomicReference<SequenceResolver> parent = new AtomicReference<>();
    private final HashMap<URI, URI> cache = new HashMap<>();

    public HTTPRedirectSequenceResolver(HTTPResolversContext context) {
        this.context = context;
    }

    public SequenceResolver getParent() {
        return parent.get();
    }

    @Override
    public void registerParent(SequenceResolver parentResolver) {
        if (!parent.compareAndSet(null, parentResolver))
            throw new IllegalStateException("Parent resolver already set.");
    }

    public Path getCacheDir() {
        return context.getCacheDir();
    }

    protected void ensureCacheDirExist() {
        try {
            Files.createDirectories(getCacheDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves address to http/https link
     *
     * @param address original address
     * @return http/https link
     */
    protected abstract URI resolveHTTPAddress(URI address);

    /**
     * Resolves HTTP address to file name
     *
     * @param address initialAddress
     * @return file name
     */
    protected String resolveCacheFileName(URI address) {
        URI httpAddress = resolveHTTPAddress(address);
        return httpAddress.toString().replaceAll("(\\W)+", "_");
    }

    protected abstract URI convertUrl(URI inputUrl);

    private synchronized URI convert1(URI inputURI) {
        try {
            URI newURI = cache.get(inputURI);
            if (newURI != null)
                return newURI;

            Path cachePath = getCacheDir()
                    .resolve(resolveCacheFileName(inputURI));

            if (Files.exists(cachePath))
                return URI.create(FileUtils.readFileToString(
                        cachePath.toFile(),
                        cahceCharset));

            newURI = convertUrl(inputURI);
            FileUtils.writeStringToFile(cachePath.toFile(), newURI.toString(), cahceCharset);

            return newURI;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CachedSequenceProvider<NucleotideSequence> resolve(SequenceAddress address) {
        return getParent().resolve(new SequenceAddress(address.getContext(), convert1(address.getUri())));
    }
}
