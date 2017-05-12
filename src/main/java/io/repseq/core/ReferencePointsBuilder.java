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
