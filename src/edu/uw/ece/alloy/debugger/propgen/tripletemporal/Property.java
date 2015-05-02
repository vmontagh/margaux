package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

/**
 * The triple relation is r:S->A->B
 * S has a temporal relation
 * A is called middle
 * B is called End
 * @author vajih
 *
 */
public abstract class Property {
	
	//The local variable names used for generated preds. 
	public final String RName;
	public final String SName;
	public final String SNext;
	public final String SFirst;
	public final String MiddleName;
	public final String EndName;
	
	//The concrete variables store the parameters names that are passed to a pred call. 
	public final String RConcreteName;
	public final String SConcreteName;
	public final String SConcreteNext;
	public final String SConcreteFirst;
	public final String MConcreteName;
	public final String EConcreteName;
	


	public Property(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName) {
		super();
		RName = rName;
		SName = sName;
		SNext = sNext;
		SFirst = sFirst;
		MiddleName = middleName;
		EndName = endName;
		RConcreteName = rConcreteName;
		SConcreteName = sConcreteName;
		SConcreteNext = sConcreteNext;
		SConcreteFirst = sConcreteFirst;
		MConcreteName = mConcreteName;
		EConcreteName = eConcreteName;
	}
	protected abstract String getPredecessor();
	protected abstract String getSuccessor();
	
	protected abstract boolean isConsistent();
	
	protected  String genPredName(){
		return this.getClass().getSimpleName()+"_";
	}
	protected String genParameters(){
		return RName+": univ->univ->univ, "+SName+", "+MiddleName+", "+EndName+": univ, "+SFirst+": univ, "+SNext+": univ->univ";
	}
	protected abstract String genBody();
	
	protected String genParametesCall(){
		return RConcreteName+", "+SConcreteName+", "+MConcreteName+", "+EConcreteName+", "+SConcreteFirst+", "+SConcreteNext;
	}
	
	public String generateProp(){
		
		if (!isConsistent()){
			return "";
		}
		
		StringBuilder result = new StringBuilder();
		
		result.append("pred ").append(genPredName()).append("[").append(genParameters()).append("]{\n");
		result.append(genBody()).append("\n}");
		return result.toString();
	}

	
	public String genPredCall(){
		if (!isConsistent()){
			return "";
		}
		
		StringBuilder result = new StringBuilder();
		
		
		result.append(genPredName()).append("[").append(genParametesCall()).append("]\n");
		
		return result.toString();
	}
	
}
