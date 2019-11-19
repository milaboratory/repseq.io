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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public final class HTTPSequenceResolvers {
    private HTTPSequenceResolvers() {
    }

    private static URI withoutFragment(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                    uri.getPort(), uri.getRawPath(), uri.getQuery(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * http://ftp-mouse.sanger.ac.uk/other/jl17/scaffolds.2001.fa#unplaced-7%20233
     */
    public static class RAWHTTPResolver extends HTTPFastaSequenceResolver {
        public RAWHTTPResolver(HTTPResolversContext context) {
            super(context);
        }

        @Override
        protected URI resolveHTTPAddress(URI address) {
            return withoutFragment(address);
        }

        @Override
        protected String resolveRecordId(URI address) {
            return address.getFragment();
        }

        @Override
        public boolean canResolve(SequenceAddress address) {
            return "http".equalsIgnoreCase(address.uri.getScheme());
        }
    }

    /**
     * nuccore://568815591
     */
    public static class NucCoreResolver extends HTTPFastaSequenceResolver {
        public NucCoreResolver(HTTPResolversContext context) {
            super(context);
        }

        private String extractId(URI address) {
            return address.getAuthority();
        }

        @Override
        protected String resolveCacheFileName(URI address) {
            return "nuccore_" + extractId(address);
        }

        @Override
        protected URI resolveHTTPAddress(URI address) {
            try {
                return URI.create("https://www.ncbi.nlm.nih.gov/sviewer/viewer.fcgi?id=" +
                        URLEncoder.encode(extractId(address), "UTF-8")
                                .replace("+", "%20")
                                .replace(".", "%2E") +
                        "&db=nuccore&report=fasta&retmode=text");
                // return URI.create("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&id=" +
                //         URLEncoder.encode(extractId(address), "UTF-8")
                //                 .replace("+", "%20")
                //                 .replace(".", "%2E") +
                //         "&rettype=fasta&retmode=text");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected String resolveRecordId(URI address) {
            return extractId(address);
        }

        @Override
        public boolean canResolve(SequenceAddress address) {
            return "nuccore".equalsIgnoreCase(address.uri.getScheme()) && address.uri.getAuthority() != null;
        }
    }
}
