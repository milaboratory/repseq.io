package io.repseq.seqbase;

import com.milaboratory.util.LongProcessReporter;
import org.apache.http.client.HttpClient;

import java.nio.file.Path;

/**
 * Shared HTTP client and cache directory for HTTP sequence resolvers
 */
public class HTTPResolversContext {
    private final Path cacheDir;
    private final HttpClient httpClient;

    public HTTPResolversContext(Path cacheDir, HttpClient httpClient) {
        this.cacheDir = cacheDir;
        this.httpClient = httpClient;
    }

    public Path getCacheDir() {
        return cacheDir;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public LongProcessReporter getReporter(Class<?> clazz) {
        return LongProcessReporter.DefaultLongProcessReporter.INSTANCE;
    }
}
