package io.repseq.gen;

import com.milaboratory.test.TestUtil;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import org.junit.Test;

import static org.junit.Assert.*;

public class VDJCGenesTest {
    @Test
    public void serializationDeserializationTest() throws Exception {
        VDJCLibrary library = VDJCLibraryRegistry
                .getDefaultLibrary("hs");
        VDJCGene v = library.getSafe("TRBV12-3*00");
        VDJCGene d = library.getSafe("TRBD1*00");
        VDJCGene j = library.getSafe("TRBJ1-2*00");
        VDJCGene c = library.getSafe("TRBC1*00");
        TestUtil.assertJson(new VDJCGenes(v, d, j, c));
        TestUtil.assertJson(new VDJCGenes(v, null, j, c));
    }
}