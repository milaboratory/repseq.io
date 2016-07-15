package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;

import java.io.File;
import java.util.List;

public class ListAction implements Action {
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryData[] libs = GlobalObjectMappers.ONE_LINE.readValue(new File(params.getInput()),
                VDJCLibraryData[].class);

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
        @Parameter(description = "library.json", arity = 1)
        public List<String> parameters;

        //@Parameter(description = "Chain pattern, regexp string, all genes with matching chain record will be collected.",
        //        names = {"-c", "--chain"})
        //public String chain;

        public String getInput() {
            return parameters.get(0);
        }
    }
}
