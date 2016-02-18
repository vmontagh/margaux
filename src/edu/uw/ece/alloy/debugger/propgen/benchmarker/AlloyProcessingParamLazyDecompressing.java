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
	
	protected AlloyProcessingParamLazyDecompressing(
			PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory,
			DBConnectionInfo dBConnectionInfo) {
		super(alloyCoder, priority, tmpDirectory, dBConnectionInfo);
		// TODO Auto-generated constructor stub
	}

	protected AlloyProcessingParamLazyDecompressing(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory){
		super(alloyCoder, priority, tmpDirectory);
	}
	
	protected AlloyProcessingParamLazyDecompressing(final PropertyToAlloyCode alloyCoder, int priority){
		super(alloyCoder, priority);
	}
	
	protected AlloyProcessingParamLazyDecompressing(){
		super();
	}
	
	protected AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory, DBConnectionInfo dBConnectionInfo) {
		return new AlloyProcessingParamLazyDecompressing(alloyCoder, priority, tmpDirectory, dBConnectionInfo);
	}

	protected AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		return new AlloyProcessingParamLazyDecompressing(alloyCoder, priority, tmpDirectory);
	}
	
	public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParamLazyDecompressing(alloyCoder, priority);
	}
	
	public AlloyProcessingParam createIt(AlloyProcessingParamLazyDecompressing param) {
		return new AlloyProcessingParamLazyDecompressing(param.alloyCoder, param.priority, param.tmpDirectory, param. dBConnectionInfo);
	}
	
	public AlloyProcessingParam prepareToSend(){
		return this;
	}

	public AlloyProcessingParam prepareToUse(){
		try {
				return AlloyProcessingParamLazyCompressing.EMPTY_PARAM.createIt(this.alloyCoder.deCompress(), this.priority, this.tmpDirectory, this. dBConnectionInfo) ;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to compress the object: "+ this, e);			
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}