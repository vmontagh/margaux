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
			
			String sigType = sig.label.replace("this/", "");
			String sigName = getCamelCase(sigType);
			String paramDecl = String.format(", %s: set %s", sigName, sigType);
			SigFieldWrapper sigWrapper = new SigFieldWrapper(sigType, paramDecl, null);
			
			for (Decl decl : sig.getFieldDecls()) {

				Field field = (Field) decl.get();
				String type = field.type().toString().replaceAll("[{\\[\\]}]", "").replace("this/", "");
				String[] typeParts = type.split("->");
				String param = String.format("%s: %s", field.label, type);
				
				FieldInfo info = sigWrapper.new FieldInfo(field.label, type, param, typeParts);
				sigWrapper.addField(info);
			}
			
			sigs.add(sigWrapper);
		}
		
		return sigs;
	}
	
	private static String getCamelCase(String in) {
		if(in == null || in.isEmpty()) return in;
		
		boolean lenG1 = in.length() > 1;
		return Character.toLowerCase(in.charAt(0)) + (lenG1 ? in.substring(1) : "");
	}

}
