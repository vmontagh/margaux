/* Alloy Analyzer 4 -- Copyright (c) 2006-2009, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4compiler.ast;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Util;

/** Immutable; reresents a scope in a "run" or "check" command.
 *
 * <p> <b>Invariant:</b>  sig != null
 * <p> <b>Invariant:</b>  endingScope >= startingScope >= 0
 * <p> <b>Invariant:</b>  increment > 0
 */

public class CommandScope {

	/** The position in the original source file where this scope was declared; can be Pos.UNKNOWN if unknown. */
	public final Pos pos;

	/** The sig whose scope is being given by this CommandScope object. */
	public final Sig sig;

	/** True iff the scope is an exact scope. */
	public final boolean isExact;

	/** The starting scope. */
	public final int startingScope;

	/** The ending scope; if this sig is not a growing sig, then this.startingScope==this.endingScope. */
	public final int endingScope;

	/** The scope increment; if this sig is not a growing sig, then this.increment is ignored. */
	public final int increment;
	//[VM]
	public final List<ExprVar> pAtoms;

	public final List<List<Expr>> pFields;

	public final boolean isPartial;

	public final boolean hasLower;

	public final boolean hasUpper;


	/** Construct a new CommandScope object.
	 * @param sig - the sig for this scope
	 * @param isExact - true iff the scope is intended to be exact
	 * @param scope - the scope
	 * @throws ErrorSyntax if scope is less than zero
	 */
	public CommandScope(Sig sig, boolean isExact, int scope) throws ErrorSyntax { this(null, sig, isExact, scope, scope, 1); }

	/** Construct a new CommandScope object.
	 * @param pos - the position where this scope is given
	 * @param sig - the sig for this scope
	 * @param isExact - true iff the scope is intended to be exact
	 * @param startingScope - the starting scope
	 * @param endingScope - the ending scope (if this sig is not intended to be growable, then startingScope should equal endingScope)
	 * @param increment - the scope increment (if this sig is not intended to be growable, then this field is ignored)
	 * @throws ErrorSyntax if startingScope is less than zero
	 * @throws ErrorSyntax if endingScope is less than startingScope
	 * @throws ErrorSyntax if increment is less than one
	 */
	public CommandScope(Pos pos, Sig sig, boolean isExact, int startingScope, int endingScope, int increment) throws ErrorSyntax {
		if (pos == null) pos = Pos.UNKNOWN;
		if (sig == null) throw new NullPointerException();
		if (startingScope < 0) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have a negative starting scope ("+startingScope+")");
		if (endingScope < 0) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have a negative ending scope ("+endingScope+")");
		if (endingScope < startingScope) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have an ending scope ("+endingScope+") smaller than its starting scope ("+startingScope+")");
		if (startingScope == endingScope) increment = 1;
		if (increment < 1) throw new ErrorSyntax(pos, "Sig "+sig+"'s increment value cannot be "+increment+".\nThe increment must be 1 or greater.");
		this.pos = pos;
		this.sig = sig;
		this.isExact = isExact;
		this.startingScope = startingScope;
		this.endingScope = endingScope;
		this.increment = increment;
		this.isPartial = false;
		this.pAtoms = new ArrayList<ExprVar>();
		this.pFields = new ArrayList<List<Expr>>();
		this.hasLower = false;
		this.hasUpper = false;
	}

	//[VM]
	public CommandScope(Pos pos, Sig sig, boolean isExact, 
			int startingScope, int endingScope, int increment, 
			List/*<ExprVar>*/ atoms, boolean lower,boolean hasUpper) throws ErrorSyntax {
		if (pos == null) pos = Pos.UNKNOWN;
		if (sig == null) throw new NullPointerException();
		if (startingScope < 0) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have a negative starting scope ("+startingScope+")");
		if (endingScope < 0) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have a negative ending scope ("+endingScope+")");
		if (endingScope < startingScope) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have an ending scope ("+endingScope+") smaller than its starting scope ("+startingScope+")");
		if (startingScope == endingScope) increment = 1;
		if (increment < 1) throw new ErrorSyntax(pos, "Sig "+sig+"'s increment value cannot be "+increment+".\nThe increment must be 1 or greater.");
		this.pos = pos;
		this.sig = sig;
		this.isExact = isExact;
		this.hasUpper = hasUpper;
		this.increment = increment;
		this.pFields = new ArrayList<List<Expr>>();
		boolean isRelation = false;

		List<List<Expr>> tmpPField = new ArrayList<List<Expr>>();
		if(atoms != null  && atoms.size() > 0){
			this.isPartial = true;
			int m = 0;
			for(Object atom: atoms){
				if(atom instanceof Pair){
					List<Expr> list = extractRels((Pair)atom);
					m = Math.max(m, list.size());
					//Pas One to detect the most arity number					
					tmpPField.add(list);    				
					isRelation = true;
				}
			}
			if(!isRelation){
				this.pAtoms = new ArrayList<ExprVar>(atoms);
			}else{
				for(int i=0; i<tmpPField.size(); i++ ){
					if(tmpPField.get(i).size() == m){
						this.pFields.add(tmpPField.get(i));
					}
				}
				this.pAtoms = new ArrayList<ExprVar>();
			}

			this.hasLower = lower;
		}else{
			this.hasLower = false;
			this.isPartial = false;
			this.pAtoms = new ArrayList<ExprVar>();    		
		}

		if(isRelation){
			this.startingScope = startingScope/2;
			this.endingScope = endingScope/2;
		}else{
			this.startingScope = startingScope;
			this.endingScope = endingScope;
		}
	}

	private List<Expr> extractRels(Pair pair){
		List<Expr> tmp = new ArrayList<Expr>();
		
		if(pair.a instanceof List && ((List)pair.a).size()>0){
			Object p = ((List)pair.a).get(0);
			if(p instanceof Pair)
				tmp.addAll(extractRels((Pair)p));
			else
				tmp.add((Expr)p);
		}
		tmp.add((Expr)pair.b);
		return tmp;
	}
	

	//[VM] In The CompModule.resolveCommand, the commandScopes are made again. So, We need to have another constructor
	//to create a relation with empty atoms.


	public CommandScope(Pos pos, Sig sig, boolean isExact,
			int startingScope, int endingScope, int increment,
			List<List<Expr>> fields, boolean lower,boolean hasUpper, boolean isField/*This is a dummy field*/) throws ErrorSyntax {
		if (sig == null) throw new NullPointerException();
		if (startingScope < 0) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have a negative starting scope ("+startingScope+")");
		if (endingScope < 0) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have a negative ending scope ("+endingScope+")");
		if (endingScope < startingScope) throw new ErrorSyntax(pos, "Sig "+sig+" cannot have an ending scope ("+endingScope+") smaller than its starting scope ("+startingScope+")");
		if (startingScope == endingScope) increment = 1;
		if (increment < 1) throw new ErrorSyntax(pos, "Sig "+sig+"'s increment value cannot be "+increment+".\nThe increment must be 1 or greater.");
		this.pos = pos;
		this.sig = sig;
		this.isExact = isExact;
		this.startingScope = startingScope;
		this.endingScope = endingScope;
		this.increment = increment;
		this.hasUpper = hasUpper;
		this.pFields = new ArrayList<List<Expr>>();

		this.isPartial = true;
		for(List<Expr> list: fields){
			List<Expr> tmp = new ArrayList<Expr>();
			for(Expr v:list){
				tmp.add(v);
			}
			this.pFields.add(tmp);
		}
		this.pAtoms = new ArrayList<ExprVar>();
		this.hasLower = lower;

		

	}
	/** {@inheritDoc} */
	@Override public String toString() {
		return (isExact ? "exactly " : "") + (isPartial ? " partial " : "")
				+ startingScope
				+ (endingScope!=startingScope ? (".."+endingScope) : "")
				+ (increment > 1 ? (":"+increment) : "")
				+ " "
				+ Util.tail(sig.label);
	}
}
