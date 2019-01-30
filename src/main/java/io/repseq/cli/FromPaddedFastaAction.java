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

import java.util.ArrayList;
import java.util.List;

public final class FromPaddedFastaAction extends FromFastaActionAbstract<FromPaddedFastaAction.ActionParameters> {
    public FromPaddedFastaAction() {
        super(new ActionParameters());
    }

    @Override
    public String command() {
        return "fromPaddedFasta";
    }

    @Parameters(commandDescription = "Converts library from padded fasta file (IMGT-like) to json library. This command can operate in two modes\n" +
            "             (1) if 3 file-parameters are specified, it will create separate non-padded fasta and put links inside newly created library pointing to it,\n" +
            "             (2) if 2 file-parameters are specified, create only library file, and embed sequences directly into it. \n" +
            "             To use library generated using mode (1) one need both output files, (see also 'repseqio compile').\n" +
            "             If library is intended for further editing and/or submission to version control system option (1) is recommended.")
    public static final class ActionParameters extends FromFastaParametersAbstract {
        @Parameter(description = "input_padded.fasta [output.fasta] output.json[.gz]")
        public List<String> parameters = new ArrayList<>();

        @Parameter(description = "Padding character",
                names = {"-p", "--padding-character"})
        public char paddingCharacter = '.';

        @Override
        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public char getPaddingCharacter() {
            return paddingCharacter;
        }

        @Override
        public boolean doEmbedSequences() {
            return parameters.size() == 2;
        }

        @Override
        public void validate() {
            super.validate();
        }
    }
}
