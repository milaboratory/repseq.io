package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Repertoire model, factory for {@link GGeneGenerator}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type",
        defaultImpl = BasicGGeneModel.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasicGGeneModel.class, name = "basic"),
})
public interface GGeneModel extends Model {
    GGeneGenerator create(RandomGenerator random, VDJCLibrary library);
}
