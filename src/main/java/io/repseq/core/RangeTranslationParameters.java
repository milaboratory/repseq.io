package io.repseq.core;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.TranslationParameters;

import java.util.ArrayList;
import java.util.List;

public final class RangeTranslationParameters {
    public final ReferencePoint beginPoint, endPoint;
    public final Range range;
    public final TranslationParameters translationParameters;
    /**
     * Position of nucleotides required to fulfill left or right incomplete codon
     */
    public final Range codonLeftoverRange;

    public RangeTranslationParameters(ReferencePoint beginPoint, ReferencePoint endPoint, Range range) {
        this(beginPoint, endPoint, range, null);
    }

    public RangeTranslationParameters(ReferencePoint beginPoint, ReferencePoint endPoint, Range range,
                                      Range codonLeftoverRange) {
        if (range == null || (beginPoint == null && endPoint == null))
            throw new NullPointerException();
        if (!(beginPoint != null && beginPoint.isTripletBoundary())
                && !(endPoint != null && endPoint.isTripletBoundary()))
            throw new IllegalArgumentException();
        this.beginPoint = beginPoint;
        this.endPoint = endPoint;
        this.range = range;
        this.translationParameters =
                (beginPoint != null && beginPoint.isTripletBoundary()) ?
                        (endPoint != null && endPoint.isTripletBoundary()) ?
                                TranslationParameters.FromCenter
                                : TranslationParameters.FromLeftWithIncompleteCodon
                        : TranslationParameters.FromRightWithIncompleteCodon;
        this.codonLeftoverRange = codonLeftoverRange;
    }

    public Range leftIncompleteCodonRange() {
        if (translationParameters.equals(TranslationParameters.FromRightWithIncompleteCodon) && range.length() % 3 != 0)
            return new Range(range.getFrom(), range.getFrom() + range.sig() * (range.length() % 3));
        else
            return null;
    }

    public Range rightIncompleteCodonRange() {
        if (translationParameters.equals(TranslationParameters.FromLeftWithIncompleteCodon) && range.length() % 3 != 0)
            return new Range(range.getTo() - range.sig() * (range.length() % 3), range.getTo());
        else
            return null;
    }

    public boolean acceptCodonLeftover() {
        return translationParameters.isIncludeIncomplete() && range.length() % 3 != 0;
    }

    public RangeTranslationParameters withCodonLeftover(Range leftover) {
        return new RangeTranslationParameters(beginPoint, endPoint, range, leftover);
    }

    public boolean inFrame() {
        return range.length() % 3 == 0;
    }

    public RangeTranslationParameters tryMergeRight(RangeTranslationParameters right) {
        if (this.range.getTo() != right.range.getFrom())
            return null;

        boolean doMerge = false;
        if ((this.beginPoint != null && this.beginPoint.isTripletBoundary())
                && (right.endPoint != null && right.endPoint.isTripletBoundary())
                && (this.range.length() + right.range.length()) % 3 == 0)
            doMerge = true;
        else if ((this.beginPoint != null && this.beginPoint.isTripletBoundary())
                && this.inFrame() && right.translationParameters.equals(TranslationParameters.FromLeftWithIncompleteCodon))
            doMerge = true;
        else if ((right.endPoint != null && right.endPoint.isTripletBoundary())
                && right.inFrame() && this.translationParameters.equals(TranslationParameters.FromRightWithIncompleteCodon))
            doMerge = true;

        if (doMerge)
            return new RangeTranslationParameters(this.beginPoint, right.endPoint,
                    new Range(this.range.getFrom(), right.range.getTo()));
        else
            return null;
    }

    @Override
    public String toString() {
        return "RangeTranslationParameters{" +
                "beginPoint=" + beginPoint +
                ", endPoint=" + endPoint +
                ", range=" + range +
                ", translationParameters=" + translationParameters +
                ", codonLeftoverRange=" + codonLeftoverRange +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RangeTranslationParameters)) return false;

        RangeTranslationParameters that = (RangeTranslationParameters) o;

        if (beginPoint != null ? !beginPoint.equals(that.beginPoint) : that.beginPoint != null) return false;
        if (endPoint != null ? !endPoint.equals(that.endPoint) : that.endPoint != null) return false;
        if (!range.equals(that.range)) return false;
        return codonLeftoverRange != null ? codonLeftoverRange.equals(that.codonLeftoverRange) : that.codonLeftoverRange == null;
    }

    @Override
    public int hashCode() {
        int result = beginPoint != null ? beginPoint.hashCode() : 0;
        result = 31 * result + (endPoint != null ? endPoint.hashCode() : 0);
        result = 31 * result + range.hashCode();
        result = 31 * result + (codonLeftoverRange != null ? codonLeftoverRange.hashCode() : 0);
        return result;
    }

    static class Accumulator {
        private final List<RangeTranslationParameters> ranges = new ArrayList<>();

        public Accumulator() {
        }

        private RangeTranslationParameters last() {
            return ranges.isEmpty() ? null : ranges.get(ranges.size() - 1);
        }

        public void put(RangeTranslationParameters range) {
            RangeTranslationParameters last = last();
            RangeTranslationParameters mergeResult = last == null ? null : last.tryMergeRight(range);

            if (mergeResult != null)
                ranges.set(ranges.size() - 1, mergeResult);
            else
                ranges.add(range);
        }

        public List<RangeTranslationParameters> getResult() {
            return ranges;
        }
    }
}
