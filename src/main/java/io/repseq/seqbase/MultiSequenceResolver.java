package io.repseq.seqbase;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;

public class MultiSequenceResolver implements SequenceResolver {
    final OptionalSequenceResolver[] resolvers;

    public MultiSequenceResolver(OptionalSequenceResolver... resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public CachedSequenceProvider<NucleotideSequence> resolve(SequenceAddress address) {
        for (OptionalSequenceResolver resolver : resolvers)
            if (resolver.canResolve(address))
                return resolver.resolve(address);
        throw new IllegalArgumentException("Can't resolve address: " + address);
    }
}
