package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.util.ArrayList;
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

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.PrettyPrintExpression;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.PatternToProperty;
import edu.uw.ece.alloy.debugger.knowledgebase.TernaryImplicationLattic;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AndPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.IfPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.InconExpressionToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.InconPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessDistributer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger.PatternRequestMessage;
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

	final static Logger logger = Logger.getLogger(
			Approximator.class.getName() + "--" + Thread.currentThread().getName());

	final public static File RelationalPropModule = new File(
			Configuration.getProp("relational_properties_tagged"));
	final public static File TemporalPropModule = new File(
			Configuration.getProp("temporal_properties_tagged"));
	final ServerSocketInterface interfacE;
	final ProcessDistributer processManager;
	final PatternToProperty patternToProperty;
	final File tmpLocalDirectory;

	final File toBeAnalyzedCode;
	final File relationalPropModule;
	final File temporalPropModule;
	final List<File> dependentFiles;
	final List<ImplicationLattic> implications;

	public Approximator(ServerSocketInterface interfacE,
			ProcessDistributer processManager, PatternToProperty patternToProperty,
			File tmpLocalDirectory, File toBeAnalyzedCode,
			File relationalPropModuleOriginal, File temporalPropModuleOriginal,
			List<File> dependentFiles) {
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
		// to the given relational and temporal patterns stored in a request message
		implications.add(new BinaryImplicationLattic());
		implications.add(new TernaryImplicationLattic());
	}

	public Approximator(ServerSocketInterface interfacE,
			ProcessDistributer processManager, File tmpLocalDirectory,
			File toBeAnalyzedCode, List<File> dependentFiles) {
		this(interfacE, processManager,
				new PatternToProperty(RelationalPropModule, TemporalPropModule,
						toBeAnalyzedCode, Optional.empty()),
				tmpLocalDirectory, toBeAnalyzedCode, RelationalPropModule,
				TemporalPropModule, dependentFiles);
	}

	/**
	 * Given a statement, then an strongest approximation is found and returned.
	 * The return is a the actual call. E.g. acyclic[r]
	 * 
	 * @param statement
	 * @return pair.a patternName, pair.b property.
	 * @throws Err 
	 */

	public List<Pair<String, String>> strongestImplicationApproximation(
			Expr statement, Field field, String scope) throws Err {
		return strongestImplicationApproximation(PrettyPrintExpression.makeString(statement), field.label,
				scope);
	}

	public List<Pair<String, String>> strongestConsistentApproximation(
			Expr statement, Field field, String scope) throws Err {
		return strongestConsistentApproximation(PrettyPrintExpression.makeString(statement), field.label,
				scope);
	}

	public List<Pair<String, String>> weakestInconsistentApproximation(
			Expr statement, Field field, String scope) throws Err {
		return weakestInconsistentApproximation(PrettyPrintExpression.makeString(statement), field.label,
				scope);
	}
	
	public Boolean isInconsistent(
			Expr statement, Field field, String scope) throws Err {
		return isInconsistent(PrettyPrintExpression.makeString(statement), field.label,
				scope);
	}
	
	public List<Pair<String, String>> strongestImplicationApproximation(
			String statement, String fieldLabel, String scope) {
		return findApproximation(statement, fieldLabel, scope,
				IfPropertyToAlloyCode.EMPTY_CONVERTOR, filterWeakerApproximations);
	}

	public List<Pair<String, String>> strongestConsistentApproximation(
			String statement, String fieldLabel, String scope) {
		return findApproximation(statement, fieldLabel, scope,
				AndPropertyToAlloyCode.EMPTY_CONVERTOR, filterWeakerApproximations);
	}

	StringBuilder sb = new StringBuilder("Map<String, List<Pair<String, String>> > weakestIncon = new HashMap<>();\n");
	
	public List<Pair<String, String>> weakestInconsistentApproximation(
			String statement, String fieldLabel, String scope) {
		List<Pair<String, String>> approx = findApproximation(statement, fieldLabel, scope,
				InconPropertyToAlloyCode.EMPTY_CONVERTOR, filterStrongerApproximations);

		// Converting to Cache.
		String key = statement + fieldLabel + scope;
		sb.append("weakestIncon.put(\"").append(key).append("\", Arrays.asList(").append(
		approx.stream().map(p-> "new Pair<>(\""+p.a+"\", \""+p.b+"\")").collect(Collectors.joining(", "))).append("));\n");
		System.out.println(sb);
		
		return approx;
	}
	
	public Boolean isInconsistent(
			String statement, String fieldLabel, String scope) {
		return !findApproximation(statement, fieldLabel, scope,
				InconExpressionToAlloyCode.EMPTY_CONVERTOR, Function.identity()).isEmpty();
	}

	/**
	 * 
	 * @param statement
	 * @param fieldLabel
	 * @param scope
	 * @return pattern and property
	 */
	protected List<Pair<String, String>> findApproximation(String statement,
			String fieldLabel, String scope, PropertyToAlloyCode coder,
			Function<List<Pair<String, String>>, List<Pair<String, String>>> filter) {
		// Creating a request message
		Map<String, LazyFile> files = new HashMap<>();

		files.put("toBeAnalyzedCode",
				new LazyFile(toBeAnalyzedCode.getAbsolutePath()));
		files.put("relationalPropModuleOriginal",
				new LazyFile(relationalPropModule.getAbsolutePath()));
		files.put("temporalPropModuleOriginal",
				new LazyFile(temporalPropModule.getAbsolutePath()));
		for (File file : dependentFiles)
			files.put("relationalLib", new LazyFile(file.getAbsolutePath()));

		PatternProcessingParam param = new PatternProcessingParam(0,
				tmpLocalDirectory, UUID.randomUUID(), Long.MAX_VALUE, fieldLabel, coder,
				statement, scope, files);
		PatternRequestMessage message = new PatternRequestMessage(
				interfacE.getHostProcess(), param);

		// Wait until the result is sent back
		final SynchronizedResult<PatternProcessedResult> result = new SynchronizedResult<>();
		MessageEventListener<MessageReceivedEventArgs> receiveListener = new MessageEventListener<MessageReceivedEventArgs>() {
			@Override
			public void actionOn(ResponseMessage responseMessage,
					MessageReceivedEventArgs messageArgs) {
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
					// Wait until the response for the same session is arrived.
				} while (!result.getResult().get().getParam().getAnalyzingSessionID()
						.equals(param.getAnalyzingSessionID()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		interfacE.MessageReceived.removeListener(receiveListener);

		
		System.out.println("result:"+result.getResult().get().getResults());
		
		return filter.apply(
				result.getResult().get().getResults().get().stream()
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
	Function<List<Pair<String, String>>, List<Pair<String, String>>> filterWeakerApproximations = 
			(properties) ->{
		final Map<String, Pair<String, String>> patternMap = new HashMap<>();
		properties.stream().forEach(p -> patternMap.put(p.a, p));
		for (Pair<String, String> patternProperty : properties) {
			for (String weakerPattern : weakerPatterns(patternProperty.a)) {
				patternMap.remove(weakerPattern);
			}
		}
		return patternMap.values().stream().collect(Collectors.toList());
	};
	
	Function<List<Pair<String, String>>, List<Pair<String, String>>> filterStrongerApproximations = 
			(properties) ->{
		final Map<String, Pair<String, String>> patternMap = new HashMap<>();
		properties.stream().forEach(p -> patternMap.put(p.a, p));
		for (Pair<String, String> patternProperty : properties) {
			for (String strongerPattern : strongerPatterns(patternProperty.a)) {
				patternMap.remove(strongerPattern);
			}
		}
		return patternMap.values().stream().collect(Collectors.toList());
	};
	

	public List<Pair<String, String>> strongerProperties(String pattern, String fieldName) {
		// property is in the form of A[r]. so that A is pattern
		return strongerPatterns(pattern).stream()
				.map(a -> new Pair<>(a, patternToProperty.getProperty(a, fieldName)))
				.collect(Collectors.toList());
	}

	public List<String> strongerPatterns(String pattern) {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getAllRevImpliedProperties(pattern));
			} catch (Err e) {
				//e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> weakerProperties(String pattern, String fieldName) {
		// property is in the form of A[r]. so that A is pattern
		return weakerPatterns(pattern).stream()
				.map(a -> patternToProperty.getProperty(a, fieldName))
				.collect(Collectors.toList());
	}

	public List<String> weakerPatterns(String pattern) {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getAllImpliedProperties(pattern));
			} catch (Err e) {
				//e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

}
