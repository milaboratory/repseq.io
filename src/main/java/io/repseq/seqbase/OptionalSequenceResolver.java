package io.repseq.seqbase;

/**
 * Resolver for individual type of address
 */
public interface OptionalSequenceResolver extends SequenceResolver {
    /**
     * Returns true if can resolve such address
     *
     * @param address address
     * @return
     */
    boolean canResolve(SequenceAddress address);
}
