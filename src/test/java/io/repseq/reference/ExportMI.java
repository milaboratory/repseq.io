package io.repseq.reference;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.core.*;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.seqbase.SequenceResolvers;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExportMI {
    @Ignore
    @Test
    public void export1() throws Exception {
        String input = "/Volumes/Data/Projects/MiLaboratory/mixcr/src/main/resources/reference/mi.ll";
        String output = "/Volumes/Data/Projects/repseqio/reference/";
        List<VDJCLibraryData> libs = new ArrayList<>();
        LociLibrary ll = LociLibraryReader.read(input, false);
        for (int taxonId : new int[]{Species.HomoSapiens, Species.MusMusculus}) {
            List<Allele> alleles = new ArrayList<>();
            for (LocusContainer locusContainer : ll.getLoci()) {
                if (locusContainer.getSpeciesAndChain().taxonId != taxonId)
                    continue;
                alleles.addAll(locusContainer.getAllAlleles());
            }

            Collections.sort(alleles, new Comparator<Allele>() {
                @Override
                public int compare(Allele o1, Allele o2) {
                    String name1 = o1.getName().split("\\*")[0];
                    String name2 = o2.getName().split("\\*")[0];

                    String fam1 = name1.split("-")[0];
                    String fam2 = name2.split("-")[0];

                    int c;
                    if ((c = leadingString(fam1).compareTo(leadingString(fam2))) != 0)
                        return c;

                    if ((c = Integer.compare(trailingNumber(fam1), trailingNumber(fam2))) != 0)
                        return c;

                    if ((c = Integer.compare(trailingNumber(name1), trailingNumber(name2))) != 0)
                        return c;

                    return name1.compareTo(name2);
                }
            });

            Map<String, VDJCGeneData> genes = new HashMap<>();
            List<VDJCGeneData> genesList = new ArrayList<>();

            for (Allele allele : alleles) {
                if (allele instanceof ReferenceAllele) {
                    ReferenceAllele ra = (ReferenceAllele) allele;
                    VDJCGeneData gene = genes.get(ra.getName());
                    if (gene != null)
                        gene.getChains().add(ra.getLocus().name());
                    else {
                        SortedMap<ReferencePoint, Long> refPoints = new TreeMap<>();
                        ReferencePoints partitioning = ra.getPartitioning();
                        for (BasicReferencePoint brp : BasicReferencePoint.values()) {
                            ReferencePoint rp = ReferencePoint.fromBasic(brp, true);
                            if (partitioning.isAvailable(rp))
                                refPoints.put(rp, (long) partitioning.getPosition(rp));
                        }
                        gene = new VDJCGeneData(new BaseSequence("nuccore://" + ra.getAccession()),
                                ra.getName(),
                                ra.getGeneType(),
                                ra.isFunctional(),
                                new HashSet<>(Collections.singleton(ra.getLocus().name())), refPoints);
                        genes.put(gene.getName(), gene);
                        genesList.add(gene);
                    }
                    //System.out.println(ra.getName());
                    //System.out.println(ra.getAccession());
                    //System.out.println(Arrays.toString(ra.getPartitioning().points));
                }
            }

            VDJCLibraryData lib = new VDJCLibraryData(taxonId, null, genesList, null);
            libs.add(lib);
        }

        GlobalObjectMappers.PRETTY.writeValue(new File(output + "mi.json"), libs);
    }

    @Test
    public void test1() throws Exception {
        Path cachePath = Paths.get(System.getProperty("user.home"), ".repseqio", "cache");
        SequenceResolvers.initDefaultResolver(cachePath);

        VDJCLibraryRegistry.getDefault().registerLibraries(Paths.get("/Volumes/Data/Projects/repseqio/reference/mi.json"));

        VDJCLibrary mi = VDJCLibraryRegistry.getDefault().getLibrary(new SpeciesAndLibraryName(Species.HomoSapiens, "mi"));
        VDJCGene gene = mi.get("TRBV12-3*00");
        NucleotideSequence feature = gene.getFeature(GeneFeature.FR3);
        System.out.println(feature);
    }

    static final Pattern tp = Pattern.compile("\\d+$");
    static final Pattern lp = Pattern.compile("^\\D+");

    public static String leadingString(String str) {
        Matcher matcher = lp.matcher(str);
        matcher.find();
        return matcher.group();
    }

    public static int trailingNumber(String str) {
        Matcher matcher = tp.matcher(str);

        if (!matcher.find())
            return -1;

        return Integer.decode(matcher.group());
    }
}
