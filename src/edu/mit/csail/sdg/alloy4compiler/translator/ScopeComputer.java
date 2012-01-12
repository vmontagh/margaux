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

package edu.mit.csail.sdg.alloy4compiler.translator;

import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.NONE;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.SEQIDX;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.SIGINT;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.STRING;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.UNIV;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import com.sun.org.apache.bcel.internal.generic.SIPUSH;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorAPI;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4.UniqueNameGenerator;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.ast.Type.ProductType;

/** Immutable; this class computes the scopes for each sig and computes the bitwidth and maximum sequence length.
 *
 * <p> The scopes are determined as follows:
 *
 * <p>  "run x": every topsig is scoped to <= 3 elements.
 *
 * <p>  "run x for N": every topsig is scoped to <= N elements.
 *
 * <p>  "run x for N but N1 SIG1, N2 SIG2...":
 * <br> Every sig following "but" is constrained explicitly.
 * <br> Any topsig that is
 * <br> a) not listed, and
 * <br> b) its scope is not derived otherwise
 * <br> will be scoped to have <= N elements.
 *
 * <p>  "run x for N1 SIG1, N2 SIG2..."
 * <br> Every sig following "but" is constrained explicitly.
 * <br> Any topsig that is
 * <br> a) not listed, and
 * <br> b) its scope is not derived otherwise
 * <br> we will give an error message.
 *
 * <p> Please see ScopeComputer.java for the exact rules for deriving the missing scopes.
 */

final class ScopeComputer {

    // It calls A4Solution's constructor

    /** Stores the reporter that will receive diagnostic messages. */
    private final A4Reporter rep;

    /** Stores the command that we're computing the scope for. */
    private final Command cmd;

    /** The integer bitwidth of this solution's model; always between 1 and 30. */
    private int bitwidth = 4;

    /** The maximum sequence length; always between 0 and (2^(bitwidth-1))-1. */
    private int maxseq = 4;

    /** The number of STRING atoms to allocate; -1 if it was not specified. */
    private int maxstring = (-1);

    /** The scope for each sig. */
    private final IdentityHashMap<PrimSig,Integer> sig2scope = new IdentityHashMap<PrimSig,Integer>();

    //[VM]
    /** The partial scope for each sig. */
    private final IdentityHashMap<PrimSig,List<String>> sig2Pscope = new IdentityHashMap<PrimSig,List<String>>();

    //[VM]
    /** The partial scope for each field. */
    private final IdentityHashMap<String,List<Pair<String,String>>> field2Pscope = new IdentityHashMap<String,List<Pair<String,String>>>();

    
    /** The sig's scope is exact iff it is in exact.keySet() (the value is irrelevant). */
    private final IdentityHashMap<Sig,Sig> exact = new IdentityHashMap<Sig,Sig>();

    //[VM] To support Lower bound and include keyword
    /** The sig's scope has lower iff it is in exact.keySet() (the value is irrelevant). */
    private final IdentityHashMap<Sig,Sig> lower = new IdentityHashMap<Sig,Sig>();
 
    //[VM] To support Lower bound and include keyword
    /** The sig's scope has upper iff it is in exact.keySet() (the value is irrelevant). */
    private final IdentityHashMap<Sig,Sig> upper = new IdentityHashMap<Sig,Sig>();    
    
    /** The list of atoms. */
    private final List<String> atoms = new ArrayList<String>();

    /** This UniqueNameGenerator allows each sig's atoms to be distinct strings. */
    private final UniqueNameGenerator un = new UniqueNameGenerator();

    /** Returns the scope for a sig (or -1 if we don't know). */
    public int sig2scope(Sig sig) {
        if (sig==SIGINT) return 1<<bitwidth;
        if (sig==SEQIDX) return maxseq;
        if (sig==STRING) return maxstring;
        Integer y = sig2scope.get(sig);
        return (y==null) ? (-1) : y;
    }

    //[VM]
    /** Returns the scope for a sig (or -1 if we don't know). */
    public int sig2PScope(Sig sig) {
        if (sig==SIGINT) return 1<<bitwidth;
        if (sig==SEQIDX) return maxseq;
        if (sig==STRING) return maxstring;
        List y = sig2Pscope.get(sig);
        return (y==null) ? (-1) : y.size();
    }

    //[VM]
    /** Returns the scope for a sig (or -1 if we don't know). */
    public List<Pair<String, String>> field2Pscope(Sig sig) {
        return field2Pscope.get(sig.label);
    }    
    
    //[VM]
    /** Returns the scope for a sig (or -1 if we don't know). */
    public List<Pair<String, String>> field2Pscope(String label) {
    	//
    	for(String l: field2Pscope.keySet()){
    		if(l.equals(label))
    			return field2Pscope.get(l);
    	}
    	return null;
        //return field2Pscope.get(label);
    }  
    
    /** Sets the scope for a sig; returns true iff the sig's scope is changed by this call. */
    private void sig2scope(Sig sig, int newValue) throws Err {
    	//[VM]
        if (sig.builtin)                throw new ErrorSyntax(cmd.pos, "Cannot specify a scope for the builtin signature \""+sig+"\"");
        if (!(sig instanceof PrimSig))  throw new ErrorSyntax(cmd.pos, "Cannot specify a scope for a subset signature \""+sig+"\"");
        if (newValue<0)                 throw new ErrorSyntax(cmd.pos, "Cannot specify a negative scope for sig \""+sig+"\"");
        int old=sig2scope(sig);
        if (old==newValue) return;
        //[VM] Not a good condition
        if (old>=0 && !hasLower(sig))        throw new ErrorSyntax(cmd.pos, "Sig \""+sig+"\" already has a scope of "+old+", so we cannot set it to be "+newValue);
        sig2scope.put((PrimSig)sig, newValue);
        rep.scope("Sig "+sig+" scope <= "+newValue+"\n");
    }

    //[VM]
    /** Sets the scope for a sig; returns true iff the sig's scope is changed by this call. */
    private void sig2Pscope(Sig sig, List<String> newValue) throws Err {
        if (sig.builtin)                throw new ErrorSyntax(cmd.pos, "Cannot specify a scope for the builtin signature \""+sig+"\"");
        if (!(sig instanceof PrimSig))  throw new ErrorSyntax(cmd.pos, "Cannot specify a scope for a subset signature \""+sig+"\"");
        if (newValue == null)                 throw new ErrorSyntax(cmd.pos, "Cannot specify a Null Partial scope for sig \""+sig+"\"");
        int old=sig2scope(sig);
        if (old==newValue.size()) return;
        if (old>=0)        throw new ErrorSyntax(cmd.pos, "Sig \""+sig+"\" already has a scope of "+old+", so we cannot set it to be "+newValue);
        sig2Pscope.put((PrimSig)sig, newValue);
        rep.scope("Sig "+sig+" scope <= "+newValue+"\n");
    }
    
    //[VM]
    /** Sets the scope for a sig; returns true iff the sig's scope is changed by this call. */
    private void field2Pscope(Sig sig, List<Pair<String,String>> newValue) throws Err {
        if (newValue == null)                 throw new ErrorSyntax(cmd.pos, "Cannot specify a Null Partial scope for sig \""+sig+"\"");
        int old = field2Pscope(sig)== null ? -1 : field2Pscope(sig).size();
        if (old==newValue.size()) return;
        if (old>=0)        throw new ErrorSyntax(cmd.pos, "Sig \""+sig+"\" already has a scope of "+old+", so we cannot set it to be "+newValue);
        field2Pscope.put(((PrimSig)sig).label, newValue);
        rep.scope("Sig "+sig+" scope <= "+newValue+"\n");
    }

    /** Sets the scope for a sig; returns true iff the sig's scope is changed by this call. */
    private void field2Pscope(String label, List<Pair<String,String>> newValue) throws Err {
        if (newValue == null)                 throw new ErrorSyntax(cmd.pos, "Cannot specify a Null Partial scope for field's label \""+label+"\"");
        int old = field2Pscope(label)== null ? -1 : field2Pscope(label).size();
        if (old==newValue.size()) return;
        if (old>=0)        throw new ErrorSyntax(cmd.pos, "Fields's label \""+label+"\" already has a scope of "+old+", so we cannot set it to be "+newValue);
        field2Pscope.put(label, newValue);
        rep.scope("Filed's label "+label+" scope <= "+newValue+"\n");
    }
    
    //[VM] It is sloppy, but the filed is not field, so I need to think in a better way. 
    /** Returns whether the scope of a sig is exact or not. */
    public boolean isExact(String label) {
    	for(Sig field: exact.keySet()){
    		if(field.label.equals(label)){
    			return true;
    		}
    	}
    	return false;
    }
    
    /** Returns whether the scope of a sig is exact or not. */
    public boolean isExact(Sig sig) {
        return sig==SIGINT || sig==SEQIDX || sig==STRING || ((sig instanceof PrimSig) && exact.containsKey(sig));
    }

    //[VM] I don't know whether we need to check (sig instanceof PrimSig)
    /** Returns whether the scope of a sig is exact or not. */
    public boolean hasLower(String label) {
    	for(Sig field: lower.keySet()){
    		if(field.label.equals(label)){
    			return true;
    		}
    	}
    	return false;    
    }
    
    /** Returns whether the scope of a sig is exact or not. */
    public boolean hasLower(Sig sig) {
        return ((sig instanceof PrimSig) && lower.containsKey(sig));
    }
    
    /** Returns whether the scope of a sig is exact or not. */
    public boolean hasUpper(Sig sig) {
        return ((sig instanceof PrimSig) && upper.containsKey(sig));
    }
    
    //[VM] I don't know whether we need to check (sig instanceof PrimSig)
    /** Returns whether the scope of a sig is exact or not. */
    public boolean hasUpper(String label) {
    	for(Sig field: upper.keySet()){
    		if(field.label.equals(label)){
    			return true;
    		}
    	}
    	return false;    
    }
    
    
    /** Make the given sig "exact". */
    private void makeExact(Pos pos, Sig sig) throws Err {
        if (!(sig instanceof PrimSig)) throw new ErrorSyntax(pos, "Cannot specify a scope for a subset signature \""+sig+"\"");
        exact.put(sig, sig);
    }

    //[VM]
    /** Make the given sig "lower bound". */
    private void makeLower(Pos pos, Sig sig) throws Err {
    	//Do we need to have a prime number?!
        if (!(sig instanceof PrimSig)) throw new ErrorSyntax(pos, "Cannot specify a scope for a subset signature \""+sig+"\"");
        lower.put(sig, sig);
    }
    
    //[VM]
    /** Make the given sig "lower bound". */
    private void makeUpper(Pos pos, Sig sig) throws Err {
    	//Do we need to have a prime number?!
        if (!(sig instanceof PrimSig)) throw new ErrorSyntax(pos, "Cannot specify a scope for a subset signature \""+sig+"\"");
        upper.put(sig, sig);
    }
    
    /** Modifies the integer bitwidth of this solution's model (and sets the max sequence length to 0) */
    private void setBitwidth(Pos pos, int newBitwidth) throws ErrorAPI, ErrorSyntax {
        if (newBitwidth<0)  throw new ErrorSyntax(pos, "Cannot specify a bitwidth less than 0");
        if (newBitwidth>30) throw new ErrorSyntax(pos, "Cannot specify a bitwidth greater than 30");
        bitwidth = newBitwidth;
        maxseq = 0;
        sig2scope.put(SIGINT, bitwidth < 1 ? 0 : 1<<bitwidth);
        sig2scope.put(SEQIDX, 0);
    }

    /** Modifies the maximum sequence length. */
    private void setMaxSeq(Pos pos, int newMaxSeq) throws ErrorAPI, ErrorSyntax {
        if (newMaxSeq > max()) throw new ErrorSyntax(pos, "With integer bitwidth of "+bitwidth+", you cannot have sequence length longer than "+max());
        if (newMaxSeq < 0) newMaxSeq = 0; //throw new ErrorSyntax(pos, "The maximum sequence length cannot be negative.");
        maxseq = newMaxSeq;
        sig2scope.put(SEQIDX, maxseq);
    }

    /** Returns the largest allowed integer. */
    private int max() { return Util.max(bitwidth); }

    /** Returns the smallest allowed integer. */
    private int min() { return Util.min(bitwidth); }

    //===========================================================================================================================//

    /** If A is abstract, unscoped, and all children are scoped, then set A's scope to be the sum;
     * if A is abstract, scoped, and every child except one is scoped, then set that child's scope to be the difference.
     */
    private boolean derive_abstract_scope (Iterable<Sig> sigs) throws Err {
    	boolean changed=false;
       again:
       for(Sig s:sigs) if (!s.builtin && (s instanceof PrimSig) && s.isAbstract!=null) {
          SafeList<PrimSig> subs = ((PrimSig)s).children();
          if (subs.size()==0) continue;
          Sig missing=null;
          int sum=0;
          for(Sig c:subs) {
             int cn = sig2scope(c);
             if (cn<0) { if (missing==null) { missing=c; continue; } else { continue again; } }
             sum=sum+cn;
             if (sum<0) throw new ErrorSyntax(cmd.pos, "The number of atoms exceeds the internal limit of "+Integer.MAX_VALUE);
          }
          int sn = sig2scope(s);
          if (sn<0) {
             if (missing!=null) continue;
             sig2scope(s, sum);
             changed=true;
          } else if (missing!=null) {
             sig2scope(missing, (sn<sum) ? 0 : sn-sum);
             changed=true;
          }
       }
       return changed;
    }

    /** If A is abstract, unscoped, and all children are scoped, then set A's scope to be the sum;
     * if A is abstract, scoped, and every child except one is scoped, then set that child's scope to be the difference.
     */
   /* private boolean distinguish_atom_scope (Iterable<Sig> sigs) throws Err {
       //[VM]
    	for(Sig s: sigs){
    		//sig2scope.put(key, value)
    	}
    }
*/
    
    //===========================================================================================================================//

    /** If A is toplevel, and we haven't been able to derive its scope yet, then let it get the "overall" scope. */
    private boolean derive_overall_scope (Iterable<Sig> sigs) throws Err {
    	boolean changed=false;
        final int overall = (cmd.overall<0 && cmd.scope.size()==0) ? 3 : cmd.overall;
        for(Sig s:sigs) {
        	
        	//[VM] Insert the rest.
        	if(!s.builtin && s.isTopLevel() && sig2PScope(s) > 0 && hasLower(s)){
        		int rest = overall - sig2PScope(s);
        		if (sig2scope(s) == overall)
        			continue;
        		if(rest > 0){
            		sig2scope(s, overall);
            		changed=true;
            		continue;
        		}
        	}else
        	
        	if (!s.builtin && s.isTopLevel() && sig2scope(s)<0 && sig2PScope(s) < 0) {
        		if (s.isEnum!=null) { 
        			sig2scope(s, 0); 
        			continue; 
        		} // enum without children should get the empty set
        		if (overall<0) 
        			throw new ErrorSyntax(cmd.pos, "You must specify a scope for sig \""+s+"\"");
        		sig2scope(s, overall);
        		changed=true;
        	}
        }
        return changed;
    }

    //===========================================================================================================================//

    /** If A is not toplevel, and we haven't been able to derive its scope yet, then give it its parent's scope. */
    private boolean derive_scope_from_parent (Iterable<Sig> sigs) throws Err {

    	boolean changed=false;
        Sig trouble=null;
        for(Sig s:sigs) if (!s.builtin && !s.isTopLevel() && sig2scope(s)<0 && sig2PScope(s) < 0 && (s instanceof PrimSig)) {
           PrimSig p = ((PrimSig)s).parent;
           int pb = sig2scope(p);
           if (pb>=0) {sig2scope(s,pb); changed=true;} else {trouble=s;}
        }
        if (changed) return true;
        if (trouble==null) return false;
        throw new ErrorSyntax(cmd.pos,"You must specify a scope for sig \""+trouble+"\"");
    }

    //===========================================================================================================================//

    /** Computes the number of atoms needed for each sig (and add these atoms to this.atoms) */
    private int computeLowerBound(final PrimSig sig) throws Err {
        if (sig.builtin) return 0;
        int n=sig2scope(sig), lower=0;
        boolean isExact = isExact(sig);
        // First, figure out what atoms *MUST* be in this sig
        for(PrimSig c:sig.children()) lower = lower + computeLowerBound(c);
        // Bump up the scope if the sum of children exceed the scope for this sig
        if (n<lower) {
           if (isExact)
              rep.scope("Sig "+sig+" scope raised from =="+n+" to be =="+lower+"\n");
           else
              rep.scope("Sig "+sig+" scope raised from <="+n+" to be <="+lower+"\n");
           n=lower;
           sig2scope.put(sig, n);
        }
        // Add special overrides for "exactly" sigs
        if (!isExact && cmd.additionalExactScopes.contains(sig)) {
            isExact=true; rep.scope("Sig "+sig+" forced to have exactly "+n+" atoms.\n"); makeExact(Pos.UNKNOWN, sig);
        }
        //[VM] 
        //int j = 0;
        StringBuilder sb2=new StringBuilder();
        if(sig2Pscope.containsKey(sig)){
        	for(String str: sig2Pscope.get(sig)){
        		if (str.startsWith("this/")) 
        			str=str.substring(5);
                	str=un.make(str);
                	atoms.add(sb2.delete(0, sb2.length()).append(str).append('%').toString());
                	lower++;
        	}
        	int rest = sig2scope.get(sig) != null ? sig2scope.get(sig) : lower;
        	
        	if( rest > lower){
                String name=sig.label;
        		StringBuilder sb=new StringBuilder();
                for(int i=0; i<(rest-lower)+1; i++) {
                   String x = sb.delete(0, sb.length()).append(name).append('$').append(i).toString();
                   atoms.add(x);
                   lower++;
                }        		
        	}
        }
        // Create atoms
        if (n>lower && (isExact || sig.isTopLevel())) {
            // Figure out how many new atoms to make
            n = n-lower;
            // Pick a name for them
            String name=sig.label;
            if (name.startsWith("this/")) name=name.substring(5);
            name=un.make(name);
            // Now, generate each atom using the format "SIGNAME$INDEX"
            // By prepending the index with 0 so that they're the same width, we ensure they sort lexicographically.
            StringBuilder sb=new StringBuilder();
            for(int i=0; i<n; i++) {
               String x = sb.delete(0, sb.length()).append(name).append('$').append(i).toString();
               atoms.add(x);
               lower++;
            }
        }
        return lower;
    }

    //===========================================================================================================================//

    /** Compute the scopes, based on the settings in the "cmd", then log messages to the reporter. */
    private ScopeComputer(A4Reporter rep, Iterable<Sig> sigs, Command cmd) throws Err {
    	this.rep = rep;
        this.cmd = cmd;
        boolean shouldUseInts = areIntsUsed(sigs);
        // Process each sig listed in the command
        for(CommandScope entry:cmd.scope) {
            Sig s = entry.sig;
            int scope = entry.startingScope;
            boolean exact = entry.isExact;
            boolean lower = entry.hasLower;
            boolean upper = entry.hasUpper;

            if (s==UNIV) throw new ErrorSyntax(cmd.pos, "You cannot set a scope on \"univ\".");
            if (s==SIGINT) throw new ErrorSyntax(cmd.pos,
                    "You can no longer set a scope on \"Int\". "
                    +"The number of atoms in Int is always exactly equal to 2^(i" +
                    		"nteger bitwidth).\n");
            if (s==SEQIDX) throw new ErrorSyntax(cmd.pos,
                    "You cannot set a scope on \"seq/Int\". "
                    +"To set the maximum allowed sequence length, use the seq keyword.\n");
            if (s==STRING) {
               if (maxstring>=0) throw new ErrorSyntax(cmd.pos, "Sig \"String\" already has a scope of "+maxstring+", so we cannot set it to be "+scope);
               if (!exact) throw new ErrorSyntax(cmd.pos, "Sig \"String\" must have an exact scope.");
               maxstring = scope;
               continue;
            }
            if (s==NONE) throw new ErrorSyntax(cmd.pos, "You cannot set a scope on \"none\".");
            if (s.isEnum!=null) throw new ErrorSyntax(cmd.pos, "You cannot set a scope on the enum \""+s.label+"\"");
            if (s.isOne!=null && scope!=1) throw new ErrorSyntax(cmd.pos,
                "Sig \""+s+"\" has the multiplicity of \"one\", so its scope must be 1, and cannot be "+scope);
            if (s.isLone!=null && scope>1) throw new ErrorSyntax(cmd.pos,
                "Sig \""+s+"\" has the multiplicity of \"lone\", so its scope must 0 or 1, and cannot be "+scope);
            if (s.isSome!=null && scope<1) throw new ErrorSyntax(cmd.pos,
                "Sig \""+s+"\" has the multiplicity of \"some\", so its scope must 1 or above, and cannot be "+scope);
            
            if(entry.isPartial){
            	if(entry.pFields.size() > 0){
            		List<Pair<String,String>> list = new ArrayList<Pair<String,String>>();
            		for(Pair<ExprVar,ExprVar> pair: entry.pFields ){
            			list.add(new Pair(pair.a.label+"%", pair.b.label+"%"));
            		}
            		field2Pscope(s,list);
            	}else{
            	List<String> list = new ArrayList<String>();
            	for(ExprVar var: entry.pAtoms)
            			list.add(var.label);
            	sig2Pscope(s,list);
            	sig2scope(s, list.size());
            	}
            
            }else{
            	sig2scope(s, scope);
            }
            if (exact) makeExact(cmd.pos, s);
            //[VM]
            if (lower) makeLower(cmd.pos, s);
            
            if(upper) makeUpper(cmd.pos, s);
        }
        //[VM] if in "value = a + b + c", the value should not be "one" or ...
        // Force "one" sigs to be exactly one, and "lone" to be at most one
        for(Sig s:sigs) if (s instanceof PrimSig) {
            if (s.isOne!=null) { makeExact(cmd.pos, s); sig2scope(s,1); } else if (s.isLone!=null && sig2scope(s)!=0) sig2scope(s,1);
        }
        // Derive the implicit scopes
        while(true) {
            if (derive_abstract_scope(sigs))    { do {} while(derive_abstract_scope(sigs));     continue; }
            if (derive_overall_scope(sigs))     { do {} while(derive_overall_scope(sigs));      continue; }
            if (derive_scope_from_parent(sigs)) { do {} while(derive_scope_from_parent(sigs));  continue; }
            break;
        }
        // Set the initial scope on "int" and "Int" and "seq"
        int maxseq=cmd.maxseq, bitwidth=cmd.bitwidth;
        if (bitwidth<0) { bitwidth = (shouldUseInts ? 4 : 0); } 
        setBitwidth(cmd.pos, bitwidth);
        if (maxseq<0) {
            if (cmd.overall>=0) maxseq=cmd.overall; else maxseq=4;
            int max = Util.max(bitwidth);
            if (maxseq > max) maxseq = max;
        }
        setMaxSeq(cmd.pos, maxseq);
        // Generate the atoms and the universe
        for(Sig s:sigs) if (s.isTopLevel()) computeLowerBound((PrimSig)s);
        int max = max(), min = min();
        if (max >= min) for(int i=min; i<=max; i++) atoms.add(""+i);
    }

    /** Whether or not Int appears in the relation types found in these sigs */
    private boolean areIntsUsed(Iterable<Sig> sigs) {
        for (Sig s : sigs) {
            for (Field f : s.getFields()) {
                for (ProductType pt : f.type()) {
                    for (int k = 0; k < pt.arity(); k++) {
                        if (pt.get(k) == SIGINT || pt.get(k) == SEQIDX)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    //===========================================================================================================================//

    /** Computes the scopes for each sig and computes the bitwidth and maximum sequence length.
     *
     * <p> The scopes are determined as follows:
     *
     * <p>  "run x": every topsig is scoped to <= 3 elements.
     *
     * <p>  "run x for N": every topsig is scoped to <= N elements.
     *
     * <p>  "run x for N but N1 SIG1, N2 SIG2...":
     * <br> Every sig following "but" is constrained explicitly.
     * <br> Any topsig that is
     * <br> a) not listed, and
     * <br> b) its scope is not derived otherwise
     * <br> will be scoped to have <= N elements.
     *
     * <p>  "run x for N1 SIG1, N2 SIG2..."
     * <br> Every sig following "but" is constrained explicitly.
     * <br> Any topsig that is
     * <br> a) not listed, and
     * <br> b) its scope is not derived otherwise
     * <br> we will give an error message.
     *
     * <p> Please see ScopeComputer.java for the exact rules for deriving the missing scopes.
     */
    static Pair<A4Solution,ScopeComputer> compute (A4Reporter rep, A4Options opt, Iterable<Sig> sigs, Command cmd) throws Err {
        ScopeComputer sc = new ScopeComputer(rep, sigs, cmd);
        Set<String> set = cmd.getAllStringConstants(sigs);
        if (sc.maxstring>=0 && set.size()>sc.maxstring) rep.scope("Sig String expanded to contain all "+set.size()+" String constant(s) referenced by this command.\n");
        for(int i=0; set.size()<sc.maxstring; i++) set.add("\"String" + i + "\"");
        sc.atoms.addAll(set);
        A4Solution sol = new A4Solution(cmd.toString(), sc.bitwidth, sc.maxseq, set, sc.atoms, rep, opt, cmd.expects);
        return new Pair<A4Solution,ScopeComputer>(sol, sc);
    }
}
