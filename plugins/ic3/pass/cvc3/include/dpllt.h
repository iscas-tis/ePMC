/*****************************************************************************/
/*!
 *\file dpllt.h
 *\brief Generic DPLL(T) module
 *
 * Author: Clark Barrett
 *
 * Created: Mon Dec 12 16:28:08 2005
 */
/*****************************************************************************/

#ifndef _cvc3__include__dpllt_h_
#define _cvc3__include__dpllt_h_

#include "queryresult.h"
#include "cnf.h"
#include "cnf_manager.h"
#include "proof.h" 
#include "theory_core.h"

namespace SAT {

class DPLLT {
public:

  enum ConsistentResult {INCONSISTENT, MAYBE_CONSISTENT, CONSISTENT };

  class TheoryAPI {
  public:
    TheoryAPI() {}
    virtual ~TheoryAPI() {}

    //! Set a checkpoint for backtracking
    virtual void push() = 0;
    //! Restore most recent checkpoint
    virtual void pop() = 0;

    //! Notify theory when a literal is set to true
    virtual void assertLit(Lit l) = 0;

    //! Check consistency of the current assignment.
    /*! The result is either INCONSISTENT, MAYBE_CONSISTENT, or CONSISTENT
     * Most of the time, fullEffort should be false, and the result will most
     * likely be either INCONSISTENT or MAYBE_CONSISTENT.  To force a full
     * check, set fullEffort to true.  When fullEffort is set to true, the
     * only way the result can be MAYBE_CONSISTENT is if there are new clauses
     * to get (via getNewClauses).
     * \param cnf should be empty initially.  If INCONSISTENT is returned,
     * then cnf will contain one or more clauses ruling out the current
     * assignment when it returns.  Otherwise, cnf is unchanged.
     * \param fullEffort true for a full check, false for a fast check
     */
    virtual ConsistentResult checkConsistent(CNF_Formula& cnf, bool fullEffort) = 0;


    //! Check if the work budget has been exceeded
    /*! If true, it means that the engine should quit and return ABORT.
     * Otherwise, it should proceed normally.  This should be checked regularly.
     */
    virtual bool outOfResources() = 0;

    //! Get a literal that is implied by the current assignment.
    /*! This is theory propagation.  It can be called repeatedly and returns a
     * Null literal when there are no more literals to propagate.  It should
     * only be called when the assignment is not known to be inconsistent.
     */
    virtual Lit getImplication() = 0;

    //! Get an explanation for a literal that was implied
    /*! Given a literal l that is true in the current assignment as a result of
     * an earlier call to getImplication(), this method returns a set of clauses which
     * justifies the propagation of that literal.  The clauses will contain the
     * literal l as well as other literals that are in the current assignment.
     * The clauses are such that they would have propagated l via unit
     * propagation at the time getImplication() was called.
     * \param l the literal
     * \param c should be empty initially. */
    virtual void getExplanation(Lit l, CNF_Formula& c) = 0;

    //! Get new clauses from the theory.
    /*! This is extended theory learning.  Returns false if there are no new
     * clauses to get.  Otherwise, returns true and new clauses are added to
     * cnf.  Note that the new clauses (if any) are theory lemmas, i.e. clauses
     * that are valid in the theory and not dependent on the current
     * assignment.  The clauses may contain new literals as well as literals
     * that are true in the current assignment.
     * \param cnf should be empty initially. */
    virtual bool getNewClauses(CNF_Formula& cnf) = 0;

  };

  class Decider {
  public:
    Decider() {}
    virtual ~Decider() {}

    //! Make a decision.
    /* Returns a NULL Lit if there are no more decisions to make */
    virtual Lit makeDecision() = 0;

  };

protected:
  TheoryAPI* d_theoryAPI;
  Decider* d_decider;

public:
  //! Constructor
  /*! The client constructing DPLLT must provide an implementation of
   * TheoryAPI.  It may also optionally provide an implementation of Decider.
   * If decider is NULL, then the DPLLT class must make its own decisions.
   */
  DPLLT(TheoryAPI* theoryAPI, Decider* decider)
    : d_theoryAPI(theoryAPI), d_decider(decider) {}
  virtual ~DPLLT() {}

  TheoryAPI* theoryAPI() { return d_theoryAPI; }
  Decider* decider() { return d_decider; }

  void setDecider(Decider* decider) { d_decider = decider; }

  //! Set a checkpoint for backtracking
  /*! This should effectively save the current state of the solver.  Note that
   * it should also result in a call to TheoryAPI::push.
   */
  virtual void push() = 0;

  //! Restore checkpoint
  /*! This should return the state to what it was immediately before the last
   * call to push.  In particular, if one or more calls to checkSat,
   * continueCheck, or addAssertion have been made since the last push, these
   * should be undone.  Note also that in this case, a single call to
   * DPLLT::pop may result in multiple calls to TheoryAPI::pop.
   */
  virtual void pop() = 0;

  //! Add new clauses to the SAT solver
  /*! This is used to add clauses that form a "context" for the next call to
   * checkSat
   */
  virtual void addAssertion(const CNF_Formula& cnf) = 0;

  virtual std::vector<SAT::Lit> getCurAssignments() =0 ;
  virtual std::vector<std::vector<SAT::Lit> > getCurClauses() =0 ;

  //! Check the satisfiability of a set of clauses in the current context
  /*! If the result is SATISFIABLE, UNKNOWN, or ABORT, the DPLLT engine should
   * remain in the state it is in until pop() is called.  If the result is
   * UNSATISFIABLE, the DPLLT engine should return to the state it was in when
   * called.  Note that it should be possible to call checkSat multiple times,
   * even if the result is true (each additional call should use the context
   * left by the previous call).
   */
  virtual CVC3::QueryResult checkSat(const CNF_Formula& cnf) = 0;

  //! Continue checking the last check with additional constraints
  /*! Should only be called after a previous call to checkSat (or
   * continueCheck) that returned SATISFIABLE.  It should add the clauses in
   * cnf to the existing clause database and search for a satisfying
   * assignment.  As with checkSat, if the result is not UNSATISFIABLE, the
   * DPLLT engine should remain in the state containing the satisfiable
   * assignment until pop() is called.  Similarly, if the result is
   * UNSATISFIABLE, the DPLLT engine should return to the state it was in when
   * checkSat was last called.
   */
  virtual CVC3::QueryResult continueCheck(const CNF_Formula& cnf) = 0;

  //! Get value of variable: unassigned, false, or true
  virtual Var::Val getValue(Var v) = 0;

  //! Get the proof from SAT engine. 
  virtual CVC3::Proof getSatProof(CNF_Manager*, CVC3::TheoryCore*) = 0 ; 

};

}

#endif
