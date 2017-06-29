package io.repseq.gen.prob;

import com.milaboratory.core.sequence.AminoAcidSequence;
import io.repseq.core.GeneFeature;
import io.repseq.core.VDJCGene;
import io.repseq.gen.GClone;
import io.repseq.gen.GGene;

/**
 * Created by mikesh on 6/29/17.
 */
public class VJCdr3aaKey implements CloneKey {
    private final VDJCGene v, j;
    private final AminoAcidSequence cdr3aa;

    public VJCdr3aaKey(GClone gClone, String gene) {
        GGene gGene = gClone.genes.get(gene); // TODO: trb -> enum?
        this.v = gGene.vdjcGenes.v;
        this.j = gGene.vdjcGenes.j;
        this.cdr3aa = AminoAcidSequence.translateFromCenter(gGene.getFeature(GeneFeature.CDR3));
    }

    public VJCdr3aaKey(VDJCGene v, VDJCGene j, AminoAcidSequence cdr3aa) {
        this.v = v;
        this.j = j;
        this.cdr3aa = cdr3aa;
    }

    public VDJCGene getV() {
        return v;
    }

    public VDJCGene getJ() {
        return j;
    }

    public AminoAcidSequence getCdr3aa() {
        return cdr3aa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VJCdr3aaKey that = (VJCdr3aaKey) o;

        if (!v.equals(that.v)) return false;
        if (!j.equals(that.j)) return false;
        return cdr3aa.equals(that.cdr3aa);
    }

    @Override
    public int hashCode() {
        int result = v.hashCode();
        result = 31 * result + j.hashCode();
        result = 31 * result + cdr3aa.hashCode();
        return result;
    }
}
