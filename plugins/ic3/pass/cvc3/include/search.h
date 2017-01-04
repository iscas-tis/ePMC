/*****************************************************************************/
/*!
 * \file search.h
 * \brief Abstract API to the proof search engine
 *
 * Author: Clark Barrett, Vijay Ganesh (Clausal Normal Form Converter)
 *
 * Created: Fri Jan 17 13:35:03 2003
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

#ifndef _cvc3__include__search_h_
#define _cvc3__include__search_h_

#include <vector>
#include "queryresult.h"
#include "cdo.h"
#include "formula_value.h"

namespace CVC3 {

class SearchEngineRules;
class Theorem;
class Expr;
class Proof;
class TheoryCore;
class CommonProofRules;

template<class Data> class ExprMap;

  /*! \defgroup SE Search Engine
   * \ingroup VC
   * The search engine includes Boolean reasoning and coordinates with theory
   * reasoning.  It consists of a generic abstract API (class SearchEngine) and
   * subclasses implementing concrete instances of search engines.
   */

  //! API to to a generic proof search engine
  /*! \ingroup SE */
class SearchEngine {

protected:
  /*! \addtogroup SE
   * @{
   */

  //! Access to theory reasoning
  TheoryCore* d_core;

  //! Common proof rules
  CommonProofRules* d_commonRules;

  //! Proof rules for the search engine
  SearchEngineRules* d_rules;

  //! Create the trusted component
  /*! This function is defined in search_theorem_producer.cpp */
  SearchEngineRules* createRules();

 public:

  //! Constructor
  SearchEngine(TheoryCore* core);

  //! Destructor
  virtual ~SearchEngine();

  //! Name of this search engine
  virtual const std::string& getName() = 0;

  //! Accessor for common rules
  CommonProofRules* getCommonRules() { return d_commonRules; }

  //! Accessor for TheoryCore
  TheoryCore* theoryCore() { return d_core; }

  //! Register an atomic formula of interest.
  /*! Registered atoms are tracked by the decision procedures.  If one of them
      is deduced to be true or false, it is added to a list of implied literals.
      Implied literals can be retrieved with the getImpliedLiteral function */
  virtual void registerAtom(const Expr& e) = 0;

  //! Return next literal implied by last assertion.  Null Expr if none.
  /*! Returned literals are either registered atomic formulas or their negation
   */
  virtual Theorem getImpliedLiteral() = 0;

  //! Push a checkpoint
  virtual void push() = 0;

  //! Restore last checkpoint
  virtual void pop() = 0;

  //! Checks the validity of a formula in the current context
  /*! If the query is valid, it returns VALID, the result parameter contains
   *  the corresponding theorem, and the scope and context are the same
   *  as when called.  If it returns INVALID, the context will be one which
   *  falsifies the query.  If it returns UNKNOWN, the context will falsify the
   *  query, but the context may be inconsistent.  Finally, if it returns
   *  ABORT, the context will be one which satisfies as much as
   *  possible.
   * \param e the formula to check.
   * \param result the resulting theorem, if the formula is valid.
 */
  virtual QueryResult checkValid(const Expr& e, Theorem& result) = 0;

  //! Reruns last check with e as an additional assumption
  /*! This method should only be called after a query which is invalid.
   * \param e the additional assumption
   * \param result the resulting theorem, if the query is valid.
   */
  virtual QueryResult restart(const Expr& e, Theorem& result) = 0;

  //! Returns to context immediately before last call to checkValid
  /*! This method should only be called after a query which returns something
   * other than VALID.
   */
  virtual void returnFromCheck() = 0;

  //! Returns the result of the most recent valid theorem
  /*! Returns Null Theorem if last call was not valid */
  virtual Theorem lastThm() = 0;

  /*! @brief Generate and add an assumption to the set of
   * assumptions in the current context. */
  /*! By default, the assumption is added at the current scope.  The default
   * can be overridden by specifying the scope parameter. */
  virtual Theorem newUserAssumption(const Expr& e) = 0;

  //! Get all user assumptions made in this and all previous contexts.
  /*! User assumptions are created either by calls to newUserAssumption or
   * a call to checkValid.  In the latter case, the negated query is added
   * as an assumption.
   * \param assumptions should be empty on entry.
  */
  virtual void getUserAssumptions(std::vector<Expr>& assumptions) = 0;

  //! Get assumptions made internally in this and all previous contexts.
  /*! Internal assumptions are literals assumed by the sat solver.
   * \param assumptions should be empty on entry.
  */
  virtual void getInternalAssumptions(std::vector<Expr>& assumptions) = 0;

  //! Get all assumptions made in this and all previous contexts.
  /*! \param assumptions should be an empty vector which will be filled \
    with the assumptions */
  virtual void getAssumptions(std::vector<Expr>& assumptions) = 0;

  //! Check if the formula has already been assumed previously
  virtual bool isAssumption(const Expr& e) = 0;

  //! Will return the set of assertions which make the queried formula false.
  /*! This method should only be called after an query which returns INVALID.
   * It will try to return the simplest possible set of assertions which are
   * sufficient to make the queried expression false.
   * \param assertions should be empty on entry.
   * \param inOrder if true, returns the assertions in the order they were
   * asserted.  This is slightly more expensive than inOrder = false.
  */
  virtual void getCounterExample(std::vector<Expr>& assertions,
                                 bool inOrder = true) = 0;

  //! Returns the proof term for the last proven query
  /*! It should be called only after a query which returns VALID.
   * In any other case, it returns Null. */
  virtual Proof getProof() = 0;

  /*! @brief Build a concrete Model (assign values to variables),
   * should only be called after a query which returns INVALID. */
  void getConcreteModel(ExprMap<Expr>& m);

  /*! @brief Try to build a concrete Model (assign values to variables),
   * should only be called after a query which returns UNKNOWN.
   * Returns a theorem if inconsistent */
  bool tryModelGeneration(Theorem& thm);

  //:ALEX: returns the current truth value of a formula
  // returns CVC3::UNKNOWN_VAL if e is not associated
  // with a boolean variable in the SAT module,
  // i.e. if its value can not determined without search.
  virtual FormulaValue getValue(const CVC3::Expr& e) = 0;

  /* @} */ // end of group SE

};


}

#endif
