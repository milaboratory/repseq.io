package io.repseq.core;

import java.util.Arrays;

public class ReferencePointsBuilder {
    final int[] points;

    public ReferencePointsBuilder() {
        this.points = new int[BasicReferencePoint.TOTAL_NUMBER_OF_BASIC_REFERENCE_POINTS];
        Arrays.fill(points, -1);
    }

    void setPosition(int index, int position) {
        // Checking position value
        if (position < -1)
            throw new IllegalArgumentException("Wrong position value: " + position);

        // Backup previous value
        int oldValue = points[index];

        try {
            // Setting position
            points[index] = position;

            // Checking for validity
            ReferencePoints.checkReferencePoints(points);

        } catch (IllegalArgumentException ex) {
            // Overwrite position with old value if check failed
            // To go back to valid array state
            points[index] = oldValue;

            // Rethrow exception
            throw ex;
        }
    }

    void setPosition(BasicReferencePoint referencePoint, int position) {
        // Checking reference point type
        if (!referencePoint.isPure())
            throw new IllegalArgumentException("Supports only pure basic reference points, " + referencePoint +
                    " is not basic.");

        // Setting position
        setPosition(referencePoint.index, position);
    }

    public void setPosition(ReferencePoint referencePoint, int position) {
        // Checking reference point type
        if (!referencePoint.isBasicPoint())
            throw new IllegalArgumentException("Supports only basic reference points, " + referencePoint +
                    " is not basic.");

        try {
            // Setting position
            setPosition(referencePoint.basicPoint, position);
        } catch (IllegalArgumentException ex) {
            // Rethrow exception with additional information on error context
            throw new IllegalArgumentException("While adding " + referencePoint + " error: " + ex.getMessage());
        }
    }

    public ReferencePoints build() {
        return new ReferencePoints(points.clone());
    }
}
