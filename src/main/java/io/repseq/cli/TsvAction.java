package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.cli.CLIUtils.GeneFeatureConverter;
import io.repseq.cli.CLIUtils.GeneFeatureSplitter;
import io.repseq.cli.CLIUtils.GeneFeatureValidator;
import io.repseq.cli.CLIUtils.GeneFeatureWithOriginalName;
import io.repseq.core.GeneFeature;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import org.apache.commons.io.output.CloseShieldOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class TsvAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryRegistry reg = VDJCLibraryRegistry.getDefault();

        if (!"default".equals(params.getInput()))
            reg.registerLibraries(params.getInput());
        else
            reg.loadAllLibraries("default");

        Pattern chainPattern = params.chain == null ? null : Pattern.compile(params.chain);
        Pattern namePattern = params.name == null ? null : Pattern.compile(params.name);

        Long taxonFilter = params.taxonId;

        if (taxonFilter == null && params.species != null)
            taxonFilter = reg.resolveSpecies(params.species);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(params.getOutputStream(), StandardCharsets.UTF_8))) {

            writer.write("Name\tGene\tChains\tFeature\tStart\tStop\tSource\tSequence\n");

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

                    for (GeneFeatureWithOriginalName feature : params.features) {
                        GeneFeature geneFeature = feature.feature;
                        NucleotideSequence featureSequence = gene.getFeature(geneFeature);

                        if (featureSequence == null)
                            continue;

                        // Don't output start and end positions for composite gene features
                        Long start = geneFeature.isComposite() ? null :
                                gene.getData().getAnchorPoints().get(geneFeature.getFirstPoint());
                        Long end = geneFeature.isComposite() ? null :
                                gene.getData().getAnchorPoints().get(geneFeature.getLastPoint());

                        NucleotideSequence nSequence = gene.getFeature(geneFeature);

                        List<String> tokens =
                                Arrays.asList(gene.getData().getName(), gene.getGeneName(),
                                        gene.getChains().toString(), feature.originalName,
                                        // NOTE: both coordinates from the library are 0-based, but end is exclusive
                                        // (so essentially 1-based inclusive). Report both as 1-based.
                                        (start == null ? "" : params.isOneBased() ?
                                                String.valueOf(start + 1) :
                                                String.valueOf(start)),
                                        (end == null ? "" : String.valueOf(end)),
                                        gene.getData().getBaseSequence().getOrigin().toString(),
                                        nSequence.toString());

                        String delim = "";
                        for (String t : tokens) {
                            writer.write(delim);
                            writer.write(t);
                            delim = "\t";
                        }

                        writer.write('\n');
                    }
                }
            }
        }
    }

    @Override
    public String command() {
        return "tsv";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    public final static class NameAndGeneFeature {
        final String name;
        final GeneFeature feature;

        public NameAndGeneFeature(String name, GeneFeature feature) {
            this.name = name;
            this.feature = feature;
        }
    }

    @Parameters(commandDescription = "Export genes region coordinates to TSV file. To output 1-based coordinates add " +
            "`-1` / `--one-based` option.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "input_library.json|default [output.txt]")
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

        @Parameter(description = "Use one-based coordinates instead of zero-based and output inclusive end position.",
                names = {"-1", "--one-based"})
        public Boolean oneBased = false;

        public boolean isOneBased() {
            return oneBased != null && oneBased;
        }

        @Parameter(description = "Gene feature(s) to export (e.g. VRegion, JRegion, VTranscript, etc...). " +
                "To specify several features use this option several times or separate multiple regions with commas.",
                names = {"-g", "--gene-feature"},
                validateWith = GeneFeatureValidator.class,
                splitter = GeneFeatureSplitter.class,
                converter = GeneFeatureConverter.class,
                required = true)
        public List<GeneFeatureWithOriginalName> features;

        public String getInput() {
            return parameters.get(0);
        }

        public OutputStream getOutputStream() throws FileNotFoundException {
            return parameters.size() == 1 ? new CloseShieldOutputStream(System.out) :
                    new FileOutputStream(parameters.get(1), false);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected List<String> getOutputFiles() {
            return parameters.size() == 1 ? Collections.EMPTY_LIST : Collections.singletonList(parameters.get(1));
        }
    }
}
