package io.repseq.cli;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPortCloseable;
import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.core.Range;
import com.milaboratory.core.io.sequence.fasta.FastaReader;
import com.milaboratory.core.io.sequence.fasta.FastaWriter;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.TranslationParameters;
import io.repseq.core.*;
import io.repseq.dto.*;
import io.repseq.util.StringWithMapping;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromFastaAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        Pattern functionalityRegexp = params.getFunctionalityRegexp();
        GeneType geneType = params.getGeneType();

        Map<String, VDJCGeneData> genes = new HashMap<>();

        Path libraryPath = Paths.get(params.getOutputJSON()).toAbsolutePath();

        // Parsing -P or --gene-feature parameters
        Map<ReferencePoint, Integer> points = new HashMap<>();
        if (params.geneFeature != null) {
            GeneFeature gf = params.getGeneFeature();
            points.put(gf.getFirstPoint(), 0);
            points.put(gf.getLastPoint(), -1);
        } else
            for (Map.Entry<String, String> p : params.points.entrySet()) {
                ReferencePoint anchorPoint = ReferencePoint.getPointByName(p.getKey());
                if (anchorPoint == null)
                    throw new IllegalArgumentException("Unknown anchor point: " + p.getKey());
                if (anchorPoint.getGeneType() != null && anchorPoint.getGeneType() != geneType)
                    throw new IllegalArgumentException("Incompatible anchor point and gene type: " + anchorPoint + " / " + geneType);
                int position = Integer.decode(p.getValue());
                points.put(anchorPoint, position);
            }

        try (FastaReader reader = new FastaReader<>(params.getInput(), null);
             SequenceStorage storage = params.getOutputFasta() == null ?
                     new EmbeddedWriter() :
                     new FastaSequenceStorage(libraryPath, Paths.get(params.getOutputFasta()).toAbsolutePath())
        ) {
            for (FastaReader.RawFastaRecord record : CUtils.it((OutputPortCloseable<FastaReader.RawFastaRecord>) reader.asRawRecordsPort())) {
                StringWithMapping swm = StringWithMapping.removeSymbol(record.sequence, params.paddingCharacter);

                NucleotideSequence seq = new NucleotideSequence(swm.getModifiedString());

                if (seq.containsWildcards()) {
                    System.out.println("Sequence dropped because contain wildcards: " + record.description);
                    continue;
                }

                String[] fields = record.description.split("\\|");

                EnumSet<GeneTag> tags = EnumSet.noneOf(GeneTag.class);

                String geneName = fields[params.nameIndex];

                boolean functionality = true;

                if (params.functionalityIndex != null)
                    functionality = functionalityRegexp.matcher(fields[params.functionalityIndex]).matches();

                SortedMap<ReferencePoint, Long> anchorPoints = new TreeMap<>();

                for (Map.Entry<String, String> p : params.patterns.entrySet()) {
                    ReferencePoint anchorPoint = ReferencePoint.getPointByName(p.getKey());

                    if (anchorPoint == null)
                        throw new IllegalArgumentException("Unknown anchor point: " + p.getKey());

                    if (anchorPoint.getGeneType() != null && anchorPoint.getGeneType() != geneType)
                        throw new IllegalArgumentException("Incompatible anchor point and gene type: " + anchorPoint + " / " + geneType);

                    Pattern pattern = Pattern.compile(p.getValue());

                    int position = -1;

                    for (boolean stops : new boolean[]{false, true})
                        for (int f = 0; f < 3; f++) {
                            if (position != -1)
                                continue;
                            TranslationParameters tp = TranslationParameters.withoutIncompleteCodon(f);
                            AminoAcidSequence aa = AminoAcidSequence.translate(seq, tp);
                            if (!stops && aa.containStops())
                                continue;
                            String str = aa.toString();
                            Matcher matcher = pattern.matcher(str);
                            if (matcher.find()) {
                                int aaPosition = matcher.start(1);
                                position = AminoAcidSequence.convertAAPositionToNt(aaPosition, seq.size(), tp);
                            }
                        }

                    if (position == -1)
                        continue;

                    anchorPoints.put(anchorPoint, (long) position);
                }

                for (Map.Entry<ReferencePoint, Integer> p : points.entrySet()) {
                    // If point was already found using amino acid pattern, leave it as is
                    // AA patterns have priority over positional anchor points
                    if (anchorPoints.containsKey(p.getKey()))
                        continue;

                    // Converting position using
                    int position = swm.convertPosition(p.getValue());

                    // Can't be converted (e.g. position of padding symbol) skipping
                    if (position == -1)
                        continue;

                    anchorPoints.put(p.getKey(), (long) position);
                }

                if (genes.containsKey(geneName)) {
                    if (params.getIgnoreDuplicates()) {
                        System.out.println("Ignored: Duplicate records for " + geneName);
                        continue;
                    } else
                        throw new IllegalArgumentException("Duplicate records for " + geneName);
                }

                BaseSequence baseSequence = storage.storeSequence(seq, geneName, record.description);

                VDJCGeneData gene = new VDJCGeneData(baseSequence,
                        geneName, geneType, functionality, new Chains(params.chain), record.description,
                        tags, anchorPoints);

                genes.put(geneName, gene);
            }

            VDJCLibraryData library = new VDJCLibraryData(params.taxonId, params.speciesNames, new ArrayList<>(genes.values()),
                    Arrays.asList(new VDJCLibraryNote(VDJCLibraryNoteType.Comment, "Imported from: " + params.getInput())),
                    storage.getBase());

            VDJCDataUtils.writeToFile(new VDJCLibraryData[]{library}, params.getOutputJSON(), false);
        }
    }

    interface SequenceStorage extends AutoCloseable {
        BaseSequence storeSequence(NucleotideSequence sequence, String geneId, String fullDescription);

        List<KnownSequenceFragmentData> getBase();
    }

    public final class FastaSequenceStorage implements SequenceStorage {
        final FastaWriter<NucleotideSequence> writer;
        final String addressPrefix;

        public FastaSequenceStorage(Path libraryPath, Path fastaPath) throws FileNotFoundException {
            String relativeFastaPath = libraryPath.toAbsolutePath().getParent().relativize(fastaPath.toAbsolutePath()).toString();
            this.writer = new FastaWriter<>(fastaPath.toFile());
            this.addressPrefix = "file://" + relativeFastaPath + "#";
        }

        @Override
        public List<KnownSequenceFragmentData> getBase() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public BaseSequence storeSequence(NucleotideSequence sequence, String geneId, String fullDescription) {
            writer.write(fullDescription, sequence);
            return new BaseSequence(addressPrefix + geneId);
        }

        @Override
        public void close() throws Exception {
            writer.close();
        }
    }

    public final class EmbeddedWriter implements SequenceStorage {
        final String addressPrefix = "embedded://" + UUID.randomUUID().toString().replace("-", "") + "/";
        final List<KnownSequenceFragmentData> base = new ArrayList<>();

        public List<KnownSequenceFragmentData> getBase() {
            return base;
        }

        @Override
        public BaseSequence storeSequence(NucleotideSequence sequence, String geneId, String fullDescription) {
            String recordId = addressPrefix + geneId;
            base.add(new KnownSequenceFragmentData(URI.create(recordId), new Range(0, sequence.size()), sequence));
            return new BaseSequence(recordId);
        }

        @Override
        public void close() throws Exception {
        }
    }

    @Override
    public String command() {
        return "fromFasta";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Converts library from padded fasta file (IMGT-like) to " +
            "json library. This command can operate in two modes (1) if 3 file-parameters are specified, it will create " +
            "separate non-padded fasta and put links inside newly created library pointing to it, (2) if 2 file-parameters " +
            "are specified, create only library file, and embed sequences directly into it. " +
            "To use library generated using mode (1) one need both output files, (see also 'repseqio compile').")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "input_padded.fasta [output.fasta] output.json[.gz]")
        public List<String> parameters;

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

        @Parameter(description = "Gene name index (0-based) in FASTA description line (e.g. 1 for IMGT files).",
                names = {"-n", "--name-index"},
                required = true)
        public int nameIndex;

        @Parameter(description = "Functionality mark index (0-based) in FASTA description line (e.g. 3 for IMGT files).",
                names = {"-j", "--functionality-index"})
        public Integer functionalityIndex;

        @Parameter(description = "Functionality regexp.",
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

        @Parameter(description = "Defines sequences for which gene feature are contained in the file.",
                names = {"--gene-feature"})
        public String geneFeature;

        @DynamicParameter(names = "-P", description = "Positions of anchor points in padded file. To define position " +
                "relative to the end of sequence " +
                "use negative values: -1 = sequence end, -2 = last but one letter. " +
                "Example: -PFR1Begin=0 -PVEnd=-1")
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
}
