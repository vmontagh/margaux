package edu.uw.ece.alloy;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr.AttrType;
import edu.mit.csail.sdg.alloy4compiler.ast.Bounds;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;

public class NewSyntaxExecuterJob extends ExecuterJob  {


	public NewSyntaxExecuterJob(String reportFile) {
		super(reportFile);
		// TODO Auto-generated constructor stub
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 131231234134343L;


	protected void callExecuter(String fileName) throws Err {

		
		ExampleUsingTheCompiler.run(new String[]{fileName},rep);
		
		updateResult(System.currentTimeMillis(), fileName, rep.evalTime,
				rep.solveTime, rep.trasnalationTime, rep.totalVaraibles, rep.clauses,/*ans.satisfiable()*/rep.sat==1, rep.evalInsts, -1);


	}


	/**
	 * @param args
	 * @throws Err 
	 */
	public static void main(String[] args) throws Err {
		// TODO Auto-generated method stub
		NewSyntaxExecuterJob nsej = new NewSyntaxExecuterJob("report.txt");
		nsej.callExecuter(args[0]);
		nsej = new NewSyntaxExecuterJob("report.txt");
		nsej.callExecuter(args[1]);
	}

}
