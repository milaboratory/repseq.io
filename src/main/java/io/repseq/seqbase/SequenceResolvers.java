package io.repseq.seqbase;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.nio.file.Path;

public class SequenceResolvers {
    private static volatile SequenceResolver defaultResolver = new MultiSequenceResolver(new LocalFileResolver(), new AnySequenceResolver());

    public static void initDefaultResolver(Path cacheFolderPath) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableCookieManagement()
                .setConnectionManager(cm)
                .build();
        HTTPResolversContext context = new HTTPResolversContext(
                cacheFolderPath,
                httpClient);
        initDefaultResolver(context);
    }

    public static void initDefaultResolver(HTTPResolversContext context) {
        defaultResolver = new MultiSequenceResolver(
                new LocalFileResolver(),
                new HTTPSequenceResolvers.NucCoreGIResolver(context),
                new AnySequenceResolver());
    }

    public static SequenceResolver getDefault() {
        return defaultResolver;
    }
}
