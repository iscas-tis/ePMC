D			[0-9]
L			[a-zA-Z_]
H			[a-fA-F0-9]
E			[Ee][+-]?{D}+
FS			(f|F|l|L)
IS			(u|U|l|L)*

%{
#include "util/Util.h"
#include "util/Error.h"
#include "AST.h"
using namespace AST;

#include "PRISM.tab.hpp"
#include "PRISMParser.h"

extern Substitution formulas;

extern void PRISMerror(char *s);
extern void PRISMerror(const std::string&);



namespace PRISM {
	void emit(int current_token = BOGY);
	void comment();
  int token;
  int check_type();

	extern Substitution formulas;
}
using namespace PRISM;

%}

%s line_directive
%%

"/*"			{ emit(); comment();         }
[ \t]+                  { emit();                    }
\/\/[^\n]*              { emit();                    }
"module"                { emit(MODULE);    return token; }
"endmodule"             { emit(ENDMODULE); return token; }
"input"                 { emit(INPUT);     return token; }
"const"                 { emit(CONSTANT);  return token; }
"global"			    { emit(GLOBAL);    return token; }
"param"                 { emit(PARAM);     return token; }
"formula"               { emit(FORMULA);   return token; }
"label"                 { emit(LABEL);   return token; }
"var"                   { emit(VAR);       return token; }
"init"                  { emit(INIT);      return token; }
"endinit"               { emit(ENDINIT);   return token; }
"invar"                 { emit(INVAR);     return token; }
"endinvar"              { emit(ENDINVAR);  return token; }
"pred"                  { emit(PRED);      return token; }
"float"			{ emit(FLOAT);     return token; }
"int"			{ emit(INTEGER);   return token; }
"bool"                  { emit(BOOL);      return token; }
"double"                { emit(DOUBLE);    return token; }
"bv"                    { emit(BV);        return token; }
"true"                  { emit(TRUE);      return token; }
"false"                 { emit(FALSE);     return token; }
"nondeterministic"      { emit(NONDET);    return token; }
"mdp"      { emit(NONDET);    return token; }
"stochastic"            { emit(STOCH);     return token; }
"dtmc"                  { emit(PROBAB);    return token; }
"ctmc"                  { emit(STOCH);      return token; }
"probabilistic"         { emit(PROBAB);    return token; }
"min"			{ emit(MIN_OP);    return token; }
"max"			{ emit(MAX_OP);    return token; }
"U"                     { emit(UNTIL);     return token; }
"X"                     { emit(NEXT);      return token; }
"F"                     { emit(FINALLY);   return token; }
"C"                     { emit(CUMULATIVE);return token; }
"S"                     { emit(STEADY);    return token; }
"Smax"                  { emit(STEADYMAX);    return token; }
"Smin"                  { emit(STEADYMIN);    return token; }
"P"                     { emit(PROB);      return token; }
"Pmax"                  { emit(PROBMAX);   return token; }
"Pmin"                  { emit(PROBMIN);   return token; }

"rewards"               { emit(REWARDS);   return token; }
"endrewards"            { emit(ENDREWARDS);return token; }
"I"                     { emit(I_REW);     return token; }
"R"                     { emit(REW);       return token; }
"Rmax"                  { emit(REWMAX);    return token; }
"Rmin"                  { emit(REWMIN);    return token; }

{L}({L}|{D})*		{ 
	emit(IDENTIFIER);  
	PRISMlval.string_const = strdup( PRISMtext ) ;
	return token; }
	
{D}+    		{ emit(INT_CONSTANT); 
			  PRISMlval.int_const = atoi(PRISMtext);
                          return token; }
".."                    { emit(DOTS);      return token; }
{D}+{E}{FS}?	        { emit(FLOAT_CONSTANT); 
			  PRISMlval.float_const = atof(PRISMtext);
                          return token; }
{D}*"."[^ "."]{D}+({E})?{FS}?	{ emit(FLOAT_CONSTANT); 
			   PRISMlval.float_const = atof(PRISMtext);
                           return token; }
{D}+"."[^ "."]{D}*({E})?{FS}?	{ emit(FLOAT_CONSTANT); 
			  PRISMlval.float_const = atof(PRISMtext);
                          return token; }

\"{D}+\"		{ emit(BV_CONSTANT); 
			  char *buf;
			  std::string str = buf = strdup( PRISMtext );
			  PRISMlval.bv_const.size = str.size()-2;
			  long long value = 0;
			  for(std::string::iterator i = str.begin(); i!=str.end(); ++i)
				switch(*i) {
					case '0': value *= 2; break;
					case '1': value = value * 2 + 1; break;
					break;
			        }
			  PRISMlval.bv_const.value = value;
			  delete buf;
                          return token; 
			}

"->"			{ emit(DISTR);     return token; }
"&"			{ emit('&');       return token; }
"|"			{ emit('|');       return token; }
"<="			{ emit(LE_OP);     return token; }
">="			{ emit(GE_OP);     return token; }
"=="			{ emit(EQ_OP);     return token; }
"!="			{ emit(NE_OP);     return token; }
";"			{ emit(';');       return token; }
("{"|"<%")		{ emit('{');       return token; }
("}"|"%>")		{ emit('}');       return token; }
","			{ emit(',');       return token; }
":"			{ emit(':');       return token; }
"="			{ emit('=');       return token; }
"("			{ emit('(');       return token; }
")"			{ emit(')');       return token; }
("["|"<:")		{ emit('[');       return token; }
("]"|":>")		{ emit(']');       return token; }
"."			{ emit('.');       return token; }
"!"			{ emit('!');       return token; }
"~"			{ emit('~');       return token; }
"-"			{ emit('-');       return token; }
"+"			{ emit('+');       return token; }
"*"			{ emit('*');       return token; }
"/"			{ emit('/');       return token; }
"%"			{ emit('%');       return token; }
"<"			{ emit('<');       return token; }
">"			{ emit('>');       return token; }
"^"			{ emit('^');       return token; }
"?"			{ emit('?');       return token; }
"\""        { emit(QUOTE);     return token; }
"\'"                    { emit(PRIMED);    return token;}
"\n"                    { line_number++; }

%%

int PRISMwrap() {
  return 1;
}

namespace PRISM {

void comment() {
    char c, c1;
	do {
		while ((c = yyinput()) != '*' && c != 0);
		if(c=='\n') ++line_number;
	}
    while ((c1 = yyinput()) != '/' && c != 0);
}


int column = 0;

inline void emit(int current_token) {
	static char c;
	for (unsigned i = 0; PRISMtext[i]!='\0'; ++i) {
		c = PRISMtext[i];
		switch(c) {
			case '\n': column = 0; ++line_number;  break;
			case '\t': column += 8 - (column % 8); break;
			default: column++; break;
		}
	}


	/* make a distinction whether this is 
	   part of distribution or not 
	*/
	static int  level = 0;
	static bool within_distr = false;

 	switch(current_token) {
		case '+':
	
			if(within_distr && level == 0 && token == '}') {
				token = DISTR_PLUS;
			} else {
				token = current_token;
			}
			break;
		case DISTR:
			within_distr = true;
			token = current_token;
			break;
		case ';':
			within_distr = false;
			token = current_token;
			break;
		case '(': 
			if(within_distr && level == 0)
				token = '{';
			else
				token = current_token;
			++level;
			break;
		case ')': 
			--level; 
			if(within_distr && level == 0)
				token = '}';
			else
				token = current_token;
			break;
		case BOGY:
			break;
		default:
			token = current_token;
			break;
	}
}

int foo() { return yyunput(42,0),2; }
}
