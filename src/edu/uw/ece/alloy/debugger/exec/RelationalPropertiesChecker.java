package edu.uw.ece.alloy.debugger.exec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.mit.csail.sdg.gen.visitor.FieldDecomposer;
import edu.uw.ece.alloy.debugger.*;
import edu.uw.ece.alloy.util.Utils;

/**
 * This class provides an Alloy program that checks the relational properties over 
 * Each relation in the given alloy program.
 * 
 * The Alloy program has one or more commands. For every command, a slice of the 
 * program is executed. The relations from that slice is extracted and checked.
 * Assuming the given Alloy program is as `P check{q} for i' then a property 
 * like `prop1'. Then, the generated code would be like:
 * 		`open general_props P check{q=>prop1[r1]}' 
 * 		`open general_props P check{prop1[r1]=>q}'
 * It means both directions are checked.
 * @author vajih
 *
 */
public class RelationalPropertiesChecker {

	//Better to be encoded in a resource class.
	final public File propertiesModuleFile ;

	final private Module world;
	final public File relationalPropertyNameFile;
	final public File alloySepcFileName;


	final Function<String, String> extracerFunction = (String s)->{
		String[] r = s.split(",");
		return r.length > 0 ? r[0] : s;
	};

	public RelationalPropertiesChecker(final File relationalPropertyNameFile_,final  File alloySepcFileName_, final File propertiesModuleFile_) {
		this.relationalPropertyNameFile  = relationalPropertyNameFile_;
		this.alloySepcFileName = alloySepcFileName_;
		this.propertiesModuleFile = propertiesModuleFile_;
		Module world_ = null;
		try{
			world_ = CompUtil.parseEverything_fromFile(null, null, alloySepcFileName_.getAbsolutePath());
			if(world_ == null)
				throw new RuntimeException("The returned module is null");
		}catch(Err e){
			LoggerUtil.debug(this, "The '%s' file is not parsed because of: %s%n", alloySepcFileName_, e.getMessage());
		}finally{
			world = world_;
		}
	}

	/**
	 * This file replaces all check and assert keywords with run and pred keywords.
	 * So the negation around the negation will be disappeared. 
	 * Note: The method is immutable.
	 * @throws Err 
	 */

	final public RelationalPropertiesChecker replacingCheckAndAsserts() throws Err{

		final Map<String, String> replaceMapping = new HashMap();

		replaceMapping.put("assert ", "pred ");
		//replaceMapping.put("relational_properties_S_c_P_", "");
		replaceMapping.put("check ", "run ");

		final String newFileName = this.alloySepcFileName.getName().replace(".als","_.als");
		Utils.replaceTextFiles(	this.alloySepcFileName.getAbsoluteFile().getParentFile(), 
				this.alloySepcFileName.getName(), 
				newFileName, 
				replaceMapping);

		return new RelationalPropertiesChecker(this.relationalPropertyNameFile, 
				new File(this.alloySepcFileName.getAbsoluteFile().getParent(), newFileName ), 
				this.propertiesModuleFile);
	}

	final private List<Sig.Field> getAllFields(){

		final List<Sig.Field> fields = new ArrayList<Sig.Field>();

		//what is inside the world? I am looking for fields
		for(Sig sig:world.getAllSigs()){
			for(Sig.Field field: sig.getFields()){
				fields.add(field);
			}
		}
		return Collections.unmodifiableList(fields);
	}

	final private List<Sig> getAllSigs(){

		return Collections.unmodifiableList(world.getAllSigs().makeCopy());
	}



	final private List<String> getAllProperties(Predicate<String> p, Function<String, String> f) throws FileNotFoundException, IOException{

		String content = Util.readAll(relationalPropertyNameFile.getAbsolutePath());

		return Collections.unmodifiableList( Arrays.asList(content.split("\n")).stream().filter(p).map(f).collect(Collectors.toList()) );

	}

	final public List<String> getAllBinaryWithDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,d,r,0");},extracerFunction);

	}

	final public List<String> getAllBinaryWithDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,d,0,0");},extracerFunction);

	}

	final public List<String> getAllBinaryWithoutDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,0,r,0");},extracerFunction);

	}	

	final public List<String> getAllBinaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,0,0,0");},extracerFunction);

	}

	final public List<String> getAllTernaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{
		return getAllProperties((s)->{return s.contains(",t,0,0,0");},extracerFunction);
	}	

	final public List<String> getAllTernaryWithDomainWithMiddleWithRangeRelationalProperties() throws FileNotFoundException, IOException{
		return getAllProperties((s)->{return s.contains(",t,d,m,r");},extracerFunction);
	}	


	final public Set<String> getAllTernaryPropertiesName() throws FileNotFoundException, IOException{
		return Collections.unmodifiableSet( new HashSet<>(getAllProperties((s)->{return s.contains(",t,");},extracerFunction)) );		
	}

	final public Set<String> getAllBinaryPropertiesName() throws FileNotFoundException, IOException{
		return Collections.unmodifiableSet( new HashSet<>(getAllProperties((s)->{return s.contains(",b,");},extracerFunction)) );		
	}

	/**
	 * 
	 * @param fieldName
	 * @param domainName
	 * @param rangeName
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	final private List<String> generateBinaryProperties(final String fieldName_, final String domainName_, final String rangeName_) throws FileNotFoundException, IOException{

		final List<String> properties = new ArrayList<>();
		final String domainName = domainName_.replace("this/", "");
		//The string name is attached to the domain name using domain restrictor operator in order to prevent ambiguity for the same fields name in other sigs 
		final String fieldName =  /*domainName + ExprBinary.Op.DOMAIN +*/fieldName_.replace("this/", "");
		final String rangeName = rangeName_.replace("this/", "");
		//Module name is prepended to the property name in order to prevent the ambiguity.
		final String moduleName = propertiesModuleFile.getName().replace(".als", "");

		for(String binary: getAllBinaryWithDomainWithRangeRelationalProperties())
			properties.add( String.format("%s/%s[%s,%s,%s]", moduleName, binary, fieldName, domainName, rangeName ) );
		for(String binary: getAllBinaryWithoutDomainWithRangeRelationalProperties())
			properties.add( String.format("%s/%s[%s,%s]",moduleName, binary, fieldName, rangeName ) );
		for(String binary: getAllBinaryWithDomainWithoutRangeRelationalProperties())
			properties.add( String.format("%s/%s[%s,%s]",moduleName, binary, fieldName, domainName ) );
		for(String binary: getAllBinaryWithoutDomainWithoutRangeRelationalProperties())
			properties.add( String.format("%s/%s[%s]",moduleName, binary, fieldName) );

		return Collections.unmodifiableList(properties);
	}

	/**
	 * 
	 * @param fieldName
	 * @param domainName
	 * @param rangeName
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	final private List<String> generateTernyProperties(final String fieldName_, final String domainName_, final String midName_, final String rangeName_) throws FileNotFoundException, IOException{

		final List<String> properties = new ArrayList<>();
		final String domainName = domainName_.replace("this/", "");
		//The string name is attached to the domain name using domain restrictor operator in order to prevent ambiguity for the same fields name in other sigs 
		final String fieldName =  /*domainName + ExprBinary.Op.DOMAIN +*/fieldName_.replace("this/", "");
		final String midName = midName_.replace("this/", "");
		final String rangeName = rangeName_.replace("this/", "");
		final String moduleName = propertiesModuleFile.getName().replace(".als", "");

		for(String ternary: getAllTernaryWithoutDomainWithoutRangeRelationalProperties() )
			properties.add( String.format("%s/%s[%s]", moduleName, ternary, fieldName ) );

		for(String ternary: getAllTernaryWithDomainWithMiddleWithRangeRelationalProperties() )
			properties.add( String.format("%s/%s[%s,%s,%s,%s]", moduleName, ternary, fieldName, domainName, midName, rangeName ) );

		properties.addAll(generateBinaryProperties(
				String.format("%s.%s",domainName, fieldName  ),
				midName, rangeName));
		properties.addAll(generateBinaryProperties(
				String.format("%s.%s",fieldName,  rangeName ),
				domainName, midName));

		return Collections.unmodifiableList(properties);
	}


	private final String findFunctions() throws Err{
		//All functions and pred with parameters are to be included in the new alloySpec.
		final StringBuilder functions = new StringBuilder();
		for(Func function: world.getAllFunc()){
			if( function.isPrivate == null )
				functions.append( PrettyPrintFunction.makeString(function) );
		}

		return functions.toString();
	}

	private final String findSigs(){
		Set<String> alreadyMetSigs = new HashSet();
		//All sigs are extracted to be included in the new alloySpec
		final StringBuilder sigs = new StringBuilder();
		for(Sig sig: world.getAllSigs()){
			//In case of having `sig A,B{}', both sigs have the same last pos.
			String lastPos = String.format("<%d,%d>", sig.pos().x2, sig.pos().y2 );
			if( ! alreadyMetSigs.contains(lastPos) ){
				sigs.append("\n");
				//finding multiplicities over sig
				if(sig.isAbstract != null)
					sigs.append("abstract ");
				if(sig.isOne != null)
					sigs.append("one ");
				if(sig.isLone != null)
					sigs.append("lone ");
				if(sig.isSome != null)
					sigs.append("some ");

				alreadyMetSigs.add(lastPos);
				//The position of sigs are properly set and no need to visit it.
				sigs.append("sig ").append( Utils.readSnippet(sig.pos) );
			}
		}
		return sigs.toString();
	}

	private final String findOpenStatements(){
		String openStatements = "";

		for(CompModule.Open key: ((CompModule) world).getOpens() ){
			//The internal modules are skipped
			if(!( key.filename.equals("util/integer") || key.filename.equals("") ) ){
				openStatements = openStatements + String.format("open %s ", key.filename); 
				if(key.args.size() > 0){
					openStatements = openStatements + "[";
					for(int i= 0; i < key.args.size(); ++i){
						openStatements = openStatements + key.args.get(i);
						if( i < key.args.size() - 1)
							openStatements = openStatements + ",";
					}
					openStatements = openStatements + "]";
				}
				//There is explicit Alias
				if( ! key.filename.replaceAll("^.*/", "").equals( key.alias) )
					openStatements = openStatements + " as " + key.alias;
			}
			openStatements = openStatements +  "\n";
		}

		return openStatements.toString();
	}

	private final String findFormula(Command cmd) throws Err{
		//Check is different from run. The pos included in the formula of check command is 
		//spanned over the whole command.
		final String commandHeader = cmd.label;
		String formula;
		if(! cmd.check && !commandHeader.contains("$")){
			formula = cmd.label;
		}else{
			formula = PrettyPrintExpression.makeString(cmd.formula);
		}

		return formula;
	}


	private final String findCommandHeader(Command cmd){
		String commandHeader = cmd.label;

		commandHeader = commandHeader.replace("$", "");

		return commandHeader;
	}

	private final String findCommandScope(final Command cmd){
		String commandScope = Utils.readSnippet(cmd.pos);
		commandScope = commandScope.contains("for") ? commandScope.replaceFirst("^(.(?!for))+", " ") : "";
		return commandScope;
	}

	private final String findFieldName(final Sig.Field field){
		//The string name is attached to the domain name using domain restricter operator in order to prevent ambiguity for the same fields name in other sigs 
		final String fieldName = String.format("(%s%s%s)", field.sig.label , ExprBinary.Op.DOMAIN , field.label);
		return fieldName;

	}

	private final List<String> generateProperties(final Sig.Field field) throws Err, FileNotFoundException, IOException{
		final List<String> properties = new ArrayList<>();
		final FieldDecomposer  fldDeocmposer = new FieldDecomposer();
		final String fieldName = findFieldName(field);
		final List<Expr> sigsInField =  fldDeocmposer.extractFieldsItems(field);
		//In case we have sig A{r:B}, sig C{s:r}
		if(sigsInField.get(0) instanceof Sig.Field){
			//decompose the field, mean `s; again.
			final List<Expr> sigsInInternalField =  fldDeocmposer.extractFieldsItems((Sig.Field)sigsInField.get(0));
			if( sigsInField.size() == 2 && sigsInInternalField.get(0) instanceof Sig && sigsInField.get(1) instanceof Sig ){
				properties.addAll( generateTernyProperties(fieldName, field.sig.label, ((Sig)sigsInInternalField.get(0)).label, ((Sig)sigsInField.get(1)).label ) );
			}
		}else{
			//Since the owner of the relation is not returned, the size of sigsInField is `1' for binary relations.
			if( (sigsInField.size() == 1) && sigsInField.get(0) instanceof Sig ){
				properties.addAll( generateBinaryProperties(fieldName, field.sig.label, sigsInField.get(0).toString() ) );
			}else if( sigsInField.size() == 2 && sigsInField.get(0) instanceof Sig && sigsInField.get(1) instanceof Sig ){
				properties.addAll( generateTernyProperties(fieldName, field.sig.label, ((Sig)sigsInField.get(0)).label, ((Sig)sigsInField.get(1)).label ) );
			}
		}
		return Collections.unmodifiableList(properties);
	}

	private final String getOpenModule(){
		return "open " + propertiesModuleFile.getName().replace(".als", "");
	}
	/**
	 * transformForChecking gets the file name, extracts its command expr, the fields,
	 * then makes new commands for checking whether: 
	 * 		`command_expr => prop_name[field_name]' or  `prop_name[field_name] => command_expr'.
	 * Each property checking is done in a command with form of:
	 * 		`assert  command_expr_if_prop1_field{ command_expr => prop_name[field_name] }
	 * 		 check command_expr_if_prop1_field'
	 * @param filename is the input file name containing the specification
	 * @return the new files containing the original spec as well as properties to be checked.
	 * A new file is created per each property-assertion.
	 * @throws Err 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	final public List<File> transformForChecking(final File destFolder) throws Err, FileNotFoundException, IOException{

		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();

		propBuilder.registerPropertyChecking(SpecToPropertyChecking.class);
		propBuilder.registerPropertyChecking(PropertyToSpecChecking.class);
		propBuilder.registerPropertyChecking(PropertyEquiSpecChecking.class);
		propBuilder.registerPropertyChecking(SpecHasPropertyChecking.class);

		List<File> retFiles = transformForChecking(propBuilder).stream().map(a->a.toAlloyFile(destFolder)).flatMap(l->l.stream()).collect(Collectors.toList());

		
		//retFiles.addAll(transformForChecking(propBuilder).stream().map(a->a.toAlloyFile(destFolder).get(0)).collect(Collectors.toList()));

		return Collections.unmodifiableList(retFiles);
	}


	final private List<PropertyCheckingSource> transformForChecking( final PropertyCheckingBuilder propBuilder) throws Err, FileNotFoundException, IOException{

		List<PropertyCheckingSource> retProps = new ArrayList<>();
		propBuilder.setOpenModule( getOpenModule() );
		propBuilder.setSourceFile(alloySepcFileName);
		propBuilder.setBinaryProperties( getAllBinaryPropertiesName() );
		propBuilder.setTernaryProperties( getAllTernaryPropertiesName() );
		propBuilder.setFunctions( findFunctions() );
		propBuilder.setSigs( findSigs() );
		propBuilder.setOpenStatements( findOpenStatements() );

		for(Command cmd: world.getAllCommands()){

			propBuilder.setCommandHeader( findCommandHeader(cmd) );
			propBuilder.setFormula(findFormula(cmd));
			propBuilder.setCommandScope( findCommandScope(cmd) );

			for(Sig.Field field : getAllFields()){

				propBuilder.setFieldName( findFieldName(field) );
				for(String property: generateProperties(field)){
					propBuilder.setProperty( property );
					retProps.addAll(propBuilder.makeCheckers());
				}
			}

		}

		return Collections.unmodifiableList(retProps);
	}
	
	
	final public void makeApproximation(final File destFolder) throws Err, FileNotFoundException, IOException{

		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();

		propBuilder.registerPropertyChecking(SpecToApproxGenerator.class);

		List<PropertyCheckingSource> result = transformForChecking(propBuilder);//.stream().map(a->a.toAlloyFile(destFolder)).collect(Collectors.toList()));
		
		result.get(0).toAlloyFile(destFolder);
		
		//Map<Pair<String,String>, Set<String>> groups = ((SpecToApproxGenerator)result.get(0)).findAllInconsistencies();
		//System.out.println(groups);
		
		//((SpecToApproxGenerator)result.get(0)).findAllPropertiesDifferenceConsistency();
		//((SpecToApproxGenerator)result.get(0)).findAllPropertiesDifferenceImply();
		//((SpecToApproxGenerator)result.get(0)).findInconsistencybyImply();

		//((SpecToApproxGenerator)result.get(0)).findAllInconsistencies();
		((SpecToApproxGenerator)result.get(0)).find_I_a();
	}

	
	final public List<File> findInconsistentProperties(final File destFolder) throws Err, FileNotFoundException, IOException{
		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();
		propBuilder.registerPropertyChecking(PropertiesConsistencyChecking.class);
		
		List<PropertyCheckingSource> checkers = transformForChecking(propBuilder);
		
		List<File> retFiles = checkers.stream().map( a->a.toAlloyFile(destFolder)) .flatMap(l -> l.stream()).collect(Collectors.toList());
		
		return Collections.unmodifiableList(retFiles);
		
	}
	
	final public List<File> findImplicationsProperties(final File destFolder) throws Err, FileNotFoundException, IOException{
		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();
		propBuilder.registerPropertyChecking(PropertiesImplicationChecking.class);
		
		List<PropertyCheckingSource> checkers = transformForChecking(propBuilder);
		
		List<File> retFiles = checkers.stream().map( a->a.toAlloyFile(destFolder)) .flatMap(l -> l.stream()).collect(Collectors.toList());
		
		return Collections.unmodifiableList(retFiles);
		
	}
	
}
