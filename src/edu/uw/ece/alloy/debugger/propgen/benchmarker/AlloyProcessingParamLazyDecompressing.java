package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.util.Utils;

public class AlloyProcessingParamLazyDecompressing extends
	AlloyProcessingParamLazyCompressing {

	private static final long serialVersionUID = -7212397055597409504L;
	final static Logger logger = Logger.getLogger(AlloyProcessingParamLazyDecompressing.class.getName()+"--"+Thread.currentThread().getName());
	
	final public static AlloyProcessingParamLazyDecompressing EMPTY_PARAM = new AlloyProcessingParamLazyDecompressing();
	
	protected AlloyProcessingParamLazyDecompressing(final PropertyToAlloyCode alloyCoder, int priority){
		super(alloyCoder, priority);
	}
	
	protected AlloyProcessingParamLazyDecompressing(){
		super();
	}
	
	public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParamLazyDecompressing(alloyCoder, priority);
	}
	
	public AlloyProcessingParam createIt(AlloyProcessingParamLazyDecompressing param) {
		return createIt(param.alloyCoder, param.priority);
	}
	
	public AlloyProcessingParam prepareToSend(){
		return this;
	}

	public AlloyProcessingParam prepareToUse(){
		try {
				return AlloyProcessingParamLazyCompressing.EMPTY_PARAM.createIt(this.alloyCoder.decompress(), this.priority) ;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to compress the object: "+ this, e);			
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
