package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.core.GeneFeature;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import org.apache.commons.io.Charsets;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(params.getOutput(), false), Charsets.UTF_8))) {
            writer.write("Gene\tChains\tFeature\tStart\tStop\tSource\tSequence\n");
            for (VDJCLibrary lib : reg.getLoadedLibraries()) {
                if (taxonFilter != null && taxonFilter != lib.getTaxonId())
                    continue;

                Map<String, GeneFeature> geneFeatureMap = params.getGeneFeatureMap();

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

                    for (String featureName : geneFeatureMap.keySet()) {
                        GeneFeature geneFeature = geneFeatureMap.get(featureName);
                        NucleotideSequence featureSequence = gene.getFeature(geneFeature);

                        if (featureSequence == null)
                            continue;

                        Long start = gene.getData().getAnchorPoints().get(geneFeature.getFirstPoint());
                        Long end = gene.getData().getAnchorPoints().get(geneFeature.getLastPoint());

                        NucleotideSequence nSequence = gene.getFeature(geneFeature);
                        StringBuilder chainString = new StringBuilder();
                        Iterator<String> it = gene.getChains().iterator();
                        while (it.hasNext())
                        {
                            if (chainString.length() > 0)
                            {
                                chainString.append(",");
                            }
                            chainString.append(it.next());
                        }

                        //NOTE: both coordinates from the library are 0-based, but end is exclusive (so essentially 1-based inclusive).  Report both as 1-based.
                        String delim = "";
                        List<String> tokens = Arrays.asList(gene.getGeneName(), chainString.toString(), featureName, (start == null ? "" : String.valueOf(start + 1)), (end == null ? "" : String.valueOf(end)), gene.getData().getBaseSequence().getOrigin().toString(), nSequence.toString());
                        for (String t : tokens){
                            writer.write(delim + t);
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

    @Parameters(commandDescription = "Export genes region coordinates to TSV file.  All coordinated are 1-based.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "input_library.json|default [output.txt]")
        public List<String> parameters;

        @Parameter(description = "Taxon id (filter multi-library file to leave single library for specified taxon id)",
                names = {"-t", "--taxon-id"})
        public Long taxonId;

        @Parameter(description = "Species name, used in the same way as --taxon-id.",
                names = {"-s", "--species"}, required = false)
        public String species = null;

        @Parameter(description = "Chain pattern, regexp string, all genes with matching chain record will be exported.",
                names = {"-c", "--chain"})
        public String chain;

        @Parameter(description = "Gene name pattern, regexp string, all genes with matching gene name will be exported.",
                names = {"-n", "--name"})
        public String name;

        @Parameter(description = "Gene feature(s) to export (e.g. VRegion, JRegion, VTranscript, etc...).  Separate multiple regions with commas.",
                names = {"-g", "--gene-feature"}, required = true)
        public String features;

        public Map<String, GeneFeature> getGeneFeatureMap() {
            Map<String, GeneFeature> ret = new HashMap<>();
            for (String f : features.split(",")) {
                GeneFeature gf = GeneFeature.parse(f);
                if (gf.size() > 1){
                    System.err.println("Only simple features are supported, skipping: " + f);
                    continue;
                }
                ret.put(f, gf);
            }

            return ret;
        }

        public String getInput() {
            return parameters.get(0);
        }

        public String getOutput() {
            return parameters.size() == 1 ? "." : parameters.get(1);
        }

        @Override
        protected List<String> getOutputFiles() {
            return Collections.singletonList(getOutput());
        }
    }
}
