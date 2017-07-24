package io.repseq.seqbase;

import java.net.URI;
import java.net.URISyntaxException;

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
            return URI.create("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&id=" +
                    extractId(address) +
                    "&rettype=fasta&retmode=text");
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
