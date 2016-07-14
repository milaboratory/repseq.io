package io.repseq.seqbase;

/**
 * Resolver which can make use of it's parent (e.g. resolvers that just perform URL conversions)
 */
public interface ParentAwareResolver {
    /**
     * Parent resolver invokes this method to provide link to itself for child
     *
     * @param parentResolver link to parent resolver
     */
    void registerParent(SequenceResolver parentResolver);
}
