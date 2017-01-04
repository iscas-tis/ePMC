/*****************************************************************************/
/*!
 * \file expr_op.h
 * \brief Class Op representing the Expr's operator.
 * 
 * Author: Sergey Berezin
 * 
 * Created: Fri Feb  7 15:14:42 2003
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

#ifndef _cvc3__expr_op_h_
#define _cvc3__expr_op_h_

namespace CVC3 {

  class ExprManager;

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: Op								     //
// Author: Clark Barrett                                                     //
// Created: Wed Nov 27 15:50:38 2002					     //
// Description: Encapsulates all possible Expr operators (including UFUNC)   //
//              and allows switching on the kind.                            //
//              Kinds should be registered with ExprManager.                 //
// 
// Technically, class Op is not part of Expr; it is provided as an
// abstraction for the user.  So, building an Expr from an Op is less
// efficient than building the same Expr directly from the kind.
///////////////////////////////////////////////////////////////////////////////
class Op {
  friend class Expr;
  friend class ExprApply;
  friend class ExprApplyTmp;
  friend class ::CInterface;

  int d_kind;
  Expr d_expr;

  // Disallow silent conversion of expr to op
  //! Constructor for operators
  Op(const Expr& e): d_kind(APPLY), d_expr(e) { }

public:
/////////////////////////////////////////////////////////////////////////
// Public methods
/////////////////////////////////////////////////////////////////////////

  Op() : d_kind(NULL_KIND) { }
  // Construct an operator from a kind.
  Op(int kind) : d_kind(kind), d_expr()
    { DebugAssert(kind != APPLY, "APPLY cannot be an operator on its own"); }
  // Copy constructor
  Op(const Op& op): d_kind(op.d_kind), d_expr(op.d_expr) { }
  // A constructor that rebuilds the Op for the given ExprManager
  Op(ExprManager* em, const Op& op);
  // Destructor (does nothing)
  ~Op() { }
  // Assignment operator
  Op& operator=(const Op& op);

  // Check if Op is NULL
  bool isNull() const { return d_kind == NULL_KIND; }
  // Return the kind of the operator
  int getKind() const { return d_kind; }
  // Return the expr associated with this operator if applicable.
  const Expr& getExpr() const
    { DebugAssert(d_kind == APPLY, "Expected APPLY"); return d_expr; }

  // Printing functions.

  std::string toString() const;
  friend std::ostream& operator<<(std::ostream& os, const Op& op) {
    return os << "Op(" << op.d_kind << " " << op.d_expr << ")";
  }
  friend bool operator==(const Op& op1, const Op& op2) {
    return op1.d_kind == op2.d_kind && op1.d_expr == op2.d_expr;
  }

}; // end of class Op


} // end of namespace CVC3

#endif
