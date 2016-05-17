package edu.uw.ece.alloy.debugger.knowledgebase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule.Open;
import edu.uw.ece.alloy.debugger.PropertyCallBuilder;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.util.Utils;

/**
 * Given a field and pattern name, the class converts it to a property
 * 
 * @author vajih
 *
 */
public class PatternToProperty {

	final static Logger logger = Logger
			.getLogger(PatternToProperty.class.getName() + "--"
					+ Thread.currentThread().getName());

	final public File relationalPropModuleOriginal;
	final public File temporalPropModuleOriginal;

	/*
	 * A map from (patternName,field/Name) to property(i.e. property call). Once a
	 * property is inferred, its call does not necessarily have the same
	 * parameters as the original has. So that, the property calls are cached,
	 * then used for changing the inferred properties.
	 */
	final public Map<Pair<String, String>, String> propertyCalls;
	/*
	 * all fields exist in the fields. No matter what field is passed to be
	 * processed.
	 */
	final List<Field> fields;

	protected static PatternToProperty self = null;

	public PatternToProperty(File relationalPropModuleOriginal,
			File temporalPropModuleOriginal, File tobeAnalyzedCode) throws Err {
		this(relationalPropModuleOriginal, temporalPropModuleOriginal,
				tobeAnalyzedCode, Optional.empty());
	}

	public PatternToProperty(File relationalPropModuleOriginal,
			File temporalPropModuleOriginal, File tobeAnalyzedCode, String fieldName)
					throws Err {
		this(relationalPropModuleOriginal, temporalPropModuleOriginal,
				tobeAnalyzedCode, Optional.ofNullable(fieldName));
	}

	public PatternToProperty(File relationalPropModuleOriginal,
			File temporalPropModuleOriginal, File tobeAnalyzedCode,
			Optional<String> fieldName) {
		this.relationalPropModuleOriginal = relationalPropModuleOriginal;
		this.temporalPropModuleOriginal = temporalPropModuleOriginal;

		Map<Pair<String, String>, String> propertyCalls = new ConcurrentHashMap<>();

		// try make all all the properties from the patterns stored in the
		// the filed and fields in the
		try {

			CompModule world = ((CompModule) A4CommandExecuter.getInstance()
					.parse(tobeAnalyzedCode.getAbsolutePath(), A4Reporter.NOP));

			List<Field> fields = new ArrayList<>();
			// just for making a log.
			List<Field> allfields = new ArrayList<>();
			for (Sig sig : world.getAllSigs()) {
				for (Field field : sig.getFields()) {
					if (fieldName.isPresent() && fieldName.get().equals(field.label))
						fields.add(field);
					allfields.add(field);
				}
			}

			if (fieldName.isPresent()) {
				if (fields.isEmpty()) {
					logger.severe(Utils.threadName() + "Field:" + fieldName
							+ " not found in:" + allfields);
					throw new RuntimeException(
							"Field:" + fieldName + " not found in:" + allfields);
				}
			} else {
				fields.addAll(allfields);
			}

			this.fields = Collections.unmodifiableList(allfields);

			generateRelationalPropertyCalls(propertyCalls, fields);

			List<Open> opens = world.getOpens();
			generateTemporalPropertyCalls(propertyCalls, fields, opens);

			this.propertyCalls = Collections.unmodifiableMap(propertyCalls);

		} catch (Err e) {
			logger.log(Level.WARNING,
					Utils.threadName() + "Failling to add make properties", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * return a the field name. It might change, so use the function.
	 * 
	 * @param field
	 * @return
	 */
	protected final String getFieldName(Field field) {
		return field.label;
	}

	void generateRelationalPropertyCalls(
			Map<Pair<String, String>, String> propertyCalls, List<Field> fields)
					throws Err {
		CompModule world = (CompModule) A4CommandExecuter.getInstance()
				.parse(relationalPropModuleOriginal.getAbsolutePath(), A4Reporter.NOP);
		for (Func func : world.getAllFunc()) {
			String funcName = func.label.replace("this/", "");
			final PropertyCallBuilder pcb = new PropertyCallBuilder();
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
				/*logger.log(Level.WARNING,
						Utils.threadName() + "Failling to add a property declaration:", ia);*/
			}
			for (Field field : fields.stream().filter(f -> f.type().arity() == 2)
					.collect(Collectors.toList())) {
				for (String PropertyCall : pcb.makeAllBinaryProperties(field)) {
					propertyCalls.put(new Pair<>(funcName, field.label), PropertyCall);
				}
			}
		}
	}

	void generateTemporalPropertyCalls(
			Map<Pair<String, String>, String> propertyCalls, List<Field> fields,
			final List<Open> opens) throws Err {
		CompModule world = (CompModule) A4CommandExecuter.getInstance()
				.parse(temporalPropModuleOriginal.getAbsolutePath(), A4Reporter.NOP);
		for (Func func : world.getAllFunc()) {
			String funcName = func.label.replace("this/", "");
			final PropertyCallBuilder pcb = new PropertyCallBuilder();
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
				logger.log(Level.WARNING, "[" + Thread.currentThread().getName() + "] "
						+ "Failling to add a property declaration:", ia);
			}
			for (Field field : fields.stream().filter(f -> f.type().arity() == 3)
					.collect(Collectors.toList())) {
				for (String PropertyCall : pcb.makeAllTernaryProperties(field, opens)) {
					propertyCalls.put(new Pair<>(funcName, field.label), PropertyCall);
				}
			}

		}
	}

	/**
	 * Given a pattern name and field, get the actual call of the pattern called
	 * property now.
	 * 
	 * @param patternName
	 * @param fieldName
	 * @throws Err
	 */
	public String getProperty(String patternName, String fieldName) {
		return getProperty(new Pair<String, String>(patternName, fieldName));
	}

	public String getProperty(Pair<String, String> patternNamefieldName) {
		if (!hasProperty(patternNamefieldName)) {
			logger.warning(
					Utils.threadName() + "The pattern and field is not the chache.");
			throw new RuntimeException();
		}

		return propertyCalls.get(patternNamefieldName);
	}

	public boolean hasProperty(Pair<String, String> patternNamefieldName) {
		return propertyCalls.containsKey(patternNamefieldName);
	}

	public boolean hasProperty(String patternName, String fieldName) {
		return hasProperty(new Pair<String, String>(patternName, fieldName));
	}

	/**
	 * Given a field name, a field object with the same label is returned.
	 * 
	 * @param fieldName
	 * @return
	 */
	public Optional<Field> getField(String fieldName) {
		return fields.stream().filter(f -> f.label.equals(fieldName)).findAny();
	}

}
