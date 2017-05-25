package io.repseq.gen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import org.apache.commons.io.output.CloseShieldOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public final class GRepertoireWriter implements AutoCloseable {
    final OutputStream os;
    final ObjectWriter writer;

    public GRepertoireWriter(OutputStream os, VDJCLibrary library) throws IOException {
        this.os = os;
        this.writer = GlobalObjectMappers.ONE_LINE.writerFor(new TypeReference<GClone>() {
        }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library);
        os.write(GlobalObjectMappers.ONE_LINE.writeValueAsString(library.getLibraryId()).getBytes());
        os.write('\n');
    }

    public void write(GClone clone) throws IOException {
        writer.writeValue(new CloseShieldOutputStream(os), clone);
        os.write('\n');
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
