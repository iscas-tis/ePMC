/*****************************************************************************/
/*!
 * \file type.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Thu Dec 12 12:53:28 2002
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

// expr.h Has to be included outside of #ifndef, since it sources us
// recursively (read comments in expr_value.h).
#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__include__type_h_
#define _cvc3__include__type_h_

namespace CVC3 {

#include "os.h"

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: Type								     //
// Author: Clark Barrett                                                     //
// Created: Thu Dec 12 12:53:34 2002					     //
// Description: Wrapper around expr for api                                  //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////
class CVC_DLL Type {
  Expr d_expr;

public:
  Type() {}
  Type(Expr expr);
  //! Special constructor that doesn't check if expr is a type
  //TODO: make this private
  Type(const Type& type) :d_expr(type.d_expr) {}
  Type(Expr expr, bool dummy) :d_expr(expr) {}
  const Expr& getExpr() const { return d_expr; }

  // Reasoning about children
  int arity() const { return d_expr.arity(); }
  Type operator[](int i) const { return Type(d_expr[i]); }

  // Core testers
  bool isNull() const { return d_expr.isNull(); }
  bool isBool() const { return d_expr.getKind() == BOOLEAN; }
  bool isSubtype() const { return d_expr.getKind() == SUBTYPE; }
  bool isFunction() const { return d_expr.getKind() == ARROW; }
  //! Return cardinality of type
  Cardinality card() const { return d_expr.typeCard(); }
  //! Return nth (starting with 0) element in a finite type
  /*! Returns NULL Expr if unable to compute nth element
   */
  Expr enumerateFinite(Unsigned n) const { return d_expr.typeEnumerateFinite(n); }
  //! Return size of a finite type; returns 0 if size cannot be determined
  Unsigned sizeFinite() const { return d_expr.typeSizeFinite(); }

  // Core constructors
  static Type typeBool(ExprManager* em) { return Type(em->boolExpr(), true); }
  static Type anyType(ExprManager* em) { return Type(em->newLeafExpr(ANY_TYPE)); }
  static Type funType(const std::vector<Type>& typeDom, const Type& typeRan);
  Type funType(const Type& typeRan) const
  { return Type(Expr(ARROW, d_expr, typeRan.d_expr)); }

  // Printing
  std::string toString() const { return getExpr().toString(); }
};

inline bool operator==(const Type& t1, const Type& t2)
{ return t1.getExpr() == t2.getExpr(); }

inline bool operator!=(const Type& t1, const Type& t2)
{ return t1.getExpr() != t2.getExpr(); }

// Printing
inline std::ostream& operator<<(std::ostream& os, const Type& t) {
  return os << t.getExpr();
}

}

#endif
