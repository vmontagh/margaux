package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.PatternToProperty;
import edu.uw.ece.alloy.debugger.knowledgebase.TernaryImplicationLattic;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.IfPropertyToAlloyCode;
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
	 * @return pair.a patternNAme, pair.b property.
	 */

	public List<Pair<String, String>> strongestApproximation(Expr statement,
			Field field, String scope) {
		return strongestApproximation(statement.toString(), field.label, scope);
	}

	public List<Pair<String, String>> strongestApproximation(String statement,
			String fieldLabel, String scope) {
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
				tmpLocalDirectory, UUID.randomUUID(), Long.MAX_VALUE, fieldLabel,
				IfPropertyToAlloyCode.EMPTY_CONVERTOR, statement, scope, files);
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

		System.out.println(
				statement + ": " + result.getResult().get().getResults().get());

		return result.getResult().get().getResults().get().stream()
				.map(b -> new Pair<>(b.getParam().getAlloyCoder().get().predNameB,
						b.getParam().getAlloyCoder().get().predCallB))
				.collect(Collectors.toList());
	}

	public List<String> strongerProperties(String property, String fieldName) {
		// property is in the form of A[r]. so that A is pattern
		String pattern = property.substring(0, property.indexOf("["));
		return strongerPatterns(pattern).stream()
				.map(a -> patternToProperty.getProperty(a, fieldName))
				.collect(Collectors.toList());
	}

	public List<String> strongerPatterns(String pattern) {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getNextImpliedProperties(pattern));
			} catch (Err e) {
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> weakerProperties(String property, String fieldName) {
		// property is in the form of A[r]. so that A is pattern
		String pattern = property.substring(0, property.indexOf("["));
		return weakerPatterns(pattern).stream()
				.map(a -> patternToProperty.getProperty(a, fieldName))
				.collect(Collectors.toList());
	}

	public List<String> weakerPatterns(String pattern) {
		List<String> result = new ArrayList<>();
		for (ImplicationLattic il : implications) {
			try {
				result.addAll(il.getNextRevImpliedProperties(pattern));
			} catch (Err e) {
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(result);
	}

}