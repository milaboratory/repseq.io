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
package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.repseq.core.*;

import java.util.List;

public class StatAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryRegistry reg = VDJCLibraryRegistry.getDefault();
        reg.registerLibraries(params.getInput());

        for (VDJCLibrary vdjcLibrary : reg.getLoadedLibraries()) {
            System.out.println("LibraryID (libraryName:taxonId): " + vdjcLibrary.getLibraryId());
            TObjectIntHashMap<GeneType> geneTypeCount = new TObjectIntHashMap<>();
            for (VDJCGene vdjcGene : vdjcLibrary.getGenes())
                geneTypeCount.adjustOrPutValue(vdjcGene.getGeneType(), 1, 1);
            System.out.println();

            TObjectIntIterator<GeneType> it = geneTypeCount.iterator();
            while (it.hasNext()) {
                it.advance();
                System.out.println(it.key() + " (total records " + it.value() + "):");

                TObjectIntHashMap<Chains> byChains = new TObjectIntHashMap<>();

                for (VDJCGene vdjcGene : vdjcLibrary.getGenes()) {
                    if (vdjcGene.getGeneType() != it.key())
                        continue;

                    byChains.adjustOrPutValue(vdjcGene.getChains(), 1, 1);
                }

                TObjectIntIterator<Chains> itByChains = byChains.iterator();
                while (itByChains.hasNext()) {
                    itByChains.advance();

                    System.out.println(itByChains.key() + ": " + itByChains.value());
                }
            }

            System.out.println();
            System.out.println("==============");
            System.out.println();
        }
    }

    @Override
    public String command() {
        return "stat";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Print library statistics.")
    public static final class Params extends ActionParameters {
        @Parameter(description = "input_library.json", arity = 1)
        public List<String> parameters;

        public String getInput() {
            return parameters.get(0);
        }
    }
}
