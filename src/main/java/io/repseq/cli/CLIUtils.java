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

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.IParameterSplitter;
import com.milaboratory.core.io.sequence.fasta.FastaWriter;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.util.ParseUtil;
import io.repseq.core.GeneFeature;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public final class CLIUtils {
    private CLIUtils() {
    }

    public static BufferedReader createBufferedReader(String fileName) throws IOException {
        return new BufferedReader(new InputStreamReader(
                fileName.equals(".") ?
                        System.in :
                        new FileInputStream(fileName)), 128 * 1024);
    }

    public static BufferedOutputStream createBufferedOutputStream(String fileName) throws IOException {
        return new BufferedOutputStream(
                fileName.equals(".") ?
                        System.out :
                        new FileOutputStream(fileName), 128 * 1024);
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
