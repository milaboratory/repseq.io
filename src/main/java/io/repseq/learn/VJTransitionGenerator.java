package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.GermlineMatchParameters;
import io.repseq.learn.param.InsertionParameters;
import io.repseq.learn.param.SegmentTrimmingParameterProvider;
import io.repseq.learn.param.SegmentTrimmingParameters;

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

    public VJHmmTransitions generate(SegmentTuple segments,
                                     NucleotideSequence query) {
        NucleotideSequence vRef = germlineSequenceProvider.getFullSequenceWithP(segments.getvId()),
                jRef = germlineSequenceProvider.getFullSequenceWithP(segments.getjId());

        SegmentTrimmingParameters vTrimmingParams = segmentTrimmingParameterProvider.get(segments.getvId()),
                jTrimmingParams = segmentTrimmingParameterProvider.get(segments.getjId());

        double[] vFactors = EmissionProbabilityUtil.getLogVFactors(germlineMatchParameters,
                vRef, query),
                jFactors = EmissionProbabilityUtil.getLogJFactors(germlineMatchParameters,
                        jRef, query);

        double[][] alpha = new double[2][query.size() + 1],
                beta = new double[2][query.size() + 1];

        // Fill in T1 (alpha) and T-1 (beta)

        for (int i = 0; i < query.size(); i++) {
            if (i >= vFactors.length) // do not consider extra bases that do not fit in V
                break;

            alpha[0][i] = vTrimmingParams.getTrimmingProb(0, i) *
                    Math.exp(vFactors[i]);
        }

        for (int i = 0; i < query.size(); i++) {
            if (i >= jFactors.length)
                break;

            beta[1][query.size() - i - 1] = jTrimmingParams.getTrimmingProb(jRef.size() - i - 1, 0) *
                    Math.exp(jFactors[jFactors.length - i - 1]);
        }


        // Fill in T2 (alpha) and T-2 (beta)

        double[][] insertionFactors = EmissionProbabilityUtil.getLogInsertFactors(vjInsertionParameters,
                query);

        for (int i = 0; i < query.size(); i++) { // where we've got to
            for (int j = 0; j < i; j++) { // where arrived from; j < i as both V and J are inclusive
                double input = alpha[0][j],
                        insertSizeProb = vjInsertionParameters.getInsertSizeProb(i - j - 1);
                if (input != 0 & insertSizeProb != 0) { // speed up
                    alpha[1][i] += input * // incoming probability
                            Math.exp(insertionFactors[j][i]) * // probability of inserted sequence
                            insertSizeProb; // probability of insert size
                }
            }
        }

        for (int i = 0; i < query.size(); i++) {
            for (int j = i + 1; j < query.size(); j++) {
                double input = beta[1][j],
                        insertSizeProb = vjInsertionParameters.getInsertSizeProb(j - i - 1);
                if (input != 0 & insertSizeProb != 0) {
                    beta[0][i] += input *
                            Math.exp(insertionFactors[i][j]) *
                            insertSizeProb;
                }
            }
        }

        // Final probabilities:
        // Overall - P = sum_i alpha[1][i] * beta[1][i]
        // At level 0 (v mapping) P(V_trim & V_match) = alpha[0][i] * beta[0][i] INCLUSIVE
        // At level 1 (j mapping) P(J_trim & J_match) = alpha[1][i] * beta[1][i] INCLUSIVE
        // At level 0.5 (insert) P(insert from i to j) = P / alpha[0][i] / beta[1][j]

        return new VJHmmTransitions(segments,
                query, vRef, jRef, alpha, beta);
    }
}
