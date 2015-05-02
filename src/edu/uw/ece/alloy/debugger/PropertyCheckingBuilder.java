package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class PropertyCheckingBuilder {

	final List<PropertyCheckingSource> checkers = Collections.synchronizedList( new ArrayList());
	final List<Class> chekerClass = Collections.synchronizedList( new ArrayList());

	private File sourceFile;

	private String property;
	private String fieldName;

	private Set<String> binaryProperties;
	private Set<String> ternaryProperties;

	private String emptyProperty ;

	private String functions;
	private String sigs;
	private String openModule;
	private String openStatements;
	private String commandHeader;
	private String formula;
	private String commandScope;
	private String facts;


	public PropertyCheckingBuilder() {
	}


	public PropertyCheckingBuilder(
			File sourceFile, String property, String fieldName,
			Set<String> binaryProperties, Set<String> ternaryProperties,
			String emptyProperty, String functions, String sigs,
			String openModule, String openStatements, String commandHeader,
			String formula, String commandScope, String facts_) {
		super();
		this.sourceFile = sourceFile;
		this.property = property;
		this.fieldName = fieldName;
		this.binaryProperties = binaryProperties;
		this.ternaryProperties = ternaryProperties;
		this.emptyProperty = emptyProperty;
		this.functions = functions;
		this.sigs = sigs;
		this.openModule = openModule;
		this.openStatements = openStatements;
		this.commandHeader = commandHeader;
		this.formula = formula;
		this.commandScope = commandScope;
		this.facts = facts_;
	}


	public File getSourceFile() {
		return sourceFile;
	}


	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}


	public String getProperty() {
		return property;
	}


	public void setProperty(String property) {
		this.property = property;
	}


	public String getFieldName() {
		return fieldName;
	}


	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Set<String> getBinaryProperties() {
		return binaryProperties;
	}


	public void setBinaryProperties(Set<String> binaryProperties) {
		this.binaryProperties = binaryProperties;
	}


	public Set<String> getTernaryProperties() {
		return ternaryProperties;
	}


	public void setTernaryProperties(Set<String> ternaryProperties) {
		this.ternaryProperties = ternaryProperties;
	}


	public String getEmptyProperty() {
		return emptyProperty;
	}


	public void setEmptyProperty(String emptyProperty) {
		this.emptyProperty = emptyProperty;
	}


	public String getFunctions() {
		return functions;
	}


	public void setFunctions(String functions) {
		this.functions = functions;
	}


	public String getSigs() {
		return sigs;
	}


	public void setSigs(String sigs) {
		this.sigs = sigs;
	}


	public String getOpenModule() {
		return openModule;
	}


	public void setOpenModule(String openModule) {
		this.openModule = openModule;
	}


	public String getOpenStatements() {
		return openStatements;
	}


	public void setOpenStatements(String openStatements) {
		this.openStatements = openStatements;
	}


	public String getCommandHeader() {
		return commandHeader;
	}


	public void setCommandHeader(String commandHeader) {
		this.commandHeader = commandHeader;
	}


	public String getFormula() {
		return formula;
	}


	public void setFormula(String formula) {
		this.formula = formula;
	}


	public String getCommandScope() {
		return commandScope;
	}


	public void setCommandScope(String commandScope) {
		this.commandScope = commandScope;
	}

	public String getFacts() {
		return facts;
	}


	public void setFacts(String facts) {
		this.facts = facts;
	}
	
	public void addChecker(Class<? extends PropertyCheckingSource> clazz){

		Constructor<? extends PropertyCheckingSource> constructor;
		try {
			constructor = clazz.getConstructor(File.class, String.class ,
					String.class, Set.class, Set.class, String.class, String.class,
					String.class, String.class, String.class, String.class, String.class, String.class);

			checkers.add( constructor.newInstance(
					this.sourceFile, this.property, this.fieldName, this.binaryProperties,
					this.ternaryProperties, this.sigs, this.openModule,
					this.openStatements, this.functions, this.commandHeader,
					this.formula, this.commandScope, this.facts
					) );
			assert ( checkers.get(checkers.size() - 1).repOk() ) : String.format("Object is failed: %n%s", checkers.get(checkers.size() - 1).toString());
		} catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException( String.format( "An object of %s cannot be created due to: %s", clazz.getName(), e.getMessage() ));
		}
	}

	public void registerPropertyChecking(Class<? extends PropertyCheckingSource> clazz){
		chekerClass.add(clazz);
	}

	public List<PropertyCheckingSource> makeCheckers(){
		cleanCheckers();
		chekerClass.stream().forEach(c->this.addChecker(c));
		return getCheckers();
	}

	public void cleanCheckers(){
		checkers.clear();
	}

	public List<PropertyCheckingSource> getCheckers(){
		return Collections.unmodifiableList(this.checkers);
	}


	public void setOrderedSigs(List<Sig> findOrderedSignatures) {
		// TODO Auto-generated method stub
		
	}

}
