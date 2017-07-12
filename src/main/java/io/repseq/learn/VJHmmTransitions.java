package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;

/**
 * Created by mikesh on 7/5/17.
 */
public class VJHmmTransitions {
    private final NucleotideSequence query, vRef, jRef;
    private final double[][] alpha, // V->J transitions
            beta; // J -> V transitions
    private final SegmentTuple segments;
    private final double Pfull;

    public VJHmmTransitions(SegmentTuple segments,
                            NucleotideSequence query,
                            NucleotideSequence vRef,
                            NucleotideSequence jRef,
                            double[][] alpha, double[][] beta) {
        this.segments = segments;
        this.query = query;
        this.vRef = vRef;
        this.jRef = jRef;
        this.alpha = alpha;
        this.beta = beta;

        double p = 0;

        for (int i = 0; i < query.size(); i++) {
            p += alpha[1][i] * beta[1][i];
        }

        this.Pfull = p;
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

    public double getPfull() {
        return Pfull;
    }

    public VDJPartitioning getBestPartitioning() {
        int vEnd = 0, jStart = 0;
        double maxVprob = 0, maxJprob = 0;

        for (int i = 0; i < query.size(); i++) {
            double vProb = beta[0][i] * alpha[0][i],
                    jProb = alpha[1][i] * beta[1][i];
            if (vProb > maxVprob) {
                maxVprob = vProb;
                vEnd = i + 1;
            }
            if (jProb > maxJprob) {
                maxJprob = jProb;
                jStart = i;
            }
        }

        return new VDJPartitioning(vEnd, jStart);
    }

    public VDJPartitioning getBestPartitioning1() {
        int vEnd = 0, jStart = 0;
        double maxProb = 0;

        for (int i = 0; i < query.size(); i++) {
            double vProb = beta[0][i] * alpha[0][i];
            for (int j = i + 1; j < query.size(); j++) {
                double prob = alpha[1][j] * beta[1][j] * vProb;

                if (prob > maxProb) {
                    maxProb = prob;
                    vEnd = i + 1;
                    jStart = j;
                }
            }
        }

        return new VDJPartitioning(vEnd, jStart);
    }
}
