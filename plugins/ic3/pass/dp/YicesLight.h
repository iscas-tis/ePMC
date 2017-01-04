#ifndef YICES_LIGHT_H
#define YICES_LIGHT_H

#include "yicesl_c.h"

namespace dp {

/*! \brief frontend to Yices */
class YicesLight : public SMT {
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
	virtual int getModel(const vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model);

	/*! \brief equivalence */
	virtual bool proveEquivalent(const CVC3::Expr&,const CVC3::Expr&) ;
	/*! \brief consistency of logical context */
	virtual bool isOkay() ;
	virtual lbool getUnsatCore(const vector<CVC3::Expr>& vec, vector<CVC3::Expr>& core );

	/*! \brief get the value of a variable after successful solving */
	virtual void getValue(const CVC3::Expr& e, CVC3::Expr& value);

	/*! \brief assert an expression in the current context */
	virtual void Assert(const CVC3::Expr& e) ;
	/*! \brief block current assignment */
	virtual void Block() ;

	/*! \brief output logical context */
	virtual void dumpContext() ;
	/*! \brief debugging output */
	virtual void Dump();

	YicesLight();
	virtual ~YicesLight();
private:
	lbool Check(); 
	std::string Lookup(const CVC3::Expr&);
	void Log(const CVC3::Expr& e, const std::string& name);
	CVC3::ExprHashMap<std::string> declared_vars;


	yicesl_context context;

	/*! current unsatisfiable core */
	vector<int> unsat_core;

	/*! solver in consistent state? */
	bool okay;
	int assertPlus(const CVC3::Expr& e);

	std::string toString(const CVC3::Expr& e);

	int send(const std::string& message);
	int send(char* message);
	int talk(const std::string& message);
	void parseReply(char const* str);

	

	lbool status;
	int id;
};

} //end of namespace dp

#endif
