/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
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
