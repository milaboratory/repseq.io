package io.repseq.gen.prob;

import io.repseq.gen.dist.VDJCGenesModel;

/**
 * A model for VDJC gene subset, e.g. will only sample TRBV21-1/TRBJ2-7
 */
public class RestrictedVDJCGenesModel {
    private final VDJCGenesModel vdjcGenesModel;
    private final double weight;

    public RestrictedVDJCGenesModel(VDJCGenesModel vdjcGenesModel, double weight) {
        this.vdjcGenesModel = vdjcGenesModel;
        this.weight = weight;
    }

    public VDJCGenesModel getVdjcGenesModel() {
        return vdjcGenesModel;
    }

    public double getWeight() {
        return weight;
    }
}
