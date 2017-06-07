package io.repseq.core;

public final class ExtendedReferencePointsBuilder extends AbstractReferencePointsBuilder<ExtendedReferencePoints> {
    public ExtendedReferencePointsBuilder() {
        super(ExtendedReferencePoints.EXTENDED_REFERENCE_POINTS_TO_CHECK);
    }

    @Override
    protected ExtendedReferencePoints create(int[] points) {
        return new ExtendedReferencePoints(points);
    }

    @Override
    protected int indexFromBasicReferencePoint(BasicReferencePoint point) {
        return point.extendedIndex;
    }
}
