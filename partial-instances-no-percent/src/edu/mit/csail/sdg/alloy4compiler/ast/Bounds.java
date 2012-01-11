package edu.mit.csail.sdg.alloy4compiler.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pos;

public class Bounds extends Expr {

    /** The label for this sig; this name does not need to be unique. */
    public final String label;

    //public final ConstList<CommandScope> scope;
    public final ArrayList<CommandScope> scope;
	
    public Bounds(String label){
        super(Pos.UNKNOWN, null);
        this.label = label;
        //scope = ConstList.make();
        scope = new ArrayList<CommandScope>();
        
    }

    public Bounds(Pos pos, String label, ArrayList<CommandScope> list /*Iterable<CommandScope> list*/){
    	super( Pos.UNKNOWN,Type.FORMULA);
        this.label = label;
        //this.scope = ConstList.make(list);
        this.scope = new ArrayList<CommandScope>(list);
    }
    
	@Override
	public void toString(StringBuilder out, int indent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	<T> T accept(VisitReturn<T> visitor) throws Err {
		return visitor.visit(this);
	}

	@Override
	public Expr resolve(Type t, Collection<ErrorWarning> warnings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHTML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Browsable> getSubnodes() {
		// TODO Auto-generated method stub
		return null;
	}
    
}
