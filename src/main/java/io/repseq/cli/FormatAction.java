package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCLibraryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FormatAction implements Action {
    private static final Logger log = LoggerFactory.getLogger(FormatAction.class);
    final Params params = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {
        Path file = Paths.get(params.getInput());
        VDJCLibraryData[] libs = VDJCDataUtils.readArrayFromFile(file);

        // writeToFile also performs sorting etc. before serialization
        VDJCDataUtils.writeToFile(libs, file, params.getCompact());

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
        @Parameter(description = "library.json[.gz]", arity = 1)
        public List<String> parameters;

        @Parameter(description = "Compact.",
                names = {"-c", "--compact"})
        public boolean compact = false;

        public boolean getCompact() {
            return compact;
        }

        public String getInput() {
            return parameters.get(0);
        }
    }
}
