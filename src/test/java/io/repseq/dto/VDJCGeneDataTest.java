package io.repseq.dto;

import com.milaboratory.core.Range;
import com.milaboratory.test.TestUtil;
import io.repseq.reference.ReferencePoint;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

public class VDJCGeneDataTest {
    @Test
    public void test1() throws Exception {
        BaseSequenceData seq = new BaseSequenceData(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), new Range[]{}, null);
        HashSet<String> chains = new HashSet<>();
        chains.add("TRA");
        chains.add("TRB");
        HashMap<ReferencePoint, Long> referencePoints = new HashMap<>();
        referencePoints.put(ReferencePoint.V5UTREnd, 123L);
        referencePoints.put(ReferencePoint.CDR3Begin, 189L);
        VDJCGeneData gene = new VDJCGeneData(seq, "TRBV12-3*01", true, chains, referencePoints);
        TestUtil.assertJson(gene, true);
    }
}