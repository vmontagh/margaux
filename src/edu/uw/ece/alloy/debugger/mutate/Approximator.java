package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.PrettyPrintExpression;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLatticImperative;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryInconsistencyGraph;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.InconsistencyGraph;
import edu.uw.ece.alloy.debugger.knowledgebase.InconsistencyGraph.STATUS;
import edu.uw.ece.alloy.debugger.knowledgebase.PatternToProperty;
import edu.uw.ece.alloy.debugger.knowledgebase.TernaryImplicationLatticImperative;
import edu.uw.ece.alloy.debugger.knowledgebase.TernaryInconsistencyGraph;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AndPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.IfPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.InconExpressionToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.InconPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessDistributer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternRequestMessage;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TemporalPropertyGenerator;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

/**
 * @author vajih
 * 
 *
 */
public class Approximator {

	public class SynchronizedResult<T> {
		T result = null;

		public Optional<T> getResult() {
			return Optional.ofNullable(result);
		}
	}

	final static Logger logger = Logger
			.getLogger(Approximator.class.getName() + "--" + Thread.currentThread().getName());

	final public static File RelationalPropModule = new File(Configuration.getProp("relational_properties_tagged"));
	final public static File TemporalPropModule = new File(Configuration.getProp("temporal_properties_tagged"));
	final ServerSocketInterface interfacE;
	final ProcessDistributer processManager;
	final PatternToProperty patternToProperty;
	final File tmpLocalDirectory;

	final File toBeAnalyzedCode;
	final File relationalPropModule;
	final File temporalPropModule;
	final List<File> dependentFiles;
	final List<ImplicationLattic> implications;
	final List<InconsistencyGraph> inconsistencies;
	final Map<String, Integer> patternToPriorityEncoder;

	int approximationRequestCount = 1;

	public Approximator(ServerSocketInterface interfacE, ProcessDistributer processManager,
			PatternToProperty patternToProperty, File tmpLocalDirectory, File toBeAnalyzedCode,
			File relationalPropModuleOriginal, File temporalPropModuleOriginal, List<File> dependentFiles) {
		this.interfacE = interfacE;
		this.processManager = processManager;
		this.toBeAnalyzedCode = toBeAnalyzedCode;
		this.relationalPropModule = relationalPropModuleOriginal;
		this.temporalPropModule = temporalPropModuleOriginal;
		this.dependentFiles = new ArrayList<>(dependentFiles);
		this.tmpLocalDirectory = tmpLocalDirectory;
		this.patternToProperty = patternToProperty;

		implications = new LinkedList<>();
		// The BinaryImplicationLattic and TernaryImplicationLAttice are not
		// connected
		// to the given relational and temporal patterns stored in a request
		// message
		implications.add(new BinaryImplicationLatticImperative());
		implications.add(new TernaryImplicationLatticImperative());

		inconsistencies = new LinkedList<>();
		inconsistencies.add(new TernaryInconsistencyGraph());
		inconsistencies.add(new BinaryInconsistencyGraph());

		patternToPriorityEncoder = new HashMap<>();
		try {
			patternToPriorityEncoder.putAll(TemporalPropertyGenerator.generateAllPropertiesPiority());
		} catch (Err e) {
			e.printStackTrace();
		}

	}

	public Approximator(ServerSocketInterface interfacE, ProcessDistributer processManager, File tmpLocalDirectory,
			File toBeAnalyzedCode, List<File> dependentFiles) {
		this(interfacE, processManager,
				new PatternToProperty(RelationalPropModule, TemporalPropModule, toBeAnalyzedCode, Optional.empty()),
				tmpLocalDirectory, toBeAnalyzedCode, RelationalPropModule, TemporalPropModule, dependentFiles);
	}

	/**
	 * Given a statement, then an strongest approximation is found and returned.
	 * The return is a the actual call. E.g. acyclic[r]
	 * 
	 * @param statement
	 * @return pair.a patternName, pair.b property.
	 * @throws Err
	 */

	public List<Pair<String, String>> strongestImplicationApproximation(Expr statement, Field field, String scope)
			throws Err {
		return strongestImplicationApproximation(PrettyPrintExpression.makeString(statement), field.label, scope);
	}

	public List<Pair<String, String>> strongestConsistentApproximation(Expr statement, Field field, String scope)
			throws Err {
		return strongestConsistentApproximation(PrettyPrintExpression.makeString(statement), field.label, scope);
	}

	public List<Pair<String, String>> weakestInconsistentApproximation(Expr statement, Field field, String scope)
			throws Err {
		return weakestInconsistentApproximation(PrettyPrintExpression.makeString(statement), field.label, scope);
	}

	/**
	 * Return all the patterns that are consistent with the given statement.
	 * 
	 * @param statement
	 * @param field
	 * @param scope
	 * @return
	 * @throws Err
	 */
	public List<Pair<String, String>> allConsistentApproximation(Expr statement, Field field, String scope) throws Err {

		return allConsistentApproximation(statement, field, scope);
	}

	public List<Pair<String, String>> allInconsistentApproximation(Expr statement, Field field, String scope)
			throws Err {
		return allInconsistentApproximation(PrettyPrintExpression.makeString(statement), field.label, scope);
	}

	public List<Pair<String, String>> weakestConsistentApproximation(Expr statement, Field field, String scope)
			throws Err {
		return weakestConsistentApproximation(PrettyPrintExpression.makeString(statement), field.label, scope);
	}

	public Boolean isInconsistent(Expr statement, Field field, String scope) throws Err {
		return isInconsistent(PrettyPrintExpression.makeString(statement), field.label, scope);
	}

	public Boolean isInconsistent(File toBeAnalyzedCode, Expr statement, Field field, String scope) throws Err {
		return isInconsistent(toBeAnalyzedCode, PrettyPrintExpression.makeString(statement), field.label, scope);
	}

	StringBuilder sb_strongestImplicationApproximation = new StringBuilder(
			"Map<String, List<Pair<String, String>> > strongestImpl = new HashMap<>();\n");

	public List<Pair<String, String>> strongestImplicationApproximation(String statement, String fieldLabel,
			String scope) {
		List<Pair<String, String>> approx = findApproximation(statement, fieldLabel, scope,
				IfPropertyToAlloyCode.EMPTY_CONVERTOR, filterWeakerApproximations);

		makeNewRecordInCacheResult(sb_strongestImplicationApproximation, "strongestImpl", statement, fieldLabel, scope,
				approx);

		return approx;
	}

	StringBuilder sb_strongestConsistentApproximation = new StringBuilder(
			"Map<String, List<Pair<String, String>> > strongestCon = new HashMap<>();\n");

	public List<Pair<String, String>> strongestConsistentApproximation(String statement, String fieldLabel,
			String scope) {
		List<Pair<String, String>> approx = findApproximation(statement, fieldLabel, scope,
				AndPropertyToAlloyCode.EMPTY_CONVERTOR, filterWeakerApproximations);

		makeNewRecordInCacheResult(sb_strongestConsistentApproximation, "strongestCon", statement, fieldLabel, scope,
				approx);

		return approx;
	}

	public StringBuilder sb_weakestInconsistentApproximation = new StringBuilder(
			"Map<String, List<Pair<String, String>> > weakestIncon = new HashMap<>();\n");

	public List<Pair<String, String>> weakestInconsistentApproximation(String statement, String fieldLabel,
			String scope) {
		List<Pair<String, String>> approx = findApproximation(statement, fieldLabel, scope,
				InconPropertyToAlloyCode.EMPTY_CONVERTOR, filterStrongerApproximations);

		makeNewRecordInCacheResult(sb_weakestInconsistentApproximation, "weakestIncon", statement, fieldLabel, scope,
				approx);

		return approx;
	}

	public StringBuilder sb_allConsistentApproximation = new StringBuilder(
			"Map<String, List<Pair<String, String>> > allCon = new HashMap<>();\n");

	public List<Pair<String, String>> allConsistentApproximation(String statement, String fieldLabel, String scope) {
		final List<Pair<String, String>> weakestIncons = weakestInconsistentApproximation(statement, fieldLabel, scope);
		final List<String> weakestInconsPatterns = weakestIncons.stream().map(a -> a.a).collect(Collectors.toList());
		final Set<String> allPatterns = inconsistencies.stream().map(a -> a.getAllPatterns()).flatMap(a -> a.stream())
				.collect(Collectors.toSet());
		final Set<String> allIncons = new HashSet<>(weakestInconsPatterns);
		// all stronger patterns of an inconsistent pattern are also
		// inconsistent.
		weakestInconsPatterns.stream().forEach(a -> allIncons.addAll(strongerPatterns(a)));
		final Set<String> allConsists = new HashSet<>(allPatterns);
		allConsists.removeAll(allIncons);
		List<Pair<String, String>> approx = convertPatternToProperty(allConsists, fieldLabel);
		makeNewRecordInCacheResult(sb_allConsistentApproximation, "allCon", statement, fieldLabel, scope, approx);
		return approx;
	}

	public StringBuilder sb_allInconsistentApproximation = new StringBuilder(
			"Map<String, List<Pair<String, String>> > allInCon = new HashMap<>();\n");

	public List<Pair<String, String>> allInconsistentApproximation(String statement, String fieldLabel, String scope) {
		List<Pair<String, String>> approx = convertPatternToProperty(
				weakestInconsistentApproximation(statement, fieldLabel, scope).stream().map(a -> strongerPatterns(a.a))
						.flatMap(a -> a.stream()).collect(Collectors.toList()),
				fieldLabel);
		makeNewRecordInCacheResult(sb_allInconsistentApproximation, "allInCon", statement, fieldLabel, scope, approx);
		return approx;
	}

	public StringBuilder sb_weakestConsistentApproximation = new StringBuilder(
			"Map<String, List<Pair<String, String>> > weakestCon = new HashMap<>();\n");

	public List<Pair<String, String>> weakestConsistentApproximation(String statement, String fieldLabel,
			String scope) {
		List<Pair<String, String>> approx = allConsistentApproximation(statement, fieldLabel, scope).stream()
				.filter(a -> implications.stream().anyMatch(b -> {
					try {
						return b.hasPattern(a.a) && b.getNextImpliedProperties(a.a).isEmpty();
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				})).collect(Collectors.toList());

		makeNewRecordInCacheResult(sb_weakestConsistentApproximation, "weakestCon", statement, fieldLabel, scope,
				approx);

		return approx;
	}

	private void makeNewRecordInCacheResult(StringBuilder sb, String name, String statement, String fieldLabel,
			String scope, List<Pair<String, String>> approx) {
		// Converting to Cache.
		String key = statement + fieldLabel;
		sb.append("allMokedApproximations.get(\"NAMENAME\").get(\"" + name + "\").put(\"").append(key)
				.append("\", Arrays.asList(").append(approx.stream()
						.map(p -> "new Pair<>(\"" + p.a + "\", \"" + p.b + "\")").collect(Collectors.joining(", ")))
				.append("));\n");
	}

	public String getAllChachedResults() {
		return Stream
				.<String> of((sb_allConsistentApproximation.toString() + sb_allInconsistentApproximation.toString()
						+ sb_weakestConsistentApproximation.toString() + sb_strongestImplicationApproximation.toString()
						+ sb_strongestConsistentApproximation.toString()
						+ sb_weakestInconsistentApproximation.toString() + sb_isInconsistent.toString()).split("\n"))
				.sorted().collect(Collectors.partitioningBy(s -> s.startsWith("Map"))).entrySet().stream()
				.flatMap(entry -> entry.getValue().stream()).collect(Collectors.joining("\n"));

	}
	
	public String getApproximationTimeLog() {
		return timeOfApproximation.toString()+cacheKey.toString();
	}

	public StringBuilder sb_isInconsistent = new StringBuilder("Map<String, Boolean > isIncon = new HashMap<>();\n");

	public Boolean isInconsistent(String statement, String fieldLabel, String scope) {
		Boolean result = !findApproximation(statement, fieldLabel, scope, InconExpressionToAlloyCode.EMPTY_CONVERTOR,
				Function.identity()).isEmpty();
		// Converting to Cache.
		String key = statement + fieldLabel;
		sb_isInconsistent.append("allMokedApproximations.get(\"NAMENAME\").get(\"isIncon\").put(\"").append(key)
				.append("\", ").append(result).append(");\n");

		return result;
	}

	public Boolean isInconsistent(File toBeAnalyzedCode, String statement, String fieldLabel, String scope) {
		Boolean result = !findApproximation(toBeAnalyzedCode, statement, fieldLabel, scope,
				InconExpressionToAlloyCode.EMPTY_CONVERTOR, Function.identity()).isEmpty();
		// Converting to Cache.
		String key = statement + fieldLabel;
		sb_isInconsistent.append("allMokedApproximations.get(\"NAMENAME\").get(\"isIncon\").put(\"").append(key)
				.append("\", ").append(result).append(");\n");

		return result;
	}

	protected List<Pair<String, String>> findApproximation(String statement, String fieldLabel, String scope,
			PropertyToAlloyCode coder, Function<List<Pair<String, String>>, List<Pair<String, String>>> filter) {
		return findApproximation(this.toBeAnalyzedCode, statement, fieldLabel, scope, coder, filter);
	}

	final Map<Integer, PatternProcessedResult> cacheApproximation = new HashMap<>();
	final StringBuilder cacheKey = new StringBuilder();

	protected Integer computeKey(File toBeAnalyzedCode, String statement, String fieldLabel, String scope,
			PropertyToAlloyCode coder) {
		Integer key = (toBeAnalyzedCode.getAbsoluteFile() + statement + fieldLabel + scope).hashCode()
				+ coder.hashCode() + coder.getClass().getName().hashCode();

		cacheKey.append("key,").append(key).append(",").append(toBeAnalyzedCode.getAbsoluteFile()).append(",").append(statement)
				.append(",").append(fieldLabel).append(",").append(scope).append(",").append(coder.getClass().getName())
				.append(",").append(coder.getPredName()).append("\n");

		return key;
	}

	final StringBuilder timeOfApproximation = new StringBuilder();

	/**
	 * 
	 * @param toBeAnalyzedCode
	 * @param statement
	 * @param fieldLabel
	 * @param scope
	 * @param coder
	 * @param filter
	 * @return
	 */
	protected List<Pair<String, String>> findApproximation(File toBeAnalyzedCode, String statement, String fieldLabel,
			String scope, PropertyToAlloyCode coder,
			Function<List<Pair<String, String>>, List<Pair<String, String>>> filter) {

		long startTime = System.currentTimeMillis();

		Integer cacheKey = computeKey(toBeAnalyzedCode, statement, fieldLabel, scope, coder);
		if (!cacheApproximation.containsKey(cacheKey)) {

			if (approximationRequestCount % 60 == 0) {
				((RemoteProcessManager) processManager).replaceAllProcesses();
				while (!((RemoteProcessManager) processManager).allProcessesIDLE()) {
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			// Creating a request message
			Map<String, LazyFile> files = new HashMap<>();

			files.put("toBeAnalyzedCode", new LazyFile(toBeAnalyzedCode.getAbsolutePath()));
			files.put("relationalPropModuleOriginal", new LazyFile(relationalPropModule.getAbsolutePath()));
			files.put("temporalPropModuleOriginal", new LazyFile(temporalPropModule.getAbsolutePath()));
			for (File file : dependentFiles)
				files.put("relationalLib", new LazyFile(file.getAbsolutePath()));

			PatternProcessingParam param = new PatternProcessingParam(0, tmpLocalDirectory, UUID.randomUUID(),
					Long.MAX_VALUE, fieldLabel, coder, statement, scope, files);
			PatternRequestMessage message = new PatternRequestMessage(interfacE.getHostProcess(), param);

			// Wait until the result is sent back
			final SynchronizedResult<PatternProcessedResult> result = new SynchronizedResult<>();
			MessageEventListener<MessageReceivedEventArgs> receiveListener = new MessageEventListener<MessageReceivedEventArgs>() {
				@Override
				public void actionOn(ResponseMessage responseMessage, MessageReceivedEventArgs messageArgs) {
					result.result = (PatternProcessedResult) responseMessage.getResult();
					synchronized (result) {
						result.notify();
					}
				}
			};
			interfacE.MessageReceived.addListener(receiveListener);
			interfacE.sendMessage(message, processManager.getActiveRandomeProcess());
			synchronized (result) {
				try {
					do {
						result.wait();
						// Wait until the response for the same session is
						// arrived.
					} while (!result.getResult().get().getParam().getAnalyzingSessionID()
							.equals(param.getAnalyzingSessionID()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			interfacE.MessageReceived.removeListener(receiveListener);

			approximationRequestCount++;
			cacheApproximation.put(cacheKey, result.getResult().get());
		}

		timeOfApproximation.append("time,")
				.append(toBeAnalyzedCode.getName()).append(",")
				.append(statement).append(",")
				.append(coder.getClass().getSimpleName()).append(",")
				.append(System.currentTimeMillis() - startTime).append(",")
				.append(Configuration.getProp("alloy_processes_number"))
				.append("\n");

		return filter.apply(cacheApproximation.get(cacheKey).getResults().get().stream()
				.map(b -> new Pair<>(b.getParam().getAlloyCoder().get().predNameB,
						b.getParam().getAlloyCoder().get().predCallB))
				.collect(Collectors.toList()));
	}

	/**
	 * The ExpressionAnalyzer tries to finds the strongest properties
	 * approximating the given expression. Since it paralyzes the computations,
	 * some noises might be returned. The function does the weaker properties if
	 * an stronger form of them exists in the input list.
	 * 
	 * @param properties
	 * @return
	 */
	Function<List<Pair<String, String>>, List<Pair<String, String>>> filterWeakerApproximations = (properties) -> {
		final Map<String, Pair<String, String>> patternMap = new HashMap<>();
		properties.stream().forEach(p -> patternMap.put(p.a, p));
		for (Pair<String, String> patternProperty : properties) {
			for (String weakerPattern : weakerPatterns(patternProperty.a)) {
				patternMap.remove(weakerPattern);
			}
		}
		return patternMap.values().stream().collect(Collectors.toList());
	};

	Function<List<Pair<String, String>>, List<Pair<String, String>>> filterStrongerApproximations = (properties) -> {
		final Map<String, Pair<String, String>> patternMap = new HashMap<>();
		properties.stream().forEach(p -> patternMap.put(p.a, p));
		for (Pair<String, String> patternProperty : properties) {
			for (String strongerPattern : strongerPatterns(patternProperty.a)) {
				patternMap.remove(strongerPattern);
			}
		}
		return patternMap.values().stream().collect(Collectors.toList());
	};

	public List<Pair<String, String>> strongerNextProperties(String pattern, String fieldName) {
		// property is in the form of A[r]. so that A is pattern
		return convertPatternToProperty(nextStrongerPatterns(pattern), fieldName);

	}

	public List<Pair<String, String>> strongerProperties(String pattern, String fieldName) {
		// property is in the form of A[r]. so that A is pattern
		return convertPatternToProperty(strongerPatterns(pattern), fieldName);

	}

	public List<String> strongerPatterns(String pattern) {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getAllRevImpliedProperties(pattern));
			} catch (Throwable e) {
				// e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> nextStrongerPatterns(String pattern) {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getNextRevImpliedProperties(pattern));
			} catch (Throwable e) {
				// e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<Pair<String, String>> weakerProperties(String pattern, String fieldName) {
		// property is in the form of A[r]. so that A is pattern
		return convertPatternToProperty(weakerPatterns(pattern), fieldName);
	}

	public List<String> weakerPatterns(String pattern) {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getAllImpliedProperties(pattern));
			} catch (Throwable e) {
			}
		}
		return Collections.unmodifiableList(result);
	}

	public boolean isInconsistent(String patternA, String patternB) {
		return inconsistencies.stream().anyMatch(a -> a.isInconsistent(patternA, patternB).equals(STATUS.True));
	}

	/**
	 * property is in the form of A[r]. so that A is pattern
	 * 
	 * @param patterns
	 * @param fieldName
	 * @return
	 */
	protected List<Pair<String, String>> convertPatternToProperty(Collection<String> patterns, String fieldName) {
		return patterns.stream().filter(p -> {
			try {
				patternToProperty.getProperty(p, fieldName);
				return true;
			} catch (RuntimeException re) {
				return false;
			}
		}).map(a -> new Pair<>(a, patternToProperty.getProperty(a, fieldName))).collect(Collectors.toList());
	}

	public Integer encodePatterForPrioritization(String pattern) {
		return patternToPriorityEncoder.containsKey(pattern) ? patternToPriorityEncoder.get(pattern)
				: Integer.MAX_VALUE;
	}

}
