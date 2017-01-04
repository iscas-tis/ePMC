
using namespace std;

#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"

#include "lang/Node.h"
#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"

using namespace util;

#include "FOCIParser.h"

/******************************************************/
/******************* FOCI parser **********************/

namespace dp {

const char _LBRACK = '[';
const char _RBRACK = ']';
const char _LPAREN = '(';
const char _RPAREN = ')';
const char _SEMICOLON = ';';
const char _PLUS = '+';
const char _TIMES = '*';
const char _DASH = '-'; // for ->
const char _AND  = '&';
const char _OR   = '|';
const char _EQ   = '=';
const char _NOT  = '~';
const char _LEQ  = '<'; // for <=
const char _EOF  = (char) 0;

FOCIParser::FOCIParser(const map<std::string,CVC3::Expr>& __symbol_table) : symbol_table(__symbol_table) {}



void FOCIParser::parseTermList(vector<CVC3::Expr>& term_lst) {
	while ( *current != _RBRACK) {
		term_lst.push_back(parseTerm());
	}
}


void FOCIParser::parseFormulaList(vector<CVC3::Expr>& term_lst) {
	while ( *current != _RBRACK) {
		term_lst.push_back(parseFormula());
	}
}

bool FOCIParser::is_whitespace(char token) {
	switch(token) {
		case ' ': case '\n': case '\r': case '\t': return true;
	}
	return false;
}

bool FOCIParser::is_ctrlChar(char token) {
	switch(token) {
		case _LBRACK : case _RBRACK :   case _LPAREN :
		case _RPAREN : case _SEMICOLON: case _PLUS  :
		case _TIMES :  case _DASH :     case _AND :
		case _OR  :    case _EQ  :      case _NOT :
		case _LEQ :    case _EOF :      return true; break;
	}
	return false;
}

void FOCIParser::next_char() {
	if(current != in_stream.end()) {
		seen += *current; //debugging code
		++current;
	}
}

void FOCIParser::next_token() {
	next_char();
	while (is_whitespace(*current)) next_char();
}

// to use after buffer_to_next_ws
void FOCIParser::next_token2() {
	while (is_whitespace(*current)) next_char();
}

// returns the chars until the next whitespace or the next control char
std::string FOCIParser::buffer_to_next_ws() {
	std::string buffer;

	buffer += *current;
	next_char();
	while( !is_whitespace(*current) && !is_ctrlChar(*current) && current != in_stream.end()) {
		buffer += *current;
		next_char();
	}
	return buffer;
}

CVC3::Expr FOCIParser::parseTerm() {

	switch(*current) {

		case '(': {
			next_token();
			CVC3::Expr f = parseTerm();
			if(*current=='*') {
				next_token();
				CVC3::Expr g = parseTerm();
				f = lang::vc.multExpr(f,g);
			}

			if(*current == ')') {
				next_token();
				return f;
			} else {
				throw RuntimeError("FOCIParser::parseTerm: mismatched '(' and ')' in "+seen+ " : "+in_stream);
			}
		}
		break;
		case '+': {
			next_token();
			if( *current != _LBRACK) {
				throw RuntimeError("FOCIParser::parseTerm: missing '[' in "+seen);
			}
			next_token();
			vector<CVC3::Expr> f_lst;
			parseTermList(f_lst);
			if( * current != _RBRACK) {
				throw RuntimeError("FOCIParser::parseTerm: missing ']' in "+seen);
			}

			next_token();
			return lang::ExprManager::Sum(f_lst);
		}
		break;
		case '-': {
			next_token();
			CVC3::Expr term = parseTerm();
			return lang::vc.uminusExpr(term);
		}
		break;
		case '*': {
			next_token();
			std::string n_str = buffer_to_next_ws();
			int n = util::stringToInt(n_str);
			next_token2();
			CVC3::Expr term = parseTerm();
			return lang::vc.multExpr(lang::vc.ratExpr(n,1),term);
		}
		break;
		case '#': {
			next_char();
			int n = util::stringToInt(buffer_to_next_ws());
			next_token2();
			CVC3::Expr t = parseTerm();
			expr_table[n] = t;
			return t;
		}
		break;
		case '@': {
			next_char();
			int n = util::stringToInt(buffer_to_next_ws());
			next_token2();
			return expr_table[n];
		}
		break;
		default: { // variable or individual term or constant

			std::string sym = buffer_to_next_ws();
			next_token2();
			if( *current != _LBRACK ) {
				// individual term or constant
				if(isdigit( sym[0] )) { // must be a constant
					return lang::vc.ratExpr(sym,10);
				} else { // must be a variable
					return symbol_table[sym];
				}

			} else {
				next_token();
				// uninterpreted function symbol
				vector<CVC3::Expr> arg;
				parseTermList(arg);

				if( *current != _RBRACK ) {
					throw RuntimeError("FOCIParser::parseTerm: missing ']' in "+seen);
					return lang::vc.trueExpr();
					} else {
					next_token();
					throw RuntimeError("FOCIParser::parseTerm: uninterpreted functions not yet supported");
					return lang::vc.trueExpr();
				}

			}

		}
		break;
	}
}

CVC3::Expr FOCIParser::parseFormula() {
	switch( *current ) {
		case '(': {

			next_token();
			CVC3::Expr f = parseFormula();
			if( * current == ')') {
				next_token();
				return f;
			} else {
				throw RuntimeError("FOCIParser::parseFormula: mismatched '(' and ')' in "+seen);
			}
		}
		break;
		case '=': {
			next_token();
			CVC3::Expr t1 = parseTerm();
			CVC3::Expr t2 = parseTerm();
			return lang::vc.eqExpr(t1,t2);
		}
		break;
		case '<': {
			next_char();
			if( *current != '=') {
				throw RuntimeError("FOCIParser::parseFormula: expected char '=' in "+seen);
			}
			next_token();
			CVC3::Expr t1 = parseTerm();
			CVC3::Expr t2 = parseTerm();
			return lang::vc.leExpr(t1,t2);
		}
		break;
		case '&': {
			next_token();
			if( *current != _LBRACK) {
				throw RuntimeError("FOCIParser::parseFormula: missing '[' in "+seen);
			}
			next_token();
			vector<CVC3::Expr> f_lst;
			parseFormulaList(f_lst);
			if( * current != _RBRACK) {
				throw RuntimeError("FOCIParser::parseFormula: missing ']' in "+seen);
			}
			next_token();
			return lang::ExprManager::Conjunction(f_lst);
		}
		break;
		case '|': {
			next_token();
			if( *current != _LBRACK) {
				throw RuntimeError("FOCIParser::parseFormula: missing '[' in "+seen);
			}
			next_token();
			vector<CVC3::Expr> f_lst;
			parseFormulaList(f_lst);
			if( * current != _RBRACK) {
				throw RuntimeError("FOCIParser::parseFormula: missing ']' in "+seen);
			}
			next_token();
			return lang::ExprManager::Disjunction(f_lst);
		}
		break;
		case '~': {
			next_token();
			return lang::vc.notExpr(parseFormula());
		}
		break;
		case '-': {
			next_char();
			if( *current != '>') {
				throw RuntimeError("FOCIParser::parseFormula: syntax error (->) in "+seen);
			}
			next_token();
			CVC3::Expr t1 = parseFormula();
			CVC3::Expr t2 = parseFormula();
			return lang::vc.impliesExpr(t1,t2);
		}
		break;
		case '#': {
			next_char();
			int n = util::stringToInt(buffer_to_next_ws());
			next_token2();
			CVC3::Expr t = parseFormula();
			expr_table[n] = t;
			return t;
		}
		break;
		case '@': {
			next_char();
			int n = util::stringToInt(buffer_to_next_ws());
			next_token2();
			return expr_table[n];
		}
		break;

		default: {
			std::string var = buffer_to_next_ws();
			next_token2();
			if(var == "true") {
				return lang::vc.trueExpr();
			} else if(var =="false") {
				return lang::vc.falseExpr();
			} else {
				if(symbol_table.find(var)==symbol_table.end()) {
					MSG(0,"variable "+var+" not found\n");
				}
				return symbol_table[var];
			}

		}
		break;
	}

}

void FOCIParser::parseSequence(std::string input, std::vector<CVC3::Expr>& result) {
	in_stream = input;
	current = in_stream.begin();

	while( *current != _EOF ) {

		result.push_back(parseFormula());
		if( * current == _SEMICOLON ) {
			next_token();
		} else break;
	}
}


}
