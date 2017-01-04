/*****************************************************************************/
/*!
 *\file theory_datatype.h
 *
 * Author: Clark Barrett
 *
 * Created: Wed Dec  1 22:24:32 2004
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

#ifndef _cvc3__include__theory_datatype_h_
#define _cvc3__include__theory_datatype_h_

#include "theory.h"
#include "smartcdo.h"
#include "cdmap.h"

namespace CVC3 {

class DatatypeProofRules;

//! Local kinds for datatypes
  typedef enum {
    DATATYPE_DECL = 600,
    DATATYPE,
    CONSTRUCTOR,
    SELECTOR,
    TESTER
  } DatatypeKinds;

/*****************************************************************************/
/*!
 *\class TheoryDatatype
 *\ingroup Theories
 *\brief This theory handles datatypes.
 *
 * Author: Clark Barrett
 *
 * Created: Wed Dec  1 22:27:12 2004
 */
/*****************************************************************************/
class TheoryDatatype :public Theory {
protected:
  DatatypeProofRules* d_rules;

  // maps DATATYPE expressions to map containing constructors for that datatype
  ExprMap<ExprMap<unsigned> > d_datatypes;
  // maps constructor to its selectors
  ExprMap<std::vector<Expr> > d_constructorMap;
  // maps selector to a pair containing the constructor and the position of the selctor for that constructor
  ExprMap<std::pair<Expr,unsigned> > d_selectorMap;
  // maps tester to constructor that it matches
  ExprMap<Expr> d_testerMap;
  ExprMap<Op> d_reach;

  CDMap<Expr, SmartCDO<Unsigned> > d_labels;

  CDList<Theorem> d_facts;
  CDList<Expr> d_splitters;
  CDO<unsigned> d_splittersIndex;
  CDO<bool> d_splitterAsserted;
  const bool& d_smartSplits;
  ExprMap<bool> d_getConstantStack;

protected:
  virtual void instantiate(const Expr& e, const Unsigned& u);
  virtual void initializeLabels(const Expr& e, const Type& t);
  virtual void mergeLabels(const Theorem& thm, const Expr& e1, const Expr& e2);
  virtual void mergeLabels(const Theorem& thm, const Expr& e,
                           unsigned position, bool positive);

public:
  TheoryDatatype(TheoryCore* theoryCore);
  virtual ~TheoryDatatype();

  // Trusted method that creates the proof rules class (used in constructor).
  // Implemented in datatype_theorem_producer.cpp
  DatatypeProofRules* createProofRules();

  // Theory interface
  void addSharedTerm(const Expr& e);
  void assertFact(const Theorem& e);
  virtual void checkSat(bool fullEffort);
  Theorem rewrite(const Expr& e);
  virtual void setup(const Expr& e);
  virtual void update(const Theorem& e, const Expr& d);
  Theorem solve(const Theorem& e);
  void checkType(const Expr& e);
  Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                             bool enumerate, bool computeSize);
  void computeType(const Expr& e);
  void computeModelTerm(const Expr& e, std::vector<Expr>& v);
  Expr computeTCC(const Expr& e);
  Expr parseExprOp(const Expr& e);
  ExprStream& print(ExprStream& os, const Expr& e);

  // Returns Expr(DATATYPE_DECL datatype)
  Expr dataType(const std::string& name,
                const std::vector<std::string>& constructors,
                const std::vector<std::vector<std::string> >& selectors,
                const std::vector<std::vector<Expr> >& types);

  // Returns Expr(DATATYPE_DECL type_1, type_2, ...)
  Expr dataType(const std::vector<std::string>& names,
                const std::vector<std::vector<std::string> >& constructors,
                const std::vector<std::vector<std::vector<std::string> > >& selectors,
                const std::vector<std::vector<std::vector<Expr> > >& types);

  Expr datatypeConsExpr(const std::string& constructor,
                        const std::vector<Expr>& args);
  Expr datatypeSelExpr(const std::string& selector, const Expr& arg);
  Expr datatypeTestExpr(const std::string& constructor, const Expr& arg);

  const std::pair<Expr,unsigned>& getSelectorInfo(const Expr& e);
  Expr getConsForTester(const Expr& e);
  unsigned getConsPos(const Expr& e);
  Expr getConstant(const Type& t);
  const Op& getReachablePredicate(const Type& t);
  bool canCollapse(const Expr& e);

};

inline bool isDatatype(const Type& t)
  { return t.getExpr().getKind() == DATATYPE; }

inline bool isConstructor(const Expr& e)
  { return (e.getKind() == CONSTRUCTOR && e.getType().arity()==1) ||
           (e.isApply() && e.getOpKind() == CONSTRUCTOR); }

inline bool isSelector(const Expr& e)
  { return e.isApply() && e.getOpKind() == SELECTOR; }

inline bool isTester(const Expr& e)
  { return e.isApply() && e.getOpKind() == TESTER; }

inline Expr getConstructor(const Expr& e)
  { DebugAssert(isConstructor(e), "Constructor expected");
    return e.isApply() ? e.getOpExpr() : e; }

}

#endif
