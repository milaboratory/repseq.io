package io.repseq.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.milaboratory.cli.ActionParametersWithOutput;
import io.repseq.core.GeneFeature;
import io.repseq.core.GeneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class FromFastaParametersAbstract extends ActionParametersWithOutput {
    public List<String> parameters = new ArrayList<>();

    @Parameter(description = "input_padded.fasta [output.fasta] output.json[.gz]")
    public List<String> getParameters() {
        return parameters;
    }

    @Parameter(description = "Padding character",
            names = {"-p", "--padding-character"})
    public char paddingCharacter = '.';

    @Parameter(description = "Gene type (V/D/J/C)",
            names = {"-g", "--gene-type"},
            required = true)
    public String geneType;

    @Parameter(description = "Ignore duplicate genes",
            names = {"-i", "--ignore-duplicates"})
    public Boolean ignoreDuplicates;

    @Parameter(description = "Species names (can be used multiple times)",
            names = {"-s", "--species-name"})
    public List<String> speciesNames = new ArrayList<>();

    @Parameter(description = "Gene name index (0-based) in `|`-separated FASTA description line (e.g. 1 for IMGT files).",
            names = {"-n", "--name-index"},
            required = true)
    public int nameIndex;

    @Parameter(description = "Functionality mark index (0-based) in `|`-separated FASTA description line " +
            "(e.g. 3 for IMGT files). If this option is omitted, all genes are considered functional.",
            names = {"-j", "--functionality-index"})
    public Integer functionalityIndex;

    @Parameter(description = "Functionality regexp, gene is considered functional if field defined by -j / " +
            "--functionality-index parameter matches this expression.",
            names = {"--functionality-regexp"})
    public String functionalityRegexp = "[\\(\\[]?[Ff].?";

    @Parameter(description = "Chain.",
            names = {"-c", "--chain"},
            required = true)
    public String chain;

    @Parameter(description = "Taxon id",
            names = {"-t", "--taxon-id"},
            required = true)
    public Long taxonId;

    @Parameter(description = "Defines gene feature which sequecnes are contained in the file (e.g. VRegion, " +
            "VGene, JRegion etc..).",
            names = {"--gene-feature"})
    public String geneFeature;

    @DynamicParameter(names = "-P", description = "Positions of anchor points in padded file. To define position " +
            "relative to the end of sequence " +
            "use negative values: -1 = sequence end, -2 = last but one letter. " +
            "Example: -PFR1Begin=0 -PVEnd=-1 , equivalent of --gene-feature VRegion")
    public Map<String, String> points = new HashMap<>();

    @DynamicParameter(names = "-L", description = "Amino-acid pattern of anchor point. Have higher priority than " +
            "-P for the same anchor point.")
    public Map<String, String> patterns = new HashMap<>();

    public boolean getIgnoreDuplicates() {
        return ignoreDuplicates != null && ignoreDuplicates;
    }

    public String getInput() {
        return parameters.get(0);
    }

    public String getOutputFasta() {
        if (parameters.size() == 2)
            return null;
        else
            return parameters.get(1);
    }

    public String getOutputJSON() {
        if (parameters.size() == 2)
            return parameters.get(1);
        else
            return parameters.get(2);
    }

    public abstract boolean doEmbedSequences();

    public GeneFeature getGeneFeature() {
        return GeneFeature.parse(geneFeature);
    }

    public GeneType getGeneType() {
        return GeneType.fromChar(geneType.charAt(0));
    }

    public Pattern getFunctionalityRegexp() {
        return Pattern.compile(functionalityRegexp);
    }

    @Override
    protected List<String> getOutputFiles() {
        return parameters.subList(1, 3);
    }

    @Override
    public void validate() {
        if (parameters.size() < 2 || parameters.size() > 3)
            throw new ParameterException("Wrong number of arguments.");
        if (geneFeature != null && !points.isEmpty())
            throw new ParameterException("-P... and --gene-feature are mutually exclusive.");
    }
}
