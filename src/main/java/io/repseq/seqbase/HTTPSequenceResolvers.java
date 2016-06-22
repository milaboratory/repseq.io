package io.repseq.seqbase;

import java.net.URI;

public final class HTTPSequenceResolvers {
    private HTTPSequenceResolvers() {
    }

    /**
     * gi:568815591
     */
    public static class NucCoreGIResolver extends HTTPSequenceResolver {
        public NucCoreGIResolver(HTTPResolversContext context) {
            super(context);
        }

        @Override
        protected String resolveCacheFileName(URI address) {
            String id = address.getRawSchemeSpecificPart();
            return "gi_" + id;
        }

        @Override
        protected URI resolveHTTPAddress(URI address) {
            String id = address.getRawSchemeSpecificPart();
            return URI.create("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&id=" +
                    id + "&rettype=fasta&retmode=text");
        }

        @Override
        protected String resolveRecordId(URI address) {
            return address.getRawSchemeSpecificPart();
        }

        @Override
        public boolean canResolve(SequenceAddress address) {
            return "gi".equals(address.uri.getScheme());
        }
    }
}
