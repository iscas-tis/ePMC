/*****************************************************************************/
/*!
 * \file proof.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Dec 10 00:37:49 GMT 2002
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
// CLASS: Proof
//
// AUTHOR: Sergey Berezin, 12/03/2002
//
// Abstract:
//
// Proof is a wrapper around Expr, to prevent accidental mix-up.  Only
// proof rules and adaptors are supposed to use any of its methods.
///////////////////////////////////////////////////////////////////////////////
#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__proof_h_
#define _cvc3__proof_h_

namespace CVC3 {

  class Proof {
  private:
    Expr d_proof;
    //    unsigned d_insts; //by yeting, this is to store the number of instantiations. debug only
  public:
    Proof(const Expr &e) : d_proof(e) { } // Constructor
    Proof(const Proof& p) : d_proof(p.d_proof) { } // Copy constructor
    Proof() : d_proof() { } // Null proof constructor
    Expr getExpr() const { return d_proof; } // Extract the expr handle
    bool isNull() const { return d_proof.isNull(); }
    // Printing
    friend std::ostream& operator<<(std::ostream& os, const Proof& pf);
    std::string toString() const {
      std::ostringstream ss;
      ss<<(*this);
      return ss.str();
    }
  }; // End of class Proof

  inline std::ostream& operator<<(std::ostream& os, const Proof& pf) {
    return os << "Proof(" << pf.getExpr() << ")";
  }

  inline bool operator==(const Proof& pf1, const Proof& pf2) {
    return pf1.getExpr() == pf2.getExpr();
  }

} // end of namespace CVC3
#endif
