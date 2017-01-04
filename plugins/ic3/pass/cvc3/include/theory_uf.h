/*****************************************************************************/
/*!
 * \file theory_uf.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Fri Jan 17 18:25:40 2003
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

#ifndef _cvc3__include__theory_uf_h_
#define _cvc3__include__theory_uf_h_

#include "theory.h"

namespace CVC3 {

class UFProofRules;

  //! Local kinds for transitive closure of binary relations
 typedef enum {
   TRANS_CLOSURE = 500,
   OLD_ARROW // for backward compatibility with old function declarations
 } UFKinds;

/*****************************************************************************/
/*!
 *\class TheoryUF
 *\ingroup Theories
 *\brief This theory handles uninterpreted functions.
 *
 * Author: Clark Barrett
 *
 * Created: Sat Feb  8 14:51:19 2003
 */
/*****************************************************************************/
class TheoryUF :public Theory {
  UFProofRules* d_rules;  
  //! Flag to include function applications to the concrete model
  const bool& d_applicationsInModel;

  // For computing transitive closure of binary relations
  typedef struct TCMapPair {
    ExprMap<CDList<Theorem>*> appearsFirstMap;
    ExprMap<CDList<Theorem>*> appearsSecondMap;
  } TCMapPair;

  ExprMap<TCMapPair*> d_transClosureMap;

  //! Backtracking list of function applications
  /*! Used for building concrete models and beta-reducing
   *  lambda-expressions. */
  CDList<Expr> d_funApplications;
  //! Pointer to the last unprocessed element (for lambda expansions)
  CDO<size_t> d_funApplicationsIdx;
  
public:
  TheoryUF(TheoryCore* core);
  ~TheoryUF();

  // Trusted method that creates the proof rules class (used in constructor).
  // Implemented in uf_theorem_producer.cpp
  UFProofRules* createProofRules();

  // Theory interface
  void addSharedTerm(const Expr& e) {}
  void assertFact(const Theorem& e);
  void checkSat(bool fullEffort);
  Theorem rewrite(const Expr& e);
  void setup(const Expr& e);
  void update(const Theorem& e, const Expr& d);
  void checkType(const Expr& e);
  Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                             bool enumerate, bool computeSize);
  void computeType(const Expr& e);
  Type computeBaseType(const Type& t);
  void computeModelTerm(const Expr& e, std::vector<Expr>& v);
  void computeModel(const Expr& e, std::vector<Expr>& vars);
  Expr computeTCC(const Expr& e);
  virtual Expr parseExprOp(const Expr& e);
  ExprStream& print(ExprStream& os, const Expr& e);

  //! Create a new LAMBDA-abstraction
  Expr lambdaExpr(const std::vector<Expr>& vars, const Expr& body);
  //! Create a transitive closure expression
  Expr transClosureExpr(const std::string& name,
			const Expr& e1, const Expr& e2);
};

}

#endif
