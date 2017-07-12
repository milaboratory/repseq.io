package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikesh on 7/11/17.
 */
public class DummyVJParameters {
    static final Map<String, NucleotideSequence> refs = new HashMap<>();
    static final Map<String, SegmentTrimmingParameters> trimming = new HashMap<>();
    static final Map<SegmentTuple, Double> segmentUsageMap = new HashMap<>();

    static {
        refs.put("V0", new NucleotideSequence("ATCAGCCATGCA"));
        trimming.put("V0", new VSegmentTrimmingParameters(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.0}));
        refs.put("V1", new NucleotideSequence("ATCAGCCATGCA"));
        trimming.put("V1", new VSegmentTrimmingParameters(new double[]{0, 0, 0, 0, 0, 0, 0, 0.025, 0.05, 0.8, 0.1, 0.025}));
        refs.put("V2", new NucleotideSequence("AGCATCGACG"));
        trimming.put("V2", new VSegmentTrimmingParameters(new double[]{0, 0, 0, 0, 0, 0, 0.01, 0.33, 0.33, 0.33}));
        refs.put("J0", new NucleotideSequence("ACGCGTGC"));
        trimming.put("J0", new JSegmentTrimmingParameters(new double[]{1.0, 0, 0, 0, 0, 0, 0, 0}));
        refs.put("J1", new NucleotideSequence("ACGCGTGC"));
        trimming.put("J1", new JSegmentTrimmingParameters(new double[]{0.025, 0.05, 0.8, 0.1, 0.025, 0, 0, 0}));
        refs.put("J2", new NucleotideSequence("GCGGCGAATTATGC"));
        trimming.put("J2", new JSegmentTrimmingParameters(new double[]{0.025, 0.05, 0.8, 0.1, 0.025, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        segmentUsageMap.put(new SegmentTuple("V1", "J1"), 0.5);
        segmentUsageMap.put(new SegmentTuple("V2", "J1"), 0.25);
        segmentUsageMap.put(new SegmentTuple("V1", "J2"), 0.125);
        segmentUsageMap.put(new SegmentTuple("V2", "J2"), 0.125);
    }

    public static final GermlineSequenceProvider germlineSequenceProvider = new
            HashedGermlineSequenceProvider(refs);
    public static final SegmentTrimmingParameterProvider segmentiTrimmingProvider = new
            HashedSegmentTrimmingParameterProvider(trimming);
    public static SegmentUsage segmentUsage = new JointSegmentSegmentUsage(segmentUsageMap);
    public static InsertionParameters vjInsertionParameters = new SimpleInsertionParameters(new double[]{0.5, 0.2, 0.1,
            0.1, 0.05, 0.05});
    public static GermlineMatchParameters germlineMatchParameters = new SimpleGermlineMatchParameters();
}
