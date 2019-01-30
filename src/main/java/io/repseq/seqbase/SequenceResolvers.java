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
                new HTTPSequenceResolvers.NucCoreResolver(context),
                new HTTPSequenceResolvers.RAWHTTPResolver(context),
                new AnySequenceResolver());
    }

    public static SequenceResolver getDefault() {
        return defaultResolver;
    }
}
