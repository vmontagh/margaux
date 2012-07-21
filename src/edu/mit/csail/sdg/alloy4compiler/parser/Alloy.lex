// Alloy Analyzer 4 -- Copyright (c) 2006-2008, Felix Chang
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
// (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
// merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
// OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package edu.mit.csail.sdg.alloy4compiler.parser;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Version;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import java.util.List;
import java_cup.runtime.*;

/** Autogenerated by JFlex 1.4.1 */

%%

// There are 3 sets of "special tokens" that the lexer will not output.
// But the Parser expects them.
// So a special Filter class is written that sits between Lexer and Parser.
// The Filter class observes the stream of tokens, and intelligently
// merges or changes some primitive tokens into special tokens.
// For more details, refer to the main documentation.
//
// But, very briefly, here are the 3 groups:
//
// (1) The lexer will generate only ALL, NO, LONE, ONE, SUM, SOME.
// It will not output ALL2, NO2, LONE2, ONE2, SUM2, SOME2.
// (The Filter class will change some ONE into ONE2, etc)
//
// (2) The lexer won't output NOTEQUALS, NOTIN, NOTLT, NOTLTE, NOTGT, NOTGTE.
// Instead it outputs them as separate tokens (eg. "NOT" "EQUALS").
// (The Filter class is used to merge them into a single "NOTEQUALS" token)
//
// (3) The lexer willn't output the 15 special arrows (eg. ONE_ARROW_ONE)
// Instead it outputs them as separate tokens (eg. "ONE", "ARROW", "ONE")
// (The Filter class is used to merge them into a single "ONE_ARROW_ONE" token)

%class CompLexer // The ordering of these directives is important
%cupsym CompSym
%cup
%eofval{
  return new Symbol(CompSym.EOF, alloy_here(" "), alloy_here(" "));
%eofval}
%public
%final
%unicode
%line
%column
%pack

%{

// The following declarations will appear in CompLexer
public String alloy_filename = "";
public int alloy_lineoffset = 0; // If not zero, it is added to the current LINE NUMBER
public List<Object> alloy_seenDollar;
public CompModule alloy_module;

// The following methods are added to the resulting lexer (CompLexer). They are used for auxiliary purposes;
// i.e., they are called when tokens are matched (see lexical specification at the bottom of this file).

// This method returns a new Pos object, which stores information about the position of the
// matched token (filename, starting col, starting row, ending col, ending row)
private final Pos alloy_here(String txt) {
	return new Pos(alloy_filename, yycolumn + 1, yyline + 1 + alloy_lineoffset, yycolumn + txt.length(), yyline+1);
}

// This method returns a Java CUP Symbol. First, it creates a new Pos object, which is then passed to
// a newly instantiated Symbol object that ultimately gets returned by this method
private final Symbol alloy_sym(String txt, int type) {
	Pos p = alloy_here(txt); return new Symbol(type, p, p);
}

// This method appears to check whether or not a string spans multiple lines or if it ends with a slash.
// Also, for some mysterious reason, it changes an "n" to a "\n", and it throws some funky errors.
// It's not clear why ExprConstant is being referred to here, since this is the lexer, which probably
// shouldn't be doing anything with the AST.
private final Symbol alloy_string(String txt) throws Err {
	Pos p = alloy_here(txt);
	if (!Version.experimental) throw new ErrorSyntax(p, "String literal is not currently supported.");
	StringBuilder sb = new StringBuilder(txt.length());
	
	for(int i = 0; i < txt.length(); i++) {
		char c = txt.charAt(i);
		
		if (c == '\r' || c == '\n')
			throw new ErrorSyntax(p, "String literal cannot span multiple lines; use \\n instead.");
		
		if (c == '\\') {
			i++;
			if (i >= txt.length())
				throw new ErrorSyntax(p, "String literal cannot end with a single \\");
			c = txt.charAt(i);
			if (c == 'n')
				c = '\n';
			else if (c != '\'' && c != '\"' && c != '\\')
				throw new ErrorSyntax(p, "String literal currenty only supports\nfour escape sequences: \\\\, \\n, \\\', and \\\"");
		}
		sb.append(c);
	}
	txt = sb.toString();
	if (txt.length() == 2)
		throw new ErrorSyntax(p, "Empty string is not allowed; try rewriting your model to use an empty set instead.");
	return new Symbol(CompSym.STR, p, ExprConstant.Op.STRING.make(p, txt));
}

private final Symbol alloy_id(String txt) throws Err {
	Pos p = alloy_here(txt);
	if (alloy_seenDollar.size() == 0 && txt.indexOf('$') >= 0)
		alloy_seenDollar.add(null);
	return new Symbol(CompSym.ID, p, ExprVar.make(p,txt));
}

// This method handles the case of a number being scanned by the lexer.
private final Symbol alloy_num(String txt) throws Err {
	Pos p = alloy_here(txt);
	int n = 0;
	try {
		n = Integer.parseInt(txt);
	} catch(NumberFormatException ex) {
		throw new ErrorSyntax(p, "The number " + txt + " is too large to be stored in a Java integer");
	}
	return new Symbol(CompSym.NUMBER, p, ExprConstant.Op.NUMBER.make(p, n));
}

%}

%%

"!"                   { return alloy_sym(yytext(), CompSym.NOT         );}
"#"                   { return alloy_sym(yytext(), CompSym.HASH        );}
"&&"                  { return alloy_sym(yytext(), CompSym.AND         );}
"&"                   { return alloy_sym(yytext(), CompSym.AMPERSAND   );}
"("                   { return alloy_sym(yytext(), CompSym.LPAREN      );}
")"                   { return alloy_sym(yytext(), CompSym.RPAREN      );}
"*"                   { return alloy_sym(yytext(), CompSym.STAR        );}
"++"                  { return alloy_sym(yytext(), CompSym.PLUSPLUS    );}
"+"                   { return alloy_sym(yytext(), CompSym.PLUS        );}
","                   { return alloy_sym(yytext(), CompSym.COMMA       );}
"->"                  { return alloy_sym(yytext(), CompSym.ARROW       );}
"-"                   { return alloy_sym(yytext(), CompSym.MINUS       );}
"."                   { return alloy_sym(yytext(), CompSym.DOT         );}
"/"                   { return alloy_sym(yytext(), CompSym.SLASH       );}
"::"                  { return alloy_sym(yytext(), CompSym.DOT         );}
":>"                  { return alloy_sym(yytext(), CompSym.RANGE       );}
":"                   { return alloy_sym(yytext(), CompSym.COLON       );}
"<=>"                 { return alloy_sym(yytext(), CompSym.IFF         );}
"<="                  { return alloy_sym(yytext(), CompSym.LTE         );}
"<:"                  { return alloy_sym(yytext(), CompSym.DOMAIN      );}
"<<"                  { return alloy_sym(yytext(), CompSym.SHL         );}
"<"                   { return alloy_sym(yytext(), CompSym.LT          );}
"=<"                  { return alloy_sym(yytext(), CompSym.LTE         );}
"=>"                  { return alloy_sym(yytext(), CompSym.IMPLIES     );}
"="                   { return alloy_sym(yytext(), CompSym.EQUALS      );}
">>>"                 { return alloy_sym(yytext(), CompSym.SHR         );}
">>"                  { return alloy_sym(yytext(), CompSym.SHA         );}
">="                  { return alloy_sym(yytext(), CompSym.GTE         );}
">"                   { return alloy_sym(yytext(), CompSym.GT          );}
"@"                   { return alloy_sym(yytext(), CompSym.AT          );}
"["                   { return alloy_sym(yytext(), CompSym.LBRACKET    );}
"]"                   { return alloy_sym(yytext(), CompSym.RBRACKET    );}
"^"                   { return alloy_sym(yytext(), CompSym.CARET       );}
"{"                   { return alloy_sym(yytext(), CompSym.LBRACE      );}
"||"                  { return alloy_sym(yytext(), CompSym.OR          );}
"|"                   { return alloy_sym(yytext(), CompSym.BAR         );}
"}"                   { return alloy_sym(yytext(), CompSym.RBRACE      );}
"~"                   { return alloy_sym(yytext(), CompSym.TILDE       );}
"abstract"            { return alloy_sym(yytext(), CompSym.ABSTRACT    );}
"all"                 { return alloy_sym(yytext(), CompSym.ALL         );}
"and"                 { return alloy_sym(yytext(), CompSym.AND         );}
"assert"              { return alloy_sym(yytext(), CompSym.ASSERT      );}
"as"                  { return alloy_sym(yytext(), CompSym.AS          );}
"inst"                { return alloy_sym(yytext(), CompSym.BOUND        );}
"but"                 { return alloy_sym(yytext(), CompSym.BUT         );}
"check"               { return alloy_sym(yytext(), CompSym.CHECK       );}
"disjoint"            { return alloy_sym(yytext(), CompSym.DISJ        );}
"disj"                { return alloy_sym(yytext(), CompSym.DISJ        );}
"else"                { return alloy_sym(yytext(), CompSym.ELSE        );}
"enum"                { return alloy_sym(yytext(), CompSym.ENUM        );}
"exactly"             { return alloy_sym(yytext(), CompSym.EXACTLY     );}
"exhaustive"          { return alloy_sym(yytext(), CompSym.EXH         );}
"exh"                 { return alloy_sym(yytext(), CompSym.EXH         );}
"expect"              { return alloy_sym(yytext(), CompSym.EXPECT      );}
"extends"             { return alloy_sym(yytext(), CompSym.EXTENDS     );}
"fact"                { return alloy_sym(yytext(), CompSym.FACT        );}
"for"                 { return alloy_sym(yytext(), CompSym.FOR         );}
"fun"                 { return alloy_sym(yytext(), CompSym.FUN         );}
"iden"                { return alloy_sym(yytext(), CompSym.IDEN        );}
"iff"                 { return alloy_sym(yytext(), CompSym.IFF         );}
"implies"             { return alloy_sym(yytext(), CompSym.IMPLIES     );}
"Int"                 { return alloy_sym(yytext(), CompSym.SIGINT      );}
"int"                 { return alloy_sym(yytext(), CompSym.INT         );}
"in"                  { return alloy_sym(yytext(), CompSym.IN          );}
"include"             { return alloy_sym(yytext(), CompSym.INCLUDE     );}
"let"                 { return alloy_sym(yytext(), CompSym.LET         );}
"lone"                { return alloy_sym(yytext(), CompSym.LONE        );}
"maximize"            { return alloy_sym(yytext(), CompSym.MAXIMIZE    );}
"minimize"            { return alloy_sym(yytext(), CompSym.MINIMIZE    );}
"module"              { return alloy_sym(yytext(), CompSym.MODULE      );}
"none"                { return alloy_sym(yytext(), CompSym.NONE        );}
"not"                 { return alloy_sym(yytext(), CompSym.NOT         );}
"no"                  { return alloy_sym(yytext(), CompSym.NO          );}
"objectives"          { return alloy_sym(yytext(), CompSym.OBJECTIVES  );}
"one"                 { return alloy_sym(yytext(), CompSym.ONE         );}
"open"                { return alloy_sym(yytext(), CompSym.OPEN        );}
"optimize"            { return alloy_sym(yytext(), CompSym.OPTIMIZE    );}
"or"                  { return alloy_sym(yytext(), CompSym.OR          );}
"partition"           { return alloy_sym(yytext(), CompSym.PART        );}
"part"                { return alloy_sym(yytext(), CompSym.PART        );}
"pred"                { return alloy_sym(yytext(), CompSym.PRED        );}
"private"             { return alloy_sym(yytext(), CompSym.PRIVATE     );}
"run"                 { return alloy_sym(yytext(), CompSym.RUN         );}
"seq"                 { return alloy_sym(yytext(), CompSym.SEQ         );}
"set"                 { return alloy_sym(yytext(), CompSym.SET         );}
"sig"                 { return alloy_sym(yytext(), CompSym.SIG         );}
"some"                { return alloy_sym(yytext(), CompSym.SOME        );}
"String"              { return alloy_sym(yytext(), CompSym.STRING      );}
"sum"                 { return alloy_sym(yytext(), CompSym.SUM         );}
"this"                { return alloy_sym(yytext(), CompSym.THIS        );}
"univ"                { return alloy_sym(yytext(), CompSym.UNIV        );}


[\"] ([^\\\"] | ("\\" .))* [\"] [\$0-9a-zA-Z_\'\"] [\$0-9a-zA-Z_\'\"]* { throw new ErrorSyntax(alloy_here(yytext()),"String literal cannot be followed by a legal identifier character."); }
[\"] ([^\\\"] | ("\\" .))* [\"]                                        { return alloy_string(yytext()); }
[\"] ([^\\\"] | ("\\" .))*                                             { throw new ErrorSyntax(alloy_here(yytext()),"String literal is missing its closing \" character"); }
[0-9][0-9]*[\$a-zA-Z_\'\"][\$0-9a-zA-Z_\'\"]*                          { throw new ErrorSyntax(alloy_here(yytext()),"Name cannot start with a number."); }
[0-9][0-9]*                                                            { return alloy_num (yytext()); }
[:jletter:][[:jletterdigit:]\'\"]*                                      { return alloy_id  (yytext()); }
//[\$a-zA-Z][\$0-9a-zA-Z_\'\"]*                                          { return alloy_id  (yytext()); }

"/**" ~"*/"                  { }

"/*" ~"*/"                   { }

("//"|"--") [^\r\n]* [\r\n]  { }

("//"|"--") [^\r\n]*         { } // This rule is shorter than the previous rule,
                                 // so it will only apply if the final line of a file is missing the \n or \r character.

[ \t\f\r\n]                  { }

. { throw new ErrorSyntax(alloy_here(" "), "Syntax error at the "+yytext()+" character."); }
