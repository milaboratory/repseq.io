package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.milaboratory.core.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import io.repseq.core.VDJCLibrary;
import org.apache.commons.math3.random.RandomGenerator;


@JsonSubTypes({
        @JsonSubTypes.Type(value = IndependentVDJCGenesModel.class, name = "v+d+j+c"),
        @JsonSubTypes.Type(value = DJDependentVDJCGenesModel.class, name = "v+dj+c")
})
public interface VDJCGenesModel extends Model {
    VDJCGenesGenerator create(RandomGenerator random, VDJCLibrary library);
}
