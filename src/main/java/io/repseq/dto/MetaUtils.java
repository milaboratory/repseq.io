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

    public static SortedMap<String, SortedSet<String>> deepCopy(SortedMap<String, SortedSet<String>> meta) {
        SortedMap<String, SortedSet<String>> ret = new TreeMap<>();
        for (Map.Entry<String, SortedSet<String>> entry : meta.entrySet())
            ret.put(entry.getKey(), new TreeSet<>(entry.getValue()));
        return ret;
    }

    /**
     * Unwraps single-element arrays
     */
    public static final class MetaValueSerializer extends JsonSerializer<SortedSet<String>> {
        @Override
        public void serialize(SortedSet<String> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value.size() == 1)
                gen.writeString(value.first());
            else
                gen.writeObject(value);
        }
    }

    /**
     * Wraps single-element arrays back on deserialization
     */
    public static final class MetaValueDeserializer extends JsonDeserializer<SortedSet<String>> {
        @Override
        public SortedSet<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.getCurrentToken() == START_ARRAY)
                return p.readValueAs(new TypeReference<SortedSet<String>>() {
                });
            else {
                SortedSet<String> values = new TreeSet<>();
                values.add(p.readValueAs(String.class));
                return values;
            }
        }
    }
}
