package io.repseq.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

public class FromFastaAction extends FromFastaActionAbstract<FromFastaAction.ActionParameters> {
    public FromFastaAction() {
        super(new ActionParameters());
    }

    @Override
    public String command() {
        return "fromFasta";
    }

    @Parameters(commandDescription = "Creates boilerplate JSON library from existing fasta file.")
    public static final class ActionParameters extends FromFastaParametersAbstract {
        @Parameter(description = "input.fasta output.json")
        public List<String> parameters = new ArrayList<>();

        @Override
        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public boolean doEmbedSequences() {
            return false;
        }

        @Override
        public char getPaddingCharacter() {
            return 0;
        }

        @Override
        public void validate() {
            super.validate();
            if (parameters.size() != 2)
                throw new ParameterException("Wrong number of arguments.");
        }
    }
}
