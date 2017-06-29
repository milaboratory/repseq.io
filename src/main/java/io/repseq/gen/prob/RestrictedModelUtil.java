package io.repseq.gen.prob;

import io.repseq.gen.dist.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mikesh on 6/29/17.
 */
public class RestrictedModelUtil {
    public static RestrictedModel<GCloneModel> restrictIndependent(GCloneModel original,
                                                                   Set<String> vNames,
                                                                   Set<String> dNames,
                                                                   Set<String> jNames,
                                                                   Set<String> cNames) {
        if (original instanceof BasicGCloneModel) {
            BasicGCloneModel mdl = (BasicGCloneModel) original;

            Map<String, GGeneModel> geneModels = new HashMap<>();

            double sum = 1.0;

            for (Map.Entry<String, GGeneModel> entry : mdl.geneModels.entrySet()) {
                RestrictedModel<GGeneModel> restrictedGeneModel = restrictIndependent(entry.getValue(),
                        vNames,
                        dNames,
                        jNames,
                        cNames);

                geneModels.put(entry.getKey(), restrictedGeneModel.getModel());
                sum *= restrictedGeneModel.getWeight();
            }

            return new RestrictedModel<>(new BasicGCloneModel(mdl.vdjcLibrary,
                    mdl.abundanceModel, geneModels),
                    sum);
        } else {
            throw new IllegalArgumentException("Can't restrict a generic clone model. " +
                    "The method is only applicable to 'basic' models.");
        }
    }

    public static RestrictedModel<GGeneModel> restrictIndependent(GGeneModel original,
                                                                  Set<String> vNames,
                                                                  Set<String> dNames,
                                                                  Set<String> jNames,
                                                                  Set<String> cNames) {
        if (original instanceof BasicGGeneModel) {
            BasicGGeneModel mdl = (BasicGGeneModel) original;

            RestrictedModel<VDJCGenesModel> restrictedModel = restrictIndependent(mdl.vdjcGenesModel,
                    vNames,
                    dNames,
                    jNames,
                    cNames);

            return new RestrictedModel<>(new BasicGGeneModel(restrictedModel.getModel(),
                    mdl.trimmingModel, mdl.vInsertModel, mdl.djInsertModel),
                    restrictedModel.getWeight());
        } else {
            throw new IllegalArgumentException("Can't restrict a generic gene model. " +
                    "The method is only applicable to 'basic' models.");
        }
    }

    public static RestrictedModel<VDJCGenesModel> restrictIndependent(VDJCGenesModel original,
                                                                      Set<String> vNames,
                                                                      Set<String> dNames,
                                                                      Set<String> jNames,
                                                                      Set<String> cNames) {
        if (original instanceof IndependentVDJCGenesModel) {
            IndependentVDJCGenesModel mdl = (IndependentVDJCGenesModel) original;

            Map<String, Double> v = new HashMap<>(mdl.v),
                    d = new HashMap<>(mdl.d),
                    j = new HashMap<>(mdl.j),
                    c = new HashMap<>(mdl.c);

            double vSum = renormalize(v, vNames),
                    dSum = renormalize(d, dNames),
                    jSum = renormalize(j, jNames),
                    cSum = renormalize(c, cNames);

            return new RestrictedModel<>(new IndependentVDJCGenesModel(v, d, j, c),
                    vSum * dSum * jSum * cSum);
        } else if (original instanceof DJDependentVDJCGenesModel) {
            DJDependentVDJCGenesModel mdl = (DJDependentVDJCGenesModel) original;

            Map<String, Double> v = new HashMap<>(mdl.v),
                    dj = new HashMap<>(mdl.dj),
                    c = new HashMap<>(mdl.c);

            double vSum = renormalize(v, vNames),
                    djSum = renormalize2(dj, dNames, jNames),
                    cSum = renormalize(c, cNames);

            return new RestrictedModel<>(new DJDependentVDJCGenesModel(v, dj, c),
                    vSum * djSum * cSum);
        } else if (original instanceof DJCDependentVDJCGenesModel) {
            DJCDependentVDJCGenesModel mdl = (DJCDependentVDJCGenesModel) original;

            Map<String, Double> v = new HashMap<>(mdl.v),
                    djc = new HashMap<>(mdl.djc);

            double vSum = renormalize(v, vNames),
                    djcSum = renormalize3(djc, dNames, jNames, cNames);

            return new RestrictedModel<>(new DJCDependentVDJCGenesModel(v, djc),
                    vSum * djcSum);
        } else {
            throw new IllegalArgumentException("Can't restrict a generic VDJC recombination model. " +
                    "The method is only applicable to 'v+d+j+c', 'v+dj+c' and 'v+djc' models.");
        }
    }

    private static double renormalize2(Map<String, Double> p, Set<String> names1, Set<String> names2) {
        if (names1.isEmpty() && names2.isEmpty()) {
            return 1.0;
        }

        if (names1.isEmpty()) {
            for (String name : p.keySet()) {
                names1.add(name.split("\\|")[0]);
            }
        }

        if (names2.isEmpty()) {
            for (String name : p.keySet()) {
                names2.add(name.split("\\|")[1]);
            }
        }

        return renormalize(p, combinations(names1, names2));
    }

    private static double renormalize3(Map<String, Double> p, Set<String> names1, Set<String> names2, Set<String> names3) {
        // Ugly - yep :)
        if (names1.isEmpty() && names2.isEmpty() && names3.isEmpty()) {
            return 1.0;
        }

        if (names1.isEmpty()) {
            for (String name : p.keySet()) {
                names1.add(name.split("\\|")[0]);
            }
        }

        if (names2.isEmpty()) {
            for (String name : p.keySet()) {
                names2.add(name.split("\\|")[1]);
            }
        }

        if (names3.isEmpty()) {
            for (String name : p.keySet()) {
                names3.add(name.split("\\|")[2]);
            }
        }

        return renormalize(p, combinations(combinations(names1, names2), names3));
    }

    private static Set<String> combinations(Set<String> names1, Set<String> names2) {
        Set<String> combinations = new HashSet<>();

        for (String name1 : names1) {
            for (String name2 : names2) {
                combinations.add(name1 + "|" + name2);
            }
        }

        return combinations;
    }

    private static double renormalize(Map<String, Double> p, Set<String> names) {
        if (names.isEmpty()) {
            return 1.0;
        }

        double sum = 0.0;

        p.keySet().retainAll(names);

        for (double x : p.values()) {
            sum += x;
        }

        for (String name : names) {
            p.put(name, p.get(name) / sum);
        }

        return sum;
    }
}
