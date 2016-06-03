package io.repseq.util;

import java.nio.file.Path;

/**
 * Resolver for individual type of address
 */
public interface OptionalResolver extends SequenceResolver {
    /**
     * Returns true if can resolve such address
     *
     * @param address
     * @return
     */
    boolean canResolve(Path context, String address);
}
