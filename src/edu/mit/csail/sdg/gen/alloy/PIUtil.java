package edu.mit.csail.sdg.gen.alloy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class PIUtil {
	
	/**
	 * This function takes a sig and returns all the fields declared for it and its parents. 
	 * @param sig
	 * @return
	 */
	static public Collection<Sig.Field> getAllFields(final Sig sig){
		List<Sig.Field> result = new ArrayList<Sig.Field>();
		Sig tmpSig = sig;
		while(true){
			result.addAll(tmpSig.getFields().makeCopy());
			if(tmpSig instanceof Sig.PrimSig && !((Sig.PrimSig)tmpSig).builtin )
				tmpSig = ((Sig.PrimSig)tmpSig).parent;
			else
				break;
		}
		
		return result;
	}
	
	static public String nameSanitizer(String name){
		return name.replace("{","").replace("}", "").replace("$", "").replace("this/","");
	}
	
	static public String tailDot(String name){
		int lst = name.lastIndexOf('.');
		return lst >= 0 ? name.substring(lst+1) : name;
	}
	
	//Copied from the CompParser
	static public Expr mult(Expr x) throws Err {
		if (x instanceof ExprUnary) {
			ExprUnary y=(ExprUnary)x;
			if (y.op==ExprUnary.Op.SOME) return ExprUnary.Op.SOMEOF.make(y.pos, y.sub);
			if (y.op==ExprUnary.Op.LONE) return ExprUnary.Op.LONEOF.make(y.pos, y.sub);
			if (y.op==ExprUnary.Op.ONE)  return ExprUnary.Op.ONEOF.make(y.pos, y.sub);
		}
		return x;
	}
	
}
