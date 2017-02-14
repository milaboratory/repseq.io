package io.repseq.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.IParameterSplitter;
import com.milaboratory.core.io.sequence.fasta.FastaWriter;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.ParseUtil;
import io.repseq.core.GeneFeature;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class CLIUtils {
    private CLIUtils() {
    }

    public static FastaWriter<NucleotideSequence> createSingleFastaWriter(String fileName) throws IOException {
        if (fileName.equals("."))
            return new FastaWriter<>(System.out);
        return new FastaWriter<>(fileName);
    }

    public static final class GeneFeatureSplitter implements IParameterSplitter {
        @Override
        public List<String> split(String value) {
            return Arrays.asList(ParseUtil.splitWithBrackets(value, ',', "(){}[]"));
        }
    }

    /**
     * Represents pair of gene feature object along with original string representation specified by user.
     */
    public static final class GeneFeatureWithOriginalName {
        public final GeneFeature feature;
        public final String originalName;

        public GeneFeatureWithOriginalName(GeneFeature feature, String originalName) {
            this.feature = feature;
            this.originalName = originalName;
        }
    }

    public static final class GeneFeatureConverter implements IStringConverter<GeneFeatureWithOriginalName> {
        @Override
        public GeneFeatureWithOriginalName convert(String value) {
            try {
                return new GeneFeatureWithOriginalName(GeneFeature.parse(value), value);
            } catch (IllegalArgumentException e) {
                throw new ParameterException("Can't parse gene feature '" + value + "'. " + e.getMessage());
            }
        }
    }

    public static final class GeneFeatureValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            try {
                GeneFeature.parse(value);
            } catch (IllegalArgumentException e) {
                throw new ParameterException("Can't parse gene feature '" + value + "' in parameter " + name + ". " + e.getMessage());
            }
        }
    }
}
