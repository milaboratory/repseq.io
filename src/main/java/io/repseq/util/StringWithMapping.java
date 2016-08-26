package io.repseq.util;

public class StringWithMapping {
    private final int[] originalToModifiedMapping;
    private final String modifiedString;

    private StringWithMapping(int[] originalToModifiedMapping, String modifiedString) {
        this.originalToModifiedMapping = originalToModifiedMapping;
        this.modifiedString = modifiedString;
    }

    public int convertPosition(int originalPosition) {
        if (originalPosition >= originalToModifiedMapping.length)
            return -1;
        if (originalPosition < 0)
            return convertPosition(originalToModifiedMapping.length + originalPosition);
        return originalToModifiedMapping[originalPosition];
    }

    public String getModifiedString() {
        return modifiedString;
    }

    public static StringWithMapping removeSymbol(String originalString, char charToRemove) {
        int modifiedStringLength = 0;
        for (int i = 0; i < originalString.length(); i++)
            if (originalString.charAt(i) != charToRemove)
                ++modifiedStringLength;
        char[] modifiedString = new char[modifiedStringLength];
        int[] mapping = new int[originalString.length()];
        int j = 0;
        for (int i = 0; i < originalString.length(); i++)
            if (originalString.charAt(i) != charToRemove) {
                mapping[i] = j;
                modifiedString[j++] = originalString.charAt(i);
            } else {
                mapping[i] = -1;
            }
        return new StringWithMapping(mapping, new String(modifiedString));
    }
}
