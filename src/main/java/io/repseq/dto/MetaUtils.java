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
