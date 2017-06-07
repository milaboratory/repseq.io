package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Abstract int distribution
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = CategoricalIndependentIntModel.class, name = "categorical")
})
public interface IndependentIntModel extends Model {
    IndependentIntGenerator create(RandomGenerator random);
}
