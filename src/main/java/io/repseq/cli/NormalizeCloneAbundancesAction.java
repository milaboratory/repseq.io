package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.milaboratory.cli.*;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryId;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.gen.GClone;
import io.repseq.gen.GRepertoire;
import io.repseq.gen.GRepertoireReader;
import io.repseq.gen.GRepertoireWriter;
import org.apache.commons.io.output.CloseShieldOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.repseq.cli.CLIUtils.createBufferedOutputStream;
import static io.repseq.cli.CLIUtils.createBufferedReader;

@AllowNoArguments
public class NormalizeCloneAbundancesAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        try (GRepertoireReader input = new GRepertoireReader(createBufferedReader(params.getInput()));
             GRepertoireWriter output = new GRepertoireWriter(createBufferedOutputStream(params.getOutput()), input.getLibrary())) {
            GRepertoire repertoire = input.readeFully();
            for (GClone clone : repertoire.clones)
                output.write(clone.setAbundance(clone.abundance / repertoire.totalAbundance));
        }
    }

    @Override
    public String command() {
        return "normalizeClones";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Normalize clone abundances in jclns file.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "[input.jclns [output.jclns]]")
        public List<String> parameters = new ArrayList<>();

        public String getInput() {
            return parameters.size() == 0 ? "." : parameters.get(0);
        }

        public String getOutput() {
            return parameters.size() <= 1 ? "." : parameters.get(1);
        }

        @Override
        protected List<String> getOutputFiles() {
            return Collections.singletonList(getOutput());
        }

        @Override
        public void validate() {
            if (parameters.size() > 2)
                throw new ParameterException("Wring number of parameters.");
        }
    }
}
