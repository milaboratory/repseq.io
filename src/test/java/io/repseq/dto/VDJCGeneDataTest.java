package io.repseq.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.milaboratory.core.Range;
import io.repseq.core.BaseSequence;
import io.repseq.reference.GeneType;
import io.repseq.reference.ReferencePoint;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

public class VDJCGeneDataTest {
    @Test
    public void test1() throws Exception {
        BaseSequence seq = new BaseSequence(URI.create("file://some_fasta.fasta#24.6jsd21.Tut"), new Range[]{}, null);
        HashSet<String> chains = new HashSet<>();
        chains.add("TRA");
        chains.add("TRB");
        HashMap<ReferencePoint, Long> referencePoints = new HashMap<>();
        referencePoints.put(ReferencePoint.V5UTREnd, 123L);
        referencePoints.put(ReferencePoint.CDR3Begin, 189L);
        VDJCGeneData gene = new VDJCGeneData(seq, "TRBV12-3*01", GeneType.Variable, true, chains, referencePoints);
        ObjectMapper om = new ObjectMapper();
        om.setDefaultPrettyPrinter(new DefaultPrettyPrinter() {
            @Override
            public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
                jg.writeRaw(": ");
            }
        });
        om.enable(SerializationFeature.INDENT_OUTPUT);
        //om.setConfig(om.getSerializationConfig().with(SerializationFeature.INDENT_OUTPUT).withDefaultPrettyPrinter());
        System.out.println(om.writeValueAsString(gene));
        //TestUtil.assertJson(gene, true);
    }

    @Test
    public void test123() throws Exception {
        @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
                getterVisibility = JsonAutoDetect.Visibility.NONE)
        class A {
            String fieldName;
        }
        A a = new A();
        a.fieldName = "fieldValue";
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.setDefaultPrettyPrinter(new DefaultPrettyPrinter1());
        System.out.println(om.writeValueAsString(a));
    }

    public static final class DefaultPrettyPrinter1 extends DefaultPrettyPrinter {
        public DefaultPrettyPrinter1() {
        }

        public DefaultPrettyPrinter1(DefaultPrettyPrinter base) {
            super(base);
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(": ");
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new DefaultPrettyPrinter1(this);
        }
    }
}