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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.Arrays;

import static io.repseq.core.BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS;

/**
 * Basic reference points, excluding extended points (alignment-attached points like VEndTrimmed).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
@JsonSerialize(using = ReferencePoints.JSerializer.class)
@JsonDeserialize(using = ReferencePoints.JDeserializer.class)
public final class ReferencePoints extends AbstractReferencePoints<ReferencePoints> implements java.io.Serializable {
    public ReferencePoints(int[] points) {
        super(points, BASIC_REFERENCE_POINTS_TO_CHECK);
    }

    public ReferencePoints(int start, int[] points) {
        super(start, points, BASIC_REFERENCE_POINTS_TO_CHECK);
    }

    @Override
    protected int indexFromReferencePoint(ReferencePoint point) {
        // Checking reference point type
        if (!point.basicPoint.isPure())
            throw new IllegalArgumentException("Supports only pure basic reference points, " + point +
                    " is not basic.");
        return point.getIndex();
    }

    @Override
    BasicReferencePoint basicReferencePointFromIndex(int index) {
        return BasicReferencePoint.getByIndex(index);
    }

    @Override
    protected ReferencePoints create(int[] points) {
        return new ReferencePoints(points);
    }

    public static final boolean[] BASIC_REFERENCE_POINTS_TO_CHECK = new boolean[TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS];

    static {
        Arrays.fill(BASIC_REFERENCE_POINTS_TO_CHECK, true);
    }

    public static final class JSerializer extends JsonSerializer<ReferencePoints> {
        @Override
        public void serialize(ReferencePoints value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            for (int i = 0; i < TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS; i++) {
                if (value.points[i] >= 0) {
                    String point = ReferencePoint.encode(new ReferencePoint(BasicReferencePoint.getByIndex(i)), true);
                    jgen.writeNumberField(point, value.points[i]);
                }
            }
            jgen.writeEndObject();
        }
    }

    public static final class JDeserializer extends JsonDeserializer<ReferencePoints> {
        @Override
        public ReferencePoints deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ReferencePointsBuilder builder = new ReferencePointsBuilder();

            while (true) {
                JsonToken jsonToken = jp.nextToken();
                if (jsonToken == JsonToken.END_OBJECT)
                    break;
                else if (jsonToken == JsonToken.FIELD_NAME) {
                    String anchorPoint = jp.getCurrentName();
                    ReferencePoint point = ReferencePoint.parse(anchorPoint);

                    if (jp.nextToken() != JsonToken.VALUE_NUMBER_INT)
                        throw ctxt.wrongTokenException(jp, JsonToken.VALUE_NUMBER_INT, "Position of anchor point expected.");

                    try {
                        builder.setPosition(point, jp.getIntValue());
                    } catch (IllegalArgumentException e) {
                        throw new JsonParseException(jp, "Error while parsing anchor points.", e);
                    }
                } else
                    throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME, null);
            }

            return builder.build();
        }
    }
}
