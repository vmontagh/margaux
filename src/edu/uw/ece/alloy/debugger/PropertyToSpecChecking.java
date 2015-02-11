/**
 * 
 */
package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.util.Set;

/**
 * @author vajih
 *
 */
public class PropertyToSpecChecking extends PropertyCheckingSource {

	
	public final String assertionFiNameFormat = new String("%s"+SEPARATOR+"_F__i_"+SEPARATOR+"%s"+SEPARATOR+"%s");
	public final String propertyCheckingFormatBackward = "assert %1$s{ (%2$s => %3$s) and %5$s}\n check %1$s %4$s\n";
	
	final public String assertionName;
	final public String assertionBody;
	/**
	 * @param sourceFile_
	 * @param property_
	 * @param fieldName_
	 * @param binaryProperties_
	 * @param ternaryProperties_
	 * @param sigs_
	 * @param openModule_
	 * @param openStatements_
	 * @param functions_
	 */
	public PropertyToSpecChecking(final File sourceFile_, final String property_,
			final String fieldName_, final Set<String> binaryProperties_,
			final Set<String> ternaryProperties_, final String sigs_, final String openModule_,
			final String openStatements_, final String functions_, final String commandHeader_, 
			final String formula_, final String commandScope_) {
		super(sourceFile_, property_, fieldName_, binaryProperties_,
				ternaryProperties_, sigs_, openModule_, openStatements_,
				functions_, commandHeader_, formula_, commandScope_);

		this.assertionName = String.format(assertionFiNameFormat, commandHeader, sanitizedPropertyName, sanitizedFieldName);
		this.assertionBody = String.format(propertyCheckingFormatBackward, assertionName, property,  formula, commandScope, emptyProperty );
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
