package io.repseq.seqbase;

import java.net.URI;

public final class HTTPSequenceResolvers {
    private HTTPSequenceResolvers() {
    }

    /**
     * gi://568815591
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
            return URI.create("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&id=" +
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
