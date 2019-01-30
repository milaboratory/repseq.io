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

import io.repseq.core.GeneType;
import io.repseq.core.ReferencePoint;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCLibraryRegistry;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Some trash tests.
 */
public class UTest {
    @Ignore
    @Test
    public void t1() throws Exception {
        Map<String, Integer> mins = new HashMap<>();
        Map<String, Integer> maxs = new HashMap<>();

        for (VDJCGene gene : VDJCLibraryRegistry.getDefault().getLibrary("default", "hs").getGenes()) {
            if (gene.getGeneType() != GeneType.Joining)
                continue;

            int vend = gene.getPartitioning().getPosition(ReferencePoint.JBegin);
            int vbegin = gene.getPartitioning().getPosition(ReferencePoint.FR4End);

            String key = gene.getData().getBaseSequence().getOrigin().toString();

            add(maxs, key, vbegin, true);
            add(maxs, key, vend, true);

            add(mins, key, vbegin, false);
            add(mins, key, vend, false);
        }

        System.out.println("MINS");
        print(mins);

        System.out.println("MAXS");
        print(maxs);
    }

    @Ignore
    @Test
    public void t2() throws Exception {
        System.out.println(VDJCLibraryRegistry.getDefault().getLibrary("default", "hs").getLibraryId());
    }

    public void print(Map<String, Integer> m) {
        for (Map.Entry<String, Integer> entry : m.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public void add(Map<String, Integer> m, String key, int val, boolean max) {
        if (val < 0)
            return;
        if (!m.containsKey(key))
            m.put(key, val);
        else
            m.put(key, max ? Math.max(val, m.get(key)) : Math.min(val, m.get(key)));
    }
}
