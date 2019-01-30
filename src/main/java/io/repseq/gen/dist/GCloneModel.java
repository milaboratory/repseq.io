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
