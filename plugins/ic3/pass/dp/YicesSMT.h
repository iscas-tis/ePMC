#ifndef YICES_SMT_H
#define YICES_SMT_H

#include "yices_c.h"

namespace dp {

/*! \brief frontend to Yices */
class YicesSMT : public SMT {
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


	/*! compute unsat cores */	
	virtual lbool getUnsatCore(const std::vector<CVC3::Expr>& vec, std::vector<CVC3::Expr>& core );


	/*! \brief get the value of a variable after successful solving */
	virtual void getValue(const CVC3::Expr& e, CVC3::Expr& value);

	/*! \brief assert an expression in the current context */
	virtual void Assert(const CVC3::Expr& e) ;
	/*! \brief block current assignment */
	virtual void Block() ;

	bool incrementalAllSat ( const CVC3::Expr& assumption, CubeConsumer& cc, unsigned int limit );
	bool initIncrementalAllSat ( const std::vector<CVC3::Expr>& v );

	/* AllSAT (solution enumeration) */
	virtual bool allSat ( const std::vector<CVC3::Expr>& v,
			      CubeConsumer&,
			      unsigned int limit = 500);

	/*! \brief output logical context */
	virtual void dumpContext() ;
	/*! \brief debugging output */
	virtual void Dump();

	YicesSMT();
	virtual ~YicesSMT();
private:
	/*! the model from the solver */
	yices_model m;

	/*! stack of contexts */
	std::vector<yices_context> contexts;

	/*! current context */
	yices_context context;

	/*! store blocking clauses */
	std::vector<yices_expr> clause;

	/*! cache for translation */
	CVC3::ExprHashMap<yices_expr> expr2yexpr;
	/*! cache a translated expression */
	void Log(const CVC3::Expr& e, yices_expr y);
	/*! lookup in translation cache */
	yices_expr Lookup(const CVC3::Expr&) const;
	/*! lookup variable in cache */
	yices_expr Lookup(char *name);
	/*! solver in consistent state? */
	bool okay;
	/*! \brief helper function for ProveImplies
		@see ProveImplies
	*/
 	bool proveImpliesHelper(const std::vector<CVC3::Expr>& ante,
 				const std::vector<CVC3::Expr>& cons);
	/*! \brief Assert in logical context */
	void Assert(yices_context,const CVC3::Expr& e);
	/*! type cache */
	std::map<std::string,yices_type> types;

	/*! create internal representation of an expression */
	yices_expr operator()(const CVC3::Expr&);

	lbool Check ();

	std::vector< std::set<CVC3::Expr> > expr_stack;


	std::vector<yices_var_decl> all_sat_var_decls;
	std::vector<yices_expr> all_sat_var_exprs;
};

} //end of namespace dp

#endif
