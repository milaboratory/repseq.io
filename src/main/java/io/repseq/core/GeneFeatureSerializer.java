/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            output.putKnownObject(object);
    }

    @Override
    public GeneFeature read(PrimitivI input) {
        GeneFeature object = new GeneFeature(input.readObject(GeneFeature.ReferenceRange[].class), true);
        if (saveRef)
            // Saving this gene feature for the all subsequent deserializations
            input.putKnownObject(object);
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
