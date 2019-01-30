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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.Serializer;

import java.io.IOException;

class IO {
    public final static class VDJCGeneSerializer implements Serializer<VDJCGene> {
        @Override
        public void write(PrimitivO output, VDJCGene object) {
            throw new RuntimeException("Serializer only for knownReference serialization.");
        }

        @Override
        public VDJCGene read(PrimitivI input) {
            throw new RuntimeException("Serializer only for knownReference serialization.");
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

    public final static class VDJCGeneIdSerializer implements Serializer<VDJCGeneId> {
        @Override
        public void write(PrimitivO output, VDJCGeneId object) {
            output.writeUTF(object.libraryId.getLibraryName());
            output.writeVarLong(object.libraryId.getTaxonId());
            output.writeObject(object.libraryId.getChecksum());
            output.writeUTF(object.geneName);
        }

        @Override
        public VDJCGeneId read(PrimitivI input) {
            String lName = input.readUTF();
            long taxonId = input.readVarLong();
            byte[] checksum = input.readObject(byte[].class);
            String geneName = input.readUTF();
            return new VDJCGeneId(new VDJCLibraryId(lName, taxonId, checksum), geneName);
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

    public final static class ReferencePointSerializer implements Serializer<ReferencePoint> {
        @Override
        public void write(PrimitivO output, ReferencePoint object) {
            output.writeUTF(object.basicPoint.name());
            output.writeInt(object.offset);
        }

        @Override
        public ReferencePoint read(PrimitivI input) {
            String referencePointName = input.readUTF();
            return new ReferencePoint(BasicReferencePoint.valueOf(referencePointName),
                    input.readInt());
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

    public final static class GeneFeatureReferenceRangeSerializer implements Serializer<GeneFeature.ReferenceRange> {
        @Override
        public void write(PrimitivO output, GeneFeature.ReferenceRange object) {
            output.writeObject(object.begin);
            output.writeObject(object.end);
        }

        @Override
        public GeneFeature.ReferenceRange read(PrimitivI input) {
            ReferencePoint begin = input.readObject(ReferencePoint.class);
            return new GeneFeature.ReferenceRange(begin, input.readObject(ReferencePoint.class));
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

    public static final class VDJCLibraryIdJSONSerializer extends JsonSerializer<VDJCLibraryId> {
        @Override
        public void serialize(VDJCLibraryId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.toString());
        }
    }

    public static final class VDJCLibraryIdJSONDeserializer extends JsonDeserializer<VDJCLibraryId> {
        @Override
        public VDJCLibraryId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return VDJCLibraryId.decode(jp.readValueAs(String.class));
        }
    }

    public static final class VDJCGeneIdJSONSerializer extends JsonSerializer<VDJCGeneId> {
        @Override
        public void serialize(VDJCGeneId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.getFullName());
        }
    }

    public static final class VDJCGeneIdJSONDeserializer extends JsonDeserializer<VDJCGeneId> {
        @Override
        public VDJCGeneId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return VDJCGeneId.decode(jp.readValueAs(String.class));
        }
    }

    public static final class VDJCGeneJSONSerializer extends JsonSerializer<VDJCGene> {
        @Override
        public void serialize(VDJCGene value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            VDJCLibrary library = (VDJCLibrary) provider.getAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY);
            if (library != null && value.getParentLibrary() != library)
                throw new IllegalArgumentException("Serialization of gene from other than current library. " +
                        "See VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE description.");
            if (library == null)
                jgen.writeString(value.getFullName());
            else
                jgen.writeString(value.getName());
        }
    }

    public static final class VDJCGeneJSONDeserializer extends JsonDeserializer<VDJCGene> {
        @Override
        public VDJCGene deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            VDJCLibrary library = (VDJCLibrary) ctxt.getAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY);
            String geneStr = jp.readValueAs(String.class);
            if (library == null) {
                VDJCGeneId geneId = VDJCGeneId.decode(geneStr);
                return VDJCLibraryRegistry.getDefault().getGene(geneId);
            } else {
                return library.getSafe(geneStr);
            }
        }
    }
}
