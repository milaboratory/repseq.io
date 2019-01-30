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
import com.milaboratory.core.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import io.repseq.core.VDJCLibrary;
import org.apache.commons.math3.random.RandomGenerator;


@JsonSubTypes({
        @JsonSubTypes.Type(value = IndependentVDJCGenesModel.class, name = "v+d+j+c"),
        @JsonSubTypes.Type(value = DJDependentVDJCGenesModel.class, name = "v+dj+c"),
        @JsonSubTypes.Type(value = DJCDependentVDJCGenesModel.class, name = "v+djc")
})
public interface VDJCGenesModel extends Model {
    VDJCGenesGenerator create(RandomGenerator random, VDJCLibrary library);
}
