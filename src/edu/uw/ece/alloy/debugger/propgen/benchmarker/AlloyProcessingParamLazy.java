package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;


public class AlloyProcessingParamLazy extends AlloyProcessingParam{

	private static final long serialVersionUID = -4596969763967530052L;

	
	final public static AlloyProcessingParamLazy EMPTY_PARAM = new AlloyProcessingParamLazy();

	protected AlloyProcessingParamLazy(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		super(alloyCoder,priority, tmpDirectory);
	}
	
	protected AlloyProcessingParamLazy(final PropertyToAlloyCode alloyCoder, int priority) {
		super(alloyCoder,priority);
	}
	
	protected AlloyProcessingParamLazy() {
		super();
	}
	
	/**
	 * The following create methods are added in order to make an instance of the object itself. It will be
	 * used for composition. The subclasses also have such methods and their functionality will be composed
	 * at runtime with the property generators.   
	 */
	protected AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority, File tmpDirectory) {
		return new AlloyProcessingParamLazy(alloyCoder,  priority, tmpDirectory);
	}

	public AlloyProcessingParam createIt(final PropertyToAlloyCode alloyCoder, int priority) {
		return new AlloyProcessingParamLazy(alloyCoder,  priority);
	}
	
	public AlloyProcessingParam createIt(AlloyProcessingParamLazy param) {
		return new AlloyProcessingParamLazy(param.alloyCoder,  param.priority, param.tmpDirectory);
	}
	

	public AlloyProcessingParam prepareToSend() throws Exception{	
		return this;
	}	

	public AlloyProcessingParam prepareToUse() throws Exception{	
		return dumpAll();
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return isEqual( (AlloyProcessingParamLazy) obj );
	}
	
	
}
