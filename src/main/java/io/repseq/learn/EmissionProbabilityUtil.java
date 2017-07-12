package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.GermlineMatchParameters;
import io.repseq.learn.param.InsertionParameters;

/**
 * Created by mikesh on 05/07/17.
 */
public class EmissionProbabilityUtil {
    public static double[][] getLogInsertFactors(InsertionParameters insertionParameters,
                                                 NucleotideSequence query) {
        double[][] iFactors = new double[query.size()][query.size()];

        for (int i = 0; i < query.size(); i++) { // position on previous slice
            for (int j = i + 2; j < query.size(); j++) { // position in current slice
                iFactors[i][j] = iFactors[i][j - 1] + insertionParameters.getLogInsertionProb(
                        query.codeAt(j - 2),
                        query.codeAt(j - 1));
                iFactors[j][i] = iFactors[i][j];
            }
        }

        return iFactors;
    }

    public static double[][] getLogInsertFactorsRev(InsertionParameters insertionParameters,
                                                    NucleotideSequence query) {
        double[][] iFactors = new double[query.size()][query.size()];

        for (int i = 0; i < query.size(); i++) { // position on previous slice
            for (int j = i + 2; j < query.size(); j++) { // position in current slice
                iFactors[i][j] = iFactors[i][j - 1] + insertionParameters.getLogInsertionProb(
                        query.codeAt(j),
                        query.codeAt(j - 1));
                iFactors[j][i] = iFactors[i][j];
            }
        }

        return iFactors;
    }

    public static double[] getLogVFactors(GermlineMatchParameters germlineMatchParameters,
                                          NucleotideSequence referenceWithP,
                                          NucleotideSequence query) {
        int len = referenceWithP.size() < query.size() ? referenceWithP.size() : query.size();

        double[] vFactors = new double[len];

        vFactors[0] = germlineMatchParameters.getLogSubstitutionProb(referenceWithP.codeAt(0),
                query.codeAt(0));

        for (int i = 1; i < len; i++) {
            vFactors[i] = vFactors[i - 1] +
                    germlineMatchParameters.getLogSubstitutionProb(referenceWithP.codeAt(i),
                            query.codeAt(i));
        }

        return vFactors;
    }

    public static double[] getLogJFactors(GermlineMatchParameters germlineMatchParameters,
                                          NucleotideSequence referenceWithP,
                                          NucleotideSequence query) {
        int len = referenceWithP.size() < query.size() ? referenceWithP.size() : query.size();

        double[] jFactors = new double[len];

        jFactors[len - 1] = germlineMatchParameters.getLogSubstitutionProb(referenceWithP.codeAt(referenceWithP.size() - 1),
                query.codeAt(query.size() - 1));

        for (int i = 1; i < len; i++) {
            jFactors[len - 1 - i] = jFactors[len - i] +
                    germlineMatchParameters.getLogSubstitutionProb(referenceWithP.codeAt(referenceWithP.size() - i - 1),
                            query.codeAt(query.size() - i - 1));
        }

        return jFactors;
    }
}
