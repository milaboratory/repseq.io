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

import com.milaboratory.util.LongProcess;
import com.milaboratory.util.LongProcessReporter;
import com.milaboratory.util.TimeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public abstract class HTTPFastaSequenceResolver extends AbstractRAFastaResolver
        implements OptionalSequenceResolver {
    private static final Logger log = LoggerFactory.getLogger(HTTPFastaSequenceResolver.class);
    public static final int CHUNK_SIZE = 32768;
    /**
     * Local reporter
     */
    final LongProcessReporter reporter;
    /**
     * Resolver context
     */
    final HTTPResolversContext context;

    public HTTPFastaSequenceResolver(HTTPResolversContext context) {
        super(true);
        this.context = context;
        this.reporter = context.getReporter(this.getClass());
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

    @Override
    protected LongProcessReporter getReporter() {
        return reporter;
    }

    @Override
    protected String resolveReaderId(SequenceAddress address) {
        return resolveCacheFileName(address.getUri());
    }

    @Override
    protected synchronized Path getFASTAFile(SequenceAddress address) {
        final URI uri = address.getUri();

        // Resolving address to cache file name
        String cacheFileName = resolveCacheFileName(uri);

        // Checking existence of cache directory
        ensureCacheDirExist();

        // Checking file cache
        Path file = getCacheDir().resolve(cacheFileName);

        // If file is already in cache directory,
        // return it's path
        if (Files.exists(file))
            return file;

        // Resolving address to http(s) link
        URI httpURI = resolveHTTPAddress(uri);

        // Downloading file
        HttpGet request = new HttpGet(httpURI);
        boolean unGZIP = httpURI.getPath().endsWith(".gz");
        log.debug("Downloading " + httpURI + " into " + file);
        try {
            HttpResponse resp = context.getHttpClient().execute(request);
            HttpEntity entity = resp.getEntity();
            long contentLength = entity.getContentLength();
            System.out.println(resp.getStatusLine().getStatusCode());
            try (LongProcess lp = reporter.start("Downloading " + httpURI);
                 InputStream istream = unGZIP ?
                         new GZIPInputStream(new BufferedInputStream(entity.getContent())) :
                         new BufferedInputStream(entity.getContent());
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

            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (request != null)
                request.releaseConnection();
        }
    }
}
