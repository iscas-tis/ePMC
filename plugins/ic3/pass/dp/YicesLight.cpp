/**************************** CPPFile ***************************

* FileName [YicesLight.cpp]

* PackageName [parser]

* Synopsis [Implementation of YicesLight]

* Description [These classes encapsulate Yices SMT solver.]

* SeeAlso []

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2006 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/
using namespace std;


#include <boost/spirit/core.hpp>
#include <boost/spirit/actor/push_back_actor.hpp>
#include <boost/spirit/actor/clear_actor.hpp>
using namespace boost::spirit;

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


using namespace lang;
using namespace util;
#include "SMT.h"
#include "YicesLight.h"

namespace dp {

/*********************************************************************/
//initialize the solver and create a context
/*********************************************************************/
YicesLight::YicesLight() {
	if(util::Database::DEBUG) {
		//yicesl_enable_type_checker(1);
		//send("(set-verbosity! 2)");
		yicesl_enable_log_file("yices.log");
	}
	context = yicesl_mk_context();
	send("(set-evidence! true)");

}

void YicesLight::parseReply(char const* str)
    {
        parse(str,

            //  Begin grammar
            (
                  str_p  ("sat")[assign_a(status,l_true)]
		^ str_p  ("unsat")[assign_a(status,l_false)]
		^ (str_p ("unsat core ids:")[clear_a(unsat_core)] >> +(' ' >> int_p[push_back_a(unsat_core)]))
		^ str_p  ("searching...")
		^ str_p  ("id: ") >> int_p[assign_a(id)]
            )
            ,
            //  End grammar

            eol_p).full;
    }

int YicesLight::send(const std::string& message) { return send((char*)message.c_str());}
int YicesLight::send(char* message) {
	int result = yicesl_read(context,message);
	if(result==0) {
		std::string yicesl_error( yicesl_get_last_error_message  ( ));
		throw util::RuntimeError("YicesLight::talk(): YicesLight failed: " + yicesl_error);
	}
	return result;
}


/** \brief talk with Yices */
int YicesLight::talk(const std::string& message) {
	std::string line;
	std::stringstream stream;
	// redirect stdout
	std::streambuf* buf_stdout = std::cout.rdbuf();
	std::streambuf* buf_stderr = std::cerr.rdbuf();
	std::cout.rdbuf(stream.rdbuf());
	std::cerr.rdbuf(stream.rdbuf());

	// issue message
	int result = send(message);



	// read reply
	while (! stream.eof() ) {
		getline (stream,line);
		parseReply(line.c_str());
	}
	// put stdout back
	std::cout.rdbuf(buf_stdout);
	std::cerr.rdbuf(buf_stderr);

	return result;
}

/*********************************************************************/
//create a new logical context
/*********************************************************************/
void YicesLight::pushContext() {
	send("(push)");
}

/*********************************************************************/
//pop the current context deleting its entire context
//Warning: Contraints defined in that context become invalid
/*********************************************************************/
void YicesLight::popContext() {
	send("(pop)");
	declared_vars.clear();
}

/*! \brief get the unsat core */
lbool YicesLight::getUnsatCore(const vector<CVC3::Expr>& vec, vector<CVC3::Expr>& core ) {

	pushContext();

	HashMap<int,CVC3::Expr> expr_map;
	for(unsigned i=0; i<vec.size();++i) {
		int id(assertPlus(vec[i]));
		expr_map[id] = vec[i];
	}
	talk("(check)");

	switch(status) {

		case l_true:
			break;
		case l_false:
			for(unsigned int i=0;i<unsat_core.size();++i) {
				HashMap<int,CVC3::Expr>::const_iterator hit = expr_map.find(unsat_core[i]);
				if(hit!=expr_map.end())
					core.push_back(hit->second);
				else {
					std::string result;
					for(HashMap<int,CVC3::Expr>::const_iterator hit = expr_map.begin();
									            hit!= expr_map.end(); ++hit) {
						result += util::intToString(hit->first) + "->"
						       +  hit->second.toString() + "\n";
					}
					throw util::RuntimeError("YicesLight::getUnsatCore: unmatched unsat core id "+util::intToString(unsat_core[i]) + "\n " + result );
				}
			}
			break;
		case l_undef:
			break;
	}
	popContext();
	return status;
}

int YicesLight::assertPlus(const CVC3::Expr& e) {
	static int assertion_counter = 1;
	std::string query ("(assert+ "+toString(e)+")");
	talk(query);
	id = assertion_counter++;
	return id;
}

/*********************************************************************/
//pop the current context deleting its entire context
//Warning: Constraints defined in that context become invalid
/*********************************************************************/
void YicesLight::resetContext() {
	send("(reset)");
}

void YicesLight::dumpContext() {
	send("(dump-context)");
}

/*********************************************************************/
//check satisifiability
/*********************************************************************/
lbool YicesLight::Solve() {
	send("(check)");
	return l_undef;
}


/*! \brief get the model of the last satisfiable query
    \param vars variables of interest */
int YicesLight::getModel(const vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model) {
	// maybe later
	return 1;
}


lbool YicesLight::Check() {
	send("(check)");

	//TODO interpret result
	return l_undef;
}



bool YicesLight::proveEquivalent(const CVC3::Expr& e1,const CVC3::Expr& e2) {
	pushContext();

//TODO
/*	yices_expr ye1 = (*this) (e1);
	yices_expr ye2 = (*this) (e2);
	yices_expr eqi = yices_mk_eq(context,ye1,ye2);
	yices_expr neq = yices_mk_not(context,eqi);
	yices_assert(context,neq); */
	lbool result = Solve();
	/* assert(result != l_undef);*/
	popContext();
	switch(result) {
		case l_true: return false;
		case l_false: return true;
		default: return false;
	}
	return false;
}




/*********************************************************************/
//Solver still in consistent state
//(otherwise the formula is UNSAT and the solver should not
// be used anymore)
/*********************************************************************/
bool YicesLight::isOkay() {
	return !yicesl_inconsistent(context);
}




/*********************************************************************/
//assert an expression
/*********************************************************************/
void YicesLight::Assert(const CVC3::Expr& e) {
	std::string str = toString(e);
	send("(assert "+ str + ")");
}

/*********************************************************************/
//Block the current assignment
/*********************************************************************/
void YicesLight::Block() {
//TODO
// 	//check if SAT
// 	if(!isOkay()) return; //instance is not SAT
// 	yices_var_decl_iterator it = yices_create_var_decl_iterator(context);
// 	yices_model m              = yices_get_model(context);
// 	vector<yices_expr> clause;
// 	while (yices_iterator_has_next(it)) {
// 		yices_var_decl d         = yices_iterator_next(it);
//
// 		yices_expr var = yices_mk_var_from_decl(context,d);
// 		switch(yices_get_value(m, d)) {
// 			case l_true: clause.push_back(yices_mk_not(context,var)); break;
// 			case l_false: clause.push_back(var); break;
// 			case l_undef:  break;
// 		 }
// 	}
// 	yices_del_iterator(it);
//
// 	//nothing to block
// 	if(clause.size()==0) return;
//
// 	yices_expr blocking_clause[clause.size()];
// 	for(size_t i = 0; i<clause.size(); ++i)
// 		blocking_clause[i] = clause[i];
//
// 	yices_assert(context,yices_mk_or(context,blocking_clause,clause.size()));
}


void YicesLight::getValue(const CVC3::Expr& e, CVC3::Expr& value) {
// TODO
// 	switch(e.getKind()) {
// 		case CVC3::UCONST:
// 		{
// 			const char* id_cstr  = e.getName().c_str();
// 			yices_var_decl decl = yices_get_var_decl_from_name(context, (char*) id_cstr);
// 			if(!decl) return;
//
// 			switch(e.getType().getExpr().getKind()) {
// 				case CVC3::BITVECTOR: {
// 						throw util::RuntimeError("YicesLight::getValue(): This feature is currently not availabe for bitvectors.");
// 					}
// 					break;
// 				case CVC3::INT: {
// 					long val;
// 					int return_code = yices_get_int_value(m,decl,&val);
// 					if(return_code==0) {
// 						throw util::RuntimeError("YicesLight::getValue(): not a proper declaration, no value assigned in model, or value can't be converted to long.");
// 					}
// 					value = lang::vc.ratExpr(val,1);
// 					}
// 					break;
// 				case CVC3::REAL: {
// 					long num, den;
// 					int return_code = yices_get_arith_value(m,decl,&num,&den);
// 					if(return_code==0) {
// 						throw util::RuntimeError("YicesLight::getValue(): not a proper declaration, no value assigned in model, or value can't be converted to long.");
// 					}
// 					value = lang::vc.ratExpr(num,den);
// 					}
// 					break;
// 				case CVC3::BOOLEAN: {
// 					lbool val = yices_get_value(m,decl);
// 					if(val == l_undef) {
// 						throw util::RuntimeError("YicesLight::getValue(): boolean variable is not assigned to in model.");
// 					}
//
// 					value = val == l_true ? lang::vc.trueExpr() : lang::vc.falseExpr();
// 					}
// 					break;
// 			}
// 			break;
// 		}
// 		default:
// 			throw util::RuntimeError("YicesLight::getValue(): Can only get value of a variable not an arbitrary expression.");
// 			break;
//
// 	}
}

std::string YicesLight::toString(const CVC3::Expr& e) {

	/* visit CVC3 expression here */
	std::string result;

	std::vector<std::string> ykids(e.arity());

	/* start with child nodes */
	if(e.arity()>0) {
		const std::vector< CVC3::Expr > & kids = e.getKids();
		for(unsigned i = 0; i<kids.size(); ++i) {
			ykids[i] = toString(kids[i]);

		}
	}

	switch(e.getKind()) {
		case CVC3::TRUE_EXPR:
			result = "true";
			break;
		case CVC3::FALSE_EXPR:
			result = "false";
			break;
		case CVC3::ID:
		case CVC3::UCONST:
		{
			result = Lookup(e);
			if(result != "") break;
			result = e.getName();
			Log(e,result);
			std::string type;
			switch(e.getType().getExpr().getKind()) {
				case CVC3::BITVECTOR: {
					//int size = e.getType().getExpr()[0].getRational().getInt();

					}
					break;
				case CVC3::INT: {
					type  = "int";
					}
					break;
				case CVC3::REAL: {
					type  = "real";
					}
					break;
				case CVC3::BOOLEAN: {
					type  = "bool";
					}
					break;
			}
			send("(define "+result+"::"+type + ")");
			break;
		}
		/* Let requires extra treatment as it binds variables
		   in subexpressions. These variables have to be handled
		   either by replacement or by introducing them into the
		   translation table. Out of laziness, we do the former. */
		case CVC3::LET:
			cout<<"LET expression"<<endl;

			break;
		case CVC3::RATIONAL_EXPR:
			result = e.getRational().toString();
			break;
		case CVC3::ITE:
			result = "(ite " + ykids[0] + " "+ ykids[1] + " " + ykids[2] + ")";
			break;
		case CVC3::NOT:
			result = "(not " + ykids[0] + ")";
			break;
		case CVC3::AND:
			result = "(and ";
			for(unsigned i = 0; i<ykids.size(); ++i) {
				result += ykids[i] + " ";
			}
			result +=")";
			break;
		case CVC3::OR:
			result = "(or ";
			for(unsigned i = 0; i<ykids.size(); ++i) {
				result += ykids[i] + " ";
			}
			result +=")";
			break;
		case CVC3::XOR:

			result = "(or ";
			for(unsigned i = 0; i<ykids.size(); ++i) {
				result += "(not " + ykids[i] + ") ";
			}
			result +=")";
			break;
		case CVC3::IMPLIES:
			result = "(=> " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::IFF:
			result = "(= " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::EQ:
			result = "(= " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::NEQ:
			result = "(/= " + ykids[0] + " "+ ykids[1] + ")";
			break;

		/* arithmetic */
		case CVC3::LT:
			result = "(< " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::GT:
			result = "(> " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::LE:
			result = "(<= " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::GE:
			result = "(>= " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::PLUS:
			result = "(+ ";
			for(unsigned i = 0; i<ykids.size(); ++i) {
				result += ykids[i] + " ";
			}
			result +=")";
			break;
		case CVC3::MINUS:
			result = "(- " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::UMINUS:
			result = "(- 0 " + ykids[0] + ")";
			break;
		case CVC3::MULT:
			result = "(* " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::DIVIDE:
			result = "(/ " + ykids[0] + " "+ ykids[1] + ")";
			break;
		case CVC3::MOD:
			result = "(% " + ykids[0] + " "+ ykids[1] + ")";
			break;

		/* bitvector operations */
		case CVC3::BVCONST:
		case CVC3::CONCAT:
		case CVC3::EXTRACT:
		case CVC3::BOOLEXTRACT:
		case CVC3::LEFTSHIFT:
		case CVC3::CONST_WIDTH_LEFTSHIFT:
		case CVC3::RIGHTSHIFT:
		case CVC3::BVSHL:
		case CVC3::BVLSHR:
		case CVC3::BVASHR:
		case CVC3::SX:
		case CVC3::BVREPEAT:
		case CVC3::BVZEROEXTEND:
		case CVC3::BVROTL:
		case CVC3::BVROTR:
		case CVC3::BVAND:
		case CVC3::BVOR:
		case CVC3::BVXOR:
		case CVC3::BVXNOR:
		case CVC3::BVNEG:
		case CVC3::BVNAND:
		case CVC3::BVNOR:
		case CVC3::BVCOMP:
		case CVC3::BVUMINUS:
		case CVC3::BVPLUS:
		case CVC3::BVSUB:
		case CVC3::BVMULT:
		case CVC3::BVUDIV:
		case CVC3::BVSDIV:
		case CVC3::BVUREM:
		case CVC3::BVSREM:
		case CVC3::BVSMOD:
		case CVC3::BVLT:
		case CVC3::BVLE:
		case CVC3::BVGT:
		case CVC3::BVGE:
		case CVC3::BVSLT:
		case CVC3::BVSLE:
		case CVC3::BVSGT:
		case CVC3::BVSGE:
		case CVC3::INTTOBV:
		case CVC3::BVTOINT:
		default:
			MSG(0,"unsupported expression "+vc.getEM()->getKindName(e.getKind()));
			break;



			break;
	}
	return result;
}

/*********************************************************************/
//look up encoding of an expression
/*********************************************************************/
std::string YicesLight::Lookup(const CVC3::Expr& e) {
	CVC3::ExprHashMap<std::string>::iterator i = declared_vars.find(e);
	if(i!=declared_vars.end()) return i->second;
	return "";
}

/*********************************************************************/
//remember an encoded expression
/*********************************************************************/
void YicesLight::Log(const CVC3::Expr& e, const std::string& name) {
	declared_vars[e] = name;
}

void YicesLight::Dump() {
}

YicesLight::~YicesLight() {
  yicesl_del_context(context);  
}

} //end of dp
