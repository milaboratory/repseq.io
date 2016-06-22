package io.repseq.seqbase;

import com.milaboratory.util.LongProcessReporter;

import java.net.URI;
import java.nio.file.Path;

public final class LocalFileResolver extends AbstractRAFastaResolver implements OptionalSequenceResolver {
    public LocalFileResolver() {
        super(false);
    }

    @Override
    protected String resolveRecordId(URI address) {
        String recordId = address.getFragment();
        if (recordId == null)
            throw new IllegalArgumentException("No record id specified in: " + address);
        return recordId;
    }

    @Override
    protected String resolveReaderId(SequenceAddress address) {
        return getFASTAFile(address).toString();
    }

    @Override
    protected LongProcessReporter getReporter() {
        return LongProcessReporter.DefaultLongProcessReporter.INSTANCE;
    }

    @Override
    protected Path getFASTAFile(SequenceAddress address) {
        String relativePath = address.getUri().getRawSchemeSpecificPart();
        return address.getContext().resolve(relativePath).normalize();
    }

    @Override
    public boolean canResolve(SequenceAddress address) {
        return false;
    }
}
