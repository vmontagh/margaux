/**
 * 
 */
package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;

/**
 * PropertyChekingSource is and abstract class including all materials needs for
 * generating Alloy source code in order to check a relational property.
 * Any subclass has to implement the toAlloy method to get the result.
 * @author vajih
 *
 */
public abstract class PropertyCheckingSource {

	final public File sourceFile;

	final public String property;
	final public String fieldName;
	//Make a file name from the property. e.g. total[s.B,B]-> total___s-B, total[s,A]-> total_s
	final public String sanitizedPropertyName;
	final public String sanitizedFieldName; 

	final public String propertyFieldName;
	final public String propertyName;

	final public Set<String> binaryProperties;
	final public Set<String> ternaryProperties;

	final public String emptyProperty ;

	final public String functions;
	final public String sigs;
	final public String openModule;
	final public String openStatements;
	final public String commandHeader;
	final public String formula;
	final public String commandScope;

	final public static String emptyCheckingBinrayFieldFormat = "!empty[ %s ]";
	final public static String emptyCheckingTernaryFieldFormat = "!empty3[ %s ]";

	final public static String SEPARATOR = "_S_p_R_";

	/**The input property is in the form of prop_name[field,....]. The return is field.
	 * 
	 * @param field
	 * @return
	 */
	final protected static String fieldExtractorFromProperty(final String property){
		return property.replaceAll("([^\\[]+\\[)([^\\],]*)(.*\\])", "$2");
	}
	
	/**
	 * The input property is in the form of prop_name[field,....]. The return is prop_name.
	 * @param property
	 * @return
	 */
	final protected static String propertyNameExtractorFromProperty(final String property){
		return property.replaceAll("([^/]*/|^)([^\\[]+)(\\[.*)", "$2");
	}
	
	protected PropertyCheckingSource(final File sourceFile_, final String property_, final String fieldName_, final Set<String> binaryProperties_, 
			final Set<String> ternaryProperties_, /*final String assertionName_, final String assertionBody_,*/ final  String sigs_,
			final String openModule_, final String openStatements_, final String functions_, 
			final String commandHeader_, final String formula_, final String commandScope_){

		this.sourceFile = sourceFile_;

		this.property = property_;
		this.fieldName = fieldName_;

		this.sanitizedPropertyName = nameSanitizer( property);
		this.sanitizedFieldName = nameSanitizer( fieldName_ ); 

		this.binaryProperties = binaryProperties_;
		this.ternaryProperties = ternaryProperties_;

		//The following part determines if the filed is binary or ternary.
		//The property name shows whether the field is binary or ternary.
		propertyFieldName = fieldExtractorFromProperty(property); 
		propertyName = propertyNameExtractorFromProperty(property);

		emptyProperty = ternaryProperties.contains(propertyName) ?  
				String.format( emptyCheckingTernaryFieldFormat , propertyFieldName ) :
					String.format( emptyCheckingBinrayFieldFormat , propertyFieldName );

				//this.assertionName = assertionName_;
				//this.assertionBody = assertionBody_;

				this.functions = functions_;
				this.sigs = sigs_;
				this.openModule = openModule_;
				this.openStatements = openStatements_;

				this.commandHeader = commandHeader_;
				this.formula = formula_;
				this.commandScope = commandScope_;

	}


	final String nameSanitizer(final String name){
		return name.replaceAll("(,.*|\\])", "").
				replaceAll("(\\)|\\()","").
				replaceAll("\\[","_F_l_d_").
				replaceAll("\\.", "_D_o_T_").
				replaceAll(ExprBinary.Op.DOMAIN.toString(), "_D_m_N_").
				replaceAll("/","_S_c_P_");
	}


	public String toAlloy(){
		final StringBuilder newAlloySpec = new StringBuilder();
		newAlloySpec.append(openStatements);
		newAlloySpec.append("\n").append(openModule);
		newAlloySpec.append("\n").append(sigs);
		newAlloySpec.append("\n").append(functions);
		newAlloySpec.append("\n").append(getNewStatement());
		return newAlloySpec.toString();
	}

	public List<File> toAlloyFile(final File destFolder) {
		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final File retFileName = new File( destFolder, makeNewFileName() ); 

		try {
			Util.writeAll(  retFileName.getAbsolutePath(), this.toAlloy());
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException( String.format( "Output file could be created: %s\n", e.getMessage() ));
		}
		
		return  Collections.unmodifiableList(Arrays.asList(retFileName.getAbsoluteFile()) );
	}


	@Override
	public String toString() {
		return "PropertyCheckingSource [sourceFile=" + sourceFile
				+ ", property=" + property + ", fieldName=" + fieldName
				+ ", sanitizedPropertyName=" + sanitizedPropertyName
				+ ", sanitizedFieldName=" + sanitizedFieldName
				+ ", propertyFieldName=" + propertyFieldName
				+ ", propertyName=" + propertyName + ", binaryProperties="
				+ binaryProperties + ", ternaryProperties=" + ternaryProperties
				+ ", emptyProperty=" + emptyProperty + ", functions="
				+ functions + ", sigs=" + sigs + ", openModule=" + openModule
				+ ", openStatements=" + openStatements + ", commandHeader="
				+ commandHeader + ", formula=" + formula + ", commandScope="
				+ commandScope + "]";
	}


	public boolean repOk(){

		return 	sourceFile != null && property != null && !property.trim().isEmpty() &&
				fieldName != null && !fieldName.trim().isEmpty() && sanitizedPropertyName != null && 
				!sanitizedPropertyName.trim().isEmpty() && sanitizedFieldName != null && 
				!sanitizedFieldName.trim().isEmpty() && propertyFieldName != null && 
				!propertyFieldName.trim().isEmpty() && propertyName != null && 
				!propertyName.trim().isEmpty() && binaryProperties != null && 
				!binaryProperties.isEmpty() && ternaryProperties != null && 
				!ternaryProperties.isEmpty() && emptyProperty != null && 
				!emptyProperty.trim().isEmpty() && functions != null && 
				sigs != null &&  !sigs.trim().isEmpty() && openModule != null && 
				openStatements != null &&  !openStatements.trim().isEmpty() && 
				commandHeader != null && !commandHeader.trim().isEmpty() && 
				formula != null && !formula.trim().isEmpty() && commandScope != null;


	}

	protected abstract String makeNewFileName();
	protected abstract String getNewStatement();
}
