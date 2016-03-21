package edu.uw.ece.alloy.debugger.onborder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.onborder.SigFieldWrapper.FieldInfo;
import kodkod.ast.Formula;

public class CodeGenerator {

	private static final String RUN = "\npred p[] {}\nrun p";
	
	private PrintWriter out;
	private String indent;
	private A4Solution sol;
	private List<SigFieldWrapper> sigs;

	public CodeGenerator(A4Solution sol) {
		this.indent = "";
		this.out = new PrintWriter(System.out);
		this.sol = sol;
		
		try {
			this.sigs = A4SolutionVisitor.getSigs(sol);
		} catch (Err e) {
			e.printStackTrace();
		}		
	}

	public CodeGenerator(A4Solution sol, PrintWriter writer) {
		this(sol);
		this.out = writer;
	}
	
	public void run() {
		try {
			this.generateDiffs(this.out);
			this.generateIsInstance(this.out);
			this.generateMarginalDifferences(out);
		} catch (Err e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		this.out.flush();
	}
	
	private void generateDiffs(PrintWriter out) {
		
		this.out = out;
		
		for(SigFieldWrapper sigWrap: this.sigs) {

			ln();		
			
			String sigName = this.getCamelCase(sigWrap.getSig());
			String s1 = String.format("%s: set %s", sigName, sigWrap.getSig());
			String s2 = String.format("%s': set %s", sigName, sigWrap.getSig());
			String s3 = String.format("%s'': set %s", sigName, sigWrap.getSig());
			
			println("pred diff%s[%s, %s, %s] {", this.getPascalCase(sigWrap.getSig()), s1, s2, s3);
			indent();
			println("%1$s != %1$s' implies (%1$s'' = %1$s' - %1$s and %1$s'' + %1$s = %1$s') else no %1$s''", sigName);
			outdent();
			println("}");
			
			for(FieldInfo field : sigWrap.getFields()) {
				ln();		
				s1 = String.format("%s: %s", field.getLabel(), field.getType());
				s2 = String.format("%s': %s", field.getLabel(), field.getType());
				s3 = String.format("%s'': %s", field.getLabel(), field.getType());

				println("pred diff%s[%s, %s, %s] {", this.getPascalCase(field.getLabel()), s1, s2, s3);
				indent();
				println("%1$s != %1$s' implies (%1$s'' = %1$s' - %1$s and %1$s'' + %1$s = %1$s') else no %1$s''", field.getLabel());
				outdent();
				println("}");
			}
			
		}
		
	}
	

	private void generateMarginalDifferences(PrintWriter out) {
		
		this.out = out;
		ln();	

		StringBuilder isInstanceCall = new StringBuilder();
		StringBuilder isNotInstanceCall = new StringBuilder();
		StringBuilder deltaCalls = new StringBuilder();
		
		StringBuilder quantifier_1 = new StringBuilder();
		StringBuilder isInstanceCall_1 = new StringBuilder();
		StringBuilder isNotInstanceCall_1 = new StringBuilder();
		StringBuilder deltaCalls_1 = new StringBuilder();
		
		println("pred findMarginalInstances[] {");
		indent();
		print(indent);
		print("some ");
		
		Iterator<SigFieldWrapper> sigItr = this.sigs.iterator(); 
		while(sigItr.hasNext()) {
	
			SigFieldWrapper sigWrap = sigItr.next();
			String sigName = this.getCamelCase(sigWrap.getSig());
			print("%1$s, %1$s', %1$s'': set %2$s", sigName, sigWrap.getSig());
			quantifier_1.append(String.format("%1$s1, %1$s1', %1$s1'': set %2$s", sigName, sigWrap.getSig()));

			isInstanceCall.append(sigName);
			isNotInstanceCall.append(sigName + "'");
			deltaCalls.append(String.format("and delta%1$s[%2$s, %2$s', %2$s'']\n", this.getPascalCase(sigWrap.getSig()), sigName));
			
			isInstanceCall_1.append(sigName + "1");
			isNotInstanceCall_1.append(sigName + "1'");
			deltaCalls_1.append(String.format("and delta%1$s[%2$s1, %2$s1', %2$s1'']\n", this.getPascalCase(sigWrap.getSig()), sigName));
			
			if(sigItr.hasNext()) {
				print(", ");
				isInstanceCall.append(", ");
				isNotInstanceCall.append(", ");
				
				quantifier_1.append(", ");
				isInstanceCall_1.append(", ");
				isNotInstanceCall_1.append(", ");
			}
			
			Iterator<FieldInfo> itr = sigWrap.getFields().iterator();
			while(itr.hasNext()) {
				
				FieldInfo field = itr.next();
				print("%1$s, %1$s', %1$s'': set %2$s", field.getLabel(), field.getType());
				quantifier_1.append(String.format("%1$s1, %1$s1', %1$s1'': set %2$s", field.getLabel(), field.getType()));
						
				isInstanceCall.append(field.getLabel());
				isNotInstanceCall.append(field.getLabel() + "'");
				deltaCalls.append(String.format("and delta%1$s[%2$s, %2$s', %2$s'']\n", this.getPascalCase(field.getLabel()), field.getLabel()));
				
				isInstanceCall_1.append(field.getLabel() + "1");
				isNotInstanceCall_1.append(field.getLabel() + "1'");
				deltaCalls_1.append(String.format("and delta%1$s[%2$s1, %2$s1', %2$s1'']\n", this.getPascalCase(field.getLabel()), field.getLabel()));
				
				
				if(itr.hasNext()) {
					print(", ");
					isInstanceCall.append(", ");
					isNotInstanceCall.append(", ");
					
					quantifier_1.append(", ");
					isInstanceCall_1.append(", ");
					isNotInstanceCall_1.append(", ");
				}
			}
			
		}
		
		// Finish outer quatifier
		print(" | {");
		ln();
		indent();
		indent();
		println("(isInstance[%s]", isInstanceCall);
		println("and not isInstance[%s]", isNotInstanceCall);
		println("%s)", deltaCalls.toString().replaceAll("\n", "\n" + indent));
		
		outdent();
		println("and");
		indent();
		
		// Print inner quantifier
		println("all %s | {", quantifier_1);
		indent();
		indent();
		println("(isInstance[%s]", isInstanceCall_1);
		println("and not isInstance[%s]", isNotInstanceCall_1);
		println("%s)", deltaCalls_1.toString().replaceAll("\n", "\n" + indent));
		
		outdent();
		println("implies");
		indent();
		
		println("(");
		
		println(")");
		
		// Close inner quantifier
		outdent();
		println("}");
		
		// Close outer quantifier
		outdent();
		println("}");
		
		// Close predicate
		outdent();
		println("}");
	}
	
	private void generateIsInstance(PrintWriter out) throws Err, IOException {
				
		this.out = out;		
		
		// Generate arguments passed into predicates
		StringBuilder args = new StringBuilder();
		StringBuilder params = new StringBuilder();
		for(SigFieldWrapper sigWrap : this.sigs) {
			
//			String sigName = this.getCamelCase(sigWrap.getSig());
			args.append(String.format(", %s", sigWrap.getSig()));
//			params.append(String.format(", %s: set %s", sigName, sigWrap.getSig()));
			params.append(String.format(", %s", sigWrap.getParamDecl()));
			
			for(FieldInfo field : sigWrap.getFields()) {
				args.append(String.format(", %s", field.getLabel()));
//				params.append(String.format(", %s: %s", field.getLabel(), field.getType()));
				params.append(String.format(", %s", field.getParamDecl()));
			}
		}
		
		// Trim initial comma and space
		args.delete(0, 2);
		params.delete(0, 2);
		
		this.generateStructuralConstraint(params.toString(), out);
		this.generateIncludeInstance(params.toString(), out);

		ln();
		println("pred isInstance [%s] {", params);
		indent(); 
		println("includeInstance[%s]", args.toString());
		println("structuralConstraints[%s]", args.toString());
		outdent();
		println("}");
	}
	
	private void generateStructuralConstraint(String params, PrintWriter out) throws Err, IOException {

		this.out = out;
		ln();
		println("pred structuralConstraints [%s] {", params);
		indent(); 
		
		// Create temp file for extracted signatures
		File file = File.createTempFile("sig", ".als");
		String sigs = getSigString();
		Util.writeAll(file.getAbsolutePath(), sigs);
		
		// Translate signatures to KodKod
		List<Formula> formulas = A4CommandExecuter.getInstance().translateAlloy2KK(file.getAbsolutePath(), A4Reporter.NOP, "p");
		
		// Use Structural formulas from KodKod and make them pretty
		String regex = "this\\/([^\\s])*\\."; // Remove all this/*.
		for(Formula f: formulas) {
			 String constraint = f.toString().replaceAll(regex, "").replace("this/", "");
			 println(constraint);
		}
		
		outdent();
		println("}");
	}
	
	private void generateIncludeInstance(String params, PrintWriter out) {
		
		this.out = out;
		ln();
		println("pred includeInstance [%s] {", params);
		indent(); 
		
		for(SigFieldWrapper sig : this.sigs) {
			
			for(FieldInfo field : sig.getFields()) {
				
				String label = field.getLabel();
				String[] typeParts = field.getTypeComponents();
				for(int i = 0; i < typeParts.length; i++) {
					
					String join = label;
					for(int m = 0; m < i; m++) {
						join = "(" + typeParts[m] + "." + join + ")";
					}
					
					for(int n = typeParts.length - 1; n > i; n--) {
						join = "(" + join  + "." + typeParts[n] + ")";
					}
					
					println("%s in %s", join, typeParts[i]);
				}
				
			}
			
		}
		
		outdent();
		println("}");
	}
		
	private String getSigString() throws Err {
		
		String sigs = Field2ConstraintMapper.getSigDeclationViaPos(this.sol);
		sigs += CodeGenerator.RUN;
		
		return sigs;
		
	}
	
	private String getPascalCase(String in) {
		if(in == null || in.isEmpty()) return in;
		
		boolean lenG1 = in.length() > 1;
		return Character.toUpperCase(in.charAt(0)) + (lenG1 ? in.substring(1) : "");
	}
	
	private String getCamelCase(String in) {
		if(in == null || in.isEmpty()) return in;
		
		boolean lenG1 = in.length() > 1;
		return Character.toLowerCase(in.charAt(0)) + (lenG1 ? in.substring(1) : "");
	}
	
	private void println(final String s, Object... args) {
		println(String.format(s, args));
	}
	
	private void println(final String s) {
		
		out.print(indent);
		out.println(s);
	}
	
	private void ln() {
		
		out.println();
	}
	
	private void print(final String s, Object... args) {
		
		print(String.format(s, args));
	}
	
	private void print(final String s) {
		
		out.print(s);
	}

	private void indent() {
		
		indent = indent + "    ";
	}
	
	private void outdent() {
		
		indent = indent.substring(0, indent.length() - 4);
	}
	
}
