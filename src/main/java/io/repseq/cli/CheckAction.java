package io.repseq.cli;

import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;

public class CheckAction implements Action {
    final Params parameters = new Params();

    @Override
    public void go(ActionHelper helper) throws Exception {

    }

    @Override
    public String command() {
        return "check";
    }

    @Override
    public ActionParameters params() {
        return parameters;
    }

    @Parameters(commandDescription = "Check library for problems.")
    public static final class Params extends ActionParameters {

    }
}
