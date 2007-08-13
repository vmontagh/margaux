/*
 * Alloy Analyzer
 * Copyright (c) 2007 Massachusetts Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */

package edu.mit.csail.sdg.alloy4compiler.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.ConstList.TempList;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.SIGINT;
import static edu.mit.csail.sdg.alloy4compiler.ast.Type.EMPTY;

/**
 * Immutable; represents an unresolved node that has several possibilities.
 */

public final class ExprChoice extends Expr {

    /** The unmodifiable list of Expr(s) that this ExprChoice can refer to; this list always has 2 or more entries. */
    final ConstList<Expr> choices;

    /** Caches the span() result. */
    private Pos span=null;

    //============================================================================================================//

    /** {@inheritDoc} */
    @Override public Pos span() {
        Pos p=span;
        if (p==null) {
            p=pos;
            for(Expr a:choices) p=p.merge(a.span());
            span=p;
        }
        return p;
    }

    //============================================================================================================//

    /** {@inheritDoc} */
    @Override public void toString(StringBuilder out, int indent) {
        if (indent<0) {
            choices.get(0).toString(out,indent); // Each choice's textual form is probably similar, so the first one would do
        } else {
            for(int i=0; i<indent; i++) { out.append(' '); }
            out.append(""+choices.size()+" choices with combined type=").append(type).append('\n');
        }
    }

    //============================================================================================================//

    /** Generate an appropriate error message in the case where there are no legal choices. */
    private static Err complain(Pos pos, ConstList<Expr> choices) {
        StringBuilder sb=new StringBuilder("Name cannot be resolved; possible incorrect function/predicate call; " +
           "perhaps you used ( ) when you should have used [ ]\n");
        for(Expr x:choices) {
            pos = pos.merge(x.span());
            if (x instanceof ExprBadCall || x instanceof ExprBadJoin) sb.append('\n').append(x.errors.get(0).msg);
        }
        return new ErrorType(pos, sb.toString());
    }

    //============================================================================================================//

    /** Constructs an ExprChoice node. */
    private ExprChoice(Pos pos, ConstList<Expr> choices, Type type, long weight) {
        super(pos, true, type, 0, weight, emptyListOfErrors.appendIfNotNull(type==EMPTY ? complain(pos,choices) : null));
        this.choices = choices;
    }

    //============================================================================================================//

    /** Construct an ExprChoice node. */
    public static Expr make(Pos pos, ConstList<Expr> choices) {
        if (choices.size()==0) return new ExprBad(pos, "", new ErrorType(pos, "This expression failed to be typechecked."));
        if (choices.size()==1) return choices.get(0); // Shortcut
        TempList<Expr> legal = new TempList<Expr>(choices.size());
        Type type=EMPTY;
        for(Expr x:choices) if (x.errors.isEmpty()) { legal.add(x); type=x.type.merge(type); }
        if (legal.size()>0) choices=legal.makeConst();
        if (choices.size()==1) return choices.get(0);
        // At this point, either every choice is legal... or every choice is illegal
        long weight=choices.get(0).weight;
        for(int i=1; i<choices.size(); i++) if (weight > choices.get(i).weight) weight = choices.get(i).weight;
        return new ExprChoice(pos, choices, type, weight);
    }

    //============================================================================================================//

    /** {@inheritDoc} */
    @Override public Expr resolve(Type t, Collection<ErrorWarning> warns) {
        if (errors.size()>0) return this;
        List<Expr> match=new ArrayList<Expr>(choices.size());
        // We first prefer exact matches
        for(Expr ch:choices) {
            Type tt=ch.type;
            if ((t.is_int && tt.is_int) || (t.is_bool && tt.is_bool) || t.intersects(tt)) match.add(ch);
        }
        // If none, we try any legal matches
        if (match.size()==0) {
            for(Expr ch:choices) if (ch.type.hasCommonArity(t)) match.add(ch);
        }
        // If none, we try sigint->int
        if (match.size()==0 && Type.SIGINT2INT && t.is_int) {
            for(Expr ch:choices) if (ch.type.intersects(SIGINT.type)) match.add(ch.cast2int());
        }
        // If none, we try int->sigint
        if (match.size()==0 && Type.INT2SIGINT && t.hasArity(1)) {
            for(Expr ch:choices) if (ch.type.is_int) match.add(ch.cast2sigint());
        }
        // If there are multiple choices, then keep the choices with the smallest weight
        if (match.size()>1) {
            List<Expr> newmatch=new ArrayList<Expr>(match.size());
            long w=0;
            for(Expr x:match) {
                if (newmatch.size()==0 || x.weight<w) { newmatch.clear(); newmatch.add(x); w=x.weight; }
                else if (x.weight==w) { newmatch.add(x); }
            }
            match=newmatch;
        }
        // If we are down to exactly 1 match, return it
        if (match.size()==1) return match.get(0).resolve(t, warns);
        // Otherwise, complain!
        String txt;
        if (match.size()>1)
            txt="\nThe expression is ambiguous due to multiple matches:";
        else {
            txt="\nThe expression cannot be resolved; its relevant type does not intersect with any of the following candidates:";
            match=choices;
        }
        StringBuilder msg=new StringBuilder(txt);
        for(Expr ch:match) { msg.append("\n\n"); ch.toString(msg,-1); msg.append(" (type: ").append(ch.type).append(")"); }
        Pos span=span();
        return new ExprBad(span, toString(), new ErrorType(span, msg.toString()));
    }

    //============================================================================================================//

    /** {@inheritDoc} */
    @Override Object accept(VisitReturn visitor) throws Err {
        if (!errors.isEmpty()) throw errors.get(0);
        throw new ErrorType(span(), "This expression failed to be resolved.");
    }
}