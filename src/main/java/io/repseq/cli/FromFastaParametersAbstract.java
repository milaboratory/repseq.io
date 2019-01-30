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
    public abstract List<String> getParameters();

    @Parameter(description = "Gene type (V/D/J/C)",
            names = {"-g", "--gene-type"},
            required = true)
    public String geneType;

    @Parameter(description = "Ignore duplicate genes",
            names = {"-i", "--ignore-duplicates"})
    public boolean ignoreDuplicates = false;

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

    @DynamicParameter(names = "-P", description = "Positions of anchor points in padded / non-padded file. " +
            "To define position relative to the end of sequence " +
            "use negative values: -1 = sequence end, -2 = last but one letter. " +
            "Example: -PFR1Begin=0 -PVEnd=-1 , equivalent of --gene-feature VRegion")
    public Map<String, String> points = new HashMap<>();

    @DynamicParameter(names = "-L", description = "Amino-acid pattern of anchor point. Have higher priority than " +
            "-P for the same anchor point.")
    public Map<String, String> patterns = new HashMap<>();

    public boolean getIgnoreDuplicates() {
        return ignoreDuplicates;
    }

    public String getInput() {
        return getParameters().get(0);
    }

    public String getOutputFasta() {
        if (getParameters().size() == 2)
            return null;
        else
            return getParameters().get(1);
    }

    public String getOutputJSON() {
        if (getParameters().size() == 2)
            return getParameters().get(1);
        else
            return getParameters().get(2);
    }

    /**
     * Whether to embed sequence information into JSON output library file.
     *
     * If this parameter is false: (1) if {@link #getOutputFasta()} is null, input files will be directly linked inside
     * json library, (2) if {@link #getOutputFasta()} is defined, separate file will be created, and linked inside
     * newly created JSON library.
     */
    public abstract boolean doEmbedSequences();

    public abstract char getPaddingCharacter();

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
        return getParameters().subList(1, 3);
    }

    @Override
    public void validate() {
        if (getParameters().size() < 2 || getParameters().size() > 3)
            throw new ParameterException("Wrong number of arguments.");
        if (geneFeature != null && !points.isEmpty())
            throw new ParameterException("-P... and --gene-feature are mutually exclusive.");
    }
}
