package edu.mit.csail.sdg.gen;

import java.io.Serializable;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;

public class MyReporter extends A4Reporter implements Serializable{
	
	private static final long serialVersionUID = 7526472295622776147L;
	
	private long lastTime=0;
	public long trasnalationTime = 0;
	public long totalVaraibles = 0;
	public long clauses = 0;
	public long solveTime = 0;
	public long evalTime = 0;
	public long evalInsts = 0;
	public int sat = 0;

	public long started2Solve = 0;
	
	// For example, here we choose to display each "warning" by printing it to System.out
	@Override public void warning(ErrorWarning msg) {
		System.out.println("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
		System.out.flush();
	}
	@Override public void solve(final int primaryVars, final int totalVars, final int clauses) {
		this.trasnalationTime = (System.currentTimeMillis()-lastTime);
		this.lastTime = System.currentTimeMillis();
		this.clauses = clauses;
		this.totalVaraibles = totalVars;
		this.started2Solve = System.currentTimeMillis();

	}
	@Override public void translate(String solver, int bitwidth, int maxseq, int skolemDepth, int symmetry) {
		this.lastTime = System.currentTimeMillis();
	}

	@Override public void resultSAT (Object command, long solvingTime, Object solution) {
		this.solveTime = solvingTime;
		this.sat=1;
	}
	
	public void resultUNSAT (Object command, long solvingTime, Object solution) {
		this.solveTime = solvingTime;
		this.sat=-1;
	}
	
	public void evalute(long elauationTime, long instances) {
		this.evalTime = elauationTime;
		this.evalInsts = instances;
	}
	
}

