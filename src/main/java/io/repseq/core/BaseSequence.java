package io.repseq.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.milaboratory.core.Range;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceBuilder;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProvider;
import com.milaboratory.core.sequence.provider.SequenceProviderUtils;
import io.repseq.seqbase.SequenceAddress;
import io.repseq.seqbase.SequenceResolver;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * DTO for BaseSequence
 *
 * Represents base sequence where V/D/J/C gene is located.
 *
 * Can be anything from a whole chromosome to a small region of some genomic/transcriptomic contig with additional
 * mutations introduced to it (used to represent allelic variants.
 *
 * This object define two optional modification steps for original sequence:
 * (1) cutting of specified regions with subsequent concatenation;
 * (2) application of mutations.
 */
@JsonSerialize(using = BaseSequence.Serializer.class)
@JsonDeserialize(using = BaseSequence.Deserializer.class)
public class BaseSequence {
    /**
     * URI of original sequence (e.g. gi://195360724 , file://some_fasta.fa#recordId etc...)
     */
    final URI origin;
    /**
     * Regions in original sequence that should be extracted and concatenated
     */
    final Range[] regions;
    /**
     * A set of mutations that should be applied to sequence extracted and concatenated using ranges to finally obtain
     * sequence that this object represents
     */
    final Mutations<NucleotideSequence> mutations;

    public BaseSequence(URI origin, Range[] regions, Mutations<NucleotideSequence> mutations) {
        if (regions != null && regions.length == 0)
            regions = null;
        if (mutations != null && mutations.isEmpty())
            mutations = null;
        if (mutations != null && regions == null)
            throw new IllegalArgumentException("Only relative mutations are supported, please specify " +
                    "regions/region for this sequence.");
        this.origin = origin;
        this.regions = regions;
        this.mutations = mutations;
    }

    /**
     * Returns SequenceProvider to access underlying sequence
     *
     * @param context  resolution context
     * @param resolver sequence resolver
     * @return SequenceProvider to access underlying sequence
     */
    public SequenceProvider<NucleotideSequence> resolve(Path context, SequenceResolver resolver) {
        CachedSequenceProvider<NucleotideSequence> originalProvider = resolver.resolve(new SequenceAddress(context, origin));
        if (isPureOriginalSequence()) {
            return originalProvider;
        } else {
            // TODO implement more lazy algorithm
            int length = 0;
            for (Range region : regions)
                length += region.length();
            SequenceBuilder<NucleotideSequence> builder = NucleotideSequence.ALPHABET.createBuilder()
                    .ensureCapacity(length);
            for (Range region : regions)
                builder.append(originalProvider.getRegion(region));
            NucleotideSequence seq = builder.createAndDestroy();
            seq = mutations == null ? seq : mutations.mutate(seq);
            return SequenceProviderUtils.fromSequence(seq);
        }
    }

    /**
     * Returns {@literal true} if this object represents original sequence without any modifications
     *
     * @return {@literal true} if this object represents original sequence without any modifications
     */
    public boolean isPureOriginalSequence() {
        return regions == null && mutations == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseSequence)) return false;

        BaseSequence that = (BaseSequence) o;

        if (!origin.equals(that.origin)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(regions, that.regions)) return false;
        return mutations != null ? mutations.equals(that.mutations) : that.mutations == null;

    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + Arrays.hashCode(regions);
        result = 31 * result + (mutations != null ? mutations.hashCode() : 0);
        return result;
    }

    public static final class Serializer extends JsonSerializer<BaseSequence> {
        @Override
        public void serialize(BaseSequence value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            if (value.isPureOriginalSequence())
                gen.writeObject(value.origin);
            else {
                gen.writeStartObject();
                gen.writeObjectField("origin", value.origin);
                if (value.regions != null)
                    if (value.regions.length == 1)
                        gen.writeObjectField("region", value.regions[0]);
                    else
                        gen.writeObjectField("regions", value.regions);
                if (value.mutations != null)
                    gen.writeObjectField("mutations", value.mutations.encode());
                gen.writeEndObject();
            }
        }
    }

    private static final TypeReference<Mutations<NucleotideSequence>> numMutationsRef = new TypeReference<Mutations<NucleotideSequence>>() {};
    private static final JavaType numMutationsType = TypeFactory.defaultInstance().constructParametricType(Mutations.class, NucleotideSequence.class);

    public static final class Deserializer extends JsonDeserializer<BaseSequence> {
        @Override
        public BaseSequence deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (p.getCurrentToken() == JsonToken.START_OBJECT) {
                URI origin = null;
                Range[] regions = null;
                Mutations<NucleotideSequence> mutations = null;
                JsonToken token;
                while ((token = p.nextToken()) != JsonToken.END_OBJECT) {
                    // Only Field name token expected here
                    if (token != JsonToken.FIELD_NAME)
                        throw ctxt.wrongTokenException(p, JsonToken.FIELD_NAME, "");

                    String fieldName = p.getCurrentName();
                    p.nextToken();
                    switch (fieldName) {
                        case "origin":
                            origin = p.readValueAs(URI.class);
                            break;
                        case "regions":
                            regions = p.readValueAs(Range[].class);
                            break;
                        case "region":
                            regions = new Range[]{p.readValueAs(Range.class)};
                            break;
                        case "mutations":
                            mutations = p.readValueAs(numMutationsRef);
                            break;
                        default:
                            throw ctxt.reportInstantiationException(BaseSequence.class, "Unknown field: " + fieldName);
                    }
                }
                return new BaseSequence(origin, regions, mutations);
            } else {
                URI origin = p.readValueAs(URI.class);
                return new BaseSequence(origin, null, null);
            }
        }
    }
}
