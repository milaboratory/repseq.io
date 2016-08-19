package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class FilterAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryData[] libs = GlobalObjectMappers.ONE_LINE.readValue(new File(params.getInput()),
                VDJCLibraryData[].class);

        List<VDJCLibraryData> filtered = new ArrayList<>();

        Pattern chainPattern = params.chain == null ? null : Pattern.compile(params.chain);

        boolean filterRecords = (params.chain != null);

        int filteredLibraries = 0;
        int filteredGenes = 0;

        for (VDJCLibraryData lib : libs) {
            if (params.taxonId != null && params.taxonId != lib.getTaxonId())
                continue;

            if (params.species != null && !lib.getSpeciesNames().contains(params.species))
                continue;

            ++filteredLibraries;

            if (!filterRecords) {
                filteredGenes += lib.getGenes().size();
                filtered.add(lib);
            } else {
                List<VDJCGeneData> genes = new ArrayList<>();

                for (VDJCGeneData gene : lib.getGenes()) {
                    if (chainPattern != null) {
                        boolean y = false;
                        for (String s : gene.getChains())
                            if (y |= chainPattern.matcher(s).matches())
                                break;
                        if (!y)
                            continue;
                    }

                    ++filteredGenes;
                    genes.add(gene);
                }

                filtered.add(new VDJCLibraryData(lib, genes));
            }
        }

        System.out.println("Filtered libraries: " + filteredLibraries);
        System.out.println("Filtered genes: " + filteredGenes);

        GlobalObjectMappers.PRETTY.writeValue(new File(params.getOutput()), filtered);
    }

    @Override
    public String command() {
        return "filter";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Filter libraries and library records.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "input_library.json output_library.json", arity = 2)
        public List<String> parameters;

        @Parameter(description = "Taxon id (filter multi-library file to leave single library for specified taxon id)",
                names = {"-t", "--taxon-id"})
        public Long taxonId;

        @Parameter(description = "Species name, used in the same way as --taxon-id.",
                names = {"-s", "--species"})
        public String species;

        @Parameter(description = "Chain pattern, regexp string, all genes with matching chain record will be collected.",
                names = {"-c", "--chain"})
        public String chain;

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
