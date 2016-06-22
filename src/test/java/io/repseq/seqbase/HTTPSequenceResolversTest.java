package io.repseq.seqbase;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Paths;

public class HTTPSequenceResolversTest {
    @Test
    public void test1() throws Exception {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        HTTPResolversContext context = new HTTPResolversContext(
                Paths.get("/Volumes/Data/Projects/MiLaboratory/tmp/cc"),
                httpClient);
        HTTPSequenceResolvers.NucCoreGIResolver r = new HTTPSequenceResolvers.NucCoreGIResolver(context);
        CachedSequenceProvider<NucleotideSequence> provider = r.resolve(new SequenceAddress(null, URI.create("gi:195360724")));
        System.out.println(provider.getRegion(new Range(10, 30)));
    }
}