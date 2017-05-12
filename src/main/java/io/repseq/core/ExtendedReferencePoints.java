package io.repseq.core;

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
