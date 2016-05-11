package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.uw.ece.alloy.debugger.filters.Decompose;
import edu.uw.ece.alloy.debugger.filters.FieldsExtractorVisitor;
import edu.uw.ece.alloy.util.Utils;

/**
 * The debugger algorithm is an abstract implementation of the Discriminating
 * Examples Search Procedure.
 * 
 * The Object-Oriented implementation follows template pattern, which some parts
 * of the algorithm is abstracted and should be implemented in the actual
 * implementations.
 * 
 * @author vajih
 *
 */
public abstract class DebuggerAlgorithm {

	protected final static Logger logger = Logger
			.getLogger(DebuggerAlgorithm.class.getName() + "--"
					+ Thread.currentThread().getName());

	/* The source of an Alloy file */
	final public File sourceFile;
	final public String sourceCode;
	/* Extracted fields are stored here */
	final protected List<Field> fields;
	/* The whole constraint = */
	final protected Expr constraint;
	final protected String scope;

	// A model is a conjunction of constraints. this.constraint = model
	final List<Expr> model;// = Collections.emptyList();
	// In a model in the form of M => P, P is a conjunction of constraints.
	// constraint = model => property
	final List<Expr> property;// = Collections.emptyList();

	// final
	Approximator approximator;

	public DebuggerAlgorithm(final File sourceFile,
			final Approximator approximator) {
		this.sourceFile = sourceFile;
		this.sourceCode = Utils.readFile(sourceFile.getAbsolutePath());
		CompModule world;
		try {
			world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null,
					sourceFile.getAbsolutePath());
		} catch (Err e) {
			logger.severe(
					Utils.threadName() + "The Alloy file cannot be loaded or parsed:"
							+ sourceFile + "\n" + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		constraint = world.getAllCommands().get(0).formula;

		if (world.getAllCommands().size() != 1)
			throw new RuntimeException("Only one valid command should be passed. "
					+ "Comment out the rest or add at least one.");

		Expr toBeCheckedModel = world.getAllCommands().get(0).formula;
		Pair<List<Expr>, List<Expr>> propertyChecking = Decompose
				.decomposetoImplications(toBeCheckedModel);
		model = Collections.unmodifiableList(propertyChecking.a);
		property = Collections.unmodifiableList(propertyChecking.b);
		// TODO(vajih) extract Scope from the command
		scope = " for 5";
		try {
			fields = Collections.unmodifiableList(
					FieldsExtractorVisitor.getReferencedFields(toBeCheckedModel).stream()
							.collect(Collectors.toList()));
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		this.approximator = approximator;
	}

	/**
	 * Given a statement, then an strongest approximation is found and returned.
	 * The return is a the actual call. E.g. acyclic[r]
	 * 
	 * @param statement
	 * @return
	 */
	public String strongestApproximation(Expr statement, Field field) {
		return null; // approximator.strongestApproximationModel(statement, field,
								 // scope);
	}

	public void run() {
		for (Field field : fields) {
			for (Expr modelPart : model) {
				String restModel = model.stream().filter(m -> !m.equals(modelPart))
						.map(a -> a.toString()).collect(Collectors.joining(" and "));

				List<Pair<String, String>> approximations = approximator
						.strongestApproximation(modelPart, field, scope);

				System.out.println("approximations->" + approximations);

				String approximationProperty = "";
				for (Pair<String, String> approximation : approximations) {
					List<String> strongerApprox = approximator
							.strongerProperties(approximation.b);
					List<String> weakerApprox = approximator
							.weakerProperties(approximation.b);
					if (!strongerApprox.isEmpty()) {
						approximationProperty = strongerApprox.get(0);
					} else if (!weakerApprox.isEmpty()) {
						approximationProperty = weakerApprox.get(0);
					} else {
						break;
					}
				}

				if (approximationProperty.isEmpty())
					break;

				String ModelPartialApproximation = approximationProperty
						+ (!restModel.trim().isEmpty() ? " and " + restModel : "");

				// Make a new Model
				String predName = "approximate_"
						+ Math.abs(ModelPartialApproximation.hashCode());
				String pred = String.format("pred %s[]{%s}\n", predName,
						ModelPartialApproximation);
				String newHeader = "open " + approximator.relationalPropModuleOriginal
						.getName().replace(".als", "\n");
				newHeader += "open " + approximator.temporalPropModuleOriginal.getName()
						.replace(".als", "\n");
				String newCommandName = "run " + predName + " " + scope;
				String newCode = newHeader + "\n" + sourceCode + "\n" + pred + "\n"
						+ newCommandName;
				File newCodeFile = new File(sourceFile.getParentFile(),
						predName + ".als");
				try {
					System.out.println("newCodeFile->" + newCodeFile);
					Util.writeAll(newCodeFile.getAbsolutePath(), newCode);
				} catch (Err e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				findOnBorderExamples(newCodeFile);
				// ask the user
			}
		}

	}

	protected Optional<Field> pickField() {
		return this.fields.size() > 0 ? Optional.of(this.fields.remove(0))
				: Optional.empty();
	}

	/**
	 * TODO implement the call to on border example generator
	 * 
	 * @return
	 */
	public List<String> findOnBorderExamples(File path) {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DebuggerAlgorithm [sourceFile=" + sourceFile + ", fields=" + fields
				+ ", constraint=" + constraint + ", model=" + model + ", property="
				+ property + "]";
	}

}
