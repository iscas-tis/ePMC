/*****************************************************************************/
/*!
 * \file theorem_producer.h
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
// CLASS: Theorem_Producer
//
// AUTHOR: Sergey Berezin, 07/05/02
//
// Abstract:
//
// This class is the only one that can create new Theorem classes.
//
// Only TRUSTED code can use it; a symbol _CVC3_TRUSTED_ must be
// defined in *.cpp file before including this one; otherwise you'll
// get a compiler warning.  Custom header files (*.h) which include
// this file should NOT define _CVC3_TRUSTED_.  This practice enforces
// the programmer to be aware of which part of his/her code is
// trusted.
//
// It defines a protected NON-virtual method newTheorem() so that any
// subclass can create a new Theorem.  This means that no untrusted
// decision procedure's code should see this interface.
// Unfortunately, this has to be a coding policy rather than something
// we can enforce by C++ class structure.
//
// The intended use of this class is to make a subclass and define new
// methods corresponding to proof rules (they take theorems and
// generate new theorems).  Each decision procedure should have such a
// subclass for its trusted core.  Each new proof rule must be sound;
// that is, each new theorem that it generates must logically follow
// from the theorems in the arguments, or the new theorem must be a
// tautology.
//
// Each such subclass must also inherit from a decision
// procedure-specific abstract interface which declares the new
// methods (other than newTheorem). The decision procedure should only
// use the new abstract interface.  Thus, the DP will not even see
// newTheorem() method.
//
// This way the untrusted part of the code will not be able to create
// an unsound theorem.
//
// Proof rules may expect theorems in the arguments be of a certain
// form; if the expectations are not met, the right thing to do is to
// fail in DebugAssert with the appropriate message.  In other words,
// it is a coding bug to pass wrong theorems to the wrong rules.
//
// It is also a bug if a wrong theorem is passed but not detected by
// the proof rule, unless such checks are explicitly turned off
// globally for efficiency.
////////////////////////////////////////////////////////////////////////

#ifndef _CVC3_TRUSTED_
#warning "This file should be included only by TRUSTED code.  Define _CVC3_TRUSTED_ before including this file."
#endif

#ifndef _cvc3__theorem_producer_h_
#define _cvc3__theorem_producer_h_

#include "assumptions.h"
#include "theorem_manager.h"
#include "exception.h"

// Macro to check for soundness.  It should only be executed within a
// TheoremProducer class, and only if the -check-proofs option is set.
// When its 'cond' is violated, it will call a function which will
// eventually throw a soundness exception.
#define CHECK_SOUND(cond, msg) { if(!(cond)) \
 soundError(__FILE__, __LINE__, #cond, msg); }

// Flag whether to check soundness or not
#define CHECK_PROOFS *d_checkProofs

namespace CVC3 {

  class TheoremProducer {

  protected:
    TheoremManager* d_tm;
    ExprManager* d_em;

    // Command-line option whether to check for soundness
    const bool* d_checkProofs;
    // Operator for creating proof terms
    Op d_pfOp;
    // Expr for filling in "condition" arguments in flea proofs
    Expr d_hole;

    // Make it possible for the subclasses to create theorems directly.

    //! Create a new theorem.  See also newRWTheorem() and newReflTheorem()
    Theorem newTheorem(const Expr& thm,
		       const Assumptions& assump,
		       const Proof& pf) {
      IF_DEBUG(if(!thm.isEq() && !thm.isIff()) {
	TRACE("newTheorem", "newTheorem(", thm, ")");
	debugger.counter("newTheorem() called on equality")++;
      })
      return Theorem(d_tm, thm, assump, pf);
    }

    //! Create a rewrite theorem: lhs = rhs
    Theorem newRWTheorem(const Expr& lhs, const Expr& rhs,
			 const Assumptions& assump,
			 const Proof& pf) {
      return Theorem(d_tm, lhs, rhs, assump, pf);
    }

    //! Create a reflexivity theorem
    Theorem newReflTheorem(const Expr& e) {
      return Theorem(e);
    }

    Theorem newAssumption(const Expr& thm, const Proof& pf, int scope = -1) {
      return Theorem(d_tm, thm, Assumptions::emptyAssump(), pf, true, scope);
    }

    Theorem3 newTheorem3(const Expr& thm,
			 const Assumptions& assump,
			 const Proof& pf) {
      IF_DEBUG(if(!thm.isEq() && !thm.isIff()) {
	TRACE("newTheorem", "newTheorem3(", thm, ")");
	debugger.counter("newTheorem3() called on equality")++;
      })
      return Theorem3(d_tm, thm, assump, pf);
    }

    Theorem3 newRWTheorem3(const Expr& lhs, const Expr& rhs,
			   const Assumptions& assump,
			   const Proof& pf) {
      return Theorem3(d_tm, lhs, rhs, assump, pf);
    }

    void soundError(const std::string& file, int line,
		    const std::string& cond, const std::string& msg);

  public:
    // Constructor
    TheoremProducer(TheoremManager *tm);
    // Destructor
    virtual ~TheoremProducer() { }

    //! Testing whether to generate proofs
    bool withProof() { return d_tm->withProof(); }

    //! Testing whether to generate assumptions
    bool withAssumptions() { return d_tm->withAssumptions(); }

    //! Create a new proof label (bound variable) for an assumption (formula)
    Proof newLabel(const Expr& e);

    //////////////////////////////////////////////////////////////////
    // Functions to create proof terms
    //////////////////////////////////////////////////////////////////

    // Apply a rule named 'name' to its arguments, Proofs or Exprs
    Proof newPf(const std::string& name);
    Proof newPf(const std::string& name, const Expr& e);
    Proof newPf(const std::string& name, const Proof& pf);
    Proof newPf(const std::string& name, const Expr& e1, const Expr& e2);
    Proof newPf(const std::string& name, const Expr& e, const Proof& pf);
    Proof newPf(const std::string& name, const Expr& e1,
		const Expr& e2, const Expr& e3);
    Proof newPf(const std::string& name, const Expr& e1,
		const Expr& e2, const Proof& pf);

    // Methods with iterators.

    // Iterators are preferred to vectors, since they are often
    // efficient

    Proof newPf(const std::string& name,
		Expr::iterator begin, const Expr::iterator &end);
    Proof newPf(const std::string& name, const Expr& e,
		Expr::iterator begin, const Expr::iterator &end);
    Proof newPf(const std::string& name,
		Expr::iterator begin, const Expr::iterator &end,
		const std::vector<Proof>& pfs);

    // Methods with vectors.
    Proof newPf(const std::string& name, const std::vector<Expr>& args);
    Proof newPf(const std::string& name, const Expr& e,
		const std::vector<Expr>& args);
    Proof newPf(const std::string& name, const Expr& e,
		const std::vector<Proof>& pfs);
    Proof newPf(const std::string& name, const Expr& e1, const Expr& e2,
		const std::vector<Proof>& pfs);
    Proof newPf(const std::string& name, const std::vector<Proof>& pfs);
    Proof newPf(const std::string& name, const std::vector<Expr>& args,
		const Proof& pf);
    Proof newPf(const std::string& name, const std::vector<Expr>& args,
		const std::vector<Proof>& pfs);

    //! Creating LAMBDA-abstraction (LAMBDA label formula proof)
    /*! The label must be a variable with a formula as a type, and
     * matching the given "frm". */
    Proof newPf(const Proof& label, const Expr& frm, const Proof& pf);

    //! Creating LAMBDA-abstraction (LAMBDA label proof).
    /*! The label must be a variable with a formula as a type. */
    Proof newPf(const Proof& label, const Proof& pf);

    /*! @brief Similarly, multi-argument lambda-abstractions:
     * (LAMBDA (u1,...,un): (f1,...,fn). pf) */
    Proof newPf(const std::vector<Proof>& labels,
		const std::vector<Expr>& frms,
		const Proof& pf);

    Proof newPf(const std::vector<Proof>& labels,
		const Proof& pf);

  }; // end of Theorem_Producer class

}  // end of namespace CVC3
#endif
