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
