package io.repseq.cli;

import cc.redberry.pipe.CUtils;
import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.core.io.sequence.fasta.FastaRecord;
import com.milaboratory.core.io.sequence.fasta.FastaWriter;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.Chains;
import io.repseq.core.GeneFeature;
import io.repseq.core.VDJCGene;
import io.repseq.gen.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.repseq.cli.CLIUtils.createBufferedReader;
import static io.repseq.cli.CLIUtils.createSingleFastaWriter;

public class ExportCloneSequencesAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        Chains chains = params.getChains();
        GeneFeature geneFeature = params.getGeneFeature();
        List<DescriptionExtractor> extractors = params.getExtractors();
        try (GRepertoireReader input = new GRepertoireReader(createBufferedReader(params.getInput()));
             FastaWriter<NucleotideSequence> output = createSingleFastaWriter(params.getOutput())) {
            ObjectWriter writer = GlobalObjectMappers.ONE_LINE.writerFor(new TypeReference<GClone>() {
            }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, input.getLibrary());
            long i = 0;
            for (GClone clone : CUtils.it(input)) {
                int f = params.factor == null ? 1 : (int) Math.round(clone.abundance * params.factor);
                for (int j = 0; j < f; j++)
                    for (Map.Entry<String, GGene> e : clone.genes.entrySet())
                        if (chains.contains(e.getKey())) {
                            StringBuilder descriptionLine = new StringBuilder(e.getKey() + "|" + writer.writeValueAsString(clone));
                            for (DescriptionExtractor extractor : extractors)
                                descriptionLine.append("|").append(extractor.extract(clone, e.getValue()));
                            output.write(new FastaRecord<>(i++, descriptionLine.toString(), e.getValue().getFeature(geneFeature)));
                        }
            }
        }
    }

    @Override
    public String command() {
        return "exportCloneSequence";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Normalize clone abundances in jclns file.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "[input.jclns [output.jclns]]")
        public List<String> parameters = new ArrayList<>();

        @Parameter(description = "Repeat each clonal sequence round(f*clone.abundance) times, " +
                "where round means mathematical rounding of non-integer numbers.",
                names = {"-q", "--abundance-factor"})
        public Double factor;

        @Parameter(description = "Which chains to export",
                names = {"-c", "--chain"})
        public String chains = "ALL";

        @Parameter(description = "Gene feature to export (e.g. CDR3, VDJRegion, VDJTranscript, VDJTranscript+CExon1 etc...)",
                names = {"-g", "--gene-feature"}, required = true)
        public String feature;

        @Parameter(description = "Add description fields to fasta header (available values NFeature[gene_feature], AAFeature[gene_feature] - for current gene," +
                "NFeature[chain,gene_feature], AAFeature[chain,gene_feature] - for multi-gene clones). Example: NFeature[CDR3], AAFeature[FR3]", names = {"-d", "--add-description"})
        public List<String> descriptionFields = new ArrayList<>();

        public GeneFeature getGeneFeature() {
            return GeneFeature.parse(feature);
        }

        public Chains getChains() {
            return Chains.parse(this.chains);
        }

        public String getInput() {
            return parameters.size() == 0 ? "." : parameters.get(0);
        }

        public String getOutput() {
            return parameters.size() <= 1 ? "." : parameters.get(1);
        }

        public List<DescriptionExtractor> getExtractors() {
            List<DescriptionExtractor> extractors = new ArrayList<>(descriptionFields.size());
            for (String descriptorStr : descriptionFields)
                extractors.add(parseExtractor(descriptorStr));
            return extractors;
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

    private static Pattern featureGene = Pattern.compile("^(N|AA)Feature\\[(.*)\\]$");
    private static Pattern featureClone = Pattern.compile("^(N|AA)Feature\\[(.*),(.*)\\]$");

    public static DescriptionExtractor parseExtractor(String str) {
        Matcher matcher = featureGene.matcher(str);
        if (matcher.matches())
            return new DescriptionExtractorImpl(GeneFeature.parse(matcher.group(2)), null, matcher.group(1).equals("AA"));
        matcher = featureClone.matcher(str);
        if (matcher.matches())
            return new DescriptionExtractorImpl(GeneFeature.parse(matcher.group(3)), matcher.group(2), matcher.group(1).equals("AA"));
        throw new IllegalArgumentException("Can't parse description extractor: " + str);
    }

    private interface DescriptionExtractor {
        String extract(GClone clone, GGene gene);
    }

    private static final class DescriptionExtractorImpl implements DescriptionExtractor {
        final GeneFeature geneFeature;
        final String chain;
        final boolean aa;

        public DescriptionExtractorImpl(GeneFeature geneFeature, String chain, boolean aa) {
            this.geneFeature = geneFeature;
            this.chain = chain;
            this.aa = aa;
        }

        @Override
        public String extract(GClone clone, GGene gene) {
            if (chain != null)
                gene = clone.genes.get(chain);
            if (aa)
                return AminoAcidSequence.translate(gene.getFeature(geneFeature), gene.getPartitioning().getTranslationParameters(geneFeature)).toString();
            else
                return gene.getFeature(geneFeature).toString();
        }
    }
}
