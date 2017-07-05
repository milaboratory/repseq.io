package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.GermlineMatchParameters;
import io.repseq.learn.param.InsertionParameters;
import io.repseq.learn.param.SegmentTrimmingParameters;

import java.util.EnumMap;

/**
 * Created by mikesh on 7/5/17.
 */
public class VJTransitionGenerator {
    private final InsertionParameters vjInsertionParameters;
    private final GermlineMatchParameters germlineMatchParameters;
    private final GermlineSequenceProvider germlineSequenceProvider;
    private final SegmentTrimmingParameterProvider segmentTrimmingParameterProvider;

    public VJTransitionGenerator(InsertionParameters vjInsertionParameters,
                                 GermlineMatchParameters germlineMatchParameters,
                                 GermlineSequenceProvider germlineSequenceProvider,
                                 SegmentTrimmingParameterProvider segmentTrimmingParameterProvider) {
        this.vjInsertionParameters = vjInsertionParameters;
        this.germlineMatchParameters = germlineMatchParameters;
        this.germlineSequenceProvider = germlineSequenceProvider;
        this.segmentTrimmingParameterProvider = segmentTrimmingParameterProvider;
    }

    public HmmTransitions generate(EnumMap<SegmentType, String> segments,
                                   NucleotideSequence query) {
        NucleotideSequence vRef = germlineSequenceProvider.getFullSequenceWithP(SegmentType.V,
                segments.get(SegmentType.V)),
                jRef = germlineSequenceProvider.getFullSequenceWithP(SegmentType.J,
                        segments.get(SegmentType.J));

        SegmentTrimmingParameters vTrimmingParams = segmentTrimmingParameterProvider.get(SegmentType.V,
                segments.get(SegmentType.V)),
                jTrimmingParams = segmentTrimmingParameterProvider.get(SegmentType.J,
                        segments.get(SegmentType.J));

        double[] vFactors = EmissionProbabilityUtil.getLogVFactors(germlineMatchParameters,
                vRef, query),
                jFactors = EmissionProbabilityUtil.getLogVFactors(germlineMatchParameters,
                        jRef, query);

        double[][] alpha = new double[3][query.size() + 1],
                beta = new double[3][query.size() + 1];

        // fill in T1 (alpha) and T-1 (beta)
        alpha[0][0] = vTrimmingParams.getTrimmingProb(0, vRef.size());

        for (int i = 1; i < query.size(); i++) {
            if (i > vFactors.length)
                break;

            alpha[0][i] = vTrimmingParams.getTrimmingProb(0, vRef.size() - i) *
                    Math.exp(vFactors[i - 1]);
        }

        beta[2][query.size() - 1] = jTrimmingParams.getTrimmingProb(jRef.size(), 0);

        for (int i = 2; i < query.size(); i++) {
            int jFactorIndex = jFactors.length - i + 1;

            if (jFactorIndex < 0)
                break;

            beta[2][query.size() - i] = jTrimmingParams.getTrimmingProb(jRef.size() - i + 1, 0) *
                    Math.exp(jFactors[jFactorIndex]);
        }

        // Fill in T2 (alpha) and T-2 (beta)

        double[][] insertionFactors = EmissionProbabilityUtil.getLogInsertFactors(vjInsertionParameters,
                query);

        double sum = 0;
        for (int i = 0; i < query.size(); i++) { // where arrived from
            sum += alpha[0][i];
            for (int j = i; j < query.size(); j++) { // where we've got to
                alpha[1][j] = sum * insertionFactors[i][j];
            }
        }

        sum = 0;
        for (int i = query.size() - 1; i >= 0; i--) { // where arrived from
            sum += beta[0][i];
            for (int j = i; j < query.size(); j++) { // where we've got to
                alpha[1][j] = sum * insertionFactors[i][j];
            }
        }
    }
}
