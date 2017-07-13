package io.repseq.learn;

import com.milaboratory.core.sequence.NucleotideSequence;
import io.repseq.learn.param.GermlineMatchParameters;
import io.repseq.learn.param.InsertionParameters;
import io.repseq.learn.param.SegmentTrimmingParameterProvider;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by mikesh on 7/13/17.
 */
public class VDJTransitionGenerator extends TransitionGeneratorBase<VDJHmmTransitions> {
    private final InsertionParameters vdInsertionParamters, djInsertionParameters;

    public VDJTransitionGenerator(GermlineMatchParameters germlineMatchParameters,
                                  SegmentTrimmingParameterProvider segmentTrimmingParameterProvider,
                                  GermlineSequenceProvider germlineSequenceProvider,
                                  InsertionParameters vdInsertionParamters,
                                  InsertionParameters djInsertionParameters) {
        super(germlineMatchParameters, segmentTrimmingParameterProvider, germlineSequenceProvider);
        this.vdInsertionParamters = vdInsertionParamters;
        this.djInsertionParameters = djInsertionParameters;
    }

    @Override
    public VDJHmmTransitions generate(SegmentTuple segments, NucleotideSequence query) {
        throw new NotImplementedException();
    }
}
