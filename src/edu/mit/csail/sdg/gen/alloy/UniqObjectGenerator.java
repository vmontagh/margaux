package edu.mit.csail.sdg.gen.alloy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import kodkod.instance.Instance;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.gen.visitor.FieldDecomposer;

public abstract class UniqObjectGenerator {

	/**The current command is needed if the a part of model is required to be solved.*/
	public abstract List<Instance> generate(final Expr expr, final Sig uniqSig, final Command command) throws Exception;
	
	protected List<Sig> extractAllSigs(final Sig sig, final Expr expr) throws Err{

		final Queue<Sig> queue = new LinkedList<>();
		final Map<String, Sig> visited = new HashMap<String, Sig>();

		final FieldDecomposer  fldDeocmposer = new FieldDecomposer();

		queue.add(sig);
		queue.addAll(fldDeocmposer.extractSigsInExpr(expr));

		//LoggerUtil.Detaileddebug(this, "The queue is %s %nThe expr is %s", queue,expr);

		while(!queue.isEmpty()){
			Sig tSig = queue.poll(); 
			if(!visited.containsKey(tSig.label) && !tSig.builtin){
				queue.addAll(fldDeocmposer.extractSigsFromFields(tSig.getFields().makeCopy()));
				if(tSig instanceof PrimSig){
					queue.addAll(tSig.allPrimSigParent());
					for(PrimSig cSig :((PrimSig) tSig).children()){
						queue.add(cSig);
					}
				}
				visited.put(tSig.label, tSig);
			}
		}

		return ConstList.make(visited.values());

	}

}