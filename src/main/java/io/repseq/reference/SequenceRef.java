package io.repseq.reference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.milaboratory.core.Range;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.io.IOException;
import java.util.Arrays;

@JsonSerialize(using = SequenceRef.JSerializer.class)
@JsonDeserialize(using = SequenceRef.JDeserializer.class)
public final class SequenceRef {
    final String ref;
    final Range[] regions;
    final Mutations<NucleotideSequence> mutations;

    public SequenceRef(String ref) {
        this(ref, null, null);
    }

    public SequenceRef(String ref, Range[] regions,
                       Mutations<NucleotideSequence> mutations) {
        this.ref = ref;
        this.regions = (regions == null || regions.length == 0 ? null : regions);
        this.mutations = (mutations == null || mutations.isEmpty() ? null : mutations);
    }

    public String getRef() {
        return ref;
    }

    public Range[] getRegions() {
        return regions;
    }

    public Mutations<NucleotideSequence> getMutations() {
        return mutations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceRef)) return false;

        SequenceRef that = (SequenceRef) o;

        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(regions, that.regions)) return false;
        return mutations != null ? mutations.equals(that.mutations) : that.mutations == null;

    }

    @Override
    public int hashCode() {
        int result = ref != null ? ref.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(regions);
        result = 31 * result + (mutations != null ? mutations.hashCode() : 0);
        return result;
    }

    public static final class JSerializer extends JsonSerializer<SequenceRef> {
        @Override
        public void serialize(SequenceRef value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            if (value.regions == null && value.mutations == null)
                jgen.writeString(value.ref);
            else {
                jgen.writeStartObject();
                jgen.writeStringField("ref", value.ref);
                if (value.regions != null)
                    jgen.writeObjectField("regions", value.regions);
                if (value.mutations != null)
                    jgen.writeStringField("mutations", value.mutations.encode());
                jgen.writeEndObject();
            }
        }
    }

    public static final class JDeserializer extends JsonDeserializer<SequenceRef> {
        @Override
        public SequenceRef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING)
                return new SequenceRef(jp.getText());
            else if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                String ref = null;
                Range[] regions = null;
                Mutations<NucleotideSequence> mutations = null;
                while (true) {
                    JsonToken jsonToken = jp.nextToken();
                    if (jsonToken == JsonToken.END_OBJECT)
                        break;
                    else if (jsonToken == JsonToken.FIELD_NAME) {
                        String fieldName = jp.getCurrentName();
                        jp.nextToken();
                        switch (fieldName) {
                            case "ref":
                                ref = jp.getText();
                                break;
                            case "regions":
                                regions = jp.readValueAs(Range[].class);
                                break;
                            case "mutations":
                                mutations = Mutations.decode(jp.getText(), NucleotideSequence.ALPHABET);
                                break;
                            default:
                                throw ctxt.reportMappingException("Unknown field \"%s\".", fieldName);
                        }
                    } else
                        throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME, null);
                }
                return new SequenceRef(ref, regions, mutations);
            } else
                throw ctxt.wrongTokenException(jp, JsonToken.START_OBJECT, "Object or string value expected.");
        }
    }
}
