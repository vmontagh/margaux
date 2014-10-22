package edu.mit.csail.sdg.gen.alloy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class FieldDependecyGroup{

	private final HashMap<String, Integer> fldLevel = new HashMap<String, Integer>();  
	private final HashMap<String, Set<String>> depTable = new HashMap<String, Set<String>>();
	private final HashMap<String, Sig.Field> name2Field = new HashMap<String, Sig.Field>();

	private void putDepField_(final String key,final String value) {
		Set<String> crntFldSet = this.depTable.get(key);
		if(crntFldSet == null){
			crntFldSet = new HashSet<String>();
		}
		if (value!=null)
			crntFldSet.add(value);
		this.depTable.put(key.toString(), crntFldSet);
		for(String fld: this.depTable.keySet()){
			this.fldLevel.put(fld,getLevel_(fld));
		}
	}

	public void putDepField(final Sig.Field key,final Sig.Field value) {
		if(key!=null)
			this.name2Field.put(key.toString(), key);
		if(value!=null)
			this.name2Field.put(value.toString(), value);
		putDepField_(key.toString(),value != null ?value.toString():null);
	}

	public void putDepExprs(final Sig.Field key,final Collection<Expr> c) {
		boolean added = false;
		for(Expr expr:c){
			if (expr instanceof Sig.Field) {
				putDepField(key,(Sig.Field)expr);
				added = true;
			}
		}	
		if(!added)
			putInpendent(key);
	}

	public void putInpendent(final Sig.Field key){

		putDepField(key, null);
	}

	private int getLevel_(final String key){
		String crnt = key;
		Set<String> crntFldSet = this.depTable.get(crnt);
		int level = 0;
		if(crntFldSet!=null && !crntFldSet.isEmpty()){
			level++;
			for(String prntfld: crntFldSet){
				level = Math.max(level, getLevel_(prntfld)+1);
			}
		}
		return level;
	}

	public int getLevel(final Sig.Field key){
		return fldLevel.get(key);
	}

	public Set<Sig.Field> getLeveli(int i){
		Set<Sig.Field> ret = new HashSet<Sig.Field>();
		for(String fld:fldLevel.keySet()){
			if(fldLevel.get(fld) == i ){
				ret.add(name2Field.get(fld));
			}
		}
		return ret;
	}

	public Set<Sig.Field> getDepTo(final Sig.Field key){
		Set<String> depTo = this.depTable.get(key.toString());
		Set<Sig.Field> ret = new HashSet<Sig.Field>();
		if(depTo != null)
			for(String fldStr :depTo){
				ret.add(name2Field.get(fldStr));
			}
		return ret;
	}

	public int maxLevel(){
		int ret = 0;
		for(Integer i: fldLevel.values()){
			ret = Math.max(ret, i);
		}
		return ret;
	}

	public String toString(){

		for(String fld: this.depTable.keySet()){
			this.fldLevel.put(fld,getLevel_(fld));
		}

		StringBuilder ret =  new StringBuilder();
		ret.append("{");
		for(String fld:depTable.keySet())
			ret.append("[Name=").append(fld).append(",Level=").append(getLevel_(fld)).append(",Deps=").append(depTable.get(fld)).append("]\n");
		if(ret.length() > 0) ret.delete(ret.length()-1, ret.length());
		ret.append("}"); 
		return ret.toString();
	}
}
