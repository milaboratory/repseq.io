package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.SequenceProviderIndexOutOfBoundsException;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.dto.KnownSequenceFragmentData;
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCLibraryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompileAction implements Action {
    private static final Logger log = LoggerFactory.getLogger(CompileAction.class);
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        compile(Paths.get(params.getInput()), Paths.get(params.getOutput()), params.surroundings);
    }

    @Override
    public String command() {
        return "compile";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    public static void compile(Path source, Path destination, int surroundings) throws IOException {
        VDJCLibraryRegistry.resetDefaultRegistry();

        VDJCLibraryRegistry reg = VDJCLibraryRegistry.getDefault();
        reg.registerLibraries(source, "lib");

        List<VDJCLibraryData> result = new ArrayList<>();

        for (VDJCLibrary lib : reg.getLoadedLibraries()) {
            VDJCDataUtils.FragmentsBuilder fragmentsBuilder = new VDJCDataUtils.FragmentsBuilder();

            for (KnownSequenceFragmentData fragment : lib.getData().getSequenceFragments())
                fragmentsBuilder.addRegion(fragment);

            for (VDJCGene gene : lib.getGenes()) {
                if (!gene.getData().getBaseSequence().isPureOriginalSequence())
                    throw new IllegalArgumentException("Don't support mutated sequences yet.");
                URI uri = gene.getData().getBaseSequence().getOrigin();
                Range region = gene.getPartitioning().getContainingRegion();
                region = region.expand(surroundings);
                NucleotideSequence seq;
                try {
                    seq = gene.getSequenceProvider().getRegion(region);
                } catch (SequenceProviderIndexOutOfBoundsException e) {
                    region = e.getAvailableRange();
                    if (region == null)
                        throw new IllegalArgumentException("Wrong anchor points for " + gene.getName() + " ?");
                    seq = gene.getSequenceProvider().getRegion(region);
                }
                fragmentsBuilder.addRegion(uri, region, seq);
            }
            result.add(new VDJCLibraryData(lib.getTaxonId(), lib.getData().getSpeciesNames(),
                    lib.getData().getGenes(), lib.getData().getNotes(), fragmentsBuilder.getFragments()));
        }

        VDJCDataUtils.writeToFile(result, destination, true);

        log.info("{} compiled successfully.", source);
    }

    @Parameters(commandDescription = "Compile a library into self-contained compiled library file, by embedding " +
            "sequence information into \"sequenceFragments\" section.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "input.json[.gz] output.json[.gz]", arity = 2)
        public List<String> parameters;

        @Parameter(description = "Length of surrounding sequences to include into library. Number of upstream and " +
                "downstream nucleotides around V/D/J/C segments to embed into output library's \"sequenceFragments\" " +
                "section. More nucleotides will be included, more surrounding sequences will be possible to request " +
                "using gene features with offset (like JRegion(-12, +3)), at the same time size of output file will be " +
                "greater.",
                names = {"-s", "--surrounding"})
        public int surroundings = 30;

        public String getInput() {
            return parameters.get(0);
        }

        public String getOutput() {
            return parameters.get(1);
        }

        @Override
        protected List<String> getOutputFiles() {
            return Collections.singletonList(getOutput());
        }
    }
}
