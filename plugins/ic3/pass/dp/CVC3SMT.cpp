/**************************** CPPFile ***************************

* FileName [CVC3SMT.cpp]

* PackageName [parser]

* Synopsis [Implementation of CVC3SMT]

* Description [These classes encapsulate the CVC3 SMT solver.]

* SeeAlso []

* Author [Patrick Wischnewski]

* Copyright [ Copyright (c) 2007 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#include "cvc3/include/vc.h"
#include "cvc3/include/command_line_flags.h"
#include "lang/Node.h"
#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"

using namespace std;
using namespace lang;
using namespace util;
#include "SMT.h"
#include "CVC3SMT.h"

namespace dp {


CVC3SMT::CVC3SMT()
: status(CVC3::UNKNOWN)
{
   //CVC3::CLFlags flags = CVC3::ValidityChecker::createFlags();
   vc = &lang::vc; //CVC3::ValidityChecker::create(flags);
}

// stubs to avoid undefined references
/*! \brief create a new logical context */
void CVC3SMT::pushContext() {
	vc->push();
	status = CVC3::UNKNOWN;
	context = vc->stackLevel();

}

// stubs to avoid undefined references
/*! \brief create a new logical context */
void CVC3SMT::popContext() {
	vc->pop();
	status = CVC3::UNKNOWN;
	context = vc->stackLevel();

}

/*! \brief reset the current context */
void CVC3SMT::resetContext() { vc->popto(0); vc->poptoScope(0); }

/*! \brief check satisifiability under the given assumptions */

lbool CVC3SMT::Solve ()
{
#if 0
	if(status == CVC3::INVALID) {
		return l_true;
	}
#endif

	lbool result = l_undef;
	status = vc->query(vc->falseExpr());

	switch(status) {
		case CVC3::VALID:
			result = l_false;

            //			MSG(0,"UNSAT\n");
			break;
	        case CVC3::INVALID:
              //	                MSG(0,"SAT\n");
			result = l_true;

			break;
		default:
          MSG(0,"DK\n");
			break;
	}

	return result;
}


/*! \brief equivalence */
bool CVC3SMT::proveEquivalent(const CVC3::Expr& e1, const CVC3::Expr& e2)
{
	CVC3::Expr e = vc->iffExpr(e1, e2);
	return Check(e);
}

/*! \brief implication */
bool CVC3SMT::proveImplies(const vector<CVC3::Expr>& ante, const vector<CVC3::Expr>& cons)
{
    CVC3::Expr hyp (vc->andExpr(ante));
    CVC3::Expr conc(vc->orExpr(cons));
    CVC3::Expr e = vc->impliesExpr(hyp, conc);
    return Check(e);
}

/*! \brief consistency of logical context */
bool CVC3SMT::isOkay()
{
	std::vector< CVC3::Expr >   assumptions;
	return !vc->inconsistent(assumptions);
}

/*! \brief assert an expression in the current context */
void CVC3SMT::Assert(const CVC3::Expr& e)
{
  vc->assertFormula(e);
}

/*! \brief block current assignment */
void CVC3SMT::Block() {
	status = vc->checkContinue();

}



int CVC3SMT::getUnsatCore(vector<CVC3::Expr>& core) {
	vc->getAssumptionsUsed(core);
	return 1;
}


/*! \brief output logical context */
void CVC3SMT::dumpContext()
{
    vector<CVC3::Expr> assertions;
    unsigned index;

    vc->getAssumptions(assertions);

    cout << "Assumptions:" << endl;
    for (index = 0; index < assertions.size(); ++index) {
      vc->printExpr(assertions[index]);
    }

}
/*! \brief debugging output */
void CVC3SMT::Dump() {}


int CVC3SMT::getModel(const vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model) {
	if(!isOkay()) return 0;
	CVC3::ExprMap<CVC3::Expr> model1;
	vc->getConcreteModel(model1);
    
    CVC3::ExprHashMap<bool> varHash;
    for (unsigned varNr(0); varNr < vars.size(); varNr++) {
      varHash.insert(vars[varNr], true);
    }
	for(CVC3::ExprMap<CVC3::Expr>::iterator i=model1.begin();i!=model1.end();++i) {
      if (0 != varHash.count(i->first)) {
          model[i->first] = i->second;
        }
	}
	return 1;
}

bool CVC3SMT::Check(const CVC3::Expr& e, bool verbose)
{
	bool result = false;
	pushContext();
	switch(vc->query(e)) {
			case CVC3::VALID:
				result = true;
				break;
			default:
				break;
	}
	popContext();
	return result;
}


} //end of dp
