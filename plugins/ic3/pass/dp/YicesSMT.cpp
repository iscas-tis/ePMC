/**************************** CPPFile ***************************

* FileName [YicesSMT.cpp]

* PackageName [parser]

* Synopsis [Implementation of YicesSMT]

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
#include "YicesSMT.h"

namespace dp {

/*********************************************************************/
//initialize the solver and create a context
/*********************************************************************/
YicesSMT::YicesSMT() {
	context = yices_mk_context();
	yices_set_arith_only(1);
	//yices_enable_type_checker(true);
	std::string yices_version_string(yices_version());
	//yices_set_verbosity(2);
	//yices_enable_log_file("yices.log");
	expr_stack.resize(1);
}

YicesSMT::~YicesSMT() {
  yices_del_context(context);
}

/*********************************************************************/
//create a new logical context
/*********************************************************************/
void YicesSMT::pushContext() {
	if(Database::DUMP_PROOF_SCRIPT) yices_enable_log_file("yices.out");
	yices_push(context);
	expr_stack.resize(expr_stack.size()+1);
}

/*********************************************************************/
//pop the current context deleting its entire context
//Warning: Contraints defined in that context become invalid
/*********************************************************************/
void YicesSMT::popContext() {
	yices_pop(context);
	foreach(CVC3::Expr expr, expr_stack.back()) {
		expr2yexpr.erase(expr);
	}
	expr_stack.resize(expr_stack.size()-1);
}

/*********************************************************************/
//pop the current context deleting its entire context
//Warning: Constraints defined in that context become invalid
/*********************************************************************/
void YicesSMT::resetContext() {
	if(contexts.size()==1) cerr<<"Warning: popping root context"<<endl;
	if(contexts.size()==0) throw RuntimeError("Trying to pop context but stack is empty");
	expr2yexpr.clear();
	assert(context==contexts.back());
	yices_reset(context);
}

void YicesSMT::dumpContext() {
	MSG(0,"YicesSMT::dumpContext() (# of contexts %d\n",contexts.size());
	int counter = 0;
	for(vector<yices_context>::iterator i=contexts.begin();i!=contexts.end();++i) {
		cerr<<"level : "<<(counter++)<<endl;
		yices_dump_context(*i);
	}
}


/*! \brief get the model of the last satisfiable query
    \param vars variables of interest */
int YicesSMT::getModel(const vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model) {
	/* check if the last query was sat */
	if(!isOkay()) return 0;
	m = yices_get_model(context);
	for(size_t i=0;i<vars.size();++i) {
		CVC3::Expr variable = vars[i];
		CVC3::Type type = variable.getType();
		std::string variable_name = variable.toString();
		char* id_cstr = (char*) variable_name.c_str();

		yices_var_decl decl = yices_get_var_decl_from_name(context,id_cstr);

		if(!decl) {
			MSG(2,"YicesSMT::getModel: Warning variable "+variable_name+ " not found\n");
			continue;
		}

		CVC3::Expr value;

		switch(variable.getType().getExpr().getKind()) {
			case CVC3::BITVECTOR:
				break;
			case CVC3::INT: {
				long den;
				switch( yices_get_int_value (m,decl,&den) ) { //error handling
					case 0: // error
						break;
					case 1: // success
						value = vc.ratExpr(den,1);
						break;
				}
			}
			break;
			case CVC3::REAL: {
				long num, den;
				switch(yices_get_arith_value(m,decl,&num,&den)) {
					case 0:
						break;
					case 1:
						value = vc.ratExpr(num,den);
						break;
				}
			}
			break;
			case CVC3::BOOLEAN: {
				switch(yices_get_value(m,decl)) {
					case l_true:
						value = vc.trueExpr();
						break;
					case l_false:
						value = vc.falseExpr();
						break;
					case l_undef:
						break;
				}
			}
			break;
		}
		if(!value.isNull())
			model[variable] = value;
	}
	return 1;
}


lbool YicesSMT::Solve () {
	// invalidate the model
	m = 0;
	//invoke the solver
	return yices_check(context);
}

lbool YicesSMT::Check () {
	//invoke the solver
	return yices_check(context);
}

/*! \brief get the unsat core */
lbool YicesSMT::getUnsatCore(const vector<CVC3::Expr>& vec, vector<CVC3::Expr>& core ) {
	pushContext();

	HashMap<assertion_id,int> mapping;
	/* assert the expressions and retrieve the indices */
	for(unsigned i=0; i<vec.size(); ++i) {
		assertion_id id = yices_assert_retractable(context,(*this)(vec[i]));		
		mapping[id] = i;	
	}

	
	lbool yresult(yices_check(context));
	switch(yresult) {
		case l_true:
		case l_undef:
			return yresult;
		case l_false:
			break;		
	}

	unsigned size(yices_get_unsat_core_size(context));
	
	std::vector<assertion_id> unsat_core(size);

	unsigned size2 = yices_get_unsat_core(context,&unsat_core[0]);

	assert(size == size2);

	core.resize(size);

	for(unsigned i=0; i<size; ++i) {
		unsigned original_index(mapping[unsat_core[i]]);
		assert(original_index < vec.size());
		core[i] = vec[original_index];		
	}
	popContext();
	return l_false;
}



bool YicesSMT::proveEquivalent(const CVC3::Expr& e1,const CVC3::Expr& e2) {
	pushContext();
	yices_expr ye1 = (*this) (e1);
	yices_expr ye2 = (*this) (e2);
	yices_expr eqi = yices_mk_eq(context,ye1,ye2);
	yices_expr neq = yices_mk_not(context,eqi);
	yices_assert(context,neq);
	lbool result = Check();
	assert(result != l_undef);
	popContext();
	return false;
}

/*********************************************************************/
//Solver still in consistent state
//(otherwise the formula is UNSAT and the solver should not
// be used anymore)
/*********************************************************************/
bool YicesSMT::isOkay() {
	return !yices_inconsistent(context);
}


void YicesSMT::Assert(const vector<CVC3::Expr>& v) {

	if(v.size()==0) return;
	vector<yices_expr> yv(v.size());
	for(unsigned i=0;i<v.size();++i) {
		yv[i] = (*this)(v[i]);
	}
	yices_assert(context,yices_mk_and(context,&yv[0],yv.size()));
}


bool YicesSMT::initIncrementalAllSat ( const std::vector<CVC3::Expr>& v ) {
	size_t n = v.size();
	if(n > 0) {
		/* declare boolean variables and the expressions */
		all_sat_var_decls.resize(n);
		all_sat_var_exprs.resize(n);
		vector<yices_expr> conj(n);
		for(unsigned int i = 0; i<n; ++i) {
			CVC3::Expr expr(v[i]);
			yices_expr v_expr = yices_mk_fresh_bool_var(context);
			yices_var_decl var_decl = yices_get_var_decl(v_expr);
			all_sat_var_decls[i] = var_decl;
			all_sat_var_exprs[i] = v_expr;
			//yices_pp_expr(e);
			yices_expr eq;
			if(expr.isBoolConst()) {
				eq = expr.isTrue() ? v_expr : yices_mk_not(context,v_expr);
			} else {
				yices_expr e = (*this)(expr);
				eq = yices_mk_eq(context,v_expr, e);
			}
			conj[i] = eq;
		}

		/* bind the boolean variables */
		yices_assert(context, yices_mk_and(context,&conj[0],conj.size()));
	}
	return true;
}

bool YicesSMT::incrementalAllSat ( const CVC3::Expr& assumption, CubeConsumer& cc, unsigned int limit ) {

	util::Timer incremental_timer;
	incremental_timer.Start();

	bool complete (true);

	unsigned n = all_sat_var_exprs.size();
	unsigned step = 0;

	util::Timer cons_timer;

 	if( n >= 1 ) {
		pushContext();

		yices_assert(context,(*this)(assumption));

		clause.resize(n);

		static Cube solutions;
		solutions.clear();

		yices_expr yfalse(yices_mk_false(context));

		unsigned old_size = 0;

		for(; Solve() == l_true ; ++step) {
			m = yices_get_model(context);

			old_size = solutions.size();

			solutions.resize(old_size + n);

			/** block the cube */
			for(unsigned int i = 0, j = old_size; i<n; ++i,++j) {
				lbool val = yices_get_value(m,all_sat_var_decls[i]);
				solutions[j] = val;
				switch(val) {
					case l_false:
						clause[i] = all_sat_var_exprs[i];
						break;
					case l_true:
						clause[i] = yices_mk_not(context,all_sat_var_exprs[i]);
						break;
					case l_undef:
						clause[i] = yfalse;
						break;
				}

			}
			yices_assert(context, yices_mk_or(context,&clause[0],n));

			if(step > limit) {
				complete = false;
				break;
			}
		}

		Cube solution_cube(n);

		for(unsigned i = 0, j = 0; i<solutions.size(); ) {
			for( j = 0; j < n; ++j, ++i) {
				solution_cube[j] = solutions[i];
			}
			cc.consume(solution_cube);
		}




		popContext();
 	}
 	incremental_timer.Stop();
 	MSG( complete ? 1 : 0,"YicesSMT::incrementalAllSat() iters %d time/iter %E \n",
 			step,incremental_timer.Read() / (double) step );

	return complete;
}

bool YicesSMT::allSat ( const std::vector<CVC3::Expr>& v,
		        CubeConsumer& cc,
		        unsigned int limit ) {
	bool complete = true;

	size_t n = v.size();

	unsigned step = 0;

	if(n > 0) {

		/* declare boolean variables and the expressions */
		vector<yices_var_decl> var_decls(n);
		vector<yices_expr> var_exprs(n);
		vector<yices_expr> conj(n);
		for(unsigned int i = 0; i<n; ++i) {
			CVC3::Expr expr(v[i]);
			yices_expr v_expr = yices_mk_fresh_bool_var(context);
			yices_var_decl var_decl = yices_get_var_decl(v_expr);
			var_decls[i] = var_decl;
			var_exprs[i] = v_expr;
			//yices_pp_expr(e);
			yices_expr eq;
			if(expr.isBoolConst()) {
				eq = expr.isTrue() ? v_expr : yices_mk_not(context,v_expr);
			} else {
				yices_expr e = (*this)(expr);
				eq = yices_mk_eq(context,v_expr, e);
			}
			conj[i] = eq;
		}

		/* bind the boolean variables */
		yices_assert(context, yices_mk_and(context,&conj[0],conj.size()));


		Cube solution_cube(n);

		clause.resize(n);
		yices_expr yfalse(yices_mk_false(context));

		for(; true ; ++step) {
			lbool smt_result = Solve();
			if( smt_result != l_true ) break;
			m = yices_get_model(context);

			/** block the cube */
			for(unsigned int i = 0; i<n; ++i) {
				solution_cube[i] = yices_get_value(m,var_decls[i]);
				switch(solution_cube[i]) {
					case l_false:
						clause[i] = var_exprs[i];
						break;
					case l_true:
						clause[i] = yices_mk_not(context,var_exprs[i]);
						break;
					case l_undef:
						clause[i] = yfalse;
						break;
				}
			}
			cc.consume(solution_cube);
			yices_assert(context, yices_mk_or(context,&clause[0],n));

			if(step > limit) {
				complete = false;
				break;
			}
		}
	}
	MSG(complete ? 1 : 0,"YicesSMT::allSat: steps %d limit %d %s\n",step,limit,complete ? "complete" : "interrupted");
	return complete;
}



/*********************************************************************/
//assert an expression
/*********************************************************************/
void YicesSMT::Assert(const CVC3::Expr& e) {
	yices_expr ye = (*this)(e);
	yices_assert(context,ye);
}

/*********************************************************************/
//assert an expression
/*********************************************************************/
void YicesSMT::Assert(yices_context c, const CVC3::Expr& e) {
	yices_context save = context;
	context = c;
	yices_expr ye = (*this)(e);
	yices_assert(context,ye);
	context = save;
}

/*********************************************************************/
//Block the current assignment
/*********************************************************************/
void YicesSMT::Block() {
	//check if SAT
	if(!isOkay()) return; //instance is not SAT

	if(!m)
		m = yices_get_model(context);

	yices_var_decl_iterator it = yices_create_var_decl_iterator(context);
	yices_model m              = yices_get_model(context);
	vector<yices_expr> clause;
	while (yices_iterator_has_next(it)) {
		yices_var_decl d         = yices_iterator_next(it);

		yices_expr var = yices_mk_var_from_decl(context,d);
		switch(yices_get_value(m, d)) {
			case l_true: clause.push_back(yices_mk_not(context,var)); break;
			case l_false: clause.push_back(var); break;
			case l_undef:  break;
		 }
	}
	yices_del_iterator(it);

	//nothing to block
	if(clause.size()==0) return;

	yices_expr blocking_clause[clause.size()];
	for(size_t i = 0; i<clause.size(); ++i)
		blocking_clause[i] = clause[i];

	yices_assert(context,yices_mk_or(context,blocking_clause,clause.size()));
}


void YicesSMT::getValue(const CVC3::Expr& e, CVC3::Expr& value) {
	switch(e.getKind()) {
		case CVC3::UCONST:
		{
			const char* id_cstr  = e.getName().c_str();
			yices_var_decl decl = yices_get_var_decl_from_name(context, (char*) id_cstr);
			if(!decl) return;

			switch(e.getType().getExpr().getKind()) {
				case CVC3::BITVECTOR: {
						throw util::RuntimeError("YicesSMT::getValue(): This feature is currently not availabe for bitvectors.");
					}
					break;
				case CVC3::INT: {
					long val;
					int return_code = yices_get_int_value(m,decl,&val);
					if(return_code==0) {
						throw util::RuntimeError("YicesSMT::getValue(): not a proper declaration, no value assigned in model, or value can't be converted to long.");
					}
					value = lang::vc.ratExpr(val,1);
					}
					break;
				case CVC3::REAL: {
					long num, den;
					int return_code = yices_get_arith_value(m,decl,&num,&den);
					if(return_code==0) {
						throw util::RuntimeError("YicesSMT::getValue(): not a proper declaration, no value assigned in model, or value can't be converted to long.");
					}
					value = lang::vc.ratExpr(num,den);
					}
					break;
				case CVC3::BOOLEAN: {
					lbool val = yices_get_value(m,decl);
					if(val == l_undef) {
						throw util::RuntimeError("YicesSMT::getValue(): boolean variable is not assigned to in model.");
					}

					value = val == l_true ? lang::vc.trueExpr() : lang::vc.falseExpr();
					}
					break;
			}
			break;
		}
		default:
			throw util::RuntimeError("YicesSMT::getValue(): Can only get value of a variable not an arbitrary expression.");
			break;

	}
}



/*********************************************************************/
//Encode an expression
/*********************************************************************/
yices_expr YicesSMT::operator()(const CVC3::Expr& expr) {
	/* visit CVC3 expression here */
	yices_expr result = Lookup(expr);

	if(result) return result;

	yices_expr ykids[expr.arity()];

	CVC3::Expr e = expr;

	/* start with child nodes */
	if(e.arity()>0) {
		const std::vector< CVC3::Expr > & kids = e.getKids();
		for(unsigned i = 0; i<kids.size(); ++i) {
			ykids[i] = operator()(kids[i]);

		}
		for(unsigned i = 0; i<kids.size(); ++i) {
			if(!ykids[i]) {
				throw RuntimeError("YicesSMT::operator(): could not encode kid["+util::intToString(i)+"]: "+kids[i].toString());
			}
		}
	}

	switch(e.getKind()) {
		case CVC3::TRUE_EXPR:
			result = yices_mk_true(context);
			break;
		case CVC3::FALSE_EXPR:
			result = yices_mk_false(context);
			break;
		case CVC3::ID:
		case CVC3::UCONST:
		{
			yices_var_decl decl;
			const char* id_cstr  = e.getName().c_str();
			result = Lookup((char*)id_cstr);
			if(result) break;

			switch(e.getType().getExpr().getKind()) {
				case CVC3::BITVECTOR: {
					int size = e.getType().getExpr()[0].getRational().getInt();
					yices_type type = yices_mk_bitvector_type(context,size);
					decl = yices_mk_var_decl(context,(char*)id_cstr,type);
					result = yices_mk_var_from_decl(context,decl);
					}
					break;
				case CVC3::INT: {
					static const char* type_cstr  = "int";
					yices_type type = yices_mk_type(context,(char*)type_cstr);
					decl = yices_mk_var_decl(context,(char*)id_cstr,type);
					result = yices_mk_var_from_decl(context,decl);
					}
					break;
				case CVC3::REAL: {
					static const char* type_cstr  = "real";
					yices_type type = yices_mk_type(context,(char*)type_cstr);
					decl = yices_mk_var_decl(context,(char*)id_cstr,type);
					result = yices_mk_var_from_decl(context,decl);
					}
					break;
				case CVC3::BOOLEAN: {
					static const char* type_cstr  = "bool";
					yices_type type = yices_mk_type(context,(char*)type_cstr);
					decl = yices_mk_var_decl(context,(char*)id_cstr,type);
					result = yices_mk_var_from_decl(context,decl);
					}
					break;
			}
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
			{
				const CVC3::Rational rat (e.getRational());

				string str = e.getRational().toString();
				const char* c = str.c_str();
				result = yices_mk_num_from_string(context,(char*)c);
			}
			break;
		case CVC3::ITE:
			result = yices_mk_ite(context,ykids[0],ykids[1],ykids[2]);
			break;
		case CVC3::NOT:
			result = yices_mk_not(context,ykids[0]);
			break;
		case CVC3::AND:
			result = yices_mk_and(context,ykids,e.arity());
			break;
		case CVC3::OR:
			result = yices_mk_or(context,ykids,e.arity());
			break;
		case CVC3::XOR:
			for(int i = 0; i<e.arity(); ++i) {
				ykids[i] = yices_mk_not(context,ykids[i]);
			}
			result = yices_mk_and(context,ykids,e.arity());
			break;
		case CVC3::IMPLIES:
			ykids[0] = yices_mk_not(context,ykids[0]);
			result = yices_mk_or(context,ykids,e.arity());
			break;
		case CVC3::IFF:
			result = yices_mk_eq(context,ykids[0],ykids[1]);
			break;
		case CVC3::EQ:
			result = yices_mk_eq(context,ykids[0],ykids[1]);
			break;
		case CVC3::NEQ:
			result = yices_mk_diseq(context,ykids[0],ykids[1]);
			break;

		/* arithmetic */
		case CVC3::LT:
			result = yices_mk_lt(context,ykids[0],ykids[1]);
			break;
		case CVC3::GT:
			result = yices_mk_gt(context,ykids[0],ykids[1]);
			break;
		case CVC3::LE:
			result = yices_mk_le(context,ykids[0],ykids[1]);
			break;
		case CVC3::GE:

			result = yices_mk_ge(context,ykids[0],ykids[1]);
			break;
		case CVC3::PLUS:
			result = yices_mk_sum(context,ykids,e.arity());
			break;
		case CVC3::MINUS:
			result = yices_mk_sub(context,ykids,e.arity());
			break;
		case CVC3::UMINUS: {
			yices_expr args[2] = {yices_mk_num(context,0),ykids[0]};
			result = yices_mk_sub(context,args,2);
			}
			break;
		case CVC3::MULT:
			result = yices_mk_mul(context,ykids,e.arity());
			break;
		case CVC3::DIVIDE:

			break;
		case CVC3::MOD:

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
	Log(expr,result);
	return result;
}


/*********************************************************************/
//look up encoding of an expression
/*********************************************************************/
yices_expr YicesSMT::Lookup(const CVC3::Expr& e) const {
	CVC3::ExprHashMap<yices_expr>::const_iterator i = expr2yexpr.find(e);
	if(i!=expr2yexpr.end()) return i->second;
	return 0;
}

yices_expr YicesSMT::Lookup(char *name) {
	for(int i=contexts.size()-1;i>-1;--i) {
		yices_var_decl decl = yices_get_var_decl_from_name(contexts[i],name);
		if(decl) return yices_mk_var_from_decl(contexts[i],decl);
	}
	return 0;
}

/*********************************************************************/
//remember an encoded expression
/*********************************************************************/
void YicesSMT::Log(const CVC3::Expr& e, yices_expr y) {
	if(!y) throw RuntimeError("Tried to store null expression when encoding ");
	expr2yexpr[e] = y;
	expr_stack.back().insert(e);
}

void YicesSMT::Dump() {

}

} //end of dp
