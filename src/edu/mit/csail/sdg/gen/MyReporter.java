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
	// For example, here we choose to display each "warning" by printing it to System.out
	@Override public void warning(ErrorWarning msg) {
		System.out.println("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
		System.out.flush();
	}
	public void typecheck (String msg) {
	}
	
	@Override public void solve(final int primaryVars, final int totalVars, final int clauses) {
		this.trasnalationTime = (System.currentTimeMillis()-lastTime);
		this.lastTime = System.currentTimeMillis();
		this.clauses = clauses;
		this.totalVaraibles = totalVars;
		System.out.println("solve");


	}
	@Override public void translate(String solver, int bitwidth, int maxseq, int skolemDepth, int symmetry) {
		System.out.println("translate");

		this.lastTime = System.currentTimeMillis();
	}

	@Override public void resultSAT (Object command, long solvingTime, Object solution) {
		System.out.printf("resultSAT ");
		this.solveTime = System.currentTimeMillis()-lastTime;
		this.sat=1;
	}
	
	public void resultUNSAT (Object command, long solvingTime, Object solution) {
		System.out.printf("resultUNSAT is called and the solvingTime is %d and the diffenrence is: %d%n",solvingTime,System.currentTimeMillis()-lastTime);
		this.solveTime = System.currentTimeMillis()-lastTime;
		this.sat=-1;
	}
	
	public void evalute(long elauationTime, long instances) {
		System.out.println("evalute");

		this.evalTime = elauationTime;
		this.evalInsts = instances;
	}
	
}

