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

        GeneFeature l3VFeature = new GeneFeature(ReferencePoint.CDR3Begin, 0, 3);
        GeneFeature l3JFeature = new GeneFeature(ReferencePoint.CDR3End, -3, 0);

        for (VDJCLibrary lib : reg.getLoadedLibraries()) {
            for (VDJCGene gene : lib.getGenes()) {
                if (namePattern != null && !namePattern.matcher(gene.getName()).matches())
                    continue;

                //first generate list of warning messages
                //note: it might be useful to generate these for non-functional genes as well
                List<String> warnings = new ArrayList<>();
                if (gene.isFunctional()) {
                    //flag AA residues flanking CDR3
                    if (gene.getGeneType() == GeneType.Variable) {
                        NucleotideSequence l3 = gene.getFeature(l3VFeature);

                        try {
                            if (l3 == null)
                                warnings.add("unable to find CDR3 start");
                            else if (AminoAcidSequence.translate(l3).codeAt(0) != AminoAcidAlphabet.C)
                                warnings.add("CDR3 does not start with C, was: " + l3.toString() + " / " + AminoAcidSequence.translate(l3).toString() + " / CDR3Begin: " + gene.getData().getAnchorPoints().get(ReferencePoint.CDR3Begin));
                        }
                        catch (IllegalArgumentException e){
                            System.out.print("Unable to translate sequence: " + gene.getName() + " / " + l3);
                        }
                    }

                    if (gene.getGeneType() == GeneType.Joining) {
                        NucleotideSequence l3 = gene.getFeature(l3JFeature);

                        try {
                            if (l3 == null)
                                warnings.add("unable to find CDR3 end");
                            else if (AminoAcidSequence.translate(l3).codeAt(0) != AminoAcidAlphabet.W &&
                                    AminoAcidSequence.translate(l3).codeAt(0) != AminoAcidAlphabet.F)
                                warnings.add("CDR3 does not end with W or F, was: " + l3.toString() + " / " + AminoAcidSequence.translate(l3).toString() + " / CDR3End: " + gene.getData().getAnchorPoints().get(ReferencePoint.CDR3End));
                        }
                        catch (IllegalArgumentException e){
                            System.out.print("Unable to translate sequence: " + gene.getName() + " / " + l3);
                        }
                    }

                    //flag suspicious exon borders
                    //https://schneider.ncifcrf.gov/gallery/SequenceLogoSculpture.gif
                    //consider allowing: GT-AG, GC-AG or AT-AC
                    if (gene.getGeneType() == GeneType.Variable) {
                        NucleotideSequence intronStart = gene.getFeature(new GeneFeature(ReferencePoint.VIntronBegin, 0, 2));
                        if (intronStart != null) {
                            if (!intronStart.toString().equals("GT")) {
                                warnings.add("expected VIntron sequence to start with GT, was: " + intronStart.toString());
                            }
                        }

                        NucleotideSequence intronEnd = gene.getFeature(new GeneFeature(ReferencePoint.VIntronEnd, -2, 0));
                        if (intronEnd != null) {
                            if (!intronEnd.toString().equals("AG")) {
                                warnings.add("expected VIntron sequence to end with AG, was: " + intronEnd.toString());
                            }
                        }
                    }

                    //now iterate all segments and flag premature stop codons:
                    for (GeneFeature geneFeature : geneFeatures.get(gene.getGeneType())) {
                        NucleotideSequence nSequence = gene.getFeature(geneFeature);
                        AminoAcidSequence aaSequence = getAminoAcidSequence(gene, geneFeature, nSequence);
                        if (aaSequence != null && GeneFeature.getFrameReference(geneFeature) != null) {
                            //flag if contains stop codon
                            if (aaSequence.numberOfStops() > 0) {
                                warnings.add(GeneFeature.encode(geneFeature) + " contains a stop codon");
                            }
                        }
                    }
                }

                if (params.getProblemOnly() && warnings.isEmpty()) {
                    continue;
                }

                System.out.println(gene.getName() + " (" + (gene.isFunctional() ? "F" : "P") + ") " + gene.getChains());

                if (!warnings.isEmpty()) {
                    System.out.println();
                    System.out.println("WARNINGS: ");
                    for (String warning : warnings){
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

        @Parameter(description = "Print only functional genes with problems.",
                names = {"-p", "--problems"})
        public Boolean problemOnly;

        @Parameter(description = "Gene name pattern, regexp string, all genes with matching gene name will be exported.",
                names = {"-n", "--name"})
        public String name;

        public boolean getProblemOnly() {
            return problemOnly != null && problemOnly;
        }

        public String getInput() {
            return parameters.get(0);
        }
    }
}
