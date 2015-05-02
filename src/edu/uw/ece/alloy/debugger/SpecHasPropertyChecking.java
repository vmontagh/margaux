package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.util.Set;

public class SpecHasPropertyChecking extends PropertyCheckingSource {

	
	final String predicateANDNameFormat = new String("%s"+SEPARATOR+"_A_n_D_"+SEPARATOR+"%s"+SEPARATOR+"%s");
	final String propertyRunningFormat = "pred %1$s{ (%2$s and %3$s)  %5$s}\n run %1$s %4$s\n";
	
	
	final public String predicateName;
	final public String predicateBody;
	
	public SpecHasPropertyChecking(File sourceFile_, String property_,
			String fieldName_, Set<String> binaryProperties_,
			Set<String> ternaryProperties_, String sigs_, String openModule_,
			String openStatements_, String functions_, String commandHeader_,
			String formula_, String commandScope_, String fact_) {
		super(sourceFile_, property_, fieldName_, binaryProperties_,
				ternaryProperties_, sigs_, openModule_, openStatements_,
				functions_, commandHeader_, formula_, commandScope_, fact_);

		this.predicateName = String.format(predicateANDNameFormat, commandHeader, sanitizedPropertyName, sanitizedFieldName);
		String empty = "";
		if ( !property_.contains("EMPTY")  )
			empty = " and " + emptyProperty ;
		this.predicateBody = String.format(propertyRunningFormat, predicateName, property,  formula, commandScope, empty );
	}

	@Override
	protected String makeNewFileName() {
		final String newFileName = sourceFile.getName().replace(".als", "_tc.als");

		return predicateName+"_"+newFileName;
	}

	@Override
	protected String getNewStatement() {
		return this.predicateBody;
	}
	
	@Override
	public String toString() {
		return "SpecToPropertyChecking [predicateName=" + predicateName
				+ ", predicateBody=" + predicateBody + super.toString() + "]";
	}

	@Override
	public boolean repOk(){
		return super.repOk() && this.predicateName != null && !this.predicateName.trim().isEmpty() &&
				this.predicateBody != null && !this.predicateBody.trim().isEmpty() ;
	}

}
