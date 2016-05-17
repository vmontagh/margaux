package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.uw.ece.alloy.MyReporter;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.filters.Decompose;
import edu.uw.ece.alloy.debugger.filters.ExtractorUtils;
import edu.uw.ece.alloy.util.Utils;

/**
 * Given the correct model, it tried to find out whether the given instance is
 * acceptable or not.
 * 
 * @author vajih
 *
 */
public class CorrectModelOracle implements Oracle {

	protected final static Logger logger = Logger
			.getLogger(CorrectModelOracle.class.getName() + "--"
					+ Thread.currentThread().getName());

	final File sourceFile;
	final String sourceCode;
	final protected Expr constraint;
	// A model is a conjunction of constraints. this.constraint = model
	final List<Expr> model;// = Collections.emptyList();
	// In a model in the form of M => P, P is a conjunction of constraints.
	// constraint = model => property
	final List<Expr> property;// = Collections.emptyList();
	final protected String scope;

	public CorrectModelOracle(final File sourceFile) {
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

		if (world.getAllCommands().size() != 1)
			throw new RuntimeException("Only one valid command should be passed. "
					+ "Comment out the rest or add at least one.");

		final Command command = world.getAllCommands().get(0);
		constraint = command.formula;

		Pair<List<Expr>, List<Expr>> propertyChecking = Decompose
				.decomposetoImplications(constraint);
		model = Collections.unmodifiableList(propertyChecking.a);
		property = Collections.unmodifiableList(propertyChecking.b);
		scope = ExtractorUtils.extractScopeFromCommand(command);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.mutate.Oracle#isIntended(java.lang.String)
	 */
	@Override
	public boolean isIntended(String example) {
		String newModel = model.stream().map(m -> m.toString())
				.collect(Collectors.joining("\n\t"));
		newModel = newModel + "\n\t(" + example + ")";
		String predName = "pred_" + Math.abs(newModel.hashCode());
		String newPred = "pred " + predName + "{" + newModel + "}";
		String newCommand = "run " + predName + " " + scope;

		String newCode = sourceCode + "\n" + newPred + "\n" + newCommand;
		File newSource = new File(sourceFile.getParentFile().getAbsolutePath(),
				sourceFile.getName().replace(".als", "") + predName + ".als");

		try {
			Util.writeAll(newSource.getAbsolutePath(), newCode);
		} catch (Err e) {
			logger.severe(Utils.threadName()
					+ " The new Source cannot be written on the disk.");
			e.printStackTrace();
			throw new RuntimeException(e);

		}
		MyReporter rep = new MyReporter();

		try {
			A4CommandExecuter.getInstance().run(newSource.getAbsolutePath(), rep,
					predName);
		} catch (Err e) {
			logger.severe(
					Utils.threadName() + " An error occured while processing the code");
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if (rep.sat != 1 && rep.sat != -1)
			throw new RuntimeException("The analyzing result is not expected");

		return rep.sat == 1;
	}

}
