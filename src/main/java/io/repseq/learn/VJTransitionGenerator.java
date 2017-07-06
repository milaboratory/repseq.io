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

        double[][] alpha = new double[2][query.size() + 1],
                beta = new double[2][query.size() + 1];

        // Fill in T1 (alpha) and T-1 (beta)

        alpha[0][0] = vTrimmingParams.getTrimmingProb(0, vRef.size());

        for (int i = 1; i < query.size(); i++) {
            if (i > vFactors.length) // do not consider extra bases that do not fit in V
                break;

            alpha[0][i] = vTrimmingParams.getTrimmingProb(0, vRef.size() - i) *
                    Math.exp(vFactors[i - 1]);
        }

        beta[1][query.size() - 1] = jTrimmingParams.getTrimmingProb(jRef.size(), 0);

        for (int i = 2; i < query.size(); i++) {
            int jFactorIndex = jFactors.length - i + 1;

            if (jFactorIndex < 0)
                break;

            beta[1][query.size() - i] = jTrimmingParams.getTrimmingProb(jRef.size() - i + 1, 0) *
                    Math.exp(jFactors[jFactorIndex]);
        }


        // Fill in T2 (alpha) and T-2 (beta)

        double[][] insertionFactors = EmissionProbabilityUtil.getLogInsertFactors(vjInsertionParameters,
                query);

        double[] i0prob = new double[query.size()], i1prob = new double[query.size()];

        for (int i = 0; i < query.size(); i++) { // where we've got to
            i0prob[i] = alpha[0][i] * beta[1][i] *
                    vjInsertionParameters.getInsertSizeProb(0);
            i1prob[i] = alpha[0][i] * beta[1][i + 1] *
                    vjInsertionParameters.getInsertSizeProb(0) *
                    Math.exp(insertionFactors[i + 1][i]);

            for (int j = 0; j <= i; j++) { // where arrived from
                double input = alpha[0][j], insertSizeProb = vjInsertionParameters.getInsertSizeProb(i - j);
                if (input != 0 & insertSizeProb != 0) { // speed up
                    alpha[1][i] += input * // incoming probability
                            Math.exp(insertionFactors[j][i]) * // probability of inserted sequence
                            insertSizeProb; // probability of insert size
                }
            }
        }

        for (int i = 0; i < query.size(); i++) {
            for (int j = i; j < query.size(); j++) {
                double input = beta[1][j], insertSizeProb = vjInsertionParameters.getInsertSizeProb(j - i);
                if (input != 0 & insertSizeProb != 0) {
                    beta[0][i] += input *
                            Math.exp(insertionFactors[i][j]) *
                            insertSizeProb;
                }
            }
        }

        // Final probabilities:
        // Overall - P = sum_i alpha[1][i] * beta[1][i]
        // At level 0 (v mapping) P(V_trim & V_match) = alpha[0][i] * beta[0][i]
        // At level 1 (j mapping) P(J_trim & J_match) = alpha[1][i] * beta[1][i]
        // At level 0.5 (insert) P(insert from i to j) = P / alpha[0][i] / beta[1][j]
        // Probability of 0 insert - i0prob, single-base insert - i1prob
        // Probability of i, i+1 bases in insert - alpha[0][i] * beta[0][i] - i0prob[i] - i1prob[i]

        return new HmmTransitions(query, vRef, jRef, alpha, beta, i0prob, i1prob);
    }
}
