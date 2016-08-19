package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCLibraryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class FormatAction implements Action {
    private static final Logger log = LoggerFactory.getLogger(FormatAction.class);
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        VDJCLibraryData[] libs = GlobalObjectMappers.ONE_LINE.readValue(new File(params.getInput()),
                VDJCLibraryData[].class);

        VDJCDataUtils.sort(libs);

        if (params.getCompact())
            GlobalObjectMappers.ONE_LINE.writeValue(new File(params.getInput()), libs);
        else
            GlobalObjectMappers.PRETTY.writeValue(new File(params.getInput()), libs);

        log.info("Formatted successfully.");
    }

    @Override
    public String command() {
        return "format";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription = "Format JSON in library; sort libraries in multi-library files, sort genes inside libraries.")
    public static final class Params extends ActionParameters {
        @Parameter(description = "library.json", arity = 1)
        public List<String> parameters;

        @Parameter(description = "Compact.",
                names = {"-c", "--compact"})
        public Boolean compact = null;

        public boolean getCompact() {
            return compact != null && compact;
        }

        public String getInput() {
            return parameters.get(0);
        }
    }
}
