
using namespace std;

#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"

#include "lang/Node.h"
#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"

using namespace lang;
using namespace util;
#include "SMT.h"
#include "MathSat.h"


#define MSAT(return_value,str) if((return_value)!=0) throw util::RuntimeError((str));

namespace dp {





void MathSat::dumpContext() {}

void MathSat::Dump() {}


/*! \brief get the value of a variable after successful solving */
void MathSat::getValue(const CVC3::Expr& e, CVC3::Expr& value) {
	msat_term t(msat_get_model_value (env, encode(env,expr2term,vars,e)));
	if(MSAT_ERROR_TERM(t))
		throw util::RuntimeError("MathSat::getValue: could not get value from MathSat.\n");
	value = decode(t,vars);
}


int MathSat::getModel(const vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model) {
	for(unsigned i=0; i<vars.size(); ++i) {
		getValue(vars[i],model[vars[i]]);
	}
}

MathSat::MathSat() : /* env(msat_create_env()),*/ okay(true) {
/*
	int res;
	// enable some theories
	env = msat_create_env();

	res = msat_add_theory(env,MSAT_LRA);
	if(res) throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");

	res = msat_add_theory(env,MSAT_LIA);
	if(res) throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");
	*/
}

MathSat::~MathSat() {
	//msat_destroy_env(env);
}

/*! \brief create a new logical context */
void MathSat::pushContext() {
	MSAT(msat_push_backtrack_point(env),"MathSat::pushContext(): failed");
}

/*! \brief delete the current context */
void MathSat::popContext() {
	MSAT(msat_pop_backtrack_point(env),"MathSat::popContext(): failed");
	expr2term.clear();
	vars.clear();
}

/*! \brief reset the current context */
void MathSat::resetContext() {
	msat_reset_env(env);
}

/*! \brief check satisfiability */
lbool MathSat::Solve() {
	lbool result(l_undef);
	switch(msat_solve(env)) {
		case MSAT_UNKNOWN:
			result = l_undef;
			okay = false;
			break;
		case MSAT_UNSAT:
			result = l_false;
			okay = false;
			break;
		case MSAT_SAT:
			result = l_true;

			/* we use a model iterator to retrieve the model values for all the
			 * variables, and the necessary function instantiations */
			msat_model_iterator iter = msat_create_model_iterator(env);
			assert(!MSAT_ERROR_MODEL_ITERATOR(iter));

			printf("Model:\n");
			while (msat_model_iterator_has_next(iter)) {
				msat_term t, v;
				char *s;
				msat_model_iterator_next(iter, &t, &v);
				s = msat_term_repr(t);
				assert(s);
				printf(" %s = ", s);
				free(s);
				s = msat_term_repr(v);
				assert(s);
				printf("%s\n", s);
				free(s);
			}
			msat_destroy_model_iterator(iter);

			okay = true;
			break;
	}
	return result;
}

bool MathSat::isOkay() { return okay; }

/*! \brief assert an expression in the current context */
void MathSat::Assert(const CVC3::Expr& e) {
	Assert(env,expr2term,vars,e);
}

/*! \brief assert a conjunction of expressions */
void MathSat::Assert(const vector<CVC3::Expr>& v) {
	for(std::vector<CVC3::Expr>::const_iterator i=v.begin();i!=v.end();++i)
	Assert(*i);
}

/*! \brief assert a conjunction of expressions */
void MathSat::Assert(msat_env env,
		     CVC3::ExprHashMap<msat_term>& cache,
		     std::map<std::string,CVC3::Expr>& vars,
		     const CVC3::Expr& e) {
	msat_term t(encode(env,cache,vars,e));
	MSG(1,"MathSat::Assert %s\n",msat_term_repr(t));
	if(MSAT_ERROR_TERM(t)) {
		throw util::RuntimeError("MathSat::Assert: could not encode expression " + e.toString() + "\n");
	}

	MSAT(msat_assert_formula ( env, t ),"MathSat::Assert failed : "+ e.toString()) ;
}

/*********************************************************************/
//Encode an expression
/*********************************************************************/

msat_term MathSat::encode(msat_env env,
			  CVC3::ExprHashMap<msat_term>& cache,
			  std::map<std::string,CVC3::Expr>& vars,
			  const CVC3::Expr& expr) {
	/* visit CVC3 expression here */

	CVC3::ExprHashMap<msat_term>::iterator it (cache.find(expr));
	msat_term result;

	if(it!=cache.end()) {
		result = it->second;
	} else {
		msat_term mkids[expr.arity()];

		CVC3::Expr e = expr;

		/* start with child nodes */
		if(e.arity()>0) {
			const std::vector< CVC3::Expr > & kids = e.getKids();
			for(unsigned i = 0; i<kids.size(); ++i) {
				mkids[i] = encode(env,cache,vars,kids[i]);
				if(MSAT_ERROR_TERM(mkids[i])) {
					throw RuntimeError("YicesSMT::encode: could not encode kid["+util::intToString(i)+"]: "+kids[i].toString());
				}

			}
		}

		switch(e.getKind()) {
			case CVC3::TRUE_EXPR:
				result = msat_make_true(env);
				break;
			case CVC3::FALSE_EXPR:
				result = msat_make_false(env);
				break;
			case CVC3::ID:
			case CVC3::UCONST:
			{
				const char *name(e.getName().c_str());
				msat_type type;

				switch(e.getType().getExpr().getKind()) {
					case CVC3::BITVECTOR: {
						//int size = e.getType().getExpr()[0].getRational().getInt();
						type = MSAT_BV;
						}
						break;
					case CVC3::INT:
						type = MSAT_INT;
						break;
					case CVC3::REAL:
						type = MSAT_REAL;
						break;
					case CVC3::BOOLEAN:
						type = MSAT_BOOL;
						break;
					default:
						throw util::RuntimeError("MathSat::encode\n");
						break;
				}
				msat_decl decl (msat_declare_variable (env, name, type));
				if(MSAT_ERROR_DECL(decl))
					throw util::RuntimeError("MathSat::encode: could not declare variable \" "+e.toString()+"\"");

				result = msat_make_variable (env, decl);
				vars[name] = expr;
				break;
			}
			case CVC3::LET:
				cout<<"LET expression"<<endl;

				break;
			case CVC3::RATIONAL_EXPR:
				{
					const CVC3::Rational& rat (e.getRational());

					string str = rat.toString();
					const char* c = str.c_str();
					result = msat_make_number(env,c);
					/*
					char *s(msat_term_repr(result));
					MSG(0,"MathSat::encode(): %s => %s\n",c,s);
					free(s);
					*/
				}
				break;
			case CVC3::ITE:
				result = msat_make_ite(env,mkids[0],mkids[1],mkids[2]);
				break;
			case CVC3::NOT:
				result = msat_make_not(env,mkids[0]);
				break;
			case CVC3::AND:
				result = mkids[0];
				for(int i=1;i<e.arity();++i)
					result = msat_make_and(env,result,mkids[i]);
				break;
			case CVC3::OR:
				result = mkids[0];
				for(int i=1;i<e.arity();++i)
					result = msat_make_or(env,result,mkids[i]);
				break;
			case CVC3::XOR:
				for(int i = 0; i<e.arity(); ++i) {
					mkids[i] = msat_make_not(env,mkids[i]);
				}
				result = mkids[0];
				for(int i=1;i<e.arity();++i)
					result = msat_make_and(env,result,mkids[i]);

				break;
			case CVC3::IMPLIES:
				result = msat_make_implies(env,mkids[0],mkids[1]);
				break;
			case CVC3::IFF:
				result = msat_make_iff(env,mkids[0],mkids[1]);
				break;
			case CVC3::EQ:
				result = msat_make_equal(env,mkids[0],mkids[1]);
				break;
			case CVC3::NEQ:
				result = msat_make_not(env, msat_make_equal(env,mkids[0],mkids[1]) );
				break;
			/* arithmetic */
			case CVC3::LT:
				result = msat_make_lt(env,mkids[0],mkids[1]);
				break;
			case CVC3::GT:
				result = msat_make_gt(env,mkids[0],mkids[1]);
				break;
			case CVC3::LE:
				result = msat_make_leq(env,mkids[0],mkids[1]);
				break;
			case CVC3::GE:
				result = msat_make_geq(env,mkids[0],mkids[1]);
				break;
			case CVC3::PLUS:
				result = mkids[0];
				for(int i=1;i<e.arity();++i)
					result = msat_make_plus(env,result,mkids[i]);
				break;
			case CVC3::MINUS:
				if(e.arity() == 1) return mkids[0];

				result = mkids[1];

				for(int i=2;i<e.arity();++i)
					result = msat_make_plus(env,result,mkids[i]);

				result = msat_make_minus(env,mkids[0],result);
				break;
			case CVC3::UMINUS:
				result = msat_make_negate(env,mkids[0]);
				break;
			case CVC3::MULT:
				result = msat_make_times(env,mkids[0],mkids[1]);
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
	}

	assert(!MSAT_ERROR_TERM(result));
	cache[expr] = result;
	return result;
}


msat_term MathSat::operator()(const CVC3::Expr& expr) {
	return encode(env,expr2term,vars,expr);
}

CVC3::Expr MathSat::decode(msat_term t, const std::map<std::string,CVC3::Expr>& vars) {
	CVC3::Expr result;

	int arity (msat_term_arity( t ));
	std::vector<CVC3::Expr> v(arity);

	// recursive step
	for(int i = 0; i< arity ; ++i) {
		v[i] = decode( msat_term_get_arg(t,i), vars );
	}

	if( msat_term_is_true (t) ) {
		result = vc.trueExpr();
	} else if ( msat_term_is_false (t) ) {
		result = vc.falseExpr();
	} else if ( msat_term_is_boolean_var (t) ) {
		result = vc.varExpr(msat_term_get_name (env, t), vc.boolType());
	} else if ( msat_term_is_atom (t) ) {
		if(msat_term_is_equal(t)) {
			result = vc.eqExpr(v[0],v[1]);
		} else if(msat_term_is_lt (t) ) {
			result = vc.ltExpr(v[0],v[1]);
		} else if ( msat_term_is_leq (t) ) {
			result = vc.leExpr(v[0],v[1]);
		} else if ( msat_term_is_gt (t) ) {
			result = vc.gtExpr(v[0],v[1]);
		} else if ( msat_term_is_geq (t) ) {
			result = vc.geExpr(v[0],v[1]);
		} else {
			MSG(0,"unknown atom %s\n",msat_term_repr(t));
		}
	} else if ( msat_term_is_number (t) ) {
		char *s(msat_term_repr(t));
		result = vc.ratExpr(s,10);
		free(s);
 	} else if ( msat_term_is_and (t) ) {
		result = vc.andExpr(v[0],v[1]);
	} else if ( msat_term_is_or (t) ) {
		result = vc.orExpr(v[0],v[1]);
	} else if ( msat_term_is_not (t) ) {
		result = vc.notExpr(v[0]);
	} else if ( msat_term_is_iff (t) ) {
		result = vc.iffExpr(v[0],v[1]);
	} else if ( msat_term_is_implies (t) ) {
		result = vc.impliesExpr(v[0],v[1]);
 	} else if ( msat_term_is_xor (t) ) {
		result = vc.andExpr(vc.notExpr(v[0]),vc.notExpr(v[1]));
	} else if ( msat_term_is_bool_ite (t) ) {
		result = vc.iteExpr(v[0],v[1],v[2]);
 	} else if ( msat_term_is_term_ite (t) ) {
		result = vc.iteExpr(v[0],v[1],v[2]);
	} else if ( msat_term_is_variable (t) ) {
		msat_type type(msat_term_get_type(t));

		char* name(msat_term_repr(t));

		std::map<std::string,CVC3::Expr>::const_iterator it (vars.find(name));
		if(it!=vars.end()) {
			result = (*it).second;
		} else {
			CVC3::Type ctype;
			switch(type) {
				case MSAT_BV:
				ctype = vc.bitvecType(10); /** \todo how to determine the actual width ? */
				break;
				case MSAT_INT:
				ctype = vc.intType();
				break;
				case MSAT_REAL:
				ctype = vc.realType();
				break;
				case MSAT_BOOL:
				ctype = vc.boolType();
				break;
				default:
				ctype = vc.boolType();
				break;
			}
			result = vc.varExpr(msat_term_get_name (env, t),ctype);
		}
		free(name);
	} else if ( msat_term_is_uif (t) ) {
		MSG(0,"uninterpreted function\n");
	} else if ( msat_term_is_equal (t) ) {
		result = vc.eqExpr(v[0],v[1]);
 	} else if ( msat_term_is_plus (t) ) {
		result = vc.plusExpr(v[0],v[1]);
	} else if ( msat_term_is_minus (t) ) {
		result = vc.minusExpr(v[0],v[1]);
	} else if ( msat_term_is_times (t) ) {
		result = vc.multExpr(v[0],v[1]);
	} else if ( msat_term_is_negate (t) ) {
		result = vc.uminusExpr(v[0]);
	} else if ( msat_term_is_bv_concat (t) ) {
		MSG(0,"unsupported bv_concat\n");
	} else if ( msat_term_is_bv_select (t) ) {
		MSG(0,"unsupported bv_select\n");
	} else if ( msat_term_is_bv_or (t) ) {
		MSG(0,"unsupported bv_or\n");
	} else if ( msat_term_is_bv_xor (t) ) {
		MSG(0,"unsupported bv_xor\n");
	} else if ( msat_term_is_bv_and (t) ) {
		MSG(0,"unsupported bv_and\n");
	} else if ( msat_term_is_bv_not (t) ) {
		MSG(0,"unsupported bv_not\n");
	} else if ( msat_term_is_bv_lsl (t) ) {
		MSG(0,"unsupported bv_lsl\n");
	} else if ( msat_term_is_bv_lsr (t) ) {
		MSG(0,"unsupported bv_lsr\n");
	} else if ( msat_term_is_bv_asr (t) ) {
		MSG(0,"unsupported bv_asr\n");
	} else if ( msat_term_is_bv_zext (t) ) {
		MSG(0,"unsupported bv_zext\n");
	} else if ( msat_term_is_bv_sext (t) ) {
		MSG(0,"unsupported bv_sext\n");
	} else if ( msat_term_is_bv_plus (t) ) {
		MSG(0,"unsupported bv_plus\n");
	} else if ( msat_term_is_bv_minus (t) ) {
		MSG(0,"unsupported bv_minus\n");
	} else if ( msat_term_is_bv_times (t) ) {
		MSG(0,"unsupported bv_times\n");
 	} else if ( msat_term_is_bv_udiv (t) ) {
		MSG(0,"unsupported bv_udiv\n");
 	} else if ( msat_term_is_bv_urem (t) ) {
		MSG(0,"unsupported bv_urem\n");
 	} else if ( msat_term_is_bv_sdiv (t) ) {
		MSG(0,"unsupported bv_sdiv\n");
 	} else if ( msat_term_is_bv_srem (t) ) {
		MSG(0,"unsupported bv_srem\n");
	} else if ( msat_term_is_bv_smod (t) ) {
		MSG(0,"unsupported bv_smod\n");
 	} else if ( msat_term_is_bv_ult (t) ) {
		MSG(0,"unsupported bv_ult\n");
 	} else if ( msat_term_is_bv_uleq (t) ) {
		MSG(0,"unsupported bv_uleq\n");
 	} else if ( msat_term_is_bv_ugt (t) ) {
		MSG(0,"unsupported bv_ugt\n");
 	} else if ( msat_term_is_bv_ugeq (t) ) {
		MSG(0,"unsupported bv_ugeq\n");
 	} else if ( msat_term_is_bv_slt (t) ) {
		MSG(0,"unsupported bv_slt\n");
 	} else if ( msat_term_is_bv_sleq (t) ) {
		MSG(0,"unsupported bv_sleq\n");
 	} else if ( msat_term_is_bv_sgt (t) ) {
		MSG(0,"unsupported bv_sgt\n");
 	} else if ( msat_term_is_bv_sgeq (t) ) {
		MSG(0,"unsupported bv_sgeq\n");
 	} else {
		MSG(0,"unknown construct");
	}

	assert(!result.isNull());
	return result;

}

lbool MathSat::Check (const CVC3::Expr& e) {
	lbool result (l_undef);
	pushContext();
	Assert(e);
	result = Solve();
	switch(result) {
		case l_true: {
			MSG(0,"Print model for " + e.toString() + "\n");
			}
			break;
		default:
			break;
	}


	popContext();
	return result;
}

/*! \brief equivalence */
bool MathSat::proveEquivalent(const CVC3::Expr& f,const CVC3::Expr& g) {
	bool result(false);
	switch(Check(vc.notExpr(vc.eqExpr(f,g)))) {
		case l_true:
			result = false;
			break;
		case l_false:
			result = true;
			break;
		case l_undef:
			result = false;
			break;
	}
	return result;
}


void MathSat::allSatCallBack(msat_term *model, int size, CubeConsumer* cc) {
	for(int i=0;i<size;++i) {


	}
	cc->consume(c);
	if(all_sat_counter > all_sat_limit)
		throw AllSatLimit();
}

bool MathSat::allSat ( const std::vector<CVC3::Expr>& v, CubeConsumer& cc, unsigned int limit ) {
	/*
	int msat_all_sat  	(  	msat_env   	 e,
			msat_term *  	important,
			int  	num_important,
			msat_all_sat_model_callback  	func,
			void *  	user_data
		)
	*/

	bool complete = true;

	size_t n = v.size();

	if(n > 0) {

		vector<msat_term> important(n);
		/* declare boolean variables and the expressions */

		for(unsigned int i = 0; i<n; ++i) {
			important[i] = encode(env, expr2term, vars, v[i]);
		}

		/* bind the boolean variables */
		try {
			all_sat_counter = 0;
			//msat_all_sat(env, &important[0], important.size(), (msat_all_sat_model_callback) &allSatCallBack, &cc);
			all_sat_counter = 0;
			complete = true;
		} catch(AllSatLimit& a ) {
			complete = false;
		}
	}
	return complete;
}

lbool MathSat::getUnsatCore(const std::vector<CVC3::Expr>& __f, std::vector<CVC3::Expr>& core ) {
	lbool result = l_undef;
	int res;

		/* preprocessing */
		const std::vector<CVC3::Expr>& f(__f);

		MSG(1,"~~ MathSat::getUnsatCore\n");
		msat_env env( msat_create_env() );
		std::map<std::string,CVC3::Expr> vars;
		CVC3::ExprHashMap<msat_term> cache;

		res = msat_add_theory(env,MSAT_LRA);
		if(res) throw util::RuntimeError("MathSat::getUnsatCore: theory not supported\n");


		res = msat_add_theory(env,MSAT_IDL);
		if(res) throw util::RuntimeError("MathSat::getUnsatCore: theory not supported\n");

	 	res = msat_init_interpolation(env);
		if(res) throw util::RuntimeError("MathSat::getUnsatCore: could not enable interpolation\n");

	// 	if(msat_add_theory(env,MSAT_RDL))
	// 		throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");

	// 	if(msat_add_theory(env,MSAT_UF))
	// 		throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");
	// 	msat_set_theory_combination(env, MSAT_COMB_DTC);


		msat_term formulas[f.size()];
		for(unsigned i=0; i<f.size(); ++i) {
			formulas[i] = encode(env,cache,vars,f[i]);

			/*
			char* s = msat_term_repr(formulas[i]);
			MSG(0,"original formula "+f[i].toString()+"\n");
			MSG(0,"formula[%d] = %s\n",i,s);
			free(s);
			*/
		}

		int groups[f.size()];
		for(unsigned i=0; i<f.size(); ++i) {
			groups[i] = msat_create_itp_group(env);
			assert(groups[i] != -1);
		}

		std::vector<bool> trivial(f.size(),false);
		for(unsigned i=0; i<f.size(); ++i) {
			if(msat_set_itp_group(env,groups[i]))
				throw util::RuntimeError("MathSat::getUnsatCore: could not set interpolation group\n");

			if(msat_assert_formula(env, formulas[i]))
				throw util::RuntimeError("MathSat::getUnsatCore: could not assert formula\n");
		}

		switch(msat_solve(env)) {
			case MSAT_UNKNOWN:
				MSG(1,"MathSat::getUnsatCore: UNKNOWN\n");
				result = l_undef;
				break;
			case MSAT_UNSAT:
				result = l_true;
				break;
			case MSAT_SAT:

#if 0
				MSG(1,"MathSat::getUnsatCore: SAT\n");
				/* we use a model iterator to retrieve the model values for all the
				 * variables, and the necessary function instantiations */
				msat_model_iterator iter = msat_create_model_iterator(env);
				assert(!MSAT_ERROR_MODEL_ITERATOR(iter));

				for(unsigned i=0; i<f.size(); ++i) {
					char* s = msat_term_repr(formulas[i]);
					MSG(1,"formula[%d] = %s\n",i,s);
					free(s);
				}

				printf("Model:\n");
				while (msat_model_iterator_has_next(iter)) {
					msat_term t, v;
					char *s;
					msat_model_iterator_next(iter, &t, &v);
					s = msat_term_repr(t);
					assert(s);
					printf(" %s = ", s);
					free(s);
					s = msat_term_repr(v);
					assert(s);
					printf("%s\n", s);
					free(s);
				}
				msat_destroy_model_iterator(iter);
#endif
				result = l_false;
				break;
		}
		msat_destroy_env(env);
		return result;
}


lbool MathSat::Interpolate(const std::vector<CVC3::Expr>& __f, std::vector<CVC3::Expr>& interpolants) {
	lbool result = l_undef;
	int res;

	/* preprocessing */
	const std::vector<CVC3::Expr>& f(__f);

	MSG(1,"~~ MathSat::Interpolate\n");
	msat_env env( msat_create_env() );
	std::map<std::string,CVC3::Expr> vars;
	CVC3::ExprHashMap<msat_term> cache;

	res = msat_add_theory(env,MSAT_LRA);
	if(res) throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");


	res = msat_add_theory(env,MSAT_IDL);
	if(res) throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");

 	res = msat_init_interpolation(env);
	if(res) throw util::RuntimeError("MathSat::Interpolate: could not enable interpolation\n");

// 	if(msat_add_theory(env,MSAT_RDL))
// 		throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");

// 	if(msat_add_theory(env,MSAT_UF))
// 		throw util::RuntimeError("MathSat::Interpolate: theory not supported\n");
// 	msat_set_theory_combination(env, MSAT_COMB_DTC);


	msat_term formulas[f.size()];
	for(unsigned i=0; i<f.size(); ++i) {
		formulas[i] = encode(env,cache,vars,f[i]);

		/*
		char* s = msat_term_repr(formulas[i]);
		MSG(0,"original formula "+f[i].toString()+"\n");
		MSG(0,"formula[%d] = %s\n",i,s);
		free(s);
		*/
	}

	int groups[f.size()];
	for(unsigned i=0; i<f.size(); ++i) {
		groups[i] = msat_create_itp_group(env);
		assert(groups[i] != -1);
	}

	std::vector<bool> trivial(f.size(),false);
	for(unsigned i=0; i<f.size(); ++i) {
		if(msat_set_itp_group(env,groups[i]))
			throw util::RuntimeError("MathSat::Interpolate: could not set interpolation group\n");

		if(msat_assert_formula(env, formulas[i]))
			throw util::RuntimeError("MathSat::Interpolate: could not assert formula\n");
	}

	switch(msat_solve(env)) {
		case MSAT_UNKNOWN:
			MSG(1,"MathSat::Interpolate: UNKNOWN\n");
			result = l_undef;
			break;
		case MSAT_UNSAT: {
		
			result = l_false;

#if 0
			char *s;
			msat_term unsat_core(msat_get_unsat_core(env));
			s = msat_term_repr(unsat_core);
			assert(s);
			printf("MathSat::Interpolate unsat core %s\n", s);
			free(s);
#endif
			for(unsigned i=1; i<f.size(); ++i) {
				msat_term t(msat_get_interpolant (env, groups, i));
				if(MSAT_ERROR_TERM(t))
					throw util::RuntimeError("MathSat::Interpolate: MathSat failed to compute an interpolant\n");
				CVC3::Expr interpolant(decode(t,vars));

				char *s(msat_term_repr(t));
				MSG(1,"MathSat::Interpolate (interpolant %d) %s\n",i,s);
				free(s);

				interpolants.push_back(interpolant);
				MSG(1,"MathSat::Interpolate: itp: " + interpolant.toString() + "\n");

			}
			if(interpolants.size()==0) {
				MSG(0,"MathSat::Interpolate: Warning: found no interpolants\n");
			}


		}
			break;
		case MSAT_SAT:
			result = l_true;

#if 0
			/* we use a model iterator to retrieve the model values for all the
			 * variables, and the necessary function instantiations */
			msat_model_iterator iter = msat_create_model_iterator(env);
			assert(!MSAT_ERROR_MODEL_ITERATOR(iter));

			for(unsigned i=0; i<f.size(); ++i) {
				char* s = msat_term_repr(formulas[i]);
				MSG(0,"formula[%d] = %s\n",i,s);
				free(s);
			}

			printf("Model:\n");
			while (msat_model_iterator_has_next(iter)) {
				msat_term t, v;
				char *s;
				msat_model_iterator_next(iter, &t, &v);
				s = msat_term_repr(t);
				assert(s);
				printf(" %s = ", s);
				free(s);
				s = msat_term_repr(v);
				assert(s);
				printf("%s\n", s);
				free(s);
			}
			msat_destroy_model_iterator(iter);
			return l_false;
#endif
			break;
	}

	msat_destroy_env(env);
	return result;
}

}
