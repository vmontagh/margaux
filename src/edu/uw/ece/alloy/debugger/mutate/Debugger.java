/**
 * 
 */
package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.filters.Decompose;
import edu.uw.ece.alloy.debugger.filters.FieldsExtractorVisitor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AnalyzeExternalRequest;

/**
 * The Class implementing the Algorithm in the paper
 * 
 * @author vajih
 *
 */
public class Debugger {

	protected final static Logger logger = Logger.getLogger(
			Debugger.class.getName() + "--" + Thread.currentThread().getName());;

	public final File inputSource;

	final List<Sig.Field> fields;// = Collections.emptyList();

	// A model is a conjunction of constraints
	final List<Expr> model;// = Collections.emptyList();
	// In a model in the form of M => P, P is a conjunction of constraints
	final List<Expr> property;// = Collections.emptyList();

	public Debugger(File input) throws Err {
		inputSource = input;
		CompModule world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null,
				inputSource.getAbsolutePath());

		Expr toBeCheckedModel = world.getAllCommands().get(0).formula;
		Pair<List<Expr>, List<Expr>> propertyChecking = Decompose
				.decomposetoImplications(toBeCheckedModel);
		model = Collections.unmodifiableList(propertyChecking.a);
		property = Collections.unmodifiableList(propertyChecking.b);
		fields = Collections.unmodifiableList(
				FieldsExtractorVisitor.getReferencedFields(toBeCheckedModel).stream()
						.collect(Collectors.toList()));

		localSocket = ProcessorUtil.findEmptyLocalSocket();
		patternsAnalyzingResults = new LinkedBlockingQueue<>();
		analyzerListener = new PatternAnalyzerStub(localSocket, remoteSocket,
				patternsAnalyzingResults);
	}

	public Debugger(String path) throws Err {
		this(new File(path));
	}

	/**
	 * According to the algorithm, the Input consists of: A) a set of relations B)
	 * A conjunction of constraints or a set of constraints. C) Only one command
	 * to be ran.
	 * 
	 * @throws Err
	 */

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Fields: ").append(fields).append("\n");

		sb.append("Model: ").append(model).append("\n");
		sb.append("Property: ").append(property).append("\n");

		return sb.toString();
	}

	public final InetSocketAddress localSocket;
	public InetSocketAddress remoteSocket;
	final static int SubMemory = Integer
			.parseInt(Configuration.getProp("sub_memory"));
	final static int SubStack = Integer
			.parseInt(Configuration.getProp("sub_stak"));
	final static String ProcessLoggerConfig = Configuration
			.getProp("process_logger_config");
	final PatternAnalyzerStub analyzerListener;
	final BlockingQueue<AlloyProcessedResult> patternsAnalyzingResults;

	/**
	 * The analyzer is started on a remote main. It starts on a remote JVM and IP
	 * addresses will be passed.
	 * 
	 * @throws IOException
	 */
	public void bootRemoteAnalyzer() throws IOException {

		remoteSocket = ProcessorUtil.findEmptyLocalSocket();

		// start the listener
		analyzerListener.startThread();

		final String java = "java";
		final String debug = Boolean.parseBoolean(System.getProperty("debug"))
				? "yes" : "no";

		try {
			ProcessBuilder pb = new ProcessBuilder(java, "-Xmx" + SubMemory + "m",
					"-Xss" + SubStack + "k", "-Ddebug=" + debug,
					"-Djava.util.logging.config.file=" + ProcessLoggerConfig, "-cp",
					System.getProperty("java.class.path"),
					ExpressionAnalyzerRunner.class.getName(), "" + remoteSocket.getPort(),
					"" + remoteSocket.getAddress().getHostAddress(),
					"" + localSocket.getPort(),
					"" + localSocket.getAddress().getHostAddress());

			System.out.println("remote->" + remoteSocket);
			System.out.println("local->" + localSocket);

			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			pb.start();

		} catch (IOException e) {
			logger
					.log(Level.SEVERE,
							"[" + Thread.currentThread().getName() + "]"
									+ "Not able to create a new process on port: " + remoteSocket,
							e);
			throw e;
		}

		try {
			// wait until the other side, i.e. ExpressionAnalyzerRunner, becomes ready
			synchronized (patternsAnalyzingResults) {
				patternsAnalyzingResults.wait(1000 * 1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<?> analyzeImpliedPatterns() {
		List<?> results = new ArrayList<>();

		// clean the result storage.
		patternsAnalyzingResults.clear();

		try {
			AnalyzeExternalRequest request = new AnalyzeExternalRequest(false, false,
					true, false, Configuration.getProp("relational_properties_tagged"),
					Configuration.getProp("temporal_properties_tagged"),
					inputSource.getAbsolutePath());

			request.sendMe(remoteSocket);
			System.out.println(
					"patternsAnalyzingResults1->" + patternsAnalyzingResults.hashCode()
							+ " " + patternsAnalyzingResults.size());
			synchronized (patternsAnalyzingResults) {
				long b = System.currentTimeMillis();
				System.out
						.println("wait is started->" + (System.currentTimeMillis() - b));
				patternsAnalyzingResults.wait(1000 * 1000);
				System.out
						.println("wait is ended->" + (System.currentTimeMillis() - b));
			}

			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] " + "The ");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(
				"patternsAnalyzingResults2->" + patternsAnalyzingResults.hashCode()
						+ " " + patternsAnalyzingResults.size());
		System.out.println("The result is:" + patternsAnalyzingResults);
		System.out.println("The result is:" + patternsAnalyzingResults.size());

		Approximator approximator = new Approximator();
		for (AlloyProcessedResult result : patternsAnalyzingResults)
			approximator.addDirectImplication(result);

		System.out.println(
				"Approximation: " + approximator.getDirectImpliedApproximation());

		// Transform the results into an appropriate result.
		return Collections.unmodifiableList(results);
	}

}
