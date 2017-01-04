/*****************************************************************************/
/*!
 * \file theory_array.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Wed Feb 26 18:32:06 2003
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

#ifndef _cvc3__include__theory_array_h_
#define _cvc3__include__theory_array_h_

#include "theory_core.h"

namespace CVC3 {

class ArrayProofRules;

 typedef enum {
   ARRAY = 2000,
   READ,
   WRITE,
   // Array literal [ [type] e ]; creates a constant array holding 'e'
   // in all elements: (CONST_ARRAY type e)
   ARRAY_LITERAL
 } ArrayKinds;

/*****************************************************************************/
/*!
 *\class TheoryArray
 *\ingroup Theories
 *\brief This theory handles arrays.
 *
 * Author: Clark Barrett
 *
 * Created: Thu Feb 27 00:38:20 2003
 */
/*****************************************************************************/
class TheoryArray :public Theory {
  ArrayProofRules* d_rules;

  //! Backtracking list of array reads, for building concrete models.
  CDList<Expr> d_reads;
  //! Set of renaming theorems \f$\exists x. t = x\f$ indexed by t
  ExprMap<Theorem> d_renameThms;
  //! Flag to include array reads to the concrete model
  const bool& d_applicationsInModel;
  //! Flag to lift ite's over reads
  const bool& d_liftReadIte;

  //! Backtracking database of subterms of shared terms
  CDMap<Expr,Expr> d_sharedSubterms;
  //! Backtracking database of subterms of shared terms
  CDList<Expr> d_sharedSubtermsList;
  //! Used in checkSat
  CDO<unsigned> d_index;

  //! Flag for use in checkSat
  int d_inCheckSat;

  // Private methods
  Theorem renameExpr(const Expr& e);

  //! Derived rule
  // w(...,i,v1,...,) => w(......,i,v1')
  // Returns Null Theorem if index does not appear
  Theorem pullIndex(const Expr& e, const Expr& index);

public:
  TheoryArray(TheoryCore* core);
  ~TheoryArray();

  // Trusted method that creates the proof rules class (used in constructor).
  // Implemented in array_theorem_producer.cpp
  ArrayProofRules* createProofRules();

  // Theory interface
  void addSharedTerm(const Expr& e);
  void assertFact(const Theorem& e);
  void checkSat(bool fullEffort);
  Theorem rewrite(const Expr& e);
  void setup(const Expr& e);
  void update(const Theorem& e, const Expr& d);
  Theorem solve(const Theorem& e);
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
};

// Array testers
inline bool isArray(const Type& t) { return t.getExpr().getKind() == ARRAY; }
inline bool isRead(const Expr& e) { return e.getKind() == READ; }
inline bool isWrite(const Expr& e) { return e.getKind() == WRITE; }
inline bool isArrayLiteral(const Expr& e)
  { return (e.isClosure() && e.getKind() == ARRAY_LITERAL); }

// Array constructors
inline Type arrayType(const Type& type1, const Type& type2)
  { return Type(Expr(ARRAY, type1.getExpr(), type2.getExpr())); }

// Expr read(const Expr& arr, const Expr& index);
// Expr write(const Expr& arr, const Expr& index, const Expr& value);
Expr arrayLiteral(const Expr& ind, const Expr& body);
} // end of namespace CVC3

#endif
