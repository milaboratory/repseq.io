package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.repseq.core.VDJCLibraryId;
import io.repseq.core.VDJCLibraryRegistry;
import org.apache.commons.math3.random.RandomGenerator;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type",
        defaultImpl = BasicGCloneModel.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasicGCloneModel.class, name = "basic"),
})
public interface GCloneModel {
    VDJCLibraryId libraryId();

    GCloneGenerator create(RandomGenerator random, VDJCLibraryRegistry registry);
}
