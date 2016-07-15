package io.repseq.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
            Collections.sort(lib.getGenes());
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
            Collections.sort(lib.getGenes());
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
