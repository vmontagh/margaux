/*
 * Alloy Analyzer 4 -- Copyright (c) 2006-2008, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4compiler.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.mit.csail.sdg.alloy4.Env;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBuiltin;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprITE;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprLet;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprQuant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprUnary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.VisitReturn;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;

/** Mutable; represents an instance. */

public final class SimContext extends VisitReturn<Object> {

    /** This maps the current local variables (LET, QUANT, Function Param) to the actual SimTupleset/Integer/Boolean */
    private Env<ExprVar,Object> env = new Env<ExprVar,Object>();

    /** The exact values of each sig. */
    private final Map<Sig,SimTupleset> sigs = new HashMap<Sig,SimTupleset>();

    /** The exact values of each field. */
    private final Map<Field,SimTupleset> fields = new HashMap<Field,SimTupleset>();

    /** Caches parameter-less functions to a Boolean, Integer, or SimTupleset. */
    private final Map<Func,Object> cacheForConstants = new IdentityHashMap<Func,Object>();

    /** This is used to detect "function recursion" (which we currently do not allow). */
    private final List<Func> current_function = new ArrayList<Func>();

    /** Caches the result of evaluating "iden"; must be cleared whenever sig contents change. */
    private SimTupleset cacheIDEN = null;

    /** Caches the result of evaluting "fun/next"; must be cleared whenever bitwidth changes. */
    private SimTupleset cacheNEXT = null;

    /** The chosen bitwidth */
    private final int bitwidth;

    /** The shiftmask based on the chosen bitwidth. */
    private final int shiftmask;

    /** The minimum allowed integer based on the chosen bitwidth. */
    private final int min;

    /** The maximum allowed integer based on the chosen bitwidth. */
    private final int max;

    /** Construct a new simulation context with the given bitwidth. */
    public SimContext(int bitwidth) throws Err {
        if (bitwidth<1 || bitwidth>32) throw new ErrorType("Bitwidth must be between 1 and 32.");
        this.bitwidth = bitwidth;
        if (bitwidth==32) { max=Integer.MAX_VALUE; min=Integer.MIN_VALUE; } else { max=(1<<(bitwidth-1))-1; min=(0-max)-1; }
        shiftmask = (1 << (32 - Integer.numberOfLeadingZeros(bitwidth))) - 1;
    }

    /** Truncate the given integer based on the current chosen bitwidth */
    private int trunc(int i) { return (i<<(32-bitwidth))>>(32-bitwidth); }

    /** Remove the "ExprUnary NOP" in front of an expression. */
    private static Expr deNOP(Expr x) {
        while(x instanceof ExprUnary && ((ExprUnary)x).op==ExprUnary.Op.NOOP) x=((ExprUnary)x).sub;
        return x;
    }

    /**
     * Convenience method that evalutes x and casts the result to be a Kodkod Formula.
     * @return the formula - if x evaluates to a Formula
     * @throws ErrorFatal - if x does not evaluate to a Formula
     */
    private boolean cform(Expr x) throws Err {
        if (!x.errors.isEmpty()) throw x.errors.pick();
        Object y=visitThis(x);
        if (y instanceof Boolean) return Boolean.TRUE.equals(y);
        throw new ErrorFatal(x.span(), "This should have been a formula.\nInstead it is "+y);
    }

    /**
     * Convenience method that evalutes x and cast the result to be a Kodkod IntExpression.
     * @return the integer expression - if x evaluates to an IntExpression
     * @throws ErrorFatal - if x does not evaluate to an IntExpression
     */
    private int cint(Expr x) throws Err {
        if (!x.errors.isEmpty()) throw x.errors.pick();
        Object y=visitThis(x);
        if (y instanceof Integer) return trunc((Integer)y);
        throw new ErrorFatal(x.span(), "This should have been an integer expression.\nInstead it is "+y);
    }

    /**
     * Convenience method that evalutes x and cast the result to be a Kodkod Expression.
     * @return the expression - if x evaluates to an Expression
     * @throws ErrorFatal - if x does not evaluate to an Expression
     */
    private SimTupleset cset(Expr x) throws Err {
        if (!x.errors.isEmpty()) throw x.errors.pick();
        Object y=visitThis(x);
        if (y instanceof SimTupleset) return (SimTupleset)y;
        throw new ErrorFatal(x.span(), "This should have been a set or a relation.\nInstead it is "+y);
    }

    /** Helper method that adds if new. */
    private void add(List<Object[]> list, Object[] tuple, int left, int right) {
        Object newtuple[] = new Object[left+right];
        if (left>0) { for(int i=0; i<left; i++) newtuple[i]=tuple[i]; } else { for(int i=0; i<right; i++) newtuple[i]=tuple[i+tuple.length-right]; }
        list.add(newtuple);
    }

    /** Helper method that evaluates the formula "a in b" */
    private boolean isIn(SimTupleset a, Expr right) throws Err {
        if (right instanceof ExprUnary) {
            // Handles possible "unary" multiplicity
            ExprUnary y=(ExprUnary)(right);
            if (y.op==ExprUnary.Op.ONEOF)  { return a.size()==1 && a.in(cset(y.sub)); }
            if (y.op==ExprUnary.Op.SETOF)  { return                a.in(cset(y.sub)); }
            if (y.op==ExprUnary.Op.LONEOF) { return a.size()<=1 && a.in(cset(y.sub)); }
            if (y.op==ExprUnary.Op.SOMEOF) { return a.size()>=1 && a.in(cset(y.sub)); }
        }
        if (right instanceof ExprBinary && right.mult!=0 && ((ExprBinary)right).op.isArrow) {
            // Handles possible "binary" or higher-arity multiplicity
            return isInBinary(a, (ExprBinary)right);
        }
        return a.in(cset(right));
    }

    /** Helper method that evaluates the formula "r in (a ?->? b)" */
    private boolean isInBinary(SimTupleset R, ExprBinary ab) throws Err {
       // Special check for ISSEQ_ARROW_LONE
       if (ab.op == ExprBinary.Op.ISSEQ_ARROW_LONE) {
          List<Object[]> list = new ArrayList<Object[]>(R.tuples);
          Integer next = 0;
          while(list.size() > 0) {
             boolean found = false;
             for(int n=list.size(), i=0; i<n; i++) if (next.equals(list.get(i)[0])) { list.set(i, list.get(n-1)); list.remove(n-1); n--; found=true; }
             if (!found) return false;
             next++;
             if (next<0 && list.size()>0) return false; // shouldn't happen, but if it wraps around and yet list.size()>0 then we indeed have illegal tuples, so we return false
          }
       }
       // "R in A ->op B" means for each tuple a in A, there are "op" tuples in r that begins with a.
       for(Object[] a: cset(ab.left).tuples) {
         SimTupleset ans = new SimTupleset();
         for(Object[] r: R.tuples) for(int i=0; ; i++) if (i==a.length) {add(ans.tuples, r, 0, r.length-i); break;} else if (a[i]!=r[i]) break;
         switch(ab.op) {
            case ISSEQ_ARROW_LONE:
            case ANY_ARROW_LONE: case SOME_ARROW_LONE: case ONE_ARROW_LONE: case LONE_ARROW_LONE: if (!(ans.size()<=1)) return false; else break;
            case ANY_ARROW_ONE:  case SOME_ARROW_ONE:  case ONE_ARROW_ONE:  case LONE_ARROW_ONE:  if (!(ans.size()==1)) return false; else break;
            case ANY_ARROW_SOME: case SOME_ARROW_SOME: case ONE_ARROW_SOME: case LONE_ARROW_SOME: if (!(ans.size()>=1)) return false; else break;
         }
         if (!isIn(ans, ab.right)) return false;
       }
       // "R in A op-> B" means for each tuple b in B, there are "op" tuples in r that end with b.
       for(Object[] b: cset(ab.right).tuples) {
         SimTupleset ans = new SimTupleset();
         for(Object[] r: R.tuples) for(int i=0, j=r.length-b.length; ; i++, j++) if (i==b.length) {add(ans.tuples, r, r.length-i, 0); break;} else if (b[i]!=r[j]) break;
         switch(ab.op) {
            case LONE_ARROW_ANY: case LONE_ARROW_SOME: case LONE_ARROW_ONE: case LONE_ARROW_LONE: if (!(ans.size()<=1)) return false; else break;
            case ONE_ARROW_ANY:  case ONE_ARROW_SOME:  case ONE_ARROW_ONE:  case ONE_ARROW_LONE:  if (!(ans.size()==1)) return false; else break;
            case SOME_ARROW_ANY: case SOME_ARROW_SOME: case SOME_ARROW_ONE: case SOME_ARROW_LONE: if (!(ans.size()>=1)) return false; else break;
         }
         if (!isIn(ans, ab.left)) return false;
       }
       return true;
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprBinary x) throws Err {
        Expr a=x.left, b=x.right;
        switch(x.op) {
          case ARROW: case ANY_ARROW_LONE: case ANY_ARROW_ONE: case ANY_ARROW_SOME:
          case LONE_ARROW_ANY: case LONE_ARROW_LONE: case LONE_ARROW_ONE: case LONE_ARROW_SOME:
          case ONE_ARROW_ANY: case ONE_ARROW_LONE: case ONE_ARROW_ONE: case ONE_ARROW_SOME:
          case SOME_ARROW_ANY: case SOME_ARROW_LONE: case SOME_ARROW_ONE: case SOME_ARROW_SOME:
          case ISSEQ_ARROW_LONE:
              return cset(x.left).product(cset(x.right));
          case IN:
              return isIn(cset(x.left), x.right);
          case JOIN:
              return cset(x.left).join(cset(x.right));
          case AND:
              return cform(x.left) && cform(x.right); // Java always has the short-circuit behavior
          case OR:
              return cform(x.left) || cform(x.right); // Java always has the short-circuit behavior
          case IFF:
              return cform(x.left) == cform(x.right);
          case SHA:
              return trunc(cint(x.left) >> (shiftmask & cint(x.right)));
          case SHR:
              return trunc(cint(x.left) >>> (shiftmask & cint(x.right)));
          case SHL:
              return trunc(cint(x.left) << (shiftmask & cint(x.right)));
          case INTERSECT:
              return cset(x.left).intersect(cset(x.right));
          case GT:
              return cint(x.left) > cint(x.right);
          case GTE:
              return cint(x.left) >= cint(x.right);
          case LT:
              return cint(x.left) < cint(x.right);
          case LTE:
              return cint(x.left) <= cint(x.right);
          case DOMAIN:
              a=deNOP(x.left); b=deNOP(x.right);
              if (a instanceof Sig && b instanceof Field && ((Field)b).sig==a) return cset(b); // simple optimization
              return cset(a).domain(cset(b));
          case RANGE:
              return cset(x.left).range(cset(x.right));
          case EQUALS:
              if (x.left.type.is_int) return cint(x.left)==cint(x.right); else return cset(x.left).eq(cset(x.right));
          case MINUS:
              // Special exception to allow "0-8" to not throw an exception, where 7 is the maximum allowed integer (when bitwidth==4)
              // (likewise, when bitwidth==5, then +15 is the maximum allowed integer, and we want to allow 0-16 without throwing an exception)
              if (a instanceof ExprConstant && ((ExprConstant)a).op==ExprConstant.Op.NUMBER && ((ExprConstant)a).num()==0)
                 if (b instanceof ExprConstant && ((ExprConstant)b).op==ExprConstant.Op.NUMBER && ((ExprConstant)b).num()==max+1)
                    return min;
              if (x.left.type.is_int) return trunc(cint(x.left)-cint(x.right)); else return cset(x.left).difference(cset(x.right));
          case PLUS:
              if (x.left.type.is_int) return trunc(cint(x.left)+cint(x.right)); else return cset(x.left).union(cset(x.right));
          case PLUSPLUS:
              return cset(x.left).override(cset(x.right));
          case MUL:
              return trunc(cint(x.left) * cint(x.right));
          case DIV:
              { int p=cint(x.left), q=cint(x.right), r=(p==0 ? 0 : (q==0 ? (p<0 ? 1 : -1) : (p/q))); return trunc(r); }
          case REM:
              { int p=cint(x.left), q=cint(x.right), r=(p==0 ? 0 : (q==0 ? (p<0 ? 1 : -1) : (p/q))); return trunc(p-r*q); }
        }
        throw new ErrorFatal(x.pos, "Unsupported operator ("+x.op+") encountered during ExprBinary.accept()");
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprBuiltin x) throws Err {
        if (x.op==ExprBuiltin.Op.TOTALORDER) {
            SimTupleset elem = cset(x.args.get(0)), first = cset(x.args.get(1)), next = cset(x.args.get(2));
            return next.totalOrder(elem, first);
        }
        SimTupleset[] ans = new SimTupleset[x.args.size()];
        for(int i=1; i<ans.length; i++) {
           for(int j=0; j<i; j++) {
              if (ans[i]==null) if ((ans[i]=cset(x.args.get(i))).size()==0) continue;
              if (ans[j]==null) if ((ans[j]=cset(x.args.get(j))).size()==0) continue;
              if (ans[j].intersects(ans[i])) return false;
           }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprCall x) throws Err {
        final Func f = x.fun;
        final Object candidate = f.params.size()==0 ? cacheForConstants.get(f) : null;
        if (candidate!=null) return candidate;
        final Expr body = f.getBody();
        final int n = f.params.size();
        for(Func ff:current_function) if (ff==f) throw new ErrorSyntax(x.span(), ""+f+" cannot call itself recursively!");
        Env<ExprVar,Object> newenv = new Env<ExprVar,Object>();
        for(int i=0; i<n; i++) newenv.put(f.params.get(i), cset(x.args.get(i)));
        Env<ExprVar,Object> oldenv = env;
        env = newenv;
        current_function.add(f);
        Object ans = visitThis(body);
        env = oldenv;
        current_function.remove(current_function.size()-1);
        if (f.params.size()==0) cacheForConstants.put(f, ans);
        return ans;
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprConstant x) throws Err {
        switch(x.op) {
          case NUMBER:
             int n = x.num();
             if (n<min) throw new ErrorType(x.pos, "Current bitwidth is set to "+bitwidth+", thus this integer constant "+n+" is smaller than the minimum integer "+min);
             if (n>max) throw new ErrorType(x.pos, "Current bitwidth is set to "+bitwidth+", thus this integer constant "+n+" is bigger than the maximum integer "+max);
             return n;
          case FALSE: return Boolean.FALSE;
          case TRUE: return Boolean.TRUE;
          case MIN: return min;
          case MAX: return max;
          case NEXT: if (cacheNEXT==null) return cacheNEXT=SimTupleset.next(min,max); else return cacheNEXT;
          case IDEN: if (cacheIDEN==null) return cacheIDEN=cset(Sig.UNIV).iden(); else return cacheIDEN;
        }
        throw new ErrorFatal(x.pos, "Unsupported operator ("+x.op+") encountered during ExprConstant.accept()");
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprITE x) throws Err {
        if (cform(x.cond)) return visitThis(x.left); else return visitThis(x.right);
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprLet x) throws Err {
        env.put(x.var, visitThis(x.var.expr));
        Object ans = visitThis(x.sub);
        env.remove(x.var);
        return ans;
    }

    private Iterator<SimTupleset> loneIterator(final SimTupleset e) {
        return new Iterator<SimTupleset>() {
            private int i = (-1); // next element to dish out
            public SimTupleset next() {
                if (i<0) {i++; return SimTupleset.EMPTY;} else if (i>=e.size()) throw new NoSuchElementException(); else i++;
                SimTupleset ans = new SimTupleset();
                ans.tuples.add(e.tuples.get(i-1));
                return ans;
            }
            public boolean hasNext() { return i < e.size(); }
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }

    private Iterator<SimTupleset> powersetIterator(final SimTupleset e) {
        return new Iterator<SimTupleset>() {
            private boolean eof = false;
            private boolean in[] = new boolean[e.size()]; // next element to dish out
            public SimTupleset next() {
                if (eof) throw new NoSuchElementException();
                SimTupleset ans = new SimTupleset();
                for(int i=0; i<e.size(); i++) if (in[i]) ans.tuples.add(e.tuples.get(i));
                for(int i=0; ; i++) if (i==e.size()) {eof=true;break;} else if (!in[i]) {in[i]=true; break;} else {in[i]=false;}
                return ans;
            }
            public boolean hasNext() { return !eof; }
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }

    private int enumerate(List<Object[]> store, int sum, final ExprQuant x, final Expr body, final int i) throws Err { // if op is ALL NO SOME ONE LONE then it always returns 0 1 2
       final int n = x.vars.size();
       final ExprVar v = x.vars.get(i);
       final SimTupleset e = cset(v.expr);
       final int mode;
       final Iterator<SimTupleset> it;
       if      (v.expr.mult==1 && ((ExprUnary)(v.expr)).op==ExprUnary.Op.LONEOF) { mode=0; it=loneIterator(e); }
       else if (v.expr.mult==1 && ((ExprUnary)(v.expr)).op==ExprUnary.Op.ONEOF)  { mode=1; it=loneIterator(e); }
       else if (v.expr.mult==1 && ((ExprUnary)(v.expr)).op==ExprUnary.Op.SOMEOF) { mode=2; it=powersetIterator(e); }
       else                                                                      { mode=3; it=powersetIterator(e); }
       while(it.hasNext()) {
          final SimTupleset binding = it.next();
          if (binding.size()==0 && (mode==1 || mode==2)) continue;
          if (!isIn(binding, v.expr)) continue;
          env.put(v, binding);
          if (i<n-1) sum = enumerate(store, sum, x, body, i+1);
             else if (x.op==ExprQuant.Op.SUM) sum += cint(body);
             else if (x.op!=ExprQuant.Op.COMPREHENSION) { sum += cform(body)?1:0; if (sum>=2) return 2; } // no need to enumerate further
             else if (cform(body)) { Object[] add = new Object[n]; for(int j=0; j<n; j++) add[j]=((SimTupleset)(env.get(x.vars.get(j)))).tuples.get(0)[0]; store.add(add); }
          env.remove(v);
       }
       return sum;
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprQuant x) throws Err {
        if (x.op == ExprQuant.Op.COMPREHENSION) {
           SimTupleset ans = new SimTupleset();
           enumerate(ans.tuples, 0, x, x.sub, 0);
           return ans;
        }
        if (x.op == ExprQuant.Op.ALL)  return enumerate(null, 0, x, x.sub.not(), 0) == 0;
        if (x.op == ExprQuant.Op.NO)   return enumerate(null, 0, x, x.sub,       0) == 0;
        if (x.op == ExprQuant.Op.SOME) return enumerate(null, 0, x, x.sub,       0) >= 1;
        if (x.op == ExprQuant.Op.LONE) return enumerate(null, 0, x, x.sub,       0) <= 1;
        if (x.op == ExprQuant.Op.ONE)  return enumerate(null, 0, x, x.sub,       0) == 1;
        if (x.op == ExprQuant.Op.SUM)  return trunc(enumerate(null, 0, x, x.sub, 0));
        throw new ErrorFatal(x.pos, "Unsupported operator ("+x.op+") encountered during ExprQuant.accept()");
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprUnary x) throws Err {
        switch(x.op) {
          case LONEOF:
          case ONEOF:
          case SETOF:
          case SOMEOF:      return cset(x.sub);
          case NOOP:        return visitThis(x.sub);
          case CARDINALITY: return trunc(cset(x.sub).size());
          case NO:          return cset(x.sub).size()==0;
          case LONE:        return cset(x.sub).size()<=1;
          case ONE:         return cset(x.sub).size()==1;
          case SOME:        return cset(x.sub).size()>=1;
          case NOT:         return cform(x.sub) ? Boolean.FALSE : Boolean.TRUE;
          case CAST2SIGINT: return SimTupleset.wrap(cint(x.sub));
          case CAST2INT:    return trunc(cset(x.sub).sum());
          case CLOSURE:     return cset(x.sub).closure();
          case RCLOSURE:    return cset(x.sub).closure().union(cset(ExprConstant.IDEN));
          case TRANSPOSE:   return cset(x.sub).transpose();
        }
        throw new ErrorFatal(x.pos, "Unsupported operator ("+x.op+") encountered during ExprUnary.accept()");
    }

    /** {@inheritDoc} */
    @Override public Object visit(ExprVar x) throws Err {
        Object ans = env.get(x);
        if (ans==null) throw new ErrorFatal(x.pos, "Variable \""+x+"\" is not bound to a legal value during translation.\n");
        return ans;
    }

    /** {@inheritDoc} */
    @Override public Object visit(Sig x) throws Err {
        Object ans = sigs.get(x);
        if (ans==null) throw new ErrorFatal("Unknown sig "+x+" encountered during evaluation."); else return ans;
    }

    /** {@inheritDoc} */
    @Override public Object visit(Field x) throws Err {
        Object ans = fields.get(x);
        if (ans==null) throw new ErrorFatal("Unknown field "+x+" encountered during evaluation."); else return ans;
    }
}
