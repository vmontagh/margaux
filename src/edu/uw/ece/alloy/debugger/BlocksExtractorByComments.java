/**
 * 
 */
package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.uw.ece.alloy.util.Utils;

/**
 * Given and alloy program, one can annotate the expressions 
 * need to be analyzed. For example:
 * 
 * sig A{r:A}
 * fact{
 * 	/*@begin-analyze*\/
 * 	some r
 * 	/*@end-analyze*\/
 * }
 * 
 * The output is like 'some r'
 * 
 * @author vajih
 *
 */
public abstract class BlocksExtractorByComments {

	final public String path;
	final public String START;
	final public String END;
	
	
	protected BlocksExtractorByComments(String path, String start, String end){
		this.path = path;
		this.START = start;
		this.END = end;
	}

	public List<Pos> findAllPairs(){
		List<Pos> result = new LinkedList<>();
		
		List<Pos> allPoses = new ArrayList<>();
		int lineNumber = 0;
		for (String line: Utils.readFileLines(path)){
			++lineNumber;
			int foundStart = line.indexOf(START);
			while (foundStart >= 0){
				allPoses.add(new Pos(START, foundStart+START.length()+1, lineNumber));
				foundStart = line.indexOf(START, foundStart+1);
			}
			int foundEnd = line.indexOf(END);
			while (foundEnd >= 0){
				allPoses.add(new Pos(END, foundEnd, lineNumber));
				foundEnd = line.indexOf(END,foundEnd+1 );
			}
		}
		
		Collections.sort(allPoses, new Comparator<Pos>() {
			@Override
			public int compare(Pos o1, Pos o2) {
				if (o1.y < o2.y)
					return -1;
				if (o1.y > o2.y)
					return 1;
				if (o1.y == o1.y && o1.x > o2.x)
					return 1;
				if (o1.y == o1.y && o1.x < o2.x)
					return -1;
				return 0;
			}			
		});
		
		Stack<Pos> stack = new Stack<>();
		for(Pos pos: allPoses){
			//if (stack.isEmpty())
			//	throw new RuntimeException("Invalid pairs of start and end");
			
			if (pos.filename.equals(END)){
				Pos start = stack.pop();
				result.add(new Pos(new File(path).getAbsolutePath(), start.x, start.y, pos.x, pos.y));
			} else {
				stack.push(pos);
			}
		}
	
		return Collections.unmodifiableList(result);
	}
	
	public List<String> getAllBlocks(){
		return findAllPairs().stream()
												 .map(p->Utils.readSnippet(p))
												 .collect(Collectors.toList());
	}
	
	
	
	public static class ExtractExpression extends BlocksExtractorByComments{

		final public static String BEGIN = "/*@begin-analyze*/";
		final public static String END = "/*@end-analyze*/";
		
		public ExtractExpression(String path) {
			super(path, BEGIN, END);
		}
		
		/**
		 * Given an immutable path, getAllExpressionsAndFields extracts all
		 * snippets and returns all the fields are directly or indirectly 
		 * references by the snippets.
		 * @return
		 * @throws Err 
		 */
		public Map<String, List<Sig.Field>> getAllExpressionsAndFields() throws Err{
			final Map<String, List<Sig.Field>> result = new HashMap<>();
			final CompModule compModule = 
					CompUtil.parseEverything_fromFile(A4Reporter.NOP, null, path);

			for(Pos snippetPos: findAllPairs()){
				Set<Sig.Field> fields = new HashSet<>();
				String snippet = Utils.readSnippet(snippetPos).trim();
				
				for (Command cmd: compModule.getAllCommands()){
					fields.addAll(ExpressionsWithinPosVisitor
							.findAllFieldsWithinPos(snippetPos, cmd.formula));
				}
				
				for (Pair<String,Expr> pair: compModule.getAllAssertions()){
					fields.addAll(ExpressionsWithinPosVisitor
							.findAllFieldsWithinPos(snippetPos, pair.b));
				}
				
				for (Pair<String,Expr> pair: compModule.getAllFacts()){
					fields.addAll(ExpressionsWithinPosVisitor
							.findAllFieldsWithinPos(snippetPos, pair.b));
				}
				
				for (Func func: compModule.getAllFunc()){
					fields.addAll(ExpressionsWithinPosVisitor
							.findAllFieldsWithinPos(snippetPos, func.getBody()));
				}
				
				result.put(snippet, new ArrayList<Sig.Field>(fields));		
			}
			return Collections.unmodifiableMap(result);
		}
		
	}
	
	public static class ExtractScope extends BlocksExtractorByComments{

		final public static String BEGIN = "/*@begin-scope*/";
		final public static String END = "/*@end-scope*/";
		
		public ExtractScope(String path) {
			super(path, BEGIN, END);
		}
		
	}
	
	
}
