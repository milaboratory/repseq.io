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
package io.repseq.dto;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.provider.CachedSequenceProvider;
import com.milaboratory.util.GlobalObjectMappers;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
                resultMap.put(library1.getTaxonId(), library1.clone());
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

                // Merging meta information

                SortedMap<String, SortedSet<String>> meta = new TreeMap<>();
                for (VDJCLibraryData lib : libsToMerge) {
                    for (Map.Entry<String, SortedSet<String>> entry : lib.getMeta().entrySet()) {
                        SortedSet<String> values = meta.get(entry.getKey());
                        if (values == null)
                            meta.put(entry.getKey(), values = new TreeSet<>());
                        values.addAll(entry.getValue());
                    }
                }

                // Putting back merged result
                resultMap.put(library1.getTaxonId(), new VDJCLibraryData(library1.getTaxonId(), speciesNames, genes,
                        meta, fragments));
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

    public static VDJCLibraryData[] readArrayFromFile(String file) throws IOException {
        return readArrayFromFile(Paths.get(file));
    }

    public static VDJCLibraryData[] readArrayFromFile(Path file) throws IOException {
        // Ungzipping if file name ends with .gz
        try (InputStream is = file.getFileName().toString().endsWith(".gz") ?
                new BufferedInputStream(new GZIPInputStream(new FileInputStream(file.toFile()))) :
                new BufferedInputStream(new FileInputStream(file.toFile()))) {
            // Getting libraries from stream
            return GlobalObjectMappers.ONE_LINE.readValue(is, VDJCLibraryData[].class);
        }
    }

    public static void writeToFile(VDJCLibraryData[] data, String file, boolean compact) throws IOException {
        writeToFile(data, Paths.get(file), compact);
    }

    public static void writeToFile(VDJCLibraryData[] data, Path file, boolean compact) throws IOException {
        writeToFile(new ArrayList<>(Arrays.asList(data)), file, compact);
    }

    public static void writeToFile(List<VDJCLibraryData> data, String file, boolean compact) throws IOException {
        writeToFile(data, Paths.get(file), compact);
    }

    public static void writeToFile(List<VDJCLibraryData> data, Path file, boolean compact) throws IOException {
        VDJCDataUtils.sort(data);
        try (OutputStream os = file.getFileName().toString().endsWith(".gz") ?
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file.toFile()))) :
                new BufferedOutputStream(new FileOutputStream(file.toFile()))) {
            if (compact)
                GlobalObjectMappers.ONE_LINE.writeValue(os, data);
            else
                GlobalObjectMappers.PRETTY.writeValue(os, data);
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

    public static final Comparator<String> SMART_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return smartCompare(o1, o2);
        }
    };

    public static final Comparator<String> SMART_COMPARATOR_INVERSE = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return smartCompare(o2, o1);
        }
    };
}
