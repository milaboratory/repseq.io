package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.core.GeneFeature;
import io.repseq.core.GeneType;
import io.repseq.core.ReferencePoint;
import io.repseq.core.ReferencePoints;

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

        for (VDJCLibrary lib : reg.getLoadedLibraries()) {
            for (VDJCGene gene : lib.getGenes()) {
                if (namePattern != null && !namePattern.matcher(gene.getName()).matches())
                    continue;

                System.out.println(gene.getName() + " (" + (gene.isFunctional() ? "F" : "P") + ") " + gene.getChains());

                for (GeneFeature geneFeature : geneFeatures.get(gene.getGeneType())) {
                    System.out.println();
                    System.out.println(GeneFeature.encode(geneFeature));

                    ReferencePoint frameReference = GeneFeature.getFrameReference(geneFeature);
                    ReferencePoints partitioning = gene.getPartitioning();

                    NucleotideSequence nSequence = gene.getFeature(geneFeature);

                    AminoAcidSequence aaSequence;
                    if (frameReference != null) {
                        int relativePosition = partitioning.getRelativePosition(geneFeature, frameReference);
                        aaSequence = nSequence == null || relativePosition < 0 ?
                                null :
                                AminoAcidSequence.translate(nSequence,
                                        withIncompleteCodon(relativePosition));
                    } else
                        aaSequence = null;

                    System.out.print("N:   ");
                    if (nSequence == null)
                        System.out.println("Not Available");
                    else
                        System.out.println(nSequence);

                    if (frameReference != null) {
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
        @Parameter(description = "input_library.json")
        public List<String> parameters;

        @Parameter(description = "Gene name pattern, regexp string, all genes with matching gene name will be exported.",
                names = {"-n", "--name"})
        public String name;

        public String getInput() {
            return parameters.get(0);
        }
    }
}
