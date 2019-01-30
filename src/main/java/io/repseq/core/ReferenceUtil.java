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
package io.repseq.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReferenceUtil {
    /**
     * For advanced use.
     */
    public static final int TOTAL_NUMBER_OF_REFERENCE_POINTS = BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS;

    private static final Map<GeneType, ReferencePoint[]> allBasicPointsByTypes;

    private ReferenceUtil() {
    }

    static {
        allBasicPointsByTypes = new HashMap<>();
        ArrayList<ReferencePoint> pointsBuffer = new ArrayList<>();
    }

    /**
     * Returns underlying reference point id.
     *
     * For advanced use.
     *
     * @return underlying reference point id
     */
    public static int getReferencePointIndex(ReferencePoint referencePoint) {
        if (!referencePoint.isBasicPoint())
            throw new IllegalArgumentException("Index is defined only for pure basic reference points.");
        return referencePoint.getIndex();
    }
}
