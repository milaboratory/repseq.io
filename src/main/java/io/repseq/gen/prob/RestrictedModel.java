package io.repseq.gen.prob;

import io.repseq.gen.dist.Model;

/**
 * Created by mikesh on 6/30/17.
 */
public class RestrictedModel<T extends Model> {
    private final T model;
    private final double weight;

    public RestrictedModel(T model, double weight) {
        this.model = model;
        this.weight = weight;
    }

    public T getModel() {
        return model;
    }

    public double getWeight() {
        return weight;
    }
}
