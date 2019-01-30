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
package io.repseq.util;

public class StringWithMapping {
    private final int[] originalToModifiedMapping;
    private final String modifiedString;

    private StringWithMapping(int[] originalToModifiedMapping, String modifiedString) {
        this.originalToModifiedMapping = originalToModifiedMapping;
        this.modifiedString = modifiedString;
    }

    public int convertPosition(int originalPosition) {
        // Sequence end case
        if (originalPosition == originalToModifiedMapping.length)
            return originalToModifiedMapping[originalPosition - 1] + 1;
        // Out of range
        if (originalPosition >= originalToModifiedMapping.length)
            return -1;
        // Negative value counts from the end of the sequence
        if (originalPosition < 0)
            return convertPosition(originalToModifiedMapping.length + originalPosition + 1);
        // Normal conversion
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
