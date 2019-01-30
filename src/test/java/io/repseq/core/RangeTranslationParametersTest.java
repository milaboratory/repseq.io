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
package io.repseq.core;

import com.milaboratory.core.Range;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RangeTranslationParametersTest {
    @Test
    public void testIncompleteCodon() throws Exception {
        assertEquals(new Range(9, 10),
                new RangeTranslationParameters(ReferencePoint.CDR3Begin, ReferencePoint.VEndTrimmed, new Range(0, 10))
                        .rightIncompleteCodonRange());
        assertEquals(new Range(9, 11),
                new RangeTranslationParameters(ReferencePoint.CDR3Begin, ReferencePoint.VEndTrimmed, new Range(0, 11))
                        .rightIncompleteCodonRange());
        assertEquals(null,
                new RangeTranslationParameters(ReferencePoint.CDR3Begin, ReferencePoint.VEndTrimmed, new Range(0, 12))
                        .rightIncompleteCodonRange());
        assertEquals(null,
                new RangeTranslationParameters(ReferencePoint.CDR3Begin, ReferencePoint.VEndTrimmed, new Range(0, 10))
                        .leftIncompleteCodonRange());

        assertEquals(new Range(0, 1),
                new RangeTranslationParameters(ReferencePoint.JBeginTrimmed, ReferencePoint.CDR3End, new Range(0, 10))
                        .leftIncompleteCodonRange());
        assertEquals(new Range(0, 2),
                new RangeTranslationParameters(ReferencePoint.JBeginTrimmed, ReferencePoint.CDR3End, new Range(0, 11))
                        .leftIncompleteCodonRange());
        assertEquals(null,
                new RangeTranslationParameters(ReferencePoint.JBeginTrimmed, ReferencePoint.CDR3End, new Range(0, 12))
                        .leftIncompleteCodonRange());
        assertEquals(null,
                new RangeTranslationParameters(ReferencePoint.JBeginTrimmed, ReferencePoint.CDR3End, new Range(0, 10))
                        .rightIncompleteCodonRange());
    }
}