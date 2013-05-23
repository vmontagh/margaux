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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.PrintWriter;
import java.io.StringWriter;

import edu.mit.csail.sdg.moolloy.solver.kodkod.api.MeasuredSolution;
import edu.mit.csail.sdg.moolloy.solver.kodkod.api.MetricPoint;
import edu.mit.csail.sdg.moolloy.solver.kodkod.api.Objective;
import kodkod.ast.BinaryExpression;
import kodkod.ast.BinaryFormula;
import kodkod.ast.BinaryIntExpression;
import kodkod.ast.ComparisonFormula;
import kodkod.ast.Comprehension;
import kodkod.ast.ConstantExpression;
import kodkod.ast.ConstantFormula;
import kodkod.ast.Decl;
import kodkod.ast.Decls;
import kodkod.ast.ExprToIntCast;
import kodkod.ast.IfExpression;
import kodkod.ast.IfIntExpression;
import kodkod.ast.IntComparisonFormula;
import kodkod.ast.IntConstant;
import kodkod.ast.IntExpression;
import kodkod.ast.IntToExprCast;
import kodkod.ast.NaryExpression;
import kodkod.ast.NaryFormula;
import kodkod.ast.NaryIntExpression;
import kodkod.ast.Node;
import kodkod.ast.ProjectExpression;
import kodkod.ast.MultiplicityFormula;
import kodkod.ast.NotFormula;
import kodkod.ast.QuantifiedFormula;
import kodkod.ast.Relation;
import kodkod.ast.RelationPredicate;
import kodkod.ast.UnaryExpression;
import kodkod.ast.SumExpression;
import kodkod.ast.UnaryIntExpression;
import kodkod.ast.Variable;
import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.RelationPredicate.Function;
import kodkod.ast.visitor.ReturnVisitor;
import kodkod.ast.visitor.VoidVisitor;
import kodkod.engine.Solution;
import kodkod.instance.Bounds;
import kodkod.instance.TupleSet;
import kodkod.instance.Tuple;
import kodkod.util.ints.IndexedEntry;
import kodkod.util.nodes.PrettyPrinter;

/** Translate a Kodkod formula node to an equivalent Java program that solves the formula.
 *
 * <p> Requirements: atoms must be String objects (since we cannot possibly
 * output a Java source code that can re-generate arbitrary Java objects).
 */

public final class TranslateKodkodToJava implements VoidVisitor {

    /** Count the height of the given Kodkod AST tree. */
    public static int countHeight(Node node) {
        ReturnVisitor<Integer,Integer,Integer,Integer> vis = new ReturnVisitor<Integer,Integer,Integer,Integer>() {
            private int max(int a, int b)                 { return (a>=b) ? a : b; }
            private int max(int a, int b, int c)          { return (a>=b) ? (a>=c ? a : c) : (b>=c ? b: c); }
            public Integer visit(Relation x)              { return 1; }
            public Integer visit(IntConstant x)           { return 1; }
            public Integer visit(ConstantFormula x)       { return 1; }
            public Integer visit(Variable x)              { return 1; }
            public Integer visit(ConstantExpression x)    { return 1; }
            public Integer visit(NotFormula x)            { return 1 + x.formula().accept(this); }
            public Integer visit(IntToExprCast x)         { return 1 + x.intExpr().accept(this); }
            public Integer visit(Decl x)                  { return 1 + x.expression().accept(this); }
            public Integer visit(ExprToIntCast x)         { return 1 + x.expression().accept(this); }
            public Integer visit(UnaryExpression x)       { return 1 + x.expression().accept(this); }
            public Integer visit(UnaryIntExpression x)    { return 1 + x.intExpr().accept(this); }
            public Integer visit(MultiplicityFormula x)   { return 1 + x.expression().accept(this); }
            public Integer visit(BinaryExpression x)      { return 1 + max(x.left().accept(this), x.right().accept(this)); }
            public Integer visit(ComparisonFormula x)     { return 1 + max(x.left().accept(this), x.right().accept(this)); }
            public Integer visit(BinaryFormula x)         { return 1 + max(x.left().accept(this), x.right().accept(this)); }
            public Integer visit(BinaryIntExpression x)   { return 1 + max(x.left().accept(this), x.right().accept(this)); }
            public Integer visit(IntComparisonFormula x)  { return 1 + max(x.left().accept(this), x.right().accept(this)); }
            public Integer visit(IfExpression x)          { return 1 + max(x.condition().accept(this), x.thenExpr().accept(this), x.elseExpr().accept(this)); }
            public Integer visit(IfIntExpression x)       { return 1 + max(x.condition().accept(this), x.thenExpr().accept(this), x.elseExpr().accept(this)); }
            public Integer visit(SumExpression x)         { return 1 + max(x.decls().accept(this), x.intExpr().accept(this)); }
            public Integer visit(QuantifiedFormula x)     { return 1 + max(x.decls().accept(this), x.formula().accept(this)); }
            public Integer visit(Comprehension x)         { return 1 + max(x.decls().accept(this), x.formula().accept(this)); }
            public Integer visit(Decls x) {
                int max = 0, n = x.size();
                for(int i=0; i<n; i++) max = max(max, x.get(i).accept(this));
                return max;
            }
            public Integer visit(ProjectExpression x) {
                int max = x.expression().accept(this);
                for(Iterator<IntExpression> t = x.columns(); t.hasNext();) { max = max(max, t.next().accept(this)); }
                return max;
            }
            public Integer visit(RelationPredicate x) {
                if (x instanceof Function) {
                    Function f = ((Function)x);
                    return max(f.domain().accept(this), f.range().accept(this));
                }
                return 1;
            }
            public Integer visit(NaryExpression x) {
                int max = 0;
                for(int m=0, n=x.size(), i=0; i<n; i++) { m=x.child(i).accept(this); if (i==0 || max<m) max=m; }
                return max + 1;
            }
            public Integer visit(NaryIntExpression x) {
                int max = 0;
                for(int m=0, n=x.size(), i=0; i<n; i++) { m=x.child(i).accept(this); if (i==0 || max<m) max=m; }
                return max + 1;
            }
            public Integer visit(NaryFormula x) {
                int max = 0;
                for(int m=0, n=x.size(), i=0; i<n; i++) { m=x.child(i).accept(this); if (i==0 || max<m) max=m; }
                return max + 1;
            }
        };
        Object ans = node.accept(vis);
        if (ans instanceof Integer) return ((Integer)ans).intValue(); else return 0;
    }

    /** Given a Kodkod formula node, return a Java program that (when compiled and executed) would solve that formula.
     *
     * <p> Requirement: atoms must be String objects (since we cannot possibly
     * output a Java source code that can re-generate arbitrary Java objects).
     *
     * @param formula - the formula to convert
     * @param bitwidth - the integer bitwidth
     * @param atoms - an iterator over the set of all atoms
     * @param bounds - the Kodkod bounds object to use
     * @param atomMap - if nonnull, it is used to map the atom name before printing
     */
    public static String convert
    (Formula formula, int bitwidth, Iterable<String> atoms, Bounds bounds, Map<Object,String> atomMap) {
        StringWriter string=new StringWriter();
        PrintWriter file=new PrintWriter(string);
        new TranslateKodkodToJava(file, formula, bitwidth, atoms, bounds, atomMap, null);
        if (file.checkError()) {
            return ""; // shouldn't happen
        } else {
            return string.toString();
        }
    }
    
    public static String convert
    (Formula formula, int bitwidth, Iterable<String> atoms, Bounds bounds, Map<Object,String> atomMap, TreeSet<Objective> objectives) {
        StringWriter string=new StringWriter();
        PrintWriter file=new PrintWriter(string);
        new TranslateKodkodToJava(file, formula, bitwidth, atoms, bounds, atomMap, objectives);
        if (file.checkError()) {
            return "";
        } else {
            return string.toString();
        }
    }

    /** The PrintWriter that is receiving the text. */
    private final JavaFilePrinter printer;

    /** This caches nodes that we have already generated. */
    private final IdentityHashMap<Node,String> map=new IdentityHashMap<Node,String>();

    /** Given a node, return its name (if no name has been chosen, then make a new name) */
    private String makename(Node obj) {
        if (map.containsKey(obj)) return null;
        String name="x"+(map.size());
        map.put(obj, name);
        return name;
    }

    /** Given a node, call the visitor to dump its text out, then return its name. */
    private String make(Node x) { x.accept(this); return map.get(x); }

    /** Constructor is private, so that the only way to access this class is via the static convert() method. */
    private TranslateKodkodToJava
    (PrintWriter pw, Formula x, int bitwidth, Iterable<String> atoms, Bounds bounds, Map<Object,String> atomMap, TreeSet<Objective> objectives) {
    	
    	printer = new JavaFilePrinter(pw);

//        file.printf("import java.util.Arrays;%n");
//        file.printf("import java.util.List;%n");
//        file.printf("import java.util.TreeSet;%n");
//        file.printf("import java.io.PrintWriter;%n");
//        file.printf("import java.io.FileWriter;%n");
//        file.printf("import kodkod.ast.*;%n");
//        file.printf("import kodkod.ast.operator.*;%n");
//        file.printf("import kodkod.instance.*;%n");
//        file.printf("import kodkod.engine.*;%n");
//        file.printf("import kodkod.engine.satlab.SATFactory;%n");
//        file.printf("import kodkod.engine.config.Options;%n%n");
//        file.printf("import edu.mit.csail.sdg.moolloy.solver.kodkod.api.*;%n%n");
//        
//        file.printf("/* %n");
//        file.printf("  ==================================================%n");
//        file.printf("    kodkod formula: %n");
//        file.printf("  ==================================================%n");
//        file.printf(PrettyPrinter.print(x, 4) + "%n");
//        file.printf("  ==================================================%n");
//        file.printf("*/%n");
//        file.printf("public final class Test {%n%n");
//        file.printf("public static void main(String[] args) throws Exception {%n%n");
        ArrayList<String> atomlist=new ArrayList<String>();
        for(Object a:atoms) {
            String b = atomMap==null ? null : atomMap.get(a);
            atomlist.add(b==null ? a.toString() : b);
        }
        Collections.sort(atomlist);
        for(Relation r:bounds.relations()) {
            String name=makename(r);
            int a=r.arity();
            
            printer.pushDeclaration(String.format("Relation %s;", name));
            
            if (a==1) {
            	
            	printer.pushStatement(String.format("%s = Relation.unary(\"%s\");", name, r.name()));
                //file.printf("Relation %s = Relation.unary(\"%s\");%n", name, r.name());
            }
            else {
            	printer.pushStatement(String.format("%s = Relation.nary(\"%s\", %d);", name, r.name(), a));
                //file.printf("Relation %s = Relation.nary(\"%s\", %d);%n", name, r.name(), a);
            }
        }
        
        printer.pushDeclaration("List<String> atomlist;");
        
        StringBuilder sb = new StringBuilder();
        sb.append("atomlist = Arrays.asList(");
        //file.printf("%nList<String> atomlist = Arrays.asList(%n");
        int j=(-1);
        for(String a:atomlist) {
            if (j!=(-1)) sb.append(","); else j=0;
            if (j==5) {sb.append(" "); j=0;} else {sb.append(" "); j++;}
            sb.append("\"");
            sb.append(a);
            sb.append("\"");
            //file.printf("\"%s\"", a);
        }
        sb.append(");");
        printer.pushStatement(sb.toString());
        printer.pushDeclaration("Universe universe;");
        printer.pushDeclaration("TupleFactory factory;");
        printer.pushDeclaration("Bounds bounds;");
        printer.pushStatement("universe = new Universe(atomlist);");
        printer.pushStatement("factory = universe.factory();");
        printer.pushStatement("bounds = new Bounds(universe);");
        //file.printf("Universe universe = new Universe(atomlist);%n");
        //file.printf("TupleFactory factory = universe.factory();%n");
        //file.printf("Bounds bounds = new Bounds(universe);%n%n");
        
        for(Relation r:bounds.relations()) {
            String n=map.get(r);
            TupleSet upper=bounds.upperBound(r);
            TupleSet lower=bounds.lowerBound(r);
            printTupleset(n+"_upper", upper, atomMap);
            if (upper.equals(lower)) {
            	printer.pushStatement(String.format("bounds.boundExactly(%s, %s_upper);%n%n",n,n));
                //file.printf("bounds.boundExactly(%s, %s_upper);%n%n",n,n);
            }
            else if (lower.size()==0) {
            	printer.pushStatement(String.format("bounds.bound(%s, %s_upper);%n%n",n,n));
                //file.printf("bounds.bound(%s, %s_upper);%n%n",n,n);
            }
            else {
                printTupleset(n+"_lower", lower, atomMap);
                printer.pushStatement(String.format("bounds.bound(%s, %s_lower, %s_upper);%n%n",n,n,n));
                //file.printf("bounds.bound(%s, %s_lower, %s_upper);%n%n",n,n,n);
            }
        }
        for(IndexedEntry<TupleSet> i:bounds.intBounds()) {
            for(Tuple t:i.value()) {
                Object a = t.atom(0);
                String b = (atomMap!=null ? atomMap.get(a) : null);
                String c = (b!=null? b : a.toString());
                printer.pushStatement(String.format("bounds.boundExactly(%d,factory.range("
                        +"factory.tuple(\"%s\"),factory.tuple(\"%s\")));%n", i.index(), c, c));
                //file.printf("bounds.boundExactly(%d,factory.range("
                //    +"factory.tuple(\"%s\"),factory.tuple(\"%s\")));%n", i.index(), c, c);
            }
        }
        
        //file.printf("%n");
        
        if (objectives != null) {
        	printer.pushDeclaration("TreeSet<Objective> objectives;");
        	printer.pushStatement("objectives = new TreeSet<Objective>();");
            //file.printf("%nTreeSet<Objective> objectives = new TreeSet<Objective>();%n");
            int o_count = 0;
            for (Objective o : objectives) {
                String o_description = o.desc;
                String o_expression = make(o.expr);
                if (o_description.startsWith("maximize")) {
                	printer.pushDeclaration(String.format("Objective o%d;", o_count));
                	printer.pushStatement(String.format("o%d = Objective.newMaxObjective(\"%s\", %s);", o_count, o_description, o_expression));
                    //file.printf("%nObjective o%d = Objective.newMaxObjective(\"%s\", %s);", o_count, o_description, o_expression);
                } else if (o_description.startsWith("minimize")) {
                	printer.pushDeclaration(String.format("Objective o%d;", o_count));
                	printer.pushStatement(String.format("o%d = Objective.newMinObjective(\"%s\", %s);", o_count, o_description, o_expression));
                    //file.printf("%nObjective o%d = Objective.newMinObjective(\"%s\", %s);", o_count, o_description, o_expression);
                } else {
                    assert false;
                }
                printer.pushStatement(String.format("%nobjectives.add(o%d);%n", o_count));
                //file.printf("%nobjectives.add(o%d);%n", o_count);
                o_count += 1;
            }
        }
        
        //file.printf("%n");
        
        String result=make(x);
        
        printer.printToFile(objectives, bitwidth, result);
//        
//        if (objectives == null) {
//            file.printf("%nSolver solver = new Solver();");
//            file.printf("%nsolver.options().setSolver(SATFactory.DefaultSAT4J);");
//            file.printf("%nsolver.options().setBitwidth(%d);",bitwidth);
//            file.printf("%nsolver.options().setFlatten(false);");
//            file.printf("%nsolver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);");
//            file.printf("%nsolver.options().setSymmetryBreaking(20);");
//            file.printf("%nsolver.options().setSkolemDepth(0);");
//            file.printf("%nSystem.out.println(\"Solving...\");");
//            file.printf("%nSystem.out.flush();");
//            file.printf("%nSolution sol = solver.solve(%s,bounds);", result);
//            file.printf("%nSystem.out.println(sol.toString());");
//        } else {
//            file.printf("%nMultiObjectiveProblem problem = new MultiObjectiveProblem(bounds, %d, %s, objectives);", bitwidth, result);
//            file.printf("%nGuidedImprovementAlgorithm gia = new GuidedImprovementAlgorithm(\"asdf\", false);");
//            
//            file.printf("%nSolutionNotifier notifier = new SolutionNotifier() {");
//            file.printf("%nint solution_count;");
//            file.printf("%npublic void tell(final MeasuredSolution s) {");
//            file.printf("%ntry {");
//            file.printf("%nFileWriter file = new FileWriter(\"kodkod_solutions_\" + solution_count + \".txt\");");
//            file.printf("%nPrintWriter print = new PrintWriter(file);");
//            file.printf("%nprint.println(s.toString());");
//            file.printf("%nsolution_count += 1;");
//            file.printf("%nprint.flush();");
//            file.printf("%nprint.close();");
//            file.printf("%nfile.close();");
//            file.printf("%n} catch (Exception e) {}");
//            file.printf("%n}");
//            file.printf("%npublic void tell(Solution s, MetricPoint values) {");
//            file.printf("%ntell(new MeasuredSolution(s, values));");
//            file.printf("%n}");
//            file.printf("%npublic void done(){};");
//            file.printf("%n};");
//            
//            file.printf("%nSystem.out.println(\"Solving...\");");
//            file.printf("%nSystem.out.flush();");
//            file.printf("%ngia.moosolve(problem, notifier, true);");
//        }
//        
//        file.printf("%n}}%n");
//        file.close();
    }

    /** Print the tupleset using the name n. */
    private void printTupleset(String n, TupleSet ts, Map<Object,String> atomMap) {
    	printer.pushDeclaration("TupleSet " + n + ";");
    	printer.pushStatement(String.format("factory.noneOf(%d);", ts.arity()));
        //file.printf("TupleSet %s = factory.noneOf(%d);%n", n, ts.arity());
        
    	for(Tuple t:ts) {
    		StringBuilder sb = new StringBuilder();
    		sb.append(String.format("%s.add(",n));
            //file.printf("%s.add(",n);
            for(int i=0; i<ts.arity(); i++) {
                if (i!=0) {
                	sb.append(".product(");
                	//file.printf(".product(");
                }
                Object a=t.atom(i);
                String b=atomMap==null ? null : atomMap.get(a);
                sb.append(String.format("factory.tuple(\"%s\")" , (b==null ? a.toString() : b) ));
                //file.printf("factory.tuple(\"%s\")" , (b==null ? a.toString() : b) );
                if (i!=0){
                	sb.append(")");
                	//file.printf(")");
                }
            }
            sb.append(");");
            printer.pushStatement(sb.toString());
            //file.printf(");%n");
        }
    }

    /** {@inheritDoc} */
    public void visit(Relation x) {
        if (!map.containsKey(x)) throw new RuntimeException("Unknown kodkod relation \""+x.name()+"\" encountered");
    }

    /** {@inheritDoc} */
    public void visit(BinaryExpression x) {
        String newname=makename(x); if (newname==null) return;
        String left=make(x.left());
        String right=make(x.right());
        
        printer.pushDeclaration(String.format("Expression %s;", newname));
        
        switch(x.op()) {
           case DIFFERENCE: 
        	   printer.pushStatement(String.format("%s=%s.difference(%s);", newname, left, right));
        	   //file.printf("Expression %s=%s.difference(%s);%n", newname, left, right); 
           break;
           case INTERSECTION:
        	   printer.pushStatement(String.format("%s=%s.intersection(%s);", newname, left, right));
        	   //file.printf("Expression %s=%s.intersection(%s);%n", newname, left, right);
        	   break;
           case JOIN:
        	   printer.pushStatement(String.format("%s=%s.join(%s);", newname, left, right));
        	   //file.printf("Expression %s=%s.join(%s);%n", newname, left, right); 
        	   break;
           case OVERRIDE: 
        	   printer.pushStatement(String.format("%s=%s.override(%s);", newname, left, right));
        	   //file.printf("Expression %s=%s.override(%s);%n", newname, left, right); 
           break;
           case PRODUCT: 
        	   printer.pushStatement(String.format("%s=%s.product(%s);", newname, left, right));
        	   //file.printf("Expression %s=%s.product(%s);%n", newname, left, right); 
        	   break;
           case UNION:
        	   printer.pushStatement(String.format("%s=%s.union(%s);", newname, left, right));
        	   //file.printf("Expression %s=%s.union(%s);%n", newname, left, right); 
        	   break;
           default: throw new RuntimeException("Unknown kodkod operator \""+x.op()+"\" encountered");
        }
    }

    /** {@inheritDoc} */
    public void visit(ComparisonFormula x) {
        String newname=makename(x); if (newname==null) return;
        String left=make(x.left());
        String right=make(x.right());
        
        printer.pushDeclaration(String.format("Formula %s;", newname));
        switch(x.op()) {
           case EQUALS: 
        	   printer.pushStatement(String.format("%s=%s.eq(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.eq(%s);%n", newname, left, right); 
        	   break;
           case SUBSET:
        	   printer.pushStatement(String.format("%s=%s.in(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.in(%s);%n", newname, left, right); 
        	   break;
           default: throw new RuntimeException("Unknown kodkod operator \""+x.op()+"\" encountered");
        }
    }

    /** {@inheritDoc} */
    public void visit(ProjectExpression x) {
        String newname=makename(x); if (newname==null) return;
        String expr=make(x.expression());
        List<String> names=new ArrayList<String>();
        for(Iterator<IntExpression> i = x.columns(); i.hasNext(); ) { names.add(make(i.next())); }
        
        printer.pushDeclaration(String.format("Expression %s;", newname));
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<names.size(); i++) {
            if (i==0){
            	sb.append(String.format("%s=%s.over(", newname, expr));
            	//file.printf("Expression %s=%s.over(", newname, expr);
            }
            else {
            	sb.append(",");
            	//file.printf(",");
            }
            sb.append(names.get(i));
            //file.printf("%s", names.get(i));
        }
        sb.append(");");
        printer.pushStatement(sb.toString());
        //file.printf(");%n");
    }

    /** {@inheritDoc} */
    public void visit(IntComparisonFormula x) {
        String newname=makename(x); if (newname==null) return;
        String left=make(x.left());
        String right=make(x.right());
        
        printer.pushDeclaration(String.format("Formula %s;", newname));
        
        switch(x.op()) {
           case EQ:
        	   printer.pushStatement(String.format("%s=%s.eq(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.eq(%s);%n", newname, left, right); 
        	   break;
           case GT: 
        	   printer.pushStatement(String.format("%s=%s.gt(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.gt(%s);%n", newname, left, right); 
        	   break;
           case GTE: 
        	   printer.pushStatement(String.format("%s=%s.gte(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.gte(%s);%n", newname, left, right); 
        	   break;
           case LT: 
        	   printer.pushStatement(String.format("%s=%s.lt(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.lt(%s);%n", newname, left, right); 
        	   break;
           case LTE:
        	   printer.pushStatement(String.format("%s=%s.lte(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.lte(%s);%n", newname, left, right); 
        	   break;
           default: throw new RuntimeException("Unknown kodkod operator \""+x.op()+"\" encountered");
        }
    }

    /** {@inheritDoc} */
    public void visit(BinaryFormula x) {
        String newname=makename(x); if (newname==null) return;
        String left=make(x.left());
        String right=make(x.right());
        
        printer.pushDeclaration(String.format("Formula %s;", newname));
        
        switch(x.op()) {
           case AND:
        	   printer.pushStatement(String.format("%s=%s.and(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.and(%s);%n", newname, left, right); 
        	   break;
           case OR:
        	   printer.pushStatement(String.format("%s=%s.or(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.or(%s);%n", newname, left, right); 
        	   break;
           case IMPLIES:
        	   printer.pushStatement(String.format("%s=%s.implies(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.implies(%s);%n", newname, left, right); 
        	   break;
           case IFF: 
        	   printer.pushStatement(String.format("%s=%s.iff(%s);", newname, left, right));
        	   //file.printf("Formula %s=%s.iff(%s);%n", newname, left, right); 
        	   break;
           default: throw new RuntimeException("Unknown kodkod operator \""+x.op()+"\" encountered");
        }
    }

    /** {@inheritDoc} */
    public void visit(BinaryIntExpression x) {
        String newname=makename(x); if (newname==null) return;
        String left=make(x.left());
        String right=make(x.right());
        
        printer.pushDeclaration(String.format("IntExpression %s;", newname));
        
        switch(x.op()) {
		case PLUS:
			printer.pushStatement(String.format("%s=%s.plus(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.plus(%s);%n", newname, left, right);
			break;
		case MINUS:
			printer.pushStatement(String.format("%s=%s.minus(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.minus(%s);%n", newname, left,right);
			break;
		case MULTIPLY:
			printer.pushStatement(String.format("%s=%s.multiply(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.multiply(%s);%n", newname, left,right);
			break;
		case DIVIDE:
			printer.pushStatement(String.format("%s=%s.divide(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.divide(%s);%n", newname, left,right);
			break;
		case MODULO:
			printer.pushStatement(String.format("%s=%s.modulo(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.modulo(%s);%n", newname, left,	right);
			break;
		case AND:
			printer.pushStatement(String.format("%s=%s.and(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.and(%s);%n", newname, left, right);
			break;
		case OR:
			printer.pushStatement(String.format("%s=%s.or(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.or(%s);%n", newname, left, right);
			break;
		case XOR:
			printer.pushStatement(String.format("%s=%s.xor(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.xor(%s);%n", newname, left, right);
			break;
		case SHA:
			printer.pushStatement(String.format("%s=%s.sha(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.sha(%s);%n", newname, left, right);
			break;
		case SHL:
			printer.pushStatement(String.format("%s=%s.shl(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.shl(%s);%n", newname, left, right);
			break;
		case SHR:
			printer.pushStatement(String.format("%s=%s.shr(%s);", newname, left, right));
			//file.printf("IntExpression %s=%s.shr(%s);%n", newname, left, right);
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
        }
    }

    /** {@inheritDoc} */
    public void visit(UnaryIntExpression x) {
        String newname=makename(x); if (newname==null) return;
        String sub=make(x.intExpr());
        
        printer.pushDeclaration(String.format("IntExpression %s;", newname));

		switch (x.op()) {
		case MINUS:
			printer.pushStatement(String.format("%s=%s.negate();", newname, sub));
			//file.printf("IntExpression %s=%s.negate();%n", newname, sub);
			break;
		case NOT:
			printer.pushStatement(String.format("%s=%s.not();", newname, sub));
			//file.printf("IntExpression %s=%s.not();%n", newname, sub);
			break;
		case ABS:
			printer.pushStatement(String.format("%s=%s.abs();", newname, sub));
			//file.printf("IntExpression %s=%s.abs();%n", newname, sub);
			break;
		case SGN:
			printer.pushStatement(String.format("%s=%s.signum();", newname, sub));
			//file.printf("IntExpression %s=%s.signum();%n", newname, sub);
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
		}
    }

    /** {@inheritDoc} */
    public void visit(UnaryExpression x) {
        String newname=makename(x); if (newname==null) return;
        String sub=make(x.expression());
        
        printer.pushDeclaration(String.format("Expression %s;", newname));

		switch (x.op()) {
		case CLOSURE:
			printer.pushStatement(String.format("%s=%s.closure();", newname, sub));
			//file.printf("Expression %s=%s.closure();%n", newname, sub);
			break;
		case REFLEXIVE_CLOSURE:
			printer.pushStatement(String.format("%s=%s.reflexiveClosure();", newname, sub));
			//file.printf("Expression %s=%s.reflexiveClosure();%n", newname, sub);
			break;
		case TRANSPOSE:
			printer.pushStatement(String.format("%s=%s.transpose();", newname, sub));
			//file.printf("Expression %s=%s.transpose();%n", newname, sub);
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
		}
    }

    /** {@inheritDoc} */
    public void visit(IfExpression x) {
        String newname=makename(x); if (newname==null) return;
        String a=make(x.condition());
        String b=make(x.thenExpr());
        String c=make(x.elseExpr());
        printer.pushDeclaration("Expression " + newname + ";");
        printer.pushStatement(String.format("%s=%s.thenElse(%s,%s);", newname, a, b, c));
        //file.printf("Expression %s=%s.thenElse(%s,%s);%n", newname, a, b, c);
    }

    /** {@inheritDoc} */
    public void visit(IfIntExpression x) {
        String newname=makename(x); if (newname==null) return;
        String a=make(x.condition());
        String b=make(x.thenExpr());
        String c=make(x.elseExpr());
        printer.pushDeclaration("IntExpression " + newname + ";");
        printer.pushStatement(String.format("%s=%s.thenElse(%s,%s);", newname, a, b, c));
        //file.printf("IntExpression %s=%s.thenElse(%s,%s);%n", newname, a, b, c);
    }

    /** {@inheritDoc} */
    public void visit(NotFormula x) {
        String newname=makename(x); if (newname==null) return;
        String sub=make(x.formula());
        printer.pushDeclaration("Formula " + newname + ";");
        printer.pushStatement(String.format("%s=%s.not();", newname, sub));
        //file.printf("Formula %s=%s.not();%n", newname, sub);
    }

    /** {@inheritDoc} */
    public void visit(IntToExprCast x) {
        String newname=makename(x); if (newname==null) return;
        String sub=make(x.intExpr());
        
        printer.pushDeclaration("Expression " + newname + ";");
        
		switch (x.op()) {
		case INTCAST:
			printer.pushStatement(String.format("%s=%s.toExpression();", newname, sub));
			//file.printf("Expression %s=%s.toExpression();%n", newname, sub);
			break;
		case BITSETCAST:
			printer.pushStatement(String.format("%s=%s.toBitset();", newname, sub));
			//file.printf("Expression %s=%s.toBitset();%n", newname, sub);
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
		}
    }

    /** {@inheritDoc} */
    public void visit(ExprToIntCast x) {
        String newname=makename(x); if (newname==null) return;
        String sub=make(x.expression());
        
        printer.pushDeclaration("IntExpression " + newname + ";");
        
		switch (x.op()) {
		case CARDINALITY:
			printer.pushStatement(String.format("%s=%s.count();", newname, sub));
			//file.printf("IntExpression %s=%s.count();%n", newname, sub);
			break;
		case SUM:
			printer.pushStatement(String.format("%s=%s.sum();", newname, sub));
			//file.printf("IntExpression %s=%s.sum();%n", newname, sub);
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
		}
    }

    /** {@inheritDoc} */
    public void visit(IntConstant x) {
        String newname=makename(x); if (newname==null) return;
        printer.pushDeclaration("IntExpression " + newname + ";");
        printer.pushStatement(String.format("%s=IntConstant.constant(%d);", newname, x.value()));
        //file.printf("IntExpression %s=IntConstant.constant(%d);%n", newname, x.value());
    }

    /** {@inheritDoc} */
    public void visit(ConstantFormula x) {
        if (map.containsKey(x)) return;
        String newname=(x.booleanValue() ? "Formula.TRUE" : "Formula.FALSE");
        map.put(x,newname);
    }

    /** {@inheritDoc} */
    public void visit(ConstantExpression x) {
        if (map.containsKey(x)) return;
        String newname=null;
        if (x==Expression.NONE) newname="Expression.NONE";
           else if (x==Expression.UNIV) newname="Expression.UNIV";
           else if (x==Expression.IDEN) newname="Expression.IDEN";
           else if (x==Expression.INTS) newname="Expression.INTS";
           else throw new RuntimeException("Unknown kodkod ConstantExpression \""+x+"\" encountered");
        map.put(x,newname);
    }

    /** {@inheritDoc} */
    public void visit(Variable x) {
        String newname=makename(x); if (newname==null) return;
        int a=x.arity();
        
        printer.pushDeclaration("Variable " + newname + ";");
        
        if (a==1) {
        	printer.pushStatement(String.format("%s=Variable.unary(\"%s\");", newname, x.name()));
            //file.printf("Variable %s=Variable.unary(\"%s\");%n", newname, x.name());
        }
        else {
        	printer.pushStatement(String.format("%s=Variable.nary(\"%s\",%d);", newname, x.name(), a));
            //file.printf("Variable %s=Variable.nary(\"%s\",%d);%n", newname, x.name(), a);
        }
    }

    /** {@inheritDoc} */
    public void visit(Comprehension x) {
        String newname=makename(x); if (newname==null) return;
        String d=make(x.decls());
        String f=make(x.formula());
        
        printer.pushDeclaration("Expression " + newname + ";");
        printer.pushStatement(String.format("%s=%s.comprehension(%s);",newname,f,d));
        //file.printf("Expression %s=%s.comprehension(%s);%n",newname,f,d);
    }

    /** {@inheritDoc} */
    public void visit(QuantifiedFormula x) {
        String newname=makename(x); if (newname==null) return;
        String d=make(x.decls());
        String f=make(x.formula());
        
        printer.pushDeclaration("Formula " + newname + ";");
        
		switch (x.quantifier()) {
		case ALL:
			printer.pushStatement(String.format("%s=%s.forAll(%s);",newname,f,d));
			//file.printf("Formula %s=%s.forAll(%s);%n", newname, f, d);
			break;
		case SOME:
			printer.pushStatement(String.format("%s=%s.forSome(%s);",newname,f,d));
			//file.printf("Formula %s=%s.forSome(%s);%n", newname, f, d);
			break;
		default:
			throw new RuntimeException("Unknown kodkod quantifier \""
					+ x.quantifier() + "\" encountered");
		}
    }

    /** {@inheritDoc} */
    public void visit(SumExpression x) {
        String newname=makename(x); if (newname==null) return;
        String d=make(x.decls());
        String f=make(x.intExpr());
        
        printer.pushDeclaration("Formula " + newname + ";");
        printer.pushStatement(String.format("%s=%s.sum(%s);",newname,f,d));
        //file.printf("IntExpression %s=%s.sum(%s);%n",newname,f,d);
    }

    /** {@inheritDoc} */
    public void visit(MultiplicityFormula x) {
        String newname=makename(x); if (newname==null) return;
        String sub=make(x.expression());
        
        printer.pushDeclaration("Formula " + newname + ";");
        
		switch (x.multiplicity()) {
		case LONE:
			printer.pushStatement(String.format("%s=%s.lone();",newname,sub));
			//file.printf("Formula %s=%s.lone();%n", newname, sub);
			break;
		case ONE:
			printer.pushStatement(String.format("%s=%s.one();",newname,sub));
			//file.printf("Formula %s=%s.one();%n", newname, sub);
			break;
		case SOME:
			printer.pushStatement(String.format("%s=%s.some();",newname,sub));
			//file.printf("Formula %s=%s.some();%n", newname, sub);
			break;
		case NO:
			printer.pushStatement(String.format("%s=%s.no();",newname,sub));
			//file.printf("Formula %s=%s.no();%n", newname, sub);
			break;
		default:
			throw new RuntimeException("Unknown kodkod multiplicity \""
					+ x.multiplicity() + "\" encountered");
		}
    }

    /** {@inheritDoc} */
    public void visit(Decl x) {
        String newname=makename(x); if (newname==null) return;
        String v=make(x.variable());
        String e=make(x.expression());
        
        printer.pushDeclaration("Decls " + newname + ";");

		switch (x.multiplicity()) {
		case LONE:
			printer.pushStatement(String.format("%s=%s.loneOf(%s);",newname, v, e));
			//file.printf("Decls %s=%s.loneOf(%s);%n", newname, v, e);
			break;
		case ONE:
			printer.pushStatement(String.format("%s=%s.oneOf(%s);",newname, v, e));
			//file.printf("Decls %s=%s.oneOf(%s);%n", newname, v, e);
			break;
		case SOME:
			printer.pushStatement(String.format("%s=%s.someOf(%s);",newname, v, e));
			//file.printf("Decls %s=%s.someOf(%s);%n", newname, v, e);
			break;
		case SET:
			printer.pushStatement(String.format("%s=%s.setOf(%s);",newname, v, e));
			//file.printf("Decls %s=%s.setOf(%s);%n", newname, v, e);
			break;
		default:
			throw new RuntimeException("Unknown kodkod multiplicity \""
					+ x.multiplicity() + "\" encountered");
		}
    }

    /** {@inheritDoc} */
    public void visit(Decls x) {
        String newname=makename(x); if (newname==null) return;
        for (Decl y:x) { y.accept(this); }
        boolean first=true;
        
        printer.pushDeclaration("Decls " + newname + ";");
        StringBuilder sb = new StringBuilder();
        sb.append(newname + "=" );
        
        //file.printf("Decls %s=",newname);
        for(Decl y:x) {
            String z=map.get(y);
			if (first) {
				sb.append(z);
				//file.printf("%s", z);
				first = false;
			} else {
				sb.append(".and(");
				sb.append(z);
				sb.append(")");
				//file.printf(".and(%s)", z);
			}
        }
        sb.append(";");
        printer.pushStatement(sb.toString());
        //file.printf(";%n");
    }

    /** {@inheritDoc} */
    public void visit(RelationPredicate x) {
        String newname=makename(x); if (newname==null) return;
        String rel=make(x.relation());
        
        printer.pushDeclaration("Formula " + newname + ";");

		switch (x.name()) {
		case TOTAL_ORDERING: {
			final RelationPredicate.TotalOrdering tp = (RelationPredicate.TotalOrdering) x;
			String o = make(tp.ordered());
			String f = make(tp.first());
			String l = make(tp.last());
			printer.pushStatement(String.format("%s=%s.totalOrder(%s,%s,%s);", newname, rel, o, f, l));
			//file.printf("Formula %s=%s.totalOrder(%s,%s,%s);%n", newname, rel,o, f, l);
			return;
		}
		case FUNCTION: {
			final RelationPredicate.Function tp = (RelationPredicate.Function) x;
			String domain = make(tp.domain());
			String range = make(tp.range());
			switch (((RelationPredicate.Function) x).targetMult()) {
			case ONE:
				printer.pushStatement(String.format("%s=%s.function(%s,%s);%n", newname, rel,domain, range));
				//file.printf("Formula %s=%s.function(%s,%s);%n", newname, rel,domain, range);
				return;
			case LONE:
				printer.pushStatement(String.format("%s=%s.partialFunction(%s,%s);%n", newname,	rel, domain, range));
				//file.printf("Formula %s=%s.partialFunction(%s,%s);%n", newname,	rel, domain, range);
				return;
			default:
				throw new RuntimeException(
						"Illegal multiplicity encountered in RelationPredicate.Function");
			}
		}
		case ACYCLIC: {
			printer.pushStatement(String.format("%s=%s.acyclic();%n", newname, rel));
			//file.printf("Formula %s=%s.acyclic();%n", newname, rel);
			return;
		}
		}
        throw new RuntimeException("Unknown RelationPredicate \""+x+"\" encountered");
    }

    /** {@inheritDoc} */
    public void visit(NaryExpression x) {
        String newname = makename(x); if (newname==null) return;
        String[] list = new String[x.size()];
        for(int i=0; i<list.length; i++)  list[i] = make(x.child(i));
        
        printer.pushDeclaration("Expression " + newname + ";" );
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s=Expression.compose(ExprOperator.", newname));
        
        //file.printf("Expression %s=Expression.compose(ExprOperator.", newname);
		switch (x.op()) {
		case INTERSECTION:
			sb.append("INTERSECTION");
			//file.print("INTERSECTION");
			break;
		case OVERRIDE:
			sb.append("OVERRIDE");
			//file.print("OVERRIDE");
			break;
		case PRODUCT:
			sb.append("PRODUCT");
			//file.print("PRODUCT");
			break;
		case UNION:
			sb.append("UNION");
			//file.print("UNION");
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
		}
		for (int i = 0; i < list.length; i++) {
			sb.append(String.format(", %s", list[i]));
			//file.printf(", %s", list[i]);
		}
		sb.append(");");
		printer.pushStatement(sb.toString());
		//file.printf(");%n");
    }

    /** {@inheritDoc} */
    public void visit(NaryIntExpression x) {
        String newname = makename(x); if (newname==null) return;
        String[] list = new String[x.size()];
        for(int i=0; i<list.length; i++)  list[i] = make(x.child(i));
        
        printer.pushDeclaration("IntExpression " + newname + ";");
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("%s=IntExpression.compose(IntOperator.", newname));
        //file.printf("IntExpression %s=IntExpression.compose(IntOperator.", newname);
		switch (x.op()) {
		case PLUS:
			sb.append("PLUS");
			//file.print("PLUS");
			break;
		case MULTIPLY:
			sb.append("MULTIPLY");
			//file.print("MULTIPLY");
			break;
		case AND:
			sb.append("AND");
			//file.print("AND");
			break;
		case OR:
			sb.append("OR");
			//file.print("OR");
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
		}
		for (int i = 0; i < list.length; i++) {
			sb.append(String.format(", %s", list[i]));
			//file.printf(", %s", list[i]);
		}
		sb.append(");");
		printer.pushStatement(sb.toString());
		//file.printf(");%n");
    }

    /** {@inheritDoc} */
    public void visit(NaryFormula x) {
        String newname = makename(x); if (newname==null) return;
        String[] list = new String[x.size()];
        for(int i=0; i<list.length; i++)  list[i] = make(x.child(i));
        
        printer.pushDeclaration("Formula " + newname + ";");
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("%s=Formula.compose(FormulaOperator.", newname));
        //file.printf("Formula %s=Formula.compose(FormulaOperator.", newname);
		switch (x.op()) {
		case AND:
			sb.append("AND");
			//file.print("AND");
			break;
		case OR:
			sb.append("OR");
			//file.print("OR");
			break;
		default:
			throw new RuntimeException("Unknown kodkod operator \"" + x.op()
					+ "\" encountered");
		}
		
		for (int i = 0; i < list.length; i++) {
			sb.append(String.format(", %s", list[i]));
			//file.printf(", %s", list[i]);
		}
		sb.append(");");
		printer.pushStatement(sb.toString());
		//file.printf(");%n");
    }
}
