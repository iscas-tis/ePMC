#ifndef MATHSAT_H
#define MATHSAT_H

#include "mathsat/include/mathsat.h"

namespace dp {

class AllSatLimit {
};


/*! \brief frontend to MathSat */
class MathSat : public SMT {
	public:
	/*! \brief create a new logical context */
	virtual void pushContext() ;
	/*! \brief delete the current context */
	virtual void popContext() ;
	/*! \brief reset the current context */
	virtual void resetContext() ;
	/*! \brief check satisfiability */
	virtual lbool Solve() ;


	/*! \brief get the model of the last satisfiable query
	    \param vars variables of interest
	    \return 1 means success
	*/
	virtual int getModel(const std::vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model);
	/*! \brief equivalence */
	virtual bool proveEquivalent(const CVC3::Expr&,const CVC3::Expr&) ;
	/*! \brief consistency of logical context */
	virtual bool isOkay() ;
	/*! \brief assert a conjunction of expressions */
	void Assert(const std::vector<CVC3::Expr>& v);


	virtual bool allSat ( const std::vector<CVC3::Expr>& v, CubeConsumer& cc, unsigned int limit );

	/*! \brief get the value of a variable after successful solving */
	virtual void getValue(const CVC3::Expr& e, CVC3::Expr& value);

	/*! \brief assert an expression in the current context */
	virtual void Assert(const CVC3::Expr& e) ;
	/*! \brief block given model */
	virtual void blockModel(CVC3::ExprHashMap<CVC3::Expr>& model) {}


	/*! \brief block current assignment */
	virtual void Block() {}

	/*! \brief output logical context */
	virtual void dumpContext() ;
	/*! \brief debugging output */
	virtual void Dump();

	/*! \brief interpolation */
	virtual lbool Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result);

	virtual lbool getUnsatCore(const std::vector<CVC3::Expr>& vec, std::vector<CVC3::Expr>& core );

	MathSat();
	virtual ~MathSat();
private:
	/*! the model from the solver */
	msat_model_iterator model_iterator;

	/*! current context */
	msat_env env;
	/*! cache for translation */
	CVC3::ExprHashMap<msat_term> expr2term;

	std::map<std::string,CVC3::Expr> vars;


	Cube c;
	unsigned all_sat_counter;
	unsigned all_sat_limit;

	void allSatCallBack(msat_term *model, int size, CubeConsumer* cc);

	/*! cache a translated expression */
	void cacheTerm(CVC3::ExprHashMap<msat_term>& cache, const CVC3::Expr& e, msat_term y);
	/*! solver in consistent state? */
	bool okay;
	/*! \brief Assert in logical context */
	void Assert(msat_env env, CVC3::ExprHashMap<msat_term>& cache,
		    std::map<std::string,CVC3::Expr>&, const CVC3::Expr& e);

	/*! type cache */
	std::map<std::string,msat_type> types;



	/*! create internal representation of an expression */
	msat_term operator()(const CVC3::Expr&);

	msat_term encode(msat_env env,
			 CVC3::ExprHashMap<msat_term>& cache,
			 std::map<std::string,CVC3::Expr>& variable,
			 const CVC3::Expr& expr);

	CVC3::Expr decode(msat_term t,
			  const std::map<std::string,CVC3::Expr>&) ;
	lbool Check (const CVC3::Expr& e);
};

} //end of namespace dp

#endif
