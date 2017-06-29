package io.repseq.gen.dist;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.test.TestUtil;
import io.repseq.core.VDJCLibraryId;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.gen.GClone;
import io.repseq.gen.GGene;
import io.repseq.gen.VDJTrimming;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class BasicGCloneModelTest {
    public static final class MapBuilder<K, V> {
        final HashMap<K, V> map = new HashMap<>();

        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public HashMap<K, V> get() {
            return map;
        }
    }

    public static <K, V> MapBuilder<K, V> b(K key, V value) {
        return new MapBuilder<K, V>().put(key, value);
    }

    @Test
    public void test1() throws Exception {
        GGeneModel geneModel = new BasicGGeneModel(
                new IndependentVDJCGenesModel(
                        b("TRBV12-2*00", 1.0).put("TRBV12-3*00", 0.0).get(),
                        b("TRBD1*00", 1.0).put("TRBD2*00", 0.0).get(),
                        b("TRBJ1-2*00", 1.0).put("TRBJ1-3*00", 0.0).get(),
                        b("TRBC1*00", 1.0).put("TRBC2*00", 0.0).get()
                ),
                new IndependentVDJTrimmingModel(
                        new CommonCategoricalGeneTrimmingModel(b(-1, 1.0).get()),
                        new CommonCategoricalDGeneTrimmingModel(b("-2|-3", 1.0).get()),
                        new CommonCategoricalGeneTrimmingModel(b(-4, 1.0).get())),
                new FixedInsertModel(new NucleotideSequence("ATTA")),
                new FixedInsertModel(new NucleotideSequence("GACA"))
        );
        BasicGCloneModel model = new BasicGCloneModel(new VDJCLibraryId("default", 9606),
                new FixedRealModel(1.0), b("TRB", geneModel).get());

        TestUtil.assertJson(model);

        GCloneGenerator gen = model.create(new Well19937c(123), VDJCLibraryRegistry.getDefault());
        GClone clone = gen.get();

        TestUtil.assertJson(clone);
        
        GGene trb = clone.genes.get("TRB");
        assertEquals(trb.vdjcGenes.v.getName(), "TRBV12-2*00");
        assertEquals(trb.vdjcGenes.d.getName(), "TRBD1*00");
        assertEquals(trb.vdjcGenes.j.getName(), "TRBJ1-2*00");
        assertEquals(trb.vdjcGenes.c.getName(), "TRBC1*00");
        assertEquals(new VDJTrimming(-1, -4, -2, -3), trb.vdjTrimming);
        assertEquals(new NucleotideSequence("ATTA"), trb.vInsert);
        assertEquals(new NucleotideSequence("GACA"), trb.djInsert);
    }
}