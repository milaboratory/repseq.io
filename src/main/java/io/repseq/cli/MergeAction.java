package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.cli.ActionParametersWithOutput;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCLibraryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.milaboratory.util.GlobalObjectMappers.ONE_LINE;
import static java.util.Arrays.asList;

public class MergeAction implements Action {
    private static final Logger log = LoggerFactory.getLogger(MergeAction.class);
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        List<VDJCLibraryData> libs = new ArrayList<>();

        for (String input : params.getInput())
            libs.addAll(asList(ONE_LINE.readValue(new File(input),
                    VDJCLibraryData[].class)));

        VDJCLibraryData[] mergeResult = VDJCDataUtils.merge(libs);

        GlobalObjectMappers.PRETTY.writeValue(new File(params.getOutput()), mergeResult);

        log.info("Merged successfully.");
    }

    @Override
    public String command() {
        return "merge";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Merge several libraries into single library.")
    public static final class Params extends ActionParametersWithOutput {
        @Parameter(description = "[input1.json [ input2.json [...] ] ] output.json")
        public List<String> parameters;

        public List<String> getInput() {
            return parameters.subList(0, parameters.size() - 1);
        }

        public String getOutput() {
            return parameters.get(parameters.size() - 1);
        }

        @Override
        protected List<String> getOutputFiles() {
            return Collections.singletonList(getOutput());
        }
    }
}
