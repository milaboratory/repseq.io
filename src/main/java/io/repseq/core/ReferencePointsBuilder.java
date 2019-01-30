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

public final class ReferencePointsBuilder extends AbstractReferencePointsBuilder<ReferencePoints> {
    public ReferencePointsBuilder() {
        super(ReferencePoints.BASIC_REFERENCE_POINTS_TO_CHECK);
    }

    @Override
    protected ReferencePoints create(int[] points) {
        return new ReferencePoints(points);
    }

    @Override
    protected int indexFromBasicReferencePoint(BasicReferencePoint point) {
        // Checking reference point type
        if (!point.isPure())
            throw new IllegalArgumentException("Supports only pure basic reference points, " + point +
                    " is not basic.");
        return point.index;
    }
}
