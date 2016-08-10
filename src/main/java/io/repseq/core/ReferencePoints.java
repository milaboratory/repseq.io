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


import com.fasterxml.jackson.core.*;
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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
@JsonSerialize(using = ReferencePoints.JSerializer.class)
@JsonDeserialize(using = ReferencePoints.JDeserializer.class)
public final class ReferencePoints extends SequencePartitioning implements java.io.Serializable {
    final int[] points;
    final boolean reversed;

    public ReferencePoints(int[] points) {
        if (points.length != BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS)
            throw new IllegalArgumentException("Illegal length of array.");
        Boolean rev = checkReferencePoints(points);
        this.reversed = rev == null ? false : rev;
        this.points = points;
    }

    public ReferencePoints(int start, int[] points) {
        Boolean rev = checkReferencePoints(points);
        int[] array = new int[BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS];
        Arrays.fill(array, -1);
        System.arraycopy(points, 0, array, start, points.length);
        this.points = array;
        this.reversed = rev == null ? false : rev;
    }

    public int numberOfDefinedPoints() {
        int ret = 0;
        for (int point : points) {
            if (point >= 0)
                ++ret;
        }
        return ret;
    }

    static Boolean checkReferencePoints(int[] points) {
        Boolean reversed = null;

        int first = -1;

        for (int ref : points)
            if (ref < -1)
                throw new IllegalArgumentException("Illegal input: " + ref);
            else if (ref > 0)
                if (first == -1)
                    first = ref;
                else if (first != ref) {
                    reversed = first > ref;
                    break;
                }

        if (reversed == null)
            return null;

        int previousPoint = -1;
        for (int point : points) {
            if (point == -1)
                continue;

            if (previousPoint == -1) {
                previousPoint = point;
                continue;
            }

            if (previousPoint != point &&
                    reversed ^ previousPoint > point)
                throw new IllegalArgumentException("Non-monotonic sequence of reference points.");

            previousPoint = point;
        }

        return reversed;
    }

    private int getPosition(int referencePointIndex) {
        if (referencePointIndex < 0
                || referencePointIndex >= points.length)
            return -1;
        return points[referencePointIndex];
    }

    public int getFirstAvailablePosition() {
        for (int i : points)
            if (i >= 0)
                return i;
        throw new IllegalStateException();
    }

    public int getLastAvailablePosition() {
        for (int i = points.length - 1; i >= 0; --i)
            if (points[i] >= 0)
                return points[i];
        throw new IllegalStateException();
    }

    public Range getContainigRegion() {
        return reversed ?
                new Range(getLastAvailablePosition(), getFirstAvailablePosition()) :
                new Range(getFirstAvailablePosition(), getLastAvailablePosition());
    }

    public int getLengthBetweenBoundaryPoints() {
        if (reversed)
            return getFirstAvailablePosition() - getLastAvailablePosition();
        else
            return getLastAvailablePosition() - getFirstAvailablePosition();
    }

    @Override
    public int getPosition(ReferencePoint referencePoint) {
        int point = getPosition(referencePoint.getIndex());
        if (point < 0)
            return -1;
        return point + (reversed ? -referencePoint.getOffset() : referencePoint.getOffset());
    }

    ReferencePoints getRelativeReferencePoints(GeneFeature geneFeature) {
        int[] newPoints = new int[points.length];
        for (int i = 0; i < points.length; ++i)
            newPoints[i] = getRelativePosition(geneFeature, new ReferencePoint(BasicReferencePoint.getByIndex(i)));
        return new ReferencePoints(newPoints);
    }

    ReferencePoints applyMutations(Mutations<NucleotideSequence> mutations) {
        int[] newPoints = new int[points.length];
        for (int i = 0; i < points.length; ++i)
            if (points[i] == -1)
                newPoints[i] = -1;
            else if ((newPoints[i] = mutations.convertPosition(points[i])) < -1)
                newPoints[i] = ~newPoints[i];
        return new ReferencePoints(newPoints);
    }

    GeneFeature getWrappingGeneFeature() {
        int start = 0, end = points.length - 1;
        for (; start < points.length && points[start] == -1; ++start) ;
        for (; end >= start && points[end] == -1; --end) ;
        if (points[start] == -1)
            return null;
        return new GeneFeature(new ReferencePoint(BasicReferencePoint.getByIndex(start)),
                new ReferencePoint(BasicReferencePoint.getByIndex(end)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferencePoints that = (ReferencePoints) o;
        return Arrays.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        int hash = 31;
        for (int i = 0; i < BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS; ++i)
            hash = getPosition(i) + hash * 17;
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(points);
    }

    public static final class JSerializer extends JsonSerializer<ReferencePoints> {
        @Override
        public void serialize(ReferencePoints value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            for (int i = 0; i < BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS; i++) {
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
