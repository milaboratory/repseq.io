package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.core.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.AlignmentHelper;
import com.milaboratory.core.alignment.BLASTMatrix;
import com.milaboratory.core.alignment.batch.AlignmentHit;
import com.milaboratory.core.alignment.batch.AlignmentResult;
import com.milaboratory.core.alignment.batch.SimpleBatchAligner;
import com.milaboratory.core.alignment.batch.SimpleBatchAlignerParameters;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.TranslationParameters;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.core.GeneFeature;
import io.repseq.core.ReferencePoint;
import io.repseq.core.ReferencePoints;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import static com.milaboratory.core.sequence.TranslationParameters.withIncompleteCodon;

public class InferAnchorPointsAction implements Action {
    private static final String TARGET_LIBRARY_NAME = "target";
    private static final String REFERENCE_LIBRARY_PREFIX = "ref";
    private static final Pattern REFERENCE_LIBRARY_PATTERN = Pattern.compile(REFERENCE_LIBRARY_PREFIX + "\\d+");
    private static final TranslationParameters[] TRANSLATION_PARAMETERS = new TranslationParameters[]{
            withIncompleteCodon(0), withIncompleteCodon(1), withIncompleteCodon(2)
    };
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryRegistry reg = VDJCLibraryRegistry.getDefault();

        // Registering target library
        reg.registerLibraries(params.getInput(), TARGET_LIBRARY_NAME);

        // Map of library names
        Map<String, String> libraryNameToAddress = new HashMap<>();

        // Registering reference library
        int i = 0;
        for (String refAddress : params.getReference()) {
            String name = REFERENCE_LIBRARY_PREFIX + (i++);

            if (!"default".equals(refAddress))
                reg.registerLibraries(refAddress, name);
            else
                reg.loadAllLibraries("default");

            libraryNameToAddress.put(name, refAddress);
        }

        // Compile gene filter
        Pattern namePattern = params.name == null ? null : Pattern.compile(params.name);

        // Parsing gene feature
        GeneFeature geneFeature = params.getGeneFeature();

        SimpleBatchAlignerParameters<AminoAcidSequence> aParams = new SimpleBatchAlignerParameters<>(1, 0.9f,
                params.getAbsoluteMinScore(), true,
                AffineGapAlignmentScoring.getAminoAcidBLASTScoring(BLASTMatrix.BLOSUM62, -10, -1));
        SimpleBatchAligner<AminoAcidSequence, Ref> aligner = new SimpleBatchAligner<>(aParams);

        int dbSize = 0;

        for (VDJCLibrary lib : reg.getLoadedLibrariesByNamePattern(REFERENCE_LIBRARY_PATTERN)) {
            for (VDJCGene gene : lib.getGenes()) {
                NucleotideSequence nSeq = gene.getFeature(geneFeature);

                if (nSeq == null)
                    continue;

                ReferencePoint frameReference = GeneFeature.getFrameReference(geneFeature);
                ReferencePoints partitioning = gene.getPartitioning();

                if (frameReference == null)
                    continue;

                int relativePosition = partitioning.getRelativePosition(geneFeature, frameReference);

                if (relativePosition < 0)
                    continue;

                TranslationParameters frame = withIncompleteCodon(relativePosition);
                AminoAcidSequence aaSequence = AminoAcidSequence.translate(nSeq, frame);

                aligner.addReference(aaSequence, new Ref(gene, frame, nSeq.size()));
                ++dbSize;
            }
        }

        System.out.println("DB size: " + dbSize);
        System.out.println();

        // Checking that db is not empty
        if (dbSize == 0)
            throw new RuntimeException("No reference genes.");

        ArrayList<VDJCLibraryData> result = new ArrayList<>();

        // Iteration over target genes
        for (VDJCLibrary lib : reg.getLoadedLibrariesByName(TARGET_LIBRARY_NAME)) {
            ArrayList<VDJCGeneData> genes = new ArrayList<>();
            for (VDJCGene targetGene : lib.getGenes()) {
                if (namePattern != null && !namePattern.matcher(targetGene.getName()).matches())
                    continue;

                System.out.println("Processing: " + targetGene.getName() + " (" + (targetGene.isFunctional() ? "F" : "P") + ") " + targetGene.getChains());

                // Getting gene feature sequence from target gene
                NucleotideSequence nSeq = targetGene.getFeature(geneFeature);

                if (nSeq == null) {
                    System.out.println("Failed to extract " + GeneFeature.encode(geneFeature));
                    System.out.println("================");
                    System.out.println();
                    continue;
                }

                // Alignment result
                AlignmentResult<AlignmentHit<AminoAcidSequence, Ref>> bestAResult = null;
                TranslationParameters bestFrame = null;

                // Searching for best alignment
                for (TranslationParameters frame : TRANSLATION_PARAMETERS) {
                    AminoAcidSequence aaSeq = AminoAcidSequence.translate(nSeq, frame);
                    AlignmentResult<AlignmentHit<AminoAcidSequence, Ref>> r = aligner.align(aaSeq);
                    if (r != null && r.hasHits() &&
                            (bestAResult == null ||
                                    bestAResult.getBestHit().getAlignment().getScore() < r.getBestHit().getAlignment().getScore())) {
                        bestAResult = r;
                        bestFrame = frame;
                    }
                }

                if (bestFrame == null) {
                    System.out.println("No alignments found.");
                    continue;
                }

                Alignment<AminoAcidSequence> bestAlignment = bestAResult.getBestHit().getAlignment();
                Ref bestRef = bestAResult.getBestHit().getRecordPayload();
                VDJCGene bestReferenceGene = bestRef.gene;

                System.out.println("Aligned with " + bestReferenceGene.getName() + " from " +
                        libraryNameToAddress.get(bestReferenceGene.getParentLibrary().getName()) + " ; Score = " + bestAlignment.getScore());
                AlignmentHelper alignmentHelper = bestAlignment.getAlignmentHelper();
                for (AlignmentHelper h : alignmentHelper.split(150))
                    System.out.println(h + "\n");

                ReferencePoints targetPartitioning = targetGene.getPartitioning();
                ReferencePoints referencePartitioning = bestReferenceGene.getPartitioning();

                VDJCGeneData targetGeneData = targetGene.getData().clone();

                for (GeneFeature.ReferenceRange range : geneFeature)
                    for (ReferencePoint point : range.getIntermediatePoints()) {
                        System.out.print(point + ": ");

                        if (targetPartitioning.isAvailable(point)) {
                            System.out.println("already set");
                            continue;
                        }

                        if (!referencePartitioning.isAvailable(point)) {
                            System.out.println("not set in reference gene");
                            continue;
                        }

                        int ntPositionInReference = referencePartitioning.getRelativePosition(geneFeature, point);

                        // Projecting position

                        AminoAcidSequence.AminoAcidSequencePosition aaPositionInReferece = AminoAcidSequence
                                .convertNtPositionToAA(ntPositionInReference, bestRef.ntSeqLength, bestRef.frame);

                        if (aaPositionInReferece == null) {
                            System.out.println("failed to convert to aa position in ref");
                            continue;
                        }

                        int aaPositionInTarget = Alignment.aabs(bestAlignment.convertPosition(aaPositionInReferece.aminoAcidPosition));

                        if (aaPositionInTarget == -1) {
                            System.out.println("failed to project using alignment");
                            continue;
                        }

                        int ntPositionInTarget = AminoAcidSequence.convertAAPositionToNt(aaPositionInTarget, nSeq.size(), bestFrame);

                        if (ntPositionInTarget == -1) {
                            System.out.println("failed");
                            continue;
                        }

                        ntPositionInTarget += aaPositionInReferece.positionInTriplet;

                        ntPositionInTarget = targetPartitioning.getAbsolutePosition(geneFeature, ntPositionInTarget);

                        if (ntPositionInTarget == -1) {
                            System.out.println("failed");
                            continue;
                        }

                        System.out.println(ntPositionInTarget);

                        targetGeneData.getAnchorPoints().put(point, (long) ntPositionInTarget);
                    }

                System.out.println("================");
                System.out.println();

                genes.add(targetGeneData);
            }

            result.add(new VDJCLibraryData(lib.getData(), genes));
        }

        GlobalObjectMappers.PRETTY.writeValue(new File(params.getOutput()), result);
    }

    @Override
    public String command() {
        return "inferPoints";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    private static final class Ref {
        final VDJCGene gene;
        final TranslationParameters frame;
        final int ntSeqLength;

        public Ref(VDJCGene gene, TranslationParameters frame, int ntSeqLength) {
            this.gene = gene;
            this.frame = frame;
            this.ntSeqLength = ntSeqLength;
        }
    }

    @Parameters(commandDescription = "Try to infer anchor point positions from gene sequences of other libraries.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "input_library.json|default reference_library1.json reference_library2.json .... output.json")
        public List<String> parameters;

        @Parameter(description = "Gene name pattern, regexp string, all genes with matching gene name will be exported.",
                names = {"-n", "--name"})
        public String name;

        @Parameter(description = "Reference gene feature to use (e.g. VRegion, JRegion, VTranscript, etc...). This " +
                "feature will be used to align target genes with reference genes. Target genes must have this gene " +
                "feature.",
                names = {"-g", "--gene-feature"},
                required = true)
        public String feature;

        //TODO change!
        @Parameter(description = "Absolute minimal score. Alignment is performed using amino acid sequences (target is " +
                "queried using all three reading frames) using BLOSUM62 matrix. (default 200 for V gene, 50 for J gene)",
                names = {"-m", "--min-score"})
        public Integer absoluteMinScore = null;

        public Integer getAbsoluteMinScore() {
            if (absoluteMinScore != null)
                return absoluteMinScore;
            switch (getGeneFeature().getGeneType()) {
                case Joining:
                    return 50;
                case Variable:
                    return 200;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public GeneFeature getGeneFeature() {
            return GeneFeature.parse(feature);
        }

        public String getInput() {
            return parameters.get(0);
        }

        public List<String> getReference() {
            return parameters.subList(1, parameters.size() - 1);
        }

        public String getOutput() {
            return parameters.get(parameters.size() - 1);
        }

        @Override
        protected List<String> getOutputFiles() {
            return Collections.singletonList(getOutput());
        }
    }
}
