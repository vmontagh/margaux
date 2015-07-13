package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AlloyProcessingParamLazyCompressing and AlloyProcessingParamLazyDecompressing 
 * are a pair of Compressing. Compressing returns the other one and vice versa.
 * @author vajih
 *
 */
public class AlloyProcessingParamLazyCompressing extends
		AlloyProcessingParamLazy {
	
	final static Logger logger = Logger.getLogger(AlloyProcessingParamLazyCompressing.class.getName()+"--"+Thread.currentThread().getName());
	
	private static final long serialVersionUID = -7212397055597409504L;
	
	final public static AlloyProcessingParamLazyCompressing EMPTY_PARAM = new AlloyProcessingParamLazyCompressing();
	
	
	protected AlloyProcessingParamLazyCompressing(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory){
		super(alloyCoder, priority, tmpDirectory);
	}
	
	protected AlloyProcessingParamLazyCompressing(final PropertyToAlloyCode alloyCoder, int priority){
		super(alloyCoder, priority);
	}
	
	protected AlloyProcessingParamLazyCompressing(){
		super();
	}
	
	protected AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		return new AlloyProcessingParamLazyCompressing(alloyCoder, priority, tmpDirectory);
	}
	
	public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParamLazyCompressing(alloyCoder, priority);
	}
	
	public AlloyProcessingParam createIt(AlloyProcessingParamLazyCompressing param) {
		return new AlloyProcessingParamLazyCompressing(param.alloyCoder, param.priority, param.tmpDirectory);
	}

	
	public AlloyProcessingParam prepareToSend(){
		try {
			return AlloyProcessingParamLazyDecompressing.EMPTY_PARAM.createIt(alloyCoder.compress(), this.priority, this.tmpDirectory);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] " + "Unable to compress the object: "+ this, e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	

	public AlloyProcessingParam prepareToUse(){
		return this;
	}
	

	
}
