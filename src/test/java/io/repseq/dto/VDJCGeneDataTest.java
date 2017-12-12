package io.repseq.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.BaseSequence;
import io.repseq.core.Chains;
import io.repseq.core.GeneType;
import io.repseq.core.ReferencePoint;
import org.junit.Test;

import java.util.List;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VDJCGeneDataTest {
    @Test
    public void testSerialization1() throws Exception {
        VDJCGeneData gene = new VDJCGeneData(new BaseSequence("embedded://testseq"),
                "TRBV1", GeneType.Variable,
                true, Chains.TRB, new TreeMap<String, List<String>>(), new TreeMap<ReferencePoint, Long>());
        gene.addMetaValue("key", "val");

        String s1 = GlobalObjectMappers.toOneLine(gene);
        JsonNode n1 = GlobalObjectMappers.ONE_LINE.readTree(s1);
        assertTrue(n1.get("meta").get("key").isValueNode());
        assertEquals(gene, GlobalObjectMappers.ONE_LINE.readValue(s1, VDJCGeneData.class));

        gene.addMetaValue("key", "val1");
        String s2 = GlobalObjectMappers.toOneLine(gene);
        JsonNode n2 = GlobalObjectMappers.ONE_LINE.readTree(s2);
        assertFalse(n2.get("meta").get("key").isValueNode());
        assertEquals(gene, GlobalObjectMappers.ONE_LINE.readValue(s2, VDJCGeneData.class));
    }
}