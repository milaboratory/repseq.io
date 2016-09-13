package io.repseq.core;

import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.Serializer;

public final class GeneFeatureSerializer implements Serializer<GeneFeature> {
    private final boolean saveRef;

    public GeneFeatureSerializer() {
        this(false);
    }

    /**
     * Constructor for custom serialization
     *
     * @param saveRef if true will write full gene feature content only once, not suitable for random access files
     */
    public GeneFeatureSerializer(boolean saveRef) {
        this.saveRef = saveRef;
    }

    @Override
    public void write(PrimitivO output, GeneFeature object) {
        output.writeObject(object.regions);
        if (saveRef)
            // Saving this gene feature for the all subsequent serializations
            output.putKnownReference(object);
    }

    @Override
    public GeneFeature read(PrimitivI input) {
        GeneFeature object = new GeneFeature(input.readObject(GeneFeature.ReferenceRange[].class), true);
        if (saveRef)
            // Saving this gene feature for the all subsequent deserializations
            input.putKnownReference(object);
        return object;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public boolean handlesReference() {
        return false;
    }
}
