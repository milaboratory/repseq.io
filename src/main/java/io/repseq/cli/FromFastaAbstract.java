package io.repseq.cli;

import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;

public abstract class FromFastaAbstract<P extends FromFastaParametersAbstract> implements Action {
    public P params;

    public FromFastaAbstract(P params) {
        this.params = params;
    }

    @Override
    public void go(ActionHelper helper) throws Exception {

    }

    @Override
    public P params() {
        return params;
    }
}
