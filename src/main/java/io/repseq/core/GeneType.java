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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

/**
 * Group type of a segment.
 *
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @author Shugay Mikhail (mikhail.shugay@gmail.com)
 */
@JsonSerialize(using = GeneType.Serializer.class)
@JsonDeserialize(using = GeneType.Deserializer.class)
public enum GeneType implements java.io.Serializable {
    Variable((byte) 0, 'V', +1, 0, 11, (byte) 0),
    Diversity((byte) 2, 'D', 0, 11, 2, (byte) 1),
    Joining((byte) 1, 'J', -1, 13, 3, (byte) 2),
    Constant((byte) 3, 'C', -2, 16, 3, (byte) 3);
    public static final GeneType[] VJC_REFERENCE = {Variable, Joining, Constant};
    public static final GeneType[] VDJC_REFERENCE = {Variable, Diversity, Joining, Constant};

    private final byte id;
    private final char letter;
    private final int cdr3Side;
    private final int completeNumberOfReferencePoints;
    private final int indexOfFirstReferencePoint;
    private final byte order;

    GeneType(byte id, char letter, int cdr3Side, int indexOfFirstReferencePoint, int completeNumberOfReferencePoints, byte order) {
        this.id = id;
        this.letter = letter;
        this.cdr3Side = cdr3Side;
        this.indexOfFirstReferencePoint = indexOfFirstReferencePoint;
        this.completeNumberOfReferencePoints = completeNumberOfReferencePoints;
        this.order = order;
    }

    public byte getOrder() {
        return order;
    }

    public int getIndexOfFirstReferencePoint() {
        return indexOfFirstReferencePoint;
    }

    public static GeneType fromChar(char letter) {
        switch (letter) {
            case 'C':
            case 'c':
                return Constant;
            case 'V':
            case 'v':
                return Variable;
            case 'J':
            case 'j':
                return Joining;
            case 'D':
            case 'd':
                return Diversity;
        }
        throw new IllegalArgumentException("Unrecognized GeneType letter.");
    }

    /**
     * Gets a segment by id
     *
     * @param id
     */
    public static GeneType get(int id) {
        for (GeneType st : values())
            if (st.id == id)
                return st;
        throw new RuntimeException("Unknown ID");
    }

    /**
     * Gets the associated letter, e.g. V for TRBV
     */
    public char getLetter() {
        return letter;
    }

    /**
     * Id of segment
     */
    public byte id() {
        return id;
    }

    /**
     * Gets an integer indicating position of segment of this type relative to CDR3
     *
     * @return +1 (upstream of CDR3, V gene), 0 (inside CDR3, D gene), -1 (downstream of CDR3, J gene) and -2
     * (downstream of CDR3, C segment)
     */
    public int cdr3Site() {
        return cdr3Side;
    }

    public int getCompleteNumberOfReferencePoints() {
        return completeNumberOfReferencePoints;
    }

    public static final int NUMBER_OF_TYPES;

    static {
        NUMBER_OF_TYPES = values().length;
    }

    public static final class Deserializer extends JsonDeserializer<GeneType> {
        @Override
        public GeneType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String val = jp.getValueAsString();
            if (val.length() == 1)
                return GeneType.fromChar(val.charAt(0));
            return GeneType.valueOf(val);
        }
    }

    public static final class Serializer extends JsonSerializer<GeneType> {
        @Override
        public void serialize(GeneType value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString("" + value.letter);
        }
    }
}
