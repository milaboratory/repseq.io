package io.repseq.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.*;

import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;

public final class MetaUtils {
    private MetaUtils() {
    }

    public static SortedMap<String, List<String>> deepCopy(SortedMap<String, List<String>> meta) {
        SortedMap<String, List<String>> ret = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : meta.entrySet())
            ret.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        return ret;
    }

    /**
     * Unwraps single-element arrays
     */
    public static final class MetaValueSerializer extends JsonSerializer<List<String>> {
        @Override
        public void serialize(List<String> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value.size() == 1)
                gen.writeString(value.get(0));
            else
                gen.writeObject(value);
        }
    }

    /**
     * Wraps single-element arrays back on deserialization
     */
    public static final class MetaValueDeserializer extends JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.getCurrentToken() == START_ARRAY)
                return p.readValueAs(new TypeReference<List<String>>() {
                });
            else {
                ArrayList<String> values = new ArrayList<>();
                values.add(p.readValueAs(String.class));
                return values;
            }
        }
    }
}
