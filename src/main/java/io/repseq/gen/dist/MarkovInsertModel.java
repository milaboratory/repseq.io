package io.repseq.gen.dist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NucleotideAlphabet;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceBuilder;
import com.milaboratory.util.ArraysUtils;
import io.repseq.core.ReferencePoint;
import io.repseq.core.SequencePartitioning;
import io.repseq.core.VDJCGene;
import io.repseq.gen.GGene;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.*;

public abstract class MarkovInsertModel implements InsertModel {
    public final IndependentIntModel lengthDistribution;
    /**
     * Map like:
     * "A>C": 0.3,
     * "A>A": 0.1
     */
    public final Map<String, Double> distribution;
    private final boolean fromLeft;

    private MarkovInsertModel(IndependentIntModel lengthDistribution,
                              Map<String, Double> distribution,
                              boolean fromLeft) {
        this.lengthDistribution = lengthDistribution;
        this.distribution = distribution;
        this.fromLeft = fromLeft;
    }

    private static ReferencePoint beginPoint(boolean fromLeft, boolean v) {
        if (fromLeft)
            if (v)
                return ReferencePoint.VEndTrimmed;
            else
                return ReferencePoint.DEndTrimmed;
        else if (v)
            return ReferencePoint.DBeginTrimmed;
        else
            return ReferencePoint.JBeginTrimmed;

    }

    @Override
    public InsertGenerator create(RandomGenerator random, final boolean v,
                                  List<VDJCGene> vGenes, List<VDJCGene> dGenes,
                                  List<VDJCGene> jGenes, List<VDJCGene> cGenes) {
        Map<Byte, List<Pair<Byte, Double>>> distParams = new HashMap<>();
        for (Map.Entry<String, Double> s : distribution.entrySet()) {
            String[] split = s.getKey().split(">");
            if (split.length != 2 || split[0].length() != 1 || split[1].length() != 1)
                throw new IllegalArgumentException("Illegal distribution key: " + s.getKey() + ". " +
                        "Expected something like \"A>C\"");
            byte codeFrom = NucleotideSequence.ALPHABET.symbolToCode(split[0].charAt(0));
            byte codeTo = NucleotideSequence.ALPHABET.symbolToCode(split[1].charAt(0));
            if (codeFrom == -1 || codeTo == -1)
                throw new IllegalArgumentException("Illegal nucleotide in: " + s.getKey() + ".");
            List<Pair<Byte, Double>> pairs = distParams.get(codeFrom);
            if (pairs == null)
                distParams.put(codeFrom, pairs = new ArrayList<>());
            pairs.add(new Pair<>(codeTo, s.getValue()));
        }
        final Map<Byte, EnumeratedDistribution<Byte>> dists = new HashMap<>();
        for (byte from = 0; from < NucleotideSequence.ALPHABET.basicSize(); from++) {
            List<Pair<Byte, Double>> d = distParams.get(from);
            if (d == null)
                throw new IllegalArgumentException("No distribution for letter: " +
                        NucleotideSequence.ALPHABET.codeToSymbol(from));
            dists.put(from, new EnumeratedDistribution<>(random, d));
        }
        final IndependentIntGenerator lengthDist = lengthDistribution.create(random);
        return new InsertGenerator() {
            @Override
            public NucleotideSequence generate(GGene gene) {
                ReferencePoint point = beginPoint(fromLeft, v);
                int pointPosition = gene.getPartitioning().getPosition(point);
                if (pointPosition == -1)
                    throw new RuntimeException("Point " + point + " is not available for gene " + gene);
                byte letter = gene.getSequence(new Range(pointPosition, pointPosition + 1)).codeAt(0);
                int length = lengthDist.sample();
                byte[] array = new byte[length];
                for (int i = 0; i < length; i++) {
                    byte cLetter = dists.get(letter).sample();
                    array[i] = cLetter;
                    letter = cLetter;
                }
                if (!fromLeft)
                    ArraysUtils.reverse(array);
                return NucleotideSequence.ALPHABET
                        .createBuilder().ensureCapacity(length).append(array).createAndDestroy();
            }
        };
    }

    public static final class Model5 extends MarkovInsertModel {
        @JsonCreator
        public Model5(@JsonProperty("lengthDistribution") IndependentIntModel lengthDistribution,
                      @JsonProperty("distribution") Map<String, Double> distribution) {
            super(lengthDistribution, distribution, true);
        }
    }

    public static final class Model3 extends MarkovInsertModel {
        @JsonCreator
        public Model3(@JsonProperty("lengthDistribution") IndependentIntModel lengthDistribution,
                      @JsonProperty("distribution") Map<String, Double> distribution) {
            super(lengthDistribution, distribution, false);
        }
    }
}
