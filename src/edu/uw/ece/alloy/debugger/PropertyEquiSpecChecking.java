package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.util.Set;

import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;

public class PropertyEquiSpecChecking extends PropertyCheckingSource {

	final String assertionFFNameFormat = new String("%s"+SEPARATOR+"_F__F_"+SEPARATOR+"%s"+SEPARATOR+"%s");
	//Spec => relation
	final public static String propertyCheckingFormatForward = "assert %1$s{ %2$s %3$s %4$s}\n check %1$s %5$s\n";
	
	final public String assertionName;
	final public String assertionBody;

	
	public PropertyEquiSpecChecking(File sourceFile_, String property_,
			String fieldName_, Set<String> binaryProperties_,
			Set<String> ternaryProperties_, String sigs_, String openModule_,
			String openStatements_, String functions_, String commandHeader_,
			String formula_, String commandScope_, String fact_) {
		super(sourceFile_, property_, fieldName_, binaryProperties_,
				ternaryProperties_, sigs_, openModule_, openStatements_,
				functions_, commandHeader_, formula_, commandScope_, fact_);
		
		this.assertionName = String.format(assertionFFNameFormat, commandHeader, sanitizedPropertyName, sanitizedFieldName);
		this.assertionBody = String.format(propertyCheckingFormatForward, assertionName, formula, ExprBinary.Op.IFF.toString(), property,  commandScope );
		
	}

	@Override
	protected String makeNewFileName() {
		final String newFileName = sourceFile.getName().replace(".als", "_tc.als");

		return assertionName+"_"+newFileName;
	}

	@Override
	protected String getNewStatement() {
		return this.assertionBody;
	}
	
	@Override
	public String toString() {
		return "SpecToPropertyChecking [assertionName=" + assertionName
				+ ", assertionBody=" + assertionBody + super.toString() + "]";
	}

	@Override
	public boolean repOk(){
		return super.repOk() && this.assertionName != null && !this.assertionName.trim().isEmpty() &&
				this.assertionBody != null && !this.assertionBody.trim().isEmpty() ;
	}

}
