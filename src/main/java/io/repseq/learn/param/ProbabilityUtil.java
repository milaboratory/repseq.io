package io.repseq.learn.param;

import java.util.Arrays;

/**
 * Created by mikesh on 7/11/17.
 */
public class ProbabilityUtil {
    public static double MIN_PROB = 1e-30, LOG_MIN_PROB = Math.log(MIN_PROB);

    public static void check(double[] probs) {
        for (double prob : probs) {
            if (prob < 0)
                throw new IllegalArgumentException("Negative value in probability/pseudocount vector.");
        }
    }

    public static void check(double[][] probs) {
        for (double[] prob : probs) {
            check(prob);
        }
    }

    public static double[][] ensureNonSingularNormalized(double[][] probs) {
        double[][] probs1 = new double[probs.length][];

        for (int i = 0; i < probs1.length; i++) {
            probs1[i] = ensureNonSingularNormalized(probs[i]);
        }

        return probs1;
    }

    public static double[] ensureNonSingularNormalized(double[] probs) {
        double[] probs1 = Arrays.copyOf(probs, probs.length);
        double sum = 0;

        for (int i = 0; i < probs1.length; i++) {
            probs1[i] = Math.max(probs1[i], MIN_PROB);
            sum += probs1[i];
        }

        for (int i = 0; i < probs1.length; i++) {
            probs1[i] /= sum;
        }

        return probs1;
    }

    public static double[][] ensureNormalized(double[][] probs) {
        double[][] probs1 = new double[probs.length][];

        for (int i = 0; i < probs1.length; i++) {
            probs1[i] = ensureNormalized(probs[i]);
        }

        return probs1;
    }

    public static double[] ensureNormalized(double[] probs) {
        double[] probs1 = Arrays.copyOf(probs, probs.length);
        double sum = 0;

        for (double prob : probs1) {
            sum += prob;
        }

        for (int i = 0; i < probs1.length; i++) {
            probs1[i] /= sum;
        }

        return probs1;
    }
}
