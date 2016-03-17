package edu.uw.ece.alloy.debugger.onborder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
		
		try {
			this.generateIsInstance(this.out);
		} catch (Err e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void generateIsInstance(PrintWriter out) throws Err, IOException {
				
		this.out = out;		
		
		// Generate arguments passed into predicates
		StringBuilder args = new StringBuilder();
		StringBuilder params = new StringBuilder();
		for(SigFieldWrapper sig : this.sigs) {
			
			String sigName = this.getCamelCase(sig.getSig());
			args.append(String.format(", %s", sig.getSig()));
			params.append(String.format(", %s: set %s", sigName, sig.getSig()));
			
			for(FieldInfo field : sig.getFields()) {
				args.append(String.format(", %s", field.getLabel()));
				params.append(String.format(", %s: %s", field.getLabel(), field.getType()));
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
		
		this.out.flush();
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

		this.out.flush();
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

		this.out.flush();
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
