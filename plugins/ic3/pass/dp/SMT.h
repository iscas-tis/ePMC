/**************************** CPPHeaderFile ***************************

* FileName [SMT.h]

* PackageName [parser]

* Synopsis [Header file for SMT classes.]

* Description [These classes encapsulate an SMT solver.]

* SeeAlso []

* Author [Bjoern Wachter]

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

#ifndef __SMT_H
#define __SMT_H

namespace dp {

#include "util/Cube.h"

struct CubeConsumer {
public:
	virtual void consume(const Cube&) = 0;
};



/*! \brief frontend to a satisfiability modulo theory (SMT) solver
    \note Some of the features used by PASS
	* model generation
	* unsatisfiable cores
	* interpolation
 */
class SMT {
public:

	/* manage logical contexts */
	/*! \brief reset the current context */
	virtual void resetContext() {}
	/*! \brief create a new logical context */
	virtual void pushContext() {}
	/*! \brief delete the current context
	    \pre  there is a current context
	*/
	virtual void popContext() {}
	/*! \brief consistency of current logical context */
	virtual bool isOkay() { return false; }

	/* assertions */
	/*! \brief assert an expression in the current context */
	virtual void Assert(const CVC3::Expr& e) {}
	/*! \brief assert a vector of expressions in current context */
	virtual void Assert(const std::vector<CVC3::Expr>& v);

	/* SAT checking */
	/*! \brief check satisfiability */
	virtual lbool Solve() { assert(false); return l_undef; }
	/*! \brief block given model */
	virtual void blockModel(CVC3::ExprHashMap<CVC3::Expr>& model);


	/*! \brief block current assignment */
	virtual void Block() {}

	/* AllSAT (solution enumeration) */
	virtual bool allSat ( const std::vector<CVC3::Expr>& v,
			      CubeConsumer&,
			      unsigned int limit = 500);

	/* model and witness generation */
	/*! \brief get the model of the last satisfiable query
	    \param vars variables of interest
	    \warning Variables that do not appear in the expression are not assigned a value.
	*/
	virtual int getModel(const std::vector<CVC3::Expr>& vars, CVC3::ExprHashMap<CVC3::Expr>& model) { return 0; }
	/*! \brief get the value of a variable after successful solving */
	virtual void getValue(const CVC3::Expr& e, CVC3::Expr& value) {}
	/*! \brief get the unsat core */
	virtual lbool getUnsatCore(const std::vector<CVC3::Expr>& vec, std::vector<CVC3::Expr>& core ) { return l_undef; }
	/*! \brief compute interpolant
	    \warning only FOCI, CSIsat, CLP-Prover */
	virtual lbool Interpolate(const std::vector<CVC3::Expr>& f, std::vector<CVC3::Expr>& result) { return l_undef; }

	/* discharge proof obligations */
	/*! \brief equivalence */
	virtual bool proveEquivalent(const CVC3::Expr&,const CVC3::Expr&) { return false; }

	/* debugging */
	/*! \brief output logical context */
	virtual void dumpContext() {}
	/*! \brief debugging output */
	virtual void Dump() {}
	/*! \brief constructor initializes the solver */
	SMT() {}
	/*! \brief destructor shuts the solver down */
	virtual ~SMT() {}

	static SMT& smt;

	/** Code that starts different SMT solvers */
	static SMT& getYices();

	/** get instance of MathSat */
	static SMT& getMathSat();

	static SMT& getDummy();

	/** invoke CVC3 */
	static SMT& getCVC3();

	/** invoke CLP-Prover */
	static SMT& getCLP();

	/** invoke CSIsat */
	static SMT& getCSIsat();

	/** invoke FOCI interpolating theorem prover */
	static SMT& getFOCI();

	static void setSMT(SMT& __smt);

	/** get global SMT instance */
	static SMT& getSMT();

	/** get interpolation procedure */
	static SMT& getInterpolator();

};

} //end of namespace dp

#endif
