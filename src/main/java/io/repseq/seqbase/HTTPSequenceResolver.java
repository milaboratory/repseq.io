package io.repseq.seqbase;

import com.milaboratory.core.io.sequence.fasta.RandomAccessFastaIndex;
import com.milaboratory.core.io.sequence.fasta.RandomAccessFastaReader;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProviderFactory;
import com.milaboratory.core.sequence.provider.SequenceProviderUtils;
import com.milaboratory.util.LongProcess;
import com.milaboratory.util.LongProcessReporter;
import com.milaboratory.util.TimeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

public abstract class HTTPSequenceResolver implements OptionalSequenceResolver {
    private static final Logger log = LoggerFactory.getLogger(HTTPSequenceResolver.class);
    public static final int CHUNK_SIZE = 32768;
    /**
     * Local reporter
     */
    final LongProcessReporter reporter;
    /**
     * Resolver context
     */
    final HTTPResolversContext context;
    /**
     * Cache file name to random access reader
     */
    final TreeMap<String, RandomAccessFastaReader<NucleotideSequence>> readers = new TreeMap<>();
    /**
     * Full record URI to sequence provider
     */
    final TreeMap<URI, CachedSequenceProvider<NucleotideSequence>> records = new TreeMap<>();

    protected HTTPSequenceResolver(HTTPResolversContext context) {
        this.context = context;
        this.reporter = context.getReporter(this.getClass());
    }

    public Path getCacheDir() {
        return context.getCacheDir();
    }

    private void ensureCacheDirExist() {
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
     * Extracts record name from original address
     *
     * @param address original address
     * @return record id
     */
    protected abstract String resolveRecordId(URI address);

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

    public synchronized RandomAccessFastaReader<NucleotideSequence> resolveReader(URI uri) {
        for (int retry = 0; retry < 2; ++retry) {
            // Resolving address to cache file name
            String cacheFileName = resolveCacheFileName(uri);

            // Checking whether this provider was previously created
            RandomAccessFastaReader<NucleotideSequence> reader = readers.get(cacheFileName);
            if (reader != null)
                return reader;

            // Checking existence of cache directory
            ensureCacheDirExist();

            // Checking file cache
            Path file = getCacheDir().resolve(cacheFileName);

            // Checking whether file exists
            if (!Files.exists(file)) {
                // Resolving address to http(s) link
                URI httpURI = resolveHTTPAddress(uri);

                // Downloading file
                HttpGet request = new HttpGet(httpURI);
                try {
                    HttpResponse resp = context.getHttpClient().execute(request);
                    HttpEntity entity = resp.getEntity();
                    long contentLength = entity.getContentLength();
                    try (LongProcess lp = reporter.start("Downloading " + httpURI);
                         BufferedInputStream istream = new BufferedInputStream(entity.getContent());
                         OutputStream ostream = new FileOutputStream(file.toFile())) {
                        long startTimestamp = System.nanoTime();
                        byte[] buffer = new byte[CHUNK_SIZE];
                        int read;
                        long done = 0;
                        while ((read = istream.read(buffer)) > 0) {
                            ostream.write(buffer, 0, read);
                            if (contentLength >= 0)
                                lp.reportStatus(1.0 * (done += read) / contentLength);
                        }
                        log.debug(httpURI + " downloaded in " +
                                TimeUtils.nanoTimeToString(System.nanoTime() - startTimestamp));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (request != null)
                        request.releaseConnection();
                }
            }

            try {
                // Creating or loading index
                RandomAccessFastaIndex index = RandomAccessFastaIndex.index(file, true, reporter);

                // Caching reader
                readers.put(cacheFileName, reader =
                        new RandomAccessFastaReader<>(file, index,
                                NucleotideSequence.ALPHABET));
                return reader;
            } catch (Exception e) {
                // Something went wrong with file, removing for re-download.
                log.warn("Error opening {}. Re-downloading file.", file, e);
                try {
                    Files.delete(file);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
        throw new RuntimeException();
    }

    @Override
    public synchronized CachedSequenceProvider<NucleotideSequence> resolve(final SequenceAddress address) {
        final URI uri = address.getUri();

        CachedSequenceProvider<NucleotideSequence> provider = records.get(uri);
        if (provider == null) {
            final String id = resolveRecordId(uri);
            records.put(uri, provider = new CachedSequenceProvider<>(
                    NucleotideSequence.ALPHABET,
                    SequenceProviderUtils.lazyProvider(new SequenceProviderFactory<NucleotideSequence>() {
                        @Override
                        public SequenceProvider<NucleotideSequence> create() {
                            RandomAccessFastaReader<NucleotideSequence> fasta = resolveReader(uri);
                            return fasta.getSequenceProvider(id);
                        }
                    })));
        }

        return provider;
    }
}
