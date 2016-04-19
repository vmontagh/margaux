package edu.uw.ece.alloy.debugger.onborder.propgen;

import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.HolaProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult;
import kodkod.instance.Instance;

public class HolaProcessedResult extends ProcessedResult {
    
    private static final long serialVersionUID = -8861660779730219972L;
    
    private final A4Solution sat;
    private final Instance instance;
    private final boolean lastResult;
    
    public HolaProcessedResult(final Instance instance) {
        super(HolaProcessingParam.EMPTY_PARAM);
        this.instance = instance;
        
        this.sat = null;
        this.lastResult = false;
    }
    
    public HolaProcessedResult(final A4Solution sat, HolaProcessingParam param) {
        super(param);
        this.sat = sat;
        
        this.instance = null;
        this.lastResult = true;
    }
    
    public Instance getInstance() {
        
        return this.instance;
    }
    
    public A4Solution getSAT() {
        
        return this.sat;
    }
    
    public boolean isLast() {
        
        return this.lastResult;
    }

    /* (non-Javadoc)
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.ProcessedResult#getEmptyParam()
     */
    @Override
    protected ProcessingParam getEmptyParam() {
        return HolaProcessingParam.EMPTY_PARAM;
    }
}
