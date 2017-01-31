package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.core.sequence.AminoAcidAlphabet;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.core.GeneFeature;
import io.repseq.core.GeneType;
import io.repseq.core.ReferencePoint;
import io.repseq.core.ReferencePoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.milaboratory.core.sequence.TranslationParameters.withIncompleteCodon;

public class DebugAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryRegistry reg = VDJCLibraryRegistry.getDefault();
        reg.registerLibraries(params.getInput());

        Pattern namePattern = params.name == null ? null : Pattern.compile(params.name);

        GeneFeature cdr3FirstTriplet = new GeneFeature(ReferencePoint.CDR3Begin, 0, 3);
        GeneFeature cdr3LastTriplet = new GeneFeature(ReferencePoint.CDR3End, -3, 0);
        GeneFeature vIntronDonor = new GeneFeature(ReferencePoint.VIntronBegin, 0, 2);
        GeneFeature vIntronAcceptor = new GeneFeature(ReferencePoint.VIntronEnd, -2, 0);

        for (VDJCLibrary lib : reg.getLoadedLibraries()) {
            for (VDJCGene gene : lib.getGenes()) {
                if (namePattern != null && !namePattern.matcher(gene.getName()).matches())
                    continue;

                // First generate list of warning messages
                List<String> warnings = new ArrayList<>();
                if (gene.isFunctional() || params.getCheckAll()) {
                    NucleotideSequence l3;
                    switch (gene.getGeneType()) {
                        case Variable:

                            // Flag AA residues flanking CDR3

                            l3 = gene.getFeature(cdr3FirstTriplet);
                            if (l3 == null)
                                warnings.add("Unable to find CDR3 start");
                            else if (l3.size() != 3)
                                warnings.add("Unable to translate sequence: " + gene.getName() + " / " + l3);
                            else if (AminoAcidSequence.translate(l3).codeAt(0) != AminoAcidAlphabet.C)
                                warnings.add("CDR3 does not start with C, was: " + l3.toString() +
                                        " / " + AminoAcidSequence.translate(l3).toString() + " / CDR3Begin: " +
                                        gene.getData().getAnchorPoints().get(ReferencePoint.CDR3Begin));

                            // Flag suspicious exon borders
                            // https://schneider.ncifcrf.gov/gallery/SequenceLogoSculpture.gif

                            NucleotideSequence vIntronDonorSeq = gene.getFeature(vIntronDonor);
                            if (vIntronDonorSeq != null && !vIntronDonorSeq.toString().equals("GT") &&
                                    !vIntronDonorSeq.toString().equals("GC"))
                                warnings.add("Expected VIntron sequence to start with GT, was: " + vIntronDonorSeq.toString());

                            NucleotideSequence vIntronAcceptorSeq = gene.getFeature(vIntronAcceptor);
                            if (vIntronAcceptorSeq != null && !vIntronAcceptorSeq.toString().equals("AG"))
                                warnings.add("Expected VIntron sequence to end with AG, was: " + vIntronAcceptorSeq.toString());

                            NucleotideSequence vTranscriptWithout5UTR = gene.getFeature(GeneFeature.VTranscriptWithout5UTR);
                            if (vTranscriptWithout5UTR != null){
                                NucleotideSequence vRegion = gene.getFeature(GeneFeature.VRegion);
                                if (vRegion != null){
                                    AminoAcidSequence vTranscriptWithout5UTRAA = getAminoAcidSequence(gene, GeneFeature.VTranscriptWithout5UTR, vTranscriptWithout5UTR);
                                    AminoAcidSequence vRegionAA = getAminoAcidSequence(gene, GeneFeature.VRegion, vRegion);

                                    if (!vTranscriptWithout5UTRAA.toString().contains(vRegionAA.toString())){
                                        warnings.add("Expected VTranscriptWithout5UTR translation to contain VRegion translation.  This may indicate an error in the L2 boundaries");
                                    }
                                }
                            }

                            break;

                        case Joining:

                            // Flag AA residues flanking CDR3

                            l3 = gene.getFeature(cdr3LastTriplet);
                            if (l3 == null)
                                warnings.add("Unable to find CDR3 end");
                            else if (l3.size() != 3)
                                warnings.add("Unable to translate sequence: " + gene.getName() + " / " + l3);
                            else if (AminoAcidSequence.translate(l3).codeAt(0) != AminoAcidAlphabet.W &&
                                    AminoAcidSequence.translate(l3).codeAt(0) != AminoAcidAlphabet.F)
                                warnings.add("CDR3 does not end with W or F, was: " + l3.toString() + " / " +
                                        AminoAcidSequence.translate(l3).toString() + " / CDR3End: " +
                                        gene.getData().getAnchorPoints().get(ReferencePoint.CDR3End));

                            break;

                    }

                    // Now iterate all segments and flag premature stop codons:

                    for (GeneFeature geneFeature : aaGeneFeatures.get(gene.getGeneType())) {
                        AminoAcidSequence aaSequence = getAminoAcidSequence(gene, geneFeature,
                                gene.getFeature(geneFeature));
                        if (aaSequence != null) {
                            // Flag if contains stop codon
                            if (aaSequence.numberOfStops() > 0)
                                warnings.add(GeneFeature.encode(geneFeature) + " contains a stop codon");
                        }
                    }
                }

                if (params.getProblemOnly() && warnings.isEmpty())
                    continue;

                System.out.println(gene.getName() + " (" + (gene.isFunctional() ? "F" : "P") + ") " + gene.getChains());

                if (!warnings.isEmpty()) {
                    System.out.println();
                    System.out.println("WARNINGS: ");
                    for (String warning : warnings) {
                        System.out.println(warning);
                    }
                    System.out.println();
                }

                for (GeneFeature geneFeature : geneFeatures.get(gene.getGeneType())) {
                    System.out.println();
                    System.out.println(GeneFeature.encode(geneFeature));

                    NucleotideSequence nSequence = gene.getFeature(geneFeature);
                    AminoAcidSequence aaSequence = getAminoAcidSequence(gene, geneFeature, nSequence);

                    System.out.print("N:   ");
                    if (nSequence == null)
                        System.out.println("Not Available");
                    else
                        System.out.println(nSequence);

                    if (GeneFeature.getFrameReference(geneFeature) != null) {
                        System.out.print("AA:  ");
                        if (aaSequence == null)
                            System.out.println("Not Available");
                        else
                            System.out.println(aaSequence);
                    }
                }

                System.out.println("=========");
                System.out.println();
            }
        }
    }

    private static AminoAcidSequence getAminoAcidSequence(VDJCGene gene, GeneFeature geneFeature, NucleotideSequence nSequence) {
        ReferencePoints partitioning = gene.getPartitioning();
        ReferencePoint frameReference = GeneFeature.getFrameReference(geneFeature);

        AminoAcidSequence aaSequence;
        if (frameReference != null) {
            int relativePosition = partitioning.getRelativePosition(geneFeature, frameReference);
            aaSequence = nSequence == null || relativePosition < 0 ?
                    null :
                    AminoAcidSequence.translate(nSequence, withIncompleteCodon(relativePosition));
        } else
            aaSequence = null;

        return aaSequence;
    }

    private static final Map<GeneType, GeneFeature[]> geneFeatures = new HashMap<>();
    private static final Map<GeneType, GeneFeature[]> aaGeneFeatures = new HashMap<>();

    static {
        geneFeatures.put(GeneType.Variable, new GeneFeature[]{
                GeneFeature.V5UTRGermline,
                GeneFeature.L1,
                GeneFeature.VIntron,
                GeneFeature.L2,
                GeneFeature.L,
                GeneFeature.FR1,
                GeneFeature.CDR1,
                GeneFeature.FR2,
                GeneFeature.CDR2,
                GeneFeature.FR3,
                GeneFeature.GermlineVCDR3Part,
                GeneFeature.VRegion,
                GeneFeature.VTranscriptWithout5UTR
        });

        geneFeatures.put(GeneType.Diversity, new GeneFeature[]{
                GeneFeature.DRegion
        });

        geneFeatures.put(GeneType.Joining, new GeneFeature[]{
                GeneFeature.JRegion,
                GeneFeature.GermlineJCDR3Part,
                GeneFeature.FR4
        });

        geneFeatures.put(GeneType.Constant, new GeneFeature[]{
                GeneFeature.CExon1
        });

        aaGeneFeatures.put(GeneType.Variable, new GeneFeature[]{
                GeneFeature.L1,
                GeneFeature.L2,
                GeneFeature.L,
                GeneFeature.FR1,
                GeneFeature.CDR1,
                GeneFeature.FR2,
                GeneFeature.CDR2,
                GeneFeature.FR3,
                GeneFeature.GermlineVCDR3Part,
                GeneFeature.VRegion,
                GeneFeature.VTranscriptWithout5UTR
        });

        aaGeneFeatures.put(GeneType.Diversity, new GeneFeature[]{});

        aaGeneFeatures.put(GeneType.Joining, new GeneFeature[]{
                GeneFeature.GermlineJCDR3Part,
                GeneFeature.FR4,
                GeneFeature.JRegion
        });

        aaGeneFeatures.put(GeneType.Constant, new GeneFeature[]{});
    }

    @Override
    public String command() {
        return "debug";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Outputs extensive information on genes in the library.")
    public static final class Params extends ActionParameters {
        @Parameter(description = "input_library.json[.gz]")
        public List<String> parameters;

        @Parameter(description = "Print only genes with problems, checks only functional genes by default (see -a option).",
                names = {"-p", "--problems"})
        public Boolean problemOnly;

        @Parameter(description = "Check all genes, used with -p option.",
                names = {"-a", "--all"})
        public Boolean checkAllGenes;

        @Parameter(description = "Gene name pattern, regexp string, all genes with matching gene name will be exported.",
                names = {"-n", "--name"})
        public String name;

        public boolean getProblemOnly() {
            return problemOnly != null && problemOnly;
        }

        public boolean getCheckAll() {
            return checkAllGenes != null && checkAllGenes;
        }

        public String getInput() {
            return parameters.get(0);
        }
    }
}