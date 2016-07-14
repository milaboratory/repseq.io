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
