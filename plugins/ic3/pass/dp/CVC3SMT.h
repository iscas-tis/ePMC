#ifndef CVC3_SMT_H
#define CVC3_SMT_H

#include "SMT.h"

namespace dp {

/*! \brief interface to CVC3 
    \note CVC3 is an open source SMT solver available at www.cs.nyu.edu/acsys/cvc3/
    \see  YicesSMT
*/
class CVC3SMT : public SMT {
	public:
	/*! \brief create a new logical context */
	virtual void pushContext() ;
	/*! \brief delete the current context */
	virtual void popContext() ;
	/*! \brief reset the current context */
	virtual void resetContext() ;

	/*! \brief get the model of the last satisfiable query 
	    \param vars variables of interest */
	int getModel(const std::vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model);
	/*! \brief get the unsat core */
	virtual int getUnsatCore(std::vector<CVC3::Expr>& core);
	/*! \brief check satisfiability */
	virtual lbool Solve() ;
	/*! \brief equivalence */
	virtual bool proveEquivalent(const CVC3::Expr&, const CVC3::Expr&) ;
	/*! \brief implication */
	virtual bool proveImplies(const std::vector<CVC3::Expr>& ante, const std::vector<CVC3::Expr>& cons);
	/*! \brief consistency of logical context */
	virtual bool isOkay() ;
	virtual void Assert(const CVC3::Expr& e) ;
	
	/*! \brief block current assignment */
	virtual void Block() ;
	/*! \brief output logical context */
	virtual void dumpContext() ;
	/*! \brief debugging output */
	virtual void Dump();
	
	CVC3SMT();
	virtual ~CVC3SMT() {}
private:
	bool Check(const CVC3::Expr&, bool=false);
	void updateModel();
	int context;
	CVC3::ValidityChecker* vc;
	CVC3::ExprHashMap<unsigned> entry;
    std::vector<lbool> model;
	CVC3::QueryResult status;
};

} //end of namespace dp

#endif
