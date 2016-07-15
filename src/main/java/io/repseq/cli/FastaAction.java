package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.core.io.sequence.fasta.FastaWriter;
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.reference.GeneFeature;

import java.util.List;
import java.util.regex.Pattern;

public class FastaAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        GeneFeature geneFeature = params.getGeneFeature();

        VDJCLibraryRegistry reg = VDJCLibraryRegistry.getDefault();
        reg.registerLibraries(params.getInput());

        Pattern chainPattern = params.chain == null ? null : Pattern.compile(params.chain);
        Pattern namePattern = params.name == null ? null : Pattern.compile(params.name);

        Long taxonFilter = params.taxonId;

        if (taxonFilter == null && params.species != null)
            taxonFilter = reg.resolveSpecies(params.species);

        try (FastaWriter<NucleotideSequence> writer = CLIUtils.createSingleFastaWriter(params.getOutput())) {
            for (VDJCLibrary lib : reg.getLoadedLibraries()) {
                if (taxonFilter != null && taxonFilter != lib.getTaxonId())
                    continue;

                for (VDJCGene gene : lib.getGenes()) {
                    if (chainPattern != null) {
                        boolean y = false;
                        for (String s : gene.getChains())
                            if (y |= chainPattern.matcher(s).matches())
                                break;
                        if (!y)
                            continue;
                    }

                    if (namePattern != null && !namePattern.matcher(gene.getName()).matches())
                        continue;

                    NucleotideSequence featureSequence = gene.getFeature(geneFeature);

                    if (featureSequence == null)
                        continue;

                    writer.write(gene.getName() + "|" + (gene.isFunctional() ? "F" : "P"), featureSequence);
                }
            }
        }
    }

    @Override
    public String command() {
        return "fasta";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    //TODO force option to overwrite output file
    @Parameters(commandDescription = "Export sequences of genes to fasta file.")
    public static final class Params extends ActionParameters {
        @Parameter(description = "input_library.json [output.fasta]")
        public List<String> parameters;

        @Parameter(description = "Taxon id (filter multi-library file to leave single library for specified taxon id)",
                names = {"-t", "--taxon-id"})
        public Long taxonId;

        @Parameter(description = "Species name, used in the same way as --taxon-id.",
                names = {"-s", "--species"})
        public String species;

        @Parameter(description = "Chain pattern, regexp string, all genes with matching chain record will be exported.",
                names = {"-c", "--chain"})
        public String chain;

        @Parameter(description = "Gene name pattern, regexp string, all genes with matching gene name will be exported.",
                names = {"-n", "--name"})
        public String name;

        @Parameter(description = "Gene feature to export (e.g. VRegion, JRegion, VTranscript, etc...)",
                names = {"-g", "--gene-feature"}, required = true)
        public String feature;

        public GeneFeature getGeneFeature() {
            return GeneFeature.parse(feature);
        }

        public String getInput() {
            return parameters.get(0);
        }

        public String getOutput() {
            return parameters.size() == 1 ? "." : parameters.get(1);
        }
    }
}
