package edu.uw.ece.alloy.debugger.exec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import edu.uw.ece.alloy.debugger.PrettyPrintExpression;
import edu.uw.ece.alloy.debugger.PrettyPrintFunction;
import edu.uw.ece.alloy.debugger.PropertiesConsistencyChecking;
import edu.uw.ece.alloy.debugger.PropertiesImplicationChecking;
import edu.uw.ece.alloy.debugger.PropertyCheckingBuilder;
import edu.uw.ece.alloy.debugger.PropertyCheckingSource;
import edu.uw.ece.alloy.debugger.PropertyEquiSpecChecking;
import edu.uw.ece.alloy.debugger.PropertySet;
import edu.uw.ece.alloy.debugger.PropertyToSpecChecking;
import edu.uw.ece.alloy.debugger.SpecHasPropertyChecking;
import edu.uw.ece.alloy.debugger.SpecToApproxGenerator;
import edu.uw.ece.alloy.debugger.SpecToPropertyChecking;
import edu.uw.ece.alloy.util.Utils;

/**
 * This class provides an Alloy program that checks the relational properties
 * over Each relation in the given alloy program.
 * 
 * The Alloy program has one or more commands. For every command, a slice of the
 * program is executed. The relations from that slice is extracted and checked.
 * Assuming the given Alloy program is as `P check{q} for i' then a property
 * like `prop1'. Then, the generated code would be like: `open general_props P
 * check{q=>prop1[r1]}' `open general_props P check{prop1[r1]=>q}' It means both
 * directions are checked.
 * 
 * @author vajih
 *
 */
public class RelationalPropertiesChecker {

	// Better to be encoded in a resource class.
	final public File propertiesModuleFile;

	final private Module world;
	final public File relationalPropertyNameFile;
	final public File alloySepcFileName;

	final Function<String, String> extracerFunction = (String s) -> {
		String[] r = s.split(",");
		return r.length > 0 ? r[0] : s;
	};

	public RelationalPropertiesChecker(final File relationalPropertyNameFile_,
			final File alloySepcFileName_, final File propertiesModuleFile_) {
		this.relationalPropertyNameFile = relationalPropertyNameFile_;
		this.alloySepcFileName = alloySepcFileName_;
		this.propertiesModuleFile = propertiesModuleFile_;
		Module world_ = null;
		try {
			world_ = CompUtil.parseEverything_fromFile(null, null,
					alloySepcFileName_.getAbsolutePath());
			if (world_ == null)
				throw new RuntimeException("The returned module is null");
		} catch (Err e) {
			LoggerUtil.debug(this, "The '%s' file is not parsed because of: %s%n",
					alloySepcFileName_, e.getMessage());
		} finally {
			world = world_;
		}
	}

	/**
	 * This file replaces all check and assert keywords with run and pred
	 * keywords. So the negation around the negation will be disappeared. Note:
	 * The method is immutable.
	 * 
	 * @throws Err
	 */

	final public RelationalPropertiesChecker replacingCheckAndAsserts()
			throws Err {

		final Map<String, String> replaceMapping = new HashMap();

		replaceMapping.put("assert ", "pred ");
		// replaceMapping.put("relational_properties_S_c_P_", "");
		replaceMapping.put("check ", "run ");

		final String newFileName = this.alloySepcFileName.getName().replace(".als",
				"_.als");
		Utils.replaceTextFiles(
				this.alloySepcFileName.getAbsoluteFile().getParentFile(),
				this.alloySepcFileName.getName(), newFileName, replaceMapping);

		return new RelationalPropertiesChecker(this.relationalPropertyNameFile,
				new File(this.alloySepcFileName.getAbsoluteFile().getParent(),
						newFileName),
				this.propertiesModuleFile);
	}

	final private List<Sig.Field> getAllFields() {

		final List<Sig.Field> fields = new ArrayList<Sig.Field>();

		// what is inside the world? I am looking for fields
		for (Sig sig : world.getAllSigs()) {
			for (Sig.Field field : sig.getFields()) {
				fields.add(field);
			}
		}
		return Collections.unmodifiableList(fields);
	}

	final private List<String> getAllFieldsNames() {

		return Collections.unmodifiableList(getAllFields().stream()
				.map(a -> findFieldName(a)).collect(Collectors.toList()));
	}

	final private List<Sig> getAllSigs() {

		return Collections.unmodifiableList(world.getAllSigs().makeCopy());
	}

	final private List<String> getAllProperties(Predicate<String> p,
			Function<String, String> f) throws FileNotFoundException, IOException {

		String content = Util.readAll(relationalPropertyNameFile.getAbsolutePath());

		return Collections.unmodifiableList(Arrays.asList(content.split("\n"))
				.stream().filter(p).map(f).collect(Collectors.toList()));

	}

	final public List<String> getAllBinaryWithDomainWithRangeRelationalProperties()
			throws FileNotFoundException, IOException {

		return getAllProperties((s) -> {
			return s.contains(",b,d,r,0");
		} , extracerFunction);

	}

	final public List<String> getAllBinaryWithDomainWithoutRangeRelationalProperties()
			throws FileNotFoundException, IOException {

		return getAllProperties((s) -> {
			return s.contains(",b,d,0,0");
		} , extracerFunction);

	}

	final public List<String> getAllBinaryWithoutDomainWithRangeRelationalProperties()
			throws FileNotFoundException, IOException {

		return getAllProperties((s) -> {
			return s.contains(",b,0,r,0");
		} , extracerFunction);

	}

	final public List<String> getAllBinaryWithoutDomainWithoutRangeRelationalProperties()
			throws FileNotFoundException, IOException {

		return getAllProperties((s) -> {
			return s.contains(",b,0,0,0");
		} , extracerFunction);

	}

	final public List<String> getAllTernaryWithoutDomainWithoutRangeRelationalProperties()
			throws FileNotFoundException, IOException {
		return getAllProperties((s) -> {
			return s.contains(",t,0,0,0");
		} , extracerFunction);
	}

	final public List<String> getAllTernaryWithDomainWithMiddleWithRangeRelationalProperties()
			throws FileNotFoundException, IOException {
		return getAllProperties((s) -> {
			return s.contains(",t,d,m,r");
		} , extracerFunction);
	}

	final public Set<String> getAllTernaryPropertiesName()
			throws FileNotFoundException, IOException {
		return Collections.unmodifiableSet(new HashSet<>(getAllProperties((s) -> {
			return s.contains(",t,");
		} , extracerFunction)));
	}

	final public Set<String> getAllBinaryPropertiesName()
			throws FileNotFoundException, IOException {
		return Collections.unmodifiableSet(new HashSet<>(getAllProperties((s) -> {
			return s.contains(",b,");
		} , extracerFunction)));
	}

	final public Set<String> getAllOrderedTernaryDomainRange()
			throws FileNotFoundException, IOException {
		return Collections.unmodifiableSet(new HashSet<>(getAllProperties((s) -> {
			return s.contains(",ot,d,0,r");
		} , extracerFunction)));
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
	final private List<String> generateBinaryProperties(final String fieldName_,
			final String domainName_, final String rangeName_)
					throws FileNotFoundException, IOException {

		final List<String> properties = new ArrayList<>();
		final String domainName = domainName_.replace("this/", "");
		// The string name is attached to the domain name using domain restrictor
		// operator in order to prevent ambiguity for the same fields name in other
		// sigs
		final String fieldName = /* domainName + ExprBinary.Op.DOMAIN + */fieldName_
				.replace("this/", "");
		final String rangeName = rangeName_.replace("this/", "");
		// Module name is prepended to the property name in order to prevent the
		// ambiguity.
		final String moduleName = propertiesModuleFile.getName().replace(".als",
				"");

		for (String binary : getAllBinaryWithDomainWithRangeRelationalProperties())
			properties.add(String.format("%s/%s[%s,%s,%s]", moduleName, binary,
					fieldName, domainName, rangeName));
		for (String binary : getAllBinaryWithoutDomainWithRangeRelationalProperties())
			properties.add(String.format("%s/%s[%s,%s]", moduleName, binary,
					fieldName, rangeName));
		for (String binary : getAllBinaryWithDomainWithoutRangeRelationalProperties())
			properties.add(String.format("%s/%s[%s,%s]", moduleName, binary,
					fieldName, domainName));
		for (String binary : getAllBinaryWithoutDomainWithoutRangeRelationalProperties())
			properties.add(String.format("%s/%s[%s]", moduleName, binary, fieldName));

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
	final private List<String> generateTernyProperties(final String fieldName_,
			final String domainName_, final String midName_, final String rangeName_)
					throws FileNotFoundException, IOException {

		final List<String> properties = new ArrayList<>();
		final String domainName = domainName_.replace("this/", "");
		// The string name is attached to the domain name using domain restrictor
		// operator in order to prevent ambiguity for the same fields name in other
		// sigs
		final String fieldName = /* domainName + ExprBinary.Op.DOMAIN + */fieldName_
				.replace("this/", "");
		final String midName = midName_.replace("this/", "");
		final String rangeName = rangeName_.replace("this/", "");
		final String moduleName = propertiesModuleFile.getName().replace(".als",
				"");

		for (String ternary : getAllTernaryWithoutDomainWithoutRangeRelationalProperties())
			properties
					.add(String.format("%s/%s[%s]", moduleName, ternary, fieldName));

		for (String ternary : getAllTernaryWithDomainWithMiddleWithRangeRelationalProperties())
			properties.add(String.format("%s/%s[%s,%s,%s,%s]", moduleName, ternary,
					fieldName, domainName, midName, rangeName));

		properties.addAll(generateBinaryProperties(
				String.format("%s.%s", domainName, fieldName), midName, rangeName));
		properties.addAll(generateBinaryProperties(
				String.format("%s.%s", fieldName, rangeName), domainName, midName));

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
	final private List<String> generateOrderedTernyDomainRangeProperties(
			final String fieldName_, final String domainName_, final String midName_,
			final String rangeName_) throws FileNotFoundException, IOException {

		final List<String> properties = new ArrayList<>();
		final String domainName = domainName_.replace("this/", "");
		// The string name is attached to the domain name using domain restrictor
		// operator in order to prevent ambiguity for the same fields name in other
		// sigs
		final String fieldName = /* domainName + ExprBinary.Op.DOMAIN + */fieldName_
				.replace("this/", "");
		final String midName = midName_.replace("this/", "");
		final String rangeName = rangeName_.replace("this/", "");
		final String moduleName = propertiesModuleFile.getName().replace(".als",
				"");

		final Map<Sig, String> orderedSigs = findOrderedSignatures();
		final Map<String, String> orderedSigsAlises = orderedSigs.keySet().stream()
				.collect(Collectors.toMap(key -> key.toString().replace("this/", ""),
						key -> orderedSigs.get(key)));

		for (String ternary : getAllOrderedTernaryDomainRange())
			properties.add(String.format(
					"%1$s/%2$s[%3$s,%4$s,%5$s,%6$s,%7$s/first,%7$s/next,%8$s/first,%8$s/next]",
					/* 1 */moduleName, /* 2 */ternary, /* 3 */fieldName,
					/* 4 */domainName, /* 5 */midName, /* 6 */rangeName,
					/* 7 */orderedSigsAlises.get(domainName),
					/* 8 */orderedSigsAlises.get(rangeName)));

		return Collections.unmodifiableList(properties);
	}

	final private List<String> generateOrderedCombinatorialTernaryDomainRangeProperties(
			final List<String> fieldsName_, final String domainName_,
			final String midName_, final String rangeName_)
					throws FileNotFoundException, IOException {

		final List<String> properties = new ArrayList<>();

		for (int i = 0; i < fieldsName_.size(); ++i) {
			for (int j = i + 1; j < fieldsName_.size(); ++j) {
				final String unionFlds = String.format("(%s+%s)", fieldsName_.get(i),
						fieldsName_.get(j));
				properties.addAll(generateOrderedTernyDomainRangeProperties(unionFlds,
						domainName_, midName_, rangeName_));

				final String differenceFlds = String.format("(%s-%s)",
						fieldsName_.get(i), fieldsName_.get(j));
				properties.addAll(generateOrderedTernyDomainRangeProperties(
						differenceFlds, domainName_, midName_, rangeName_));

				final String diferenctFldsRev = String.format("(%s-%s)",
						fieldsName_.get(j), fieldsName_.get(i));
				properties.addAll(generateOrderedTernyDomainRangeProperties(
						diferenctFldsRev, domainName_, midName_, rangeName_));

				final String intersectionFlds = String.format("(%s&%s)",
						fieldsName_.get(j), fieldsName_.get(i));
				properties.addAll(generateOrderedTernyDomainRangeProperties(
						intersectionFlds, domainName_, midName_, rangeName_));
			}
		}

		return Collections.unmodifiableList(properties);

	}

	private final String findFunctions() throws Err {
		// All functions and pred with parameters are to be included in the new
		// alloySpec.
		final StringBuilder functions = new StringBuilder();
		for (Func function : world.getAllFunc()) {
			if (function.isPrivate == null)
				functions.append(PrettyPrintFunction.makeString(function));
		}

		return functions.toString();
	}

	private final String findSigs() {
		Set<String> alreadyMetSigs = new HashSet();
		// All sigs are extracted to be included in the new alloySpec
		final StringBuilder sigs = new StringBuilder();
		for (Sig sig : world.getAllSigs()) {
			// In case of having `sig A,B{}', both sigs have the same last pos.
			String lastPos = String.format("<%d,%d>", sig.pos().x2, sig.pos().y2);
			if (!alreadyMetSigs.contains(lastPos)) {
				sigs.append("\n");
				// finding multiplicities over sig
				if (sig.isAbstract != null)
					sigs.append("abstract ");
				if (sig.isOne != null)
					sigs.append("one ");
				if (sig.isLone != null)
					sigs.append("lone ");
				if (sig.isSome != null)
					sigs.append("some ");

				alreadyMetSigs.add(lastPos);
				// The position of sigs are properly set and no need to visit it.
				sigs.append("sig ").append(Utils.readSnippet(sig.pos));
			}
		}
		return sigs.toString();
	}

	private final String findFacts() {

		StringBuilder result = new StringBuilder();

		for (Pair<String, Expr> fact : world.getAllFacts()) {

			result = result.append(Utils.readSnippet(fact.b.pos)).append("\n");
		}

		return result.toString();
	}

	/**
	 * The return map is a map from the ordered sig to the alias name. Example:
	 * Input: 'open util/ordering [A]' Output: 'this/A->ordered_A' Input: 'open
	 * util/ordering [A] as ao' Output: 'this/A->so'
	 * 
	 */
	private final Map<Sig, String> findOrderedSignatures() {
		Map<Sig, String> result = new HashMap<>();

		for (CompModule.Open key : ((CompModule) world).getOpens()) {
			// The internal modules are skipped
			if (!(key.filename.equals("util/integer") || key.filename.equals(""))
					&& key.filename.equals("util/ordering")) {
				for (String arg : key.args) {
					for (Sig sig : ((CompModule) world).getAllSigs()) {
						if (sig.toString().equals("this/" + arg)) {
							if (key.alias != null && !key.alias.equals("")
									&& !key.alias.equals("ordering"))
								result.put(sig, key.alias);
							else
								result.put(sig, "oredered_" + arg);
						}
					}
				}
			}
		}

		return Collections.unmodifiableMap(result);
	}

	private final String findOpenStatements() {
		String openStatements = "";

		for (CompModule.Open key : ((CompModule) world).getOpens()) {
			// The internal modules are skipped
			if (!(key.filename.equals("util/integer") || key.filename.equals(""))) {
				openStatements = openStatements
						+ String.format("open %s ", key.filename);
				if (key.args.size() > 0) {
					openStatements = openStatements + "[";
					for (int i = 0; i < key.args.size(); ++i) {
						openStatements = openStatements + key.args.get(i);
						if (i < key.args.size() - 1)
							openStatements = openStatements + ",";
					}
					openStatements = openStatements + "]";
				}
				// There is explicit Alias
				if (!key.filename.replaceAll("^.*/", "").equals(key.alias))
					openStatements = openStatements + " as " + key.alias;
			}
			openStatements = openStatements + "\n";
		}

		return openStatements.toString();
	}

	private final String findFormula(Command cmd) throws Err {
		// Check is different from run. The pos included in the formula of check
		// command is
		// spanned over the whole command.
		final String commandHeader = cmd.label;
		String formula;
		if (!cmd.check && !commandHeader.contains("$")) {
			formula = cmd.label;
		} else {
			formula = PrettyPrintExpression.makeString(cmd.formula);
		}

		return formula;
	}

	private final String findCommandHeader(Command cmd) {
		String commandHeader = cmd.label;

		commandHeader = commandHeader.replace("$", "");

		return commandHeader;
	}

	private final String findCommandScope(final Command cmd) {
		String commandScope = Utils.readSnippet(cmd.pos);
		commandScope = commandScope.contains("for")
				? commandScope.replaceFirst("^(.(?!for))+", " ") : "";
		return commandScope;
	}

	private final String findFieldName(final Sig.Field field) {
		// The string name is attached to the domain name using domain restricter
		// operator in order to prevent ambiguity for the same fields name in other
		// sigs
		final String fieldName = String.format("(%s%s%s)", field.sig.label,
				ExprBinary.Op.DOMAIN, field.label);
		return fieldName;

	}

	private final boolean isOrderedDomainRange(final Sig.Field field) throws Err {
		final FieldDecomposer fldDeocmposer = new FieldDecomposer();
		final List<Expr> sigsInField = fldDeocmposer.extractFieldsItems(field);
		assert sigsInField.size() == 2;
		final Set<Sig> orderedSigs = findOrderedSignatures().keySet();

		return orderedSigs.contains(field.sig)
				&& orderedSigs.contains(sigsInField.get(1));

	}

	private final List<String> generateProperties(final Sig.Field field)
			throws Err, FileNotFoundException, IOException {
		final List<String> properties = new ArrayList<>();
		final FieldDecomposer fldDeocmposer = new FieldDecomposer();
		final String fieldName = findFieldName(field);
		final List<Expr> sigsInField = fldDeocmposer.extractFieldsItems(field);

		getAllOrderedTernaryDomainRange();

		// In case we have sig A{r:B}, sig C{s:r}
		if (sigsInField.get(0) instanceof Sig.Field) {
			// decompose the field, mean `s; again.
			final List<Expr> sigsInInternalField = fldDeocmposer
					.extractFieldsItems((Sig.Field) sigsInField.get(0));
			if (sigsInField.size() == 2 && sigsInInternalField.get(0) instanceof Sig
					&& sigsInField.get(1) instanceof Sig) {
				properties.addAll(generateTernyProperties(fieldName, field.sig.label,
						((Sig) sigsInInternalField.get(0)).label,
						((Sig) sigsInField.get(1)).label));
			}
		} else {
			// Since the owner of the relation is not returned, the size of
			// sigsInField is `1' for binary relations.
			if ((sigsInField.size() == 1) && sigsInField.get(0) instanceof Sig) {
				properties.addAll(generateBinaryProperties(fieldName, field.sig.label,
						sigsInField.get(0).toString()));
			} else if (sigsInField.size() == 2 && sigsInField.get(0) instanceof Sig
					&& sigsInField.get(1) instanceof Sig) {
				properties.addAll(generateTernyProperties(fieldName, field.sig.label,
						((Sig) sigsInField.get(0)).label,
						((Sig) sigsInField.get(1)).label));
			}
		}

		// generateOrderedCombinatorialTernaryDomainRangeProperties is called
		// multiple times. The result is stored in a hash to prevent the
		// duplications.
		final Set<String> uniqProperties = new HashSet<>();
		// ordered sigs
		if (!(sigsInField.get(0) instanceof Sig.Field) && sigsInField.size() == 2
				&& sigsInField.get(0) instanceof Sig
				&& sigsInField.get(1) instanceof Sig && isOrderedDomainRange(field)) {
			uniqProperties.addAll(generateOrderedTernyDomainRangeProperties(fieldName,
					field.sig.label, ((Sig) sigsInField.get(0)).label,
					((Sig) sigsInField.get(1)).label));

			uniqProperties
					.addAll(generateOrderedCombinatorialTernaryDomainRangeProperties(
							getAllFieldsNames(), field.sig.label,
							((Sig) sigsInField.get(0)).label,
							((Sig) sigsInField.get(1)).label));
		}

		properties.addAll(uniqProperties);

		return Collections.unmodifiableList(properties);
	}

	private final String getOpenModule() {
		return "open " + propertiesModuleFile.getName().replace(".als", "");
	}

	/**
	 * transformForChecking gets the file name, extracts its command expr, the
	 * fields, then makes new commands for checking whether: `command_expr =>
	 * prop_name[field_name]' or `prop_name[field_name] => command_expr'. Each
	 * property checking is done in a command with form of: `assert
	 * command_expr_if_prop1_field{ command_expr => prop_name[field_name] } check
	 * command_expr_if_prop1_field'
	 * 
	 * @param filename
	 *          is the input file name containing the specification
	 * @return the new files containing the original spec as well as properties to
	 *         be checked. A new file is created per each property-assertion.
	 * @throws Err
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	final public List<File> transformForChecking(final File destFolder)
			throws Err, FileNotFoundException, IOException {

		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();

		propBuilder.registerPropertyChecking(SpecToPropertyChecking.class);
		propBuilder.registerPropertyChecking(PropertyToSpecChecking.class);
		propBuilder.registerPropertyChecking(PropertyEquiSpecChecking.class);
		propBuilder.registerPropertyChecking(SpecHasPropertyChecking.class);

		List<File> retFiles = transformForChecking(propBuilder).stream()
				.map(a -> a.toAlloyFile(destFolder)).flatMap(l -> l.stream())
				.collect(Collectors.toList());

		// retFiles.addAll(transformForChecking(propBuilder).stream().map(a->a.toAlloyFile(destFolder).get(0)).collect(Collectors.toList()));

		return Collections.unmodifiableList(retFiles);
	}

	final private List<PropertyCheckingSource> transformForChecking(
			final PropertyCheckingBuilder propBuilder)
					throws Err, FileNotFoundException, IOException {

		List<PropertyCheckingSource> retProps = new ArrayList<>();
		propBuilder.setOpenModule(getOpenModule());
		propBuilder.setSourceFile(alloySepcFileName);
		propBuilder.setBinaryProperties(getAllBinaryPropertiesName());
		Set<String> ternaries = new HashSet<>(getAllTernaryPropertiesName());
		ternaries.addAll(getAllOrderedTernaryDomainRange());
		propBuilder.setTernaryProperties(Collections.unmodifiableSet(ternaries));
		propBuilder.setFunctions(findFunctions());
		propBuilder.setSigs(findSigs());
		propBuilder.setOpenStatements(findOpenStatements());
		propBuilder.setFacts(findFacts());

		for (Command cmd : world.getAllCommands()) {

			propBuilder.setCommandHeader(findCommandHeader(cmd));
			propBuilder.setFormula(findFormula(cmd));
			propBuilder.setCommandScope(findCommandScope(cmd));

			for (Sig.Field field : getAllFields()) {

				propBuilder.setFieldName(findFieldName(field));
				for (String property : generateProperties(field)) {
					propBuilder.setProperty(property);
					retProps.addAll(propBuilder.makeCheckers());
				}
			}

		}

		return Collections.unmodifiableList(retProps);
	}

	final public void makeApproximation(final File destFolder)
			throws Err, FileNotFoundException, IOException {

		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();

		propBuilder.registerPropertyChecking(SpecToApproxGenerator.class);

		List<PropertyCheckingSource> result = transformForChecking(propBuilder);// .stream().map(a->a.toAlloyFile(destFolder)).collect(Collectors.toList()));

		result.get(0).toAlloyFile(destFolder);

		// Map<Pair<String,String>, Set<String>> groups =
		// ((SpecToApproxGenerator)result.get(0)).findAllInconsistencies();
		// System.out.println(groups);

		// ((SpecToApproxGenerator)result.get(0)).findAllPropertiesDifferenceConsistency();
		// ((SpecToApproxGenerator)result.get(0)).findAllPropertiesDifferenceImply();
		// ((SpecToApproxGenerator)result.get(0)).findInconsistencybyImply();

		// ((SpecToApproxGenerator)result.get(0)).findAllInconsistencies();

		System.out.println("Inconsistent");
		((SpecToApproxGenerator) result.get(0)).printSet(
				((SpecToApproxGenerator) result.get(0)).findAllInconsistents());

		System.out.println("\nImplied");
		((SpecToApproxGenerator) result.get(0)).printSet(
				((SpecToApproxGenerator) result.get(0)).findAllImplications());

		System.out.println("\nConsistent");
		((SpecToApproxGenerator) result.get(0))
				.printSet(((SpecToApproxGenerator) result.get(0)).findAllConsistents());

		System.out.println("\nfindI_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findI_a());

		System.out.println("\nfindII_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findII_a());

		System.out.println("\findIV_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIV_a());

		System.exit(-1);

		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> resI_a = ((SpecToApproxGenerator) result
				.get(0)).findI_a();
		resI_a = ((SpecToApproxGenerator) result.get(0)).filterMap(resI_a);
		SpecToApproxGenerator.printMap(resI_a);

		System.out.println("\nfiltered Backward I_a:\n\n");
		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> resBI_a = ((SpecToApproxGenerator) result
				.get(0)).filterbyImplicationRightside(resI_a);
		SpecToApproxGenerator.printMap(resBI_a);

		System.out.println("\nfiltered Forward I_a:\n\n");
		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> resFI_a = ((SpecToApproxGenerator) result
				.get(0)).filterbyImplicationLeftside(resI_a);
		SpecToApproxGenerator.printMap(resFI_a);

		System.out.println("\nfiltered ForwardBackward I_a:\n\n");
		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> resFBI_a = ((SpecToApproxGenerator) result
				.get(0)).filterbyImplicationLeftside(resBI_a);
		SpecToApproxGenerator.printMap(resFBI_a);

		System.out.println("II_b:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIII_e());

		System.out.println("IV_e:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIV_e());

		SpecToApproxGenerator.printSet(
				((SpecToApproxGenerator) result.get(0)).findAllImplications());

		System.exit(-1);

		System.out.println("I_b:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findI_b());
		System.out.println("I_c:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findI_c());
		System.out.println("I_d:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findI_d());
		System.out.println("I_e:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findI_e());

		System.out.println("II_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findII_a());
		System.out.println("II_b:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findII_b());
		System.out.println("II_c:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findII_c());
		System.out.println("II_d:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findII_d());
		System.out.println("II_e:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findII_e());

		System.out.println("III_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIII_a());
		System.out.println("III_b:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIII_b());
		System.out.println("III_c:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIII_c());
		System.out.println("III_d:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIII_d());
		System.out.println("\nIII_e:\n\n");
		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> res = ((SpecToApproxGenerator) result
				.get(0)).findIII_e();
		SpecToApproxGenerator.printMap(res);

		System.out.println("\nfiltered Backward III_e:\n\n");
		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> res2 = ((SpecToApproxGenerator) result
				.get(0)).filterbyImplicationRightside(res);
		SpecToApproxGenerator.printMap(res2);

		System.out.println("\nfiltered Forward III_e:\n\n");
		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> res3 = ((SpecToApproxGenerator) result
				.get(0)).filterbyImplicationLeftside(res);
		SpecToApproxGenerator.printMap(res3);

		System.out.println("\nfiltered ForwardBackward III_e:\n\n");
		Set<Pair<PropertySet.PropertyRelation, PropertySet.PropertyRelation>> res4 = ((SpecToApproxGenerator) result
				.get(0)).filterbyImplicationLeftside(res2);
		SpecToApproxGenerator.printMap(res4);

		System.out.println("IV_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIV_a());
		System.out.println("IV_b:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIV_b());
		System.out.println("IV_c:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIV_c());
		System.out.println("IV_d:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIV_d());
		System.out.println("IV_e:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findIV_e());

		System.out.println("V_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findV_a());
		System.out.println("V_b:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findV_b());
		System.out.println("V_c:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findV_c());
		System.out.println("V_d:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findV_d());
		System.out.println("V_e:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findV_e());

		System.out.println("VI_a:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findVI_a());
		System.out.println("VI_b:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findVI_b());
		System.out.println("VI_c:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findVI_c());
		System.out.println("VI_d:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findVI_d());
		System.out.println("VI_e:\n\n");
		SpecToApproxGenerator
				.printMap(((SpecToApproxGenerator) result.get(0)).findVI_e());

		System.out.println("Implied Properties:\n\n");
		SpecToApproxGenerator.printSet(
				((SpecToApproxGenerator) result.get(0)).findAllImplications());

		System.out.println("Consistent Consistents:\n\n");
		SpecToApproxGenerator
				.printSet(((SpecToApproxGenerator) result.get(0)).findAllConsistents());

		System.out.println("Consistent Inconsistents:\n\n");
		SpecToApproxGenerator.printSet(
				((SpecToApproxGenerator) result.get(0)).findAllInconsistents());

	}

	final public List<File> findInconsistentProperties(final File destFolder)
			throws Err, FileNotFoundException, IOException {
		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();
		propBuilder.registerPropertyChecking(PropertiesConsistencyChecking.class);

		List<PropertyCheckingSource> checkers = transformForChecking(propBuilder);

		List<File> retFiles = checkers.stream().map(a -> a.toAlloyFile(destFolder))
				.flatMap(l -> l.stream()).collect(Collectors.toList());

		return Collections.unmodifiableList(retFiles);

	}

	final public List<File> findImplicationsProperties(final File destFolder)
			throws Err, FileNotFoundException, IOException {
		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final PropertyCheckingBuilder propBuilder = new PropertyCheckingBuilder();
		propBuilder.registerPropertyChecking(PropertiesImplicationChecking.class);

		List<PropertyCheckingSource> checkers = transformForChecking(propBuilder);

		List<File> retFiles = checkers.stream().map(a -> a.toAlloyFile(destFolder))
				.flatMap(l -> l.stream()).collect(Collectors.toList());

		return Collections.unmodifiableList(retFiles);

	}

}
