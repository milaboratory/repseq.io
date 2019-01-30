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
package io.repseq.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents immunological clone repertoire
 */
public final class GRepertoire {
    public final List<GClone> clones;
    public final double totalAbundance;

    public GRepertoire(List<GClone> clones) {
        this.clones = Collections.unmodifiableList(new ArrayList<>(clones));
        double sum = 0;
        for (GClone clone : clones)
            sum += clone.abundance;
        this.totalAbundance = sum;
    }
}
