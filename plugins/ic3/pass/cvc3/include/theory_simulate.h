/*****************************************************************************/
/*!
 *\file theory_simulate.h
 *\brief Implementation of a symbolic simulator
 *
 * Author: Sergey Berezin
 *
 * Created: Tue Oct  7 10:13:15 2003
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 * 
 * <hr>
 * 
 */
/*****************************************************************************/

#ifndef _cvc3__include__theory_simulate_h_
#define _cvc3__include__theory_simulate_h_

#include "theory.h"

namespace CVC3 {

class SimulateProofRules;

/*****************************************************************************/
/*!
 * \class TheorySimulate
 * \ingroup Theories
 * \brief "Theory" of symbolic simulation.
 *
 * Author: Sergey Berezin
 *
 * Created: Tue Oct  7 10:13:15 2003
 *
 * This theory owns the SIMULATE operator.  It's job is to replace the above
 * expressions by their definitions using rewrite rules.
 */
/*****************************************************************************/

class TheorySimulate: public Theory {
private:
  //! Our local proof rules
  SimulateProofRules* d_rules;
  //! Create proof rules for this theory
  SimulateProofRules* createProofRules();
public:
  //! Constructor
  TheorySimulate(TheoryCore* core);
  //! Destructor
  ~TheorySimulate();
  // The required Theory API functions
  void assertFact(const Theorem& e) { }
  void checkSat(bool fullEffort) { }
  Theorem rewrite(const Expr& e);
  void computeType(const Expr& e);
  Expr computeTCC(const Expr& e);
  Expr parseExprOp(const Expr& e);
  ExprStream& print(ExprStream& os, const Expr& e);
}; // end of class TheorySimulate

} // end of namespace CVC3

#endif
