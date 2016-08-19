package io.repseq.dto;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VDJCDataUtils {
    private VDJCDataUtils() {
    }

    /**
     * Sort libraries and genes inside them.
     *
     * @param libraries libraries to sort
     */
    public static void sort(List<VDJCLibraryData> libraries) {
        // Sorting libraries in multi-library file
        Collections.sort(libraries);

        // Sorting genes inside each library
        for (VDJCLibraryData lib : libraries)
            sort(lib);
    }

    /**
     * Sort libraries and genes inside them.
     *
     * @param libraries libraries to sort
     */
    public static void sort(VDJCLibraryData[] libraries) {
        // Sorting libraries in multi-library file
        Arrays.sort(libraries);

        // Sorting genes inside each library
        for (VDJCLibraryData lib : libraries)
            sort(lib);
    }

    /**
     * Sort records inside library
     *
     * @param library library
     */
    public static void sort(VDJCLibraryData library) {
        Collections.sort(library.getGenes());
        Collections.sort(library.getSpeciesNames());
        Collections.sort(library.getSequenceFragments());
    }

    /**
     * Merges VDJCLibraryData objects for the same species into single object.
     *
     * @param libraries libraries
     * @return list of merged libraries
     */
    public static VDJCLibraryData[] merge(List<VDJCLibraryData> libraries) {
        Map<Long, VDJCLibraryData> resultMap = new HashMap<>();
        for (VDJCLibraryData library1 : libraries) {
            if (!resultMap.containsKey(library1.getTaxonId()))
                resultMap.put(library1.getTaxonId(), library1);
            else {
                // Merging two libraries with the same taxon ID
                VDJCLibraryData[] libsToMerge = new VDJCLibraryData[]{library1, resultMap.get(library1.getTaxonId())};

                // Merging species name
                Set<String> speciesNamesS = new HashSet<>();
                for (VDJCLibraryData lib : libsToMerge)
                    speciesNamesS.addAll(lib.getSpeciesNames());
                List<String> speciesNames = new ArrayList<>(speciesNamesS);

                // Merging genes
                Map<String, VDJCGeneData> genesMap = new HashMap<>();

                for (VDJCLibraryData lib : libsToMerge)
                    for (VDJCGeneData gene : lib.getGenes()) {
                        VDJCGeneData geneInMap = genesMap.put(gene.getName(), gene);
                        if (geneInMap != null && !geneInMap.equals(gene))
                            throw new IllegalArgumentException("Gene conflict: " + gene.getName());
                    }

                List<VDJCGeneData> genes = new ArrayList<>(genesMap.values());

                // Merging sequence fragments
                FragmentsBuilder fragmentsBuilder = new FragmentsBuilder();

                for (VDJCLibraryData lib : libsToMerge)
                    for (KnownSequenceFragmentData fragment : lib.getSequenceFragments())
                        fragmentsBuilder.addRegion(fragment);

                List<KnownSequenceFragmentData> fragments = fragmentsBuilder.getFragments();

                // Putting back merged result
                resultMap.put(library1.getTaxonId(), new VDJCLibraryData(library1.getTaxonId(), speciesNames, genes, fragments));
            }
        }

        // Converting result to array
        VDJCLibraryData[] result = new VDJCLibraryData[resultMap.size()];
        int i = 0;
        for (VDJCLibraryData data : resultMap.values())
            result[i++] = data;

        // Canonicalizing result
        sort(result);

        return result;
    }

    public static List<KnownSequenceFragmentData> extractFragments(URI uri, CachedSequenceProvider<NucleotideSequence> provider) {
        List<KnownSequenceFragmentData> segments = new ArrayList<>();

        for (Map.Entry<Range, NucleotideSequence> entry : provider.entrySet())
            segments.add(new KnownSequenceFragmentData(uri, entry.getKey(), entry.getValue()));

        Collections.sort(segments);

        return segments;
    }

    public static final class FragmentsBuilder {
        private final Map<URI, CachedSequenceProvider<NucleotideSequence>> fragmentsMap = new HashMap<>();

        public void addRegion(KnownSequenceFragmentData fragment) {
            addRegion(fragment.getUri(), fragment.getRange(), fragment.getSequence());
        }

        public void addRegion(URI uri, Range range, NucleotideSequence sequence) {
            CachedSequenceProvider<NucleotideSequence> frBase = fragmentsMap.get(uri);
            if (frBase == null)
                fragmentsMap.put(uri, frBase = new CachedSequenceProvider<>(NucleotideSequence.ALPHABET));
            frBase.setRegion(range, sequence);
        }

        public List<KnownSequenceFragmentData> getFragments() {
            List<KnownSequenceFragmentData> fragments = new ArrayList<>();
            for (Map.Entry<URI, CachedSequenceProvider<NucleotideSequence>> entry : fragmentsMap.entrySet())
                fragments.addAll(extractFragments(entry.getKey(), entry.getValue()));
            return fragments;
        }
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    public static int smartCompare(String s1, String s2) {
        int c;

        Matcher matcher1 = NUMBER_PATTERN.matcher(s1);
        Matcher matcher2 = NUMBER_PATTERN.matcher(s2);
        int lastPosition1 = 0, lastPosition2 = 0;

        while (matcher1.find() & matcher2.find()) {
            if ((c = s1.substring(lastPosition1, matcher1.start()).compareTo(s2.substring(lastPosition2, matcher2.start()))) != 0)
                return c;
            if ((c = Long.compare(Long.parseLong(matcher1.group()), Long.parseLong(matcher2.group()))) != 0)
                return c;
            lastPosition1 = matcher1.end();
            lastPosition2 = matcher2.end();
        }

        return s1.substring(lastPosition1).compareTo(s2.substring(lastPosition2));
    }
}
