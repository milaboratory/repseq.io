/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.repseq.cli;

import cc.redberry.pipe.CUtils;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonGeneratorImpl;
import com.fasterxml.jackson.core.json.WriterBasedJsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import io.repseq.core.VDJCLibrary;
import io.repseq.gen.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

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
        RandomGenerator random = new Well19937c(1232434);
        try (GRepertoireReader input = new GRepertoireReader(createBufferedReader(params.getInput()));
             FastaWriter<NucleotideSequence> output = createSingleFastaWriter(params.getOutput())) {
            List<DescriptionExtractor> extractors = params.getExtractors(input.getLibrary());
            long i = 0;
            for (GClone clone : CUtils.it(input)) {
                int f = params.factor == null ? 1 : randomizedRound(clone.abundance * params.factor, random);
                for (int j = 0; j < f; j++)
                    for (Map.Entry<String, GGene> e : clone.genes.entrySet())
                        if (chains.contains(e.getKey())) {
                            StringBuilder descriptionLine = new StringBuilder("GClone");
                            for (DescriptionExtractor extractor : extractors)
                                descriptionLine.append("|").append(extractor.extract(clone, e.getValue(), e.getKey()));
                            output.write(new FastaRecord<>(i++, descriptionLine.toString(), e.getValue().getFeature(geneFeature)));
                        }
            }
        }
    }

    public static int randomizedRound(double value, RandomGenerator random) {
        if (value < 0)
            throw new IllegalArgumentException("Only positive values are supported.");
        int floor = (int) Math.floor(value);
        if (random.nextDouble() < (value - floor))
            ++floor;
        return floor;
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

        @Parameter(description = "Add description fields to fasta header (available values NFeature[gene_feature], " +
                "AAFeature[gene_feature] - for current gene," +
                "NFeature[chain,gene_feature], AAFeature[chain,gene_feature] - for multi-gene clones, JSONClone, " +
                "JSONGene, JSONClone.field_name, JSONGene.field_name, Chain). Example: NFeature[CDR3], AAFeature[FR3]",
                names = {"-d", "--add-description"})
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

        public List<DescriptionExtractor> getExtractors(VDJCLibrary library) {
            List<DescriptionExtractor> extractors = new ArrayList<>(descriptionFields.size());
            for (String descriptorStr : descriptionFields)
                extractors.add(parseExtractor(descriptorStr, library));
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

    private static Pattern extractorPatternFeatureGene = Pattern.compile("^(N|AA)Feature\\[(.*)\\]$", Pattern.CASE_INSENSITIVE);
    private static Pattern extractorPatternFeatureClone = Pattern.compile("^(N|AA)Feature\\[(.*),(.*)\\]$", Pattern.CASE_INSENSITIVE);
    private static Pattern extractorPatternField = Pattern.compile("^JSON(Gene|Clone)\\.(.*)", Pattern.CASE_INSENSITIVE);

    public static DescriptionExtractor parseExtractor(String str, final VDJCLibrary library) {
        Matcher matcher = extractorPatternFeatureGene.matcher(str);
        if (matcher.matches())
            return new DescriptionExtractorSeq(GeneFeature.parse(matcher.group(2)), null, matcher.group(1).equals("AA"));

        matcher = extractorPatternFeatureClone.matcher(str);
        if (matcher.matches())
            return new DescriptionExtractorSeq(GeneFeature.parse(matcher.group(3)), matcher.group(2), matcher.group(1).equals("AA"));

        if (str.equalsIgnoreCase("JSONClone"))
            return new DescriptionExtractor() {
                final ObjectWriter writer = GlobalObjectMappers.ONE_LINE.writerFor(new TypeReference<GClone>() {
                }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library);

                @Override
                public String extract(GClone clone, GGene gene, String chain) {
                    try {
                        return writer.writeValueAsString(clone);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

        if (str.equalsIgnoreCase("JSONGene"))
            return new DescriptionExtractor() {
                final ObjectWriter writer = GlobalObjectMappers.ONE_LINE.writerFor(new TypeReference<GGene>() {
                }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library);

                @Override
                public String extract(GClone clone, GGene gene, String chain) {
                    try {
                        return writer.writeValueAsString(gene);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

        if (str.equalsIgnoreCase("chain"))
            return new DescriptionExtractor() {
                @Override
                public String extract(GClone clone, GGene gene, String chain) {
                    return chain;
                }
            };

        matcher = extractorPatternField.matcher(str);
        if (matcher.matches()) {
            final boolean isGene = matcher.group(1).equalsIgnoreCase("Gene");
            final String fieldName = matcher.group(2);
            return new DescriptionExtractor() {
                final ObjectWriter writer = isGene ?
                        GlobalObjectMappers.ONE_LINE.writerFor(new TypeReference<GGene>() {
                        }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library) :
                        GlobalObjectMappers.ONE_LINE.writerFor(new TypeReference<GClone>() {
                        }).withAttribute(VDJCGene.JSON_CURRENT_LIBRARY_ATTRIBUTE_KEY, library);

                @Override
                public String extract(GClone clone, GGene gene, String chain) {
                    try {
                        String str = writer.writeValueAsString(isGene ? gene : clone);
                        JsonNode tree = GlobalObjectMappers.ONE_LINE.readTree(str);
                        JsonNode targetNode = tree.get(fieldName);
                        return targetNode == null ? "" : targetNode.asText();
                    } catch (java.io.IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        throw new IllegalArgumentException("Can't parse description extractor: " + str);
    }

    private interface DescriptionExtractor {
        String extract(GClone clone, GGene gene, String chain);
    }

    private static final class DescriptionExtractorSeq implements DescriptionExtractor {
        final GeneFeature geneFeature;
        final String chain;
        final boolean aa;

        public DescriptionExtractorSeq(GeneFeature geneFeature, String chain, boolean aa) {
            this.geneFeature = geneFeature;
            this.chain = chain;
            this.aa = aa;
        }

        @Override
        public String extract(GClone clone, GGene gene, String chain) {
            if (chain != null)
                gene = clone.genes.get(chain);
            if (aa)
                return AminoAcidSequence.translate(gene.getFeature(geneFeature), gene.getPartitioning().getTranslationParameters(geneFeature)).toString();
            else
                return gene.getFeature(geneFeature).toString();
        }
    }
}
