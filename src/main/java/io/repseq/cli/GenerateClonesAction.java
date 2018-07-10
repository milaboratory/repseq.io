package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.GeneFeature;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.gen.GClone;
import io.repseq.gen.GGene;
import io.repseq.gen.dist.GCloneGenerator;
import io.repseq.gen.dist.GCloneModel;
import io.repseq.gen.dist.GModels;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.math3.random.Well19937c;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;

public class GenerateClonesAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        GCloneModel model = GModels.getGCloneModelByName(params.getModelName());
        GCloneGenerator generator = model.create(new Well19937c(params.getSeed()),
                VDJCLibraryRegistry.getDefault());
        VDJCLibrary library = VDJCLibraryRegistry.getDefault().getLibrary(model.libraryId());
        try (BufferedOutputStream s = new BufferedOutputStream(
                params.getOutput().equals(".") ? System.out : new FileOutputStream(params.getOutput()),
                128 * 1024)) {
            s.write(GlobalObjectMappers.toOneLine(model.libraryId()).getBytes());
            s.write('\n');
            ObjectWriter writer = GlobalObjectMappers.ONE_LINE.writerFor(new TypeReference<GClone>() {
            }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library);
            OUTER:
            for (int i = 0; i < params.numberOfClones; i++) {
                GClone clone = generator.sample();
                for (GGene g : clone.genes.values()) {
                    NucleotideSequence cdr3 = g.getFeature(GeneFeature.CDR3);
                    if (params.isInFrame())
                        if (cdr3.size() % 3 != 0) {
                            --i;
                            continue OUTER;
                        }
                    if (params.isNoStops())
                        if (AminoAcidSequence.translateFromCenter(cdr3).containStops()) {
                            --i;
                            continue OUTER;
                        }
                }
                writer.writeValue(new CloseShieldOutputStream(s), clone);
                s.write('\n');
            }
        }
    }

    @Override
    public String command() {
        return "generateClones";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Generate synthetic clonotypes, and write in in jclns format.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "model_name|model_file_name [output.jclns]")
        public List<String> parameters;

        @Parameter(description = "Number of clones to generate.", names = {"-c", "--number-of-clones"}, required = true)
        public long numberOfClones;

        @Parameter(description = "Random generator seed (0 to use current time as random seed).", names = {"-s", "--seed"})
        public Long seed;

        @Parameter(description = "In-frame clones only.",
                names = {"-a", "--in-frame"})
        public boolean inFrame = false;

        @Parameter(description = "Output clones without stop codons in CDR3 (valid only with -a / --in-frame).",
                names = {"-b", "--no-stops"})
        public boolean noStops = false;

        public long getSeed() {
            if (seed == null)
                return System.nanoTime();
            return seed;
        }

        public String getModelName() {
            return parameters.get(0);
        }

        public String getOutput() {
            return parameters.size() == 1 ? "." : parameters.get(1);
        }

        @Override
        protected List<String> getOutputFiles() {
            return Collections.singletonList(getOutput());
        }

        public boolean isInFrame() {
            return inFrame;
        }

        public boolean isNoStops() {
            return noStops;
        }

        @Override
        public void validate() {
            if (parameters.size() == 0 || parameters.size() > 2)
                throw new ParameterException("Wring number of parameters.");
            if (isNoStops() && !isInFrame())
                throw new ParameterException("-b / --no-stops allowed only with -a / --in-frame.");
        }
    }

}
