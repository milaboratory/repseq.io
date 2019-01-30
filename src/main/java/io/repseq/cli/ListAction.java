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
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;

import java.util.List;

public class ListAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryData[] libs = VDJCDataUtils.readArrayFromFile(params.getInput());

        System.out.println("TaxonId\tGeneName\tFunctional");
        for (VDJCLibraryData lib : libs)
            for (VDJCGeneData geneData : lib.getGenes()) {
                System.out.println(lib.getTaxonId() + "\t" +
                        geneData.getName() + "\t" +
                        (geneData.isFunctional() ? "F" : "P"));
            }
    }

    @Override
    public String command() {
        return "list";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    //TODO force option to overwrite output file
    @Parameters(commandDescription = "Format JSON in library; sort libraries in multi-library files, sort genes inside libraries.")
    public static final class Params extends ActionParameters {
        @Parameter(description = "library.json[.gz]", arity = 1)
        public List<String> parameters;

        //@Parameter(description = "Chain pattern, regexp string, all genes with matching chain record will be collected.",
        //        names = {"-c", "--chain"})
        //public String chain;

        public String getInput() {
            return parameters.get(0);
        }
    }
}
