package io.repseq.seqbase;

import com.milaboratory.util.LongProcessReporter;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path path = Paths.get(address.getUri().getRawSchemeSpecificPart().replaceAll("^//", "")).normalize();
        if (path.isAbsolute())
            return path;
        else
            return address.getContext().resolve(path).normalize();
    }

    @Override
    public boolean canResolve(SequenceAddress address) {
        return "file".equalsIgnoreCase(address.uri.getScheme());
    }
}
