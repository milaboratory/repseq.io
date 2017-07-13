package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.GermlineMatchParameters;
import io.repseq.learn.param.SegmentTrimmingParameterProvider;
import io.repseq.learn.param.SegmentTrimmingParameters;

/**
 * Created by mikesh on 7/13/17.
 */
public abstract class TransitionGeneratorBase<T extends HmmTransitions> implements TransitionGenerator<T> {
    private final GermlineMatchParameters germlineMatchParameters;
    private final SegmentTrimmingParameterProvider segmentTrimmingParameterProvider;
    protected final GermlineSequenceProvider germlineSequenceProvider;

    public TransitionGeneratorBase(GermlineMatchParameters germlineMatchParameters,
                                   SegmentTrimmingParameterProvider segmentTrimmingParameterProvider,
                                   GermlineSequenceProvider germlineSequenceProvider) {
        this.germlineMatchParameters = germlineMatchParameters;
        this.segmentTrimmingParameterProvider = segmentTrimmingParameterProvider;
        this.germlineSequenceProvider = germlineSequenceProvider;
    }

    protected void fillAlpha0(SegmentTuple segments,
                              NucleotideSequence vRef, NucleotideSequence query,
                              double[][] alpha) {
        double[] vFactors = EmissionProbabilityUtil.getLogVFactors(germlineMatchParameters,
                vRef, query);
        SegmentTrimmingParameters vTrimmingParams = segmentTrimmingParameterProvider.get(segments.getvId());

        for (int i = 0; i < query.size(); i++) {
            if (i >= vFactors.length) // do not consider extra bases that do not fit in V
                break;

            alpha[0][i] = vTrimmingParams.getTrimmingProb(0, i) *
                    Math.exp(vFactors[i]);
        }
    }

    protected void fillBeta1(SegmentTuple segments,
                             NucleotideSequence jRef, NucleotideSequence query,
                             double[][] beta) {
        double[] jFactors = EmissionProbabilityUtil.getLogVFactors(germlineMatchParameters,
                jRef, query);
        SegmentTrimmingParameters jTrimmingParams = segmentTrimmingParameterProvider.get(segments.getjId());

        for (int i = 0; i < query.size(); i++) {
            if (i >= jFactors.length)
                break;

            beta[1][query.size() - i - 1] = jTrimmingParams.getTrimmingProb(jRef.size() - i - 1, 0) *
                    Math.exp(jFactors[jFactors.length - i - 1]);
        }
    }
}
