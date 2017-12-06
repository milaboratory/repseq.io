package io.repseq.cli;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
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

public abstract class FromFastaActionAbstract<P extends FromFastaParametersAbstract> implements Action {
    public P params;

    public FromFastaActionAbstract(P params) {
        this.params = params;
    }

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
                int position = Integer.decode(p.getValue());
                points.put(anchorPoint, position);
            }

        // Check
        for (Map.Entry<ReferencePoint, Integer> entry : points.entrySet())
            if (entry.getKey().getGeneType() != null && entry.getKey().getGeneType() != geneType)
                throw new IllegalArgumentException("Incompatible anchor point and gene type: " + entry.getKey() + " / " + geneType);

        try (FastaReader reader = new FastaReader<>(params.getInput(), null);
             SequenceStorage storage = params.doEmbedSequences() ?
                     new EmbeddedWriter() :
                     params.getOutputFasta() == null ?
                             new ExistingFileWriter(libraryPath, Paths.get(params.getInput()).toAbsolutePath()) :
                             new FastaSequenceStorage(libraryPath, Paths.get(params.getOutputFasta()).toAbsolutePath())
        ) {
            for (FastaReader.RawFastaRecord record : CUtils.it((OutputPortCloseable<FastaReader.RawFastaRecord>)
                    reader.asRawRecordsPort())) {
                StringWithMapping swm = StringWithMapping.removeSymbol(record.sequence, params.getPaddingCharacter());

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

    public final class ExistingFileWriter implements SequenceStorage {
        final String addressPrefix;

        public ExistingFileWriter(Path libraryPath, Path fastaPath) throws FileNotFoundException {
            String relativeFastaPath = libraryPath.toAbsolutePath().getParent()
                    .relativize(fastaPath.toAbsolutePath()).toString();
            this.addressPrefix = "file://" + relativeFastaPath + "#";
        }

        public List<KnownSequenceFragmentData> getBase() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public BaseSequence storeSequence(NucleotideSequence sequence, String geneId, String fullDescription) {
            String recordId = addressPrefix + geneId;
            return new BaseSequence(recordId);
        }

        @Override
        public void close() throws Exception {
        }
    }

    @Override
    public P params() {
        return params;
    }
}
