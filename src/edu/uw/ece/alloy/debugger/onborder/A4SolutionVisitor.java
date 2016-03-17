package edu.uw.ece.alloy.debugger.onborder;

import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.uw.ece.alloy.debugger.onborder.SigFieldWrapper.FieldInfo; 

public class A4SolutionVisitor {

	public static List<SigFieldWrapper> getSigs(A4Solution sol) throws Err {

		List<SigFieldWrapper> sigs = new ArrayList<>();
		for (Sig sig : sol.getAllReachableSigs()) {

			if(sig.builtin) continue;
			
			SigFieldWrapper sigWrapper = new SigFieldWrapper(sig.label.replace("this/", ""));
			for (Decl decl : sig.getFieldDecls()) {

				Field field = (Field) decl.get();
				String type = field.type().toString().replaceAll("[{\\[\\]}]", "").replace("this/", "");
				String[] typeParts = type.split("->");
				
				FieldInfo info = sigWrapper.new FieldInfo(field.label, type, typeParts);
				sigWrapper.addField(info);
			}
			
			sigs.add(sigWrapper);
		}
		
		return sigs;
	}

}
