package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Independent real value distribution.
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = ParetoModel.class, name = "pareto"),
        @JsonSubTypes.Type(value = FixedRealModel.class, name = "fixed")
})
public interface IndependentRealModel extends Model {
    IndependentRealGenerator create(RandomGenerator random);
}