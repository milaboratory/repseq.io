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

/**
 * All reference points, including alignment-attached reference points (e.g. VEndTrimmed).
 */
public final class ExtendedReferencePoints extends AbstractReferencePoints<ExtendedReferencePoints> {
    public ExtendedReferencePoints(int[] points) {
        super(points, EXTENDED_REFERENCE_POINTS_TO_CHECK);
    }

    public ExtendedReferencePoints(int start, int[] points) {
        super(start, points, EXTENDED_REFERENCE_POINTS_TO_CHECK);
    }

    @Override
    protected int indexFromReferencePoint(ReferencePoint point) {
        return point.getExtendedIndex();
    }

    @Override
    BasicReferencePoint basicReferencePointFromIndex(int index) {
        return BasicReferencePoint.getByExtendedIndex(index);
    }

    @Override
    protected ExtendedReferencePoints create(int[] points) {
        return new ExtendedReferencePoints(points);
    }

    public static final boolean[] EXTENDED_REFERENCE_POINTS_TO_CHECK;

    static {
        EXTENDED_REFERENCE_POINTS_TO_CHECK = new boolean[BasicReferencePoint.values().length];
        // Only pure reference points will be checked for ordering
        for (BasicReferencePoint brp : BasicReferencePoint.values())
            EXTENDED_REFERENCE_POINTS_TO_CHECK[brp.extendedIndex] = brp.isPure();
    }
}
