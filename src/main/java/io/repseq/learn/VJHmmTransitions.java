package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

/**
 * Created by mikesh on 7/5/17.
 */
public class VJHmmTransitions {
    private final NucleotideSequence query, vRef, jRef;
    private final double[][] alpha, // V->J transitions
            beta; // J -> V transitions
    private final double[] i0prob, i1prob;
    private final SegmentTuple segments;

    public VJHmmTransitions(SegmentTuple segments,
                            NucleotideSequence query,
                            NucleotideSequence vRef,
                            NucleotideSequence jRef,
                            double[][] alpha, double[][] beta,
                            double[] i0prob, double[] i1prob) {
        this.segments = segments;
        this.query = query;
        this.vRef = vRef;
        this.jRef = jRef;
        this.alpha = alpha;
        this.beta = beta;
        this.i0prob = i0prob;
        this.i1prob = i1prob;
    }

    public NucleotideSequence getQuery() {
        return query;
    }

    public NucleotideSequence getvRef() {
        return vRef;
    }

    public NucleotideSequence getjRef() {
        return jRef;
    }

    public SegmentTuple getSegments() {
        return segments;
    }

    public double[][] getAlpha() {
        return alpha;
    }

    public double[][] getBeta() {
        return beta;
    }

    public double[] getI0prob() {
        return i0prob;
    }

    public double[] getI1prob() {
        return i1prob;
    }
}
