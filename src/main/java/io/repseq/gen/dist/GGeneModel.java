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
