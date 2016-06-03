package io.repseq.util;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.SequenceProvider;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public abstract class HTTPSequenceResolver implements SequenceResolver {
    private static final Logger log = LoggerFactory.getLogger(HTTPSequenceResolver.class);
    public static final int CHUNK_SIZE = 32768;
    final Path cachePath;
    final HttpClient client;
    final HashMap<String, SequenceProvider<NucleotideSequence>> providers = new HashMap<>();

    public HTTPSequenceResolver(Path cachePath, HttpClient client) {
        this.cachePath = cachePath;
        this.client = client;
    }

    private void ensureCacheDirExist() {
        try {
            Files.createDirectories(cachePath);
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
    protected abstract URI resolveAddress(URI address);

    /**
     * Resolves address to file name
     *
     * @param address original address
     * @return file name
     */
    protected abstract String resolveCacheFileName(URI address);

    protected abstract SequenceProvider<NucleotideSequence> createProviderFromFile(Path file);

    @Override
    public synchronized SequenceProvider<NucleotideSequence> resolve(Path context, String address) {
        try {
            // Creating URI from address
            URI uri = new URI(address);

            // Resolving address to cache file name
            String cacheFileName = resolveCacheFileName(uri);

            // Checking whether this provider was previously created
            SequenceProvider<NucleotideSequence> provider = providers.get(cacheFileName);
            if (provider != null)
                return provider;

            // Checking existence of cache directory
            ensureCacheDirExist();

            // Checking file cache
            Path filePath = cachePath.resolve(cacheFileName);
            if (Files.exists(filePath)) {
                try {
                    providers.put(cacheFileName, provider = createProviderFromFile(filePath));
                    return provider;
                } catch (Exception e) {
                    // Something went wrong with file, removing for re-download.
                    log.warn("Error opening {}. Re-downloading file.", filePath, e);
                }
            }

            // Resolving address to http(s) link
            URI link = resolveAddress(uri);

            // Downloading file
            HttpGet request = new HttpGet(link);
            try {
                HttpResponse resp = client.execute(request);
                BufferedInputStream istream = new BufferedInputStream(resp.getEntity().getContent());
                try (BufferedOutputStream ostream = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
                    long ts = System.currentTimeMillis();
                    byte[] buffer = new byte[CHUNK_SIZE];
                    int read;
                    while ((read = istream.read(buffer)) != -1) {

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (request != null)
                    request.releaseConnection();
            }

            return null;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
