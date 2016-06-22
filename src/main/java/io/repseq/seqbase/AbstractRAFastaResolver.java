package io.repseq.seqbase;

import com.milaboratory.core.io.sequence.fasta.RandomAccessFastaIndex;
import com.milaboratory.core.io.sequence.fasta.RandomAccessFastaReader;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProviderFactory;
import com.milaboratory.core.sequence.provider.SequenceProviderUtils;
import com.milaboratory.util.LongProcessReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

public abstract class AbstractRAFastaResolver implements SequenceResolver {
    private static final Logger log = LoggerFactory.getLogger(AbstractRAFastaResolver.class);

    final boolean deleteOnError;

    protected AbstractRAFastaResolver(boolean deleteOnError) {
        this.deleteOnError = deleteOnError;
    }

    /**
     * Cache file name to random access reader
     */
    final TreeMap<String, RandomAccessFastaReader<NucleotideSequence>> readers = new TreeMap<>();
    /**
     * Full record URI to sequence provider
     */
    final TreeMap<URI, CachedSequenceProvider<NucleotideSequence>> records = new TreeMap<>();

    /**
     * Extracts record name from original address
     *
     * @param address original address
     * @return record id
     */
    protected abstract String resolveRecordId(URI address);

    /**
     * Extracts id to be used as key for reader from original address
     *
     * @param address original address
     * @return reader id
     */
    protected abstract String resolveReaderId(SequenceAddress address);

    /**
     * Returns instance specific reporter
     *
     * @return instance specific reporter
     */
    protected abstract LongProcessReporter getReporter();

    /**
     * Resolves uri to existing fasta file. E.g. this method performs download.
     *
     * @param uri original uri
     * @return path to fasta file
     */
    protected abstract Path getFASTAFile(SequenceAddress uri);

    public synchronized RandomAccessFastaReader<NucleotideSequence> resolveReader(SequenceAddress address) {
        URI uri = address.getUri();

        for (int retry = 0; retry < 2; ++retry) {
            // Getting reader key
            String readerKey = resolveReaderId(address);

            // Checking if reader already opened
            RandomAccessFastaReader<NucleotideSequence> reader = readers.get(readerKey);

            Path file = null;

            try {
                // Getting fasta file path
                // Download occur here
                file = getFASTAFile(address);

                // Creating or loading index
                RandomAccessFastaIndex index = RandomAccessFastaIndex.index(file, true, getReporter());

                // Caching reader
                readers.put(readerKey, reader =
                        new RandomAccessFastaReader<>(file, index,
                                NucleotideSequence.ALPHABET));

                return reader;
            } catch (Exception e) {
                // Something went wrong with file, removing for re-download.
                log.warn("Error opening {}." + (deleteOnError ? " Removing." : ""), file, e);

                // If deleteOnError flag is set,
                // removing source file and index if exists
                if (deleteOnError && file != null)
                    try {
                        Files.delete(file);
                        Path indexFile = file.resolveSibling(file.getFileName() +
                                RandomAccessFastaIndex.INDEX_SUFFIX);
                        if (Files.exists(indexFile))
                            Files.delete(indexFile);
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }

                // Retry
            }
        }
        throw new RuntimeException();
    }

    @Override
    public CachedSequenceProvider<NucleotideSequence> resolve(final SequenceAddress address) {
        final URI uri = address.getUri();

        CachedSequenceProvider<NucleotideSequence> provider = records.get(uri);
        if (provider == null) {
            final String recordId = resolveRecordId(uri);
            records.put(uri, provider = new CachedSequenceProvider<>(
                    NucleotideSequence.ALPHABET,
                    SequenceProviderUtils.lazyProvider(new SequenceProviderFactory<NucleotideSequence>() {
                        @Override
                        public SequenceProvider<NucleotideSequence> create() {
                            RandomAccessFastaReader<NucleotideSequence> fasta = resolveReader(address);
                            return fasta.getSequenceProvider(recordId);
                        }
                    })));
        }

        return provider;
    }
}
