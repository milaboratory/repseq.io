package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.GermlineMatchParameters;
import io.repseq.learn.param.InsertionParameters;
import io.repseq.learn.param.SegmentTrimmingParameterProvider;

/**
 * Created by mikesh on 7/5/17.
 */
public class VJTransitionGenerator extends TransitionGeneratorBase<VJHmmTransitions> {
    private final InsertionParameters vjInsertionParameters;

    public VJTransitionGenerator(InsertionParameters vjInsertionParameters,
                                 GermlineMatchParameters germlineMatchParameters,
                                 GermlineSequenceProvider germlineSequenceProvider,
                                 SegmentTrimmingParameterProvider segmentTrimmingParameterProvider) {
        super(germlineMatchParameters, segmentTrimmingParameterProvider, germlineSequenceProvider);
        this.vjInsertionParameters = vjInsertionParameters;
    }

    @Override
    public VJHmmTransitions generate(SegmentTuple segments,
                                     NucleotideSequence query) {
        NucleotideSequence vRef = germlineSequenceProvider.getFullSequenceWithP(segments.getvId()),
                jRef = germlineSequenceProvider.getFullSequenceWithP(segments.getjId());

        double[][] alpha = new double[2][query.size()],
                beta = new double[2][query.size()];

        // Fill in T1 (alpha) and T-1 (beta)

        fillAlpha0(segments, vRef, query, alpha);
        fillBeta1(segments, jRef, query, beta);


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

        return new VJHmmTransitions(segments,
                query, vRef, jRef, alpha, beta);
    }


}
