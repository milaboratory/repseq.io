package io.repseq.learn;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikesh on 7/6/17.
 */
public class VJProbabilityEstimation {
    public final double[][] substitutionMatrix = new double[4][4];
    public final double[][] insertProbs = new double[4][4];
    public final double[] singleInsertProbs = new double[4];
    public final Map<String, double[]> trimmingProbs = new HashMap<>();
    public final Map<String, Map<String, Double>> vjProbs = new HashMap<>();


}
