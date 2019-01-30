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
