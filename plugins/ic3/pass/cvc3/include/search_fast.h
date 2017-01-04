///////////////////////////////////////////////////////////////////////////////
/*!
 * \file search_fast.h
 *
 * Author: Mark Zavislak
 *
 * Created: Mon Jul 21 17:33:18 UTC 2003
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
 * A faster implementation of the proof search engine
 */
///////////////////////////////////////////////////////////////////////////////

#ifndef _cvc3__include__search_fast_h_
#define _cvc3__include__search_fast_h_

#include <deque>
#include <utility>
#include "search_impl_base.h"
#include "variable.h"
#include "circuit.h"
#include "statistics.h"
#include <set>
#include "smartcdo.h"

namespace CVC3 {

  class VariableManager;
  class DecisionEngine;

////////////////////////////////////////////////////////////////////////////
//////////// Definition of modules (groups) for doxygen ////////////////////
////////////////////////////////////////////////////////////////////////////

/*!
 * \defgroup SE_Fast Fast Search Engine
 * \ingroup SE
 *
 * This module includes all the components of the fast search
 * engine.
 * @{
 */

  //! Implementation of a faster search engine, using newer techniques.
  /*!

  This search engine is engineered for greater speed.  It seeks to
  eliminate the use of recursion, and instead replace it with
  iterative procedures that have cleanly defined invariants.  This
  will hopefully not only eliminate bugs or inefficiencies that have
  been difficult to track down in the default version, but it should
  also make it easier to trace, read, and understand.  It strives to
  be in line with the most modern SAT techniques.

  There are three other significant changes.

  One, we want to improve the performance on heavily non-CNF problems.
  Unlike the older CVC, CVC3 does not expand problems into CNF and
  solve, but rather uses decision procedures to effect the same thing,
  but often much more slowly.  This search engine will leverage off
  knowledge gained from the DPs in the form of conflict clauses as
  much as possible.

  Two, the solver has traditionally had a difficult time getting
  started on non-CNF problems.  Modern satsolvers also have this
  problem, and so they employ "restarts" to try solving the problem
  again after gaining more knowledge about the problem at hand.  This
  allows a more accurate choice of splitters, and in the case of
  non-CNF problems, the solver can immediately leverage off CNF
  conflict clauses that were not initially available.

  Third, this code is specifically designed to deal with the new
  dependency tracking.  Lazy maps will be eliminated in favor implicit
  conflict graphs, reducing computation time in two different ways.

  */

  class SearchEngineFast : public SearchImplBase {

    friend class Circuit;

  /*! \addtogroup SE_Fast
   * @{
   */

    //! Name
    std::string d_name;
    //! Decision Engine
    DecisionEngine *d_decisionEngine;
    //! Total number of unit propagations
    StatCounter d_unitPropCount;
    //! Total number of circuit propagations
    StatCounter d_circuitPropCount;
    //! Total number of conflicts
    StatCounter d_conflictCount;
    //! Total number of conflict clauses generated (not all may be active)
    StatCounter d_conflictClauseCount;

    //! Backtrackable list of clauses.
    /*! New clauses may come into play
      from the decision procedures that are context dependent. */
    CDList<ClauseOwner> d_clauses;

    //! Backtrackable set of pending unprocessed literals.
    /*! These can be discovered at any scope level during conflict
      analysis. */
    CDMap<Expr,Theorem> d_unreportedLits;
    CDMap<Expr,bool> d_unreportedLitsHandled;

    //! Backtrackable list of non-literals (non-CNF formulas).
    /*! We treat nonliterals like clauses, because they are a superset
        of clauses.  We stick the real clauses into d_clauses, but all
        the rest have to be stored elsewhere. */
    CDList<SmartCDO<Theorem> > d_nonLiterals;
    CDMap<Expr,Theorem> d_nonLiteralsSaved; //!< prevent reprocessing
    //    CDMap<Expr,bool> d_nonLiteralSimplified; //!< Simplified non-literals

    //! Theorem which records simplification of the last query
    CDO<Theorem> d_simplifiedThm;

    //! Size of d_nonLiterals before most recent query
    CDO<unsigned> d_nonlitQueryStart;
    //! Size of d_nonLiterals after query (not including DP-generated non-literals)
    CDO<unsigned> d_nonlitQueryEnd;
    //! Size of d_clauses before most recent query
    CDO<unsigned> d_clausesQueryStart;
    //! Size of d_clauses after query
    CDO<unsigned> d_clausesQueryEnd;

    //! Array of conflict clauses: one deque for each outstanding query
    std::vector<std::deque<ClauseOwner>* > d_conflictClauseStack;
    //! Reference to top deque of conflict clauses
    std::deque<ClauseOwner>* d_conflictClauses;

    //! Helper class for managing conflict clauses
    /*! Conflict clauses should only get popped when the context in which a
     *  call to checkValid originates is popped.  This helper class checks
     *  every time there's a pop to see if the conflict clauses need to be
     *  restored.
     */
    friend class ConflictClauseManager;
    class ConflictClauseManager :public ContextNotifyObj {
      SearchEngineFast* d_se;
      std::vector<int> d_restorePoints;
    public:
      ConflictClauseManager(Context* context, SearchEngineFast* se)
        : ContextNotifyObj(context), d_se(se) {}
      void setRestorePoint();
      void notify();
    };
    ConflictClauseManager d_conflictClauseManager;

    //! Unprocessed unit conflict clauses
    /*! When we find unit conflict clauses, we are automatically going
        to jump back to the original scope.  Hopefully we won't find
        multiple ones, but you never know with those wacky decision
        procedures just spitting new information out.  These are then
        directly asserted then promptly forgotten about.  Chaff keeps
        them around (for correctness maybe), but we already have the
        proofs stored in the literals themselves. */
    std::vector<Clause> d_unitConflictClauses;


    //! Set of literals to be processed by bcp.
    /*! These are emptied out upon backtracking, because they can only
        be valid if they were already all processed without conflicts.
        Therefore, they don't need to be context dependent. */
    std::vector<Literal> d_literals;
    //! Set of asserted literals which may survive accross checkValid() calls
    /*!
     *  When a literal is asserted outside of checkValid() call, its
     *  value is remembered in a Literal database, but the literal
     *  queue for BCP is cleared.  We add literals to this set at the
     *  proper scope levels, and propagate them at the beginning of a
     *  checkValid() call.
     */
    CDMap<Expr,Literal> d_literalSet;

    //! Queue of derived facts to be sent to DPs
    /*! \sa addFact() and commitFacts() */
    std::vector<Theorem> d_factQueue;
    /*! @brief When true, use TheoryCore::enqueueFact() instead of
     * addFact() in commitFacts()
     */
    bool d_useEnqueueFact;
    //! True when checkSAT() is running
    /*! Used by addLiteralFact() to determine whether to BCP the
     *  literals immediately (outside of checkSAT()) or not.
     */
    bool d_inCheckSAT;


    //! Set of alive literals that shouldn't be garbage-collected
    /*! Unfortunately, I have a keep-alive issue.  I think literals
        actually have to hang around, so I assert them to a separate
        d_litsAlive CDList. */
    CDList<Literal> d_litsAlive;

    /*! @brief Mappings of literals to vectors of pointers to the
      corresponding watched literals.  */
    /*! A pointer is a pair (clause,i), where 'i' in {0,1} is the index
      of the watch pointer in the clause.
    */
    // ExprHashMap<std::vector<std::pair<Clause, int> > > d_wp;

    std::vector<Circuit*> d_circuits;
    ExprHashMap<std::vector<Circuit*> > d_circuitsByExpr;

    //! The scope of the last conflict
    /*! This is the true scope of the conflict, not necessarily the
      scope where the conflict was detected. */
    int d_lastConflictScope;
    //! The last conflict clause (for checkSAT()).  May be Null.
    /*! It records which conflict clause must be processed by BCP after
      backtracking from a conflict.  A conflict may generate several
      conflict clauses, but only one of them will cause a unit
      propagation.
    */
    Clause d_lastConflictClause;
    //! Theorem(FALSE) which generated a conflict
    Theorem d_conflictTheorem;

    /*! @brief Return a ref to the vector of watched literals.  If no
      such vector exists, create it. */
    std::vector<std::pair<Clause, int> >& wp(const Literal& literal);

    /*! @brief \return true if SAT, false otherwise.
     *
     * When false is returned, proof is saved in d_lastConflictTheorem */
    QueryResult checkSAT();

    //! Choose a splitter.
    /*! Preconditions: The current context is consistent.
     *
     * \return true if splitter available, and it asserts it through
     * newIntAssumption() after first pushing a new context.
     *
     * \return false if no splitters are available, which means the
     * context is SAT.
     *
     * Postconditions: A literal has been asserted through
     * newIntAssumption().
     */
    bool split();

    // Moved from the decision engine:
    //! Returns a splitter
    Expr findSplitter();
    //! Position of a literal with max score in d_litsByScores
    unsigned d_litsMaxScorePos;
    //! Vector of literals sorted by score
    std::vector<Literal> d_litsByScores;
    /*
    //! Mapping of literals to scores
    ExprHashMap<unsigned> d_litScores;
    //! Mapping of literals to their counters
    ExprHashMap<unsigned> d_litCounts;
    //! Mapping of literals to previous counters (what's that, anyway?)
    ExprHashMap<unsigned> d_litCountPrev;
    */
    //! Internal splitter counter for delaying updateLitScores()
    int d_splitterCount;
    //! Internal (decrementing) count of added splitters, to sort d_litByScores
    int d_litSortCount;

    //! Flag to switch on/off the berkmin heuristic
    const bool d_berkminFlag;

    //! Clear the list of asserted literals (d_literals)
    void clearLiterals();

    void updateLitScores(bool firstTime);
    //! Add the literals of a new clause to d_litsByScores
    void updateLitCounts(const Clause& c);


    //! Boolean constraint propagation.
    /*! Preconditions: On every run besides the first, the CNF clause
     *  database must not have any unit or unsat clauses, and there
     *  must be a literal queued up for processing.  The current
     *  context must be consistent.  Any and all assertions and
     *  assignments must actually be made within the bcp loop.  Other
     *  parts of the solver may queue new facts with addLiteralFact()
     *  and addNonLiteralFact().  bcp() will either process them, or
     *  it will find a conflict, in which case they will no longer be
     *  valid and will be dumped.  Any nonLiterals must already be
     *  simplified.
     *
     *  Description: BCP will systematically work through all the
     *  literals and nonliterals, using boolean constraint propagation
     *  by detecting unit clauses and using addLiteralFact() on the
     *  unit literal while also marking the clause as SAT.  Any
     *  clauses marked SAT are guaranteed to be SAT, but clauses not
     *  marked SAT are not guaranteed to be unsat.
     *
     * \return false if a conflict is found, true otherwise.
     *
     *  Postconditions: False indicates conflict.  If the conflict was
     *  discovered in CNF, we call the proof rule, then store that
     *  clause pointer so fixConflict() can skip over it during the
     *  search (when we finally change dependency tracking).
     *
     *  True indicates success.  All literals and nonLiterals have
     *  been processed without causing a conflict.  Processing
     *  nonliterals implies running simplify on them, immediately
     *  asserting any simplifications back to the core, and marking
     *  the original nonLiteral as simplified, to be ignored by all
     *  future (this context or deeper) splitters and bcp runs.
     *  Therefore, there will be no unsimplified nonliterals
     *  remaining.
     */
    bool bcp();

    //! Determines backtracking level and adds conflict clauses.
    /*! Preconditions: The current context is inconsistent.  If it
     *  resulted from a conflictRule() application, then the theorem
     *  is stored inside d_lastConflictTheorem.
     *
     *  If this was caused from bcp, we obtain the conflictRule()
     *  theorem from the d_lastConflictTheorem instance variable.
     *  From here we build conflict clauses and determine the correct
     *  backtracking level, at which point we actually backtrack
     *  there.  Finally, we also call addLiteralFact() on the "failure
     *  driven assertion" literal so that bcp has some place to begin
     *  (and it satisfies the bcp preconditions)
     *
     *  Postconditions: If True is returned: The current context is
     *  consistent, and a literal is queued up for bcp to process.  If
     *  False is returned: The context cannot be made consistent
     *  without backtracking past the original one, so the formula is
     *  unsat.
     */
    bool fixConflict();
    //! FIXME: document this
    void assertAssumptions();
    //! Queue up a fact to assert to the DPs later
    void enqueueFact(const Theorem& thm);
    //! Set the context inconsistent.  Takes Theorem(FALSE).
    void setInconsistent(const Theorem& thm);
    //! Commit all the enqueued facts to the DPs
    void commitFacts();
    //! Clear the local fact queue
    void clearFacts();

    /*! @name Processing a Conflict */
    //@{
    /*! @brief Take a conflict in the form of Literal, or
        Theorem, and generate all the necessary conflict clauses. */
    Theorem processConflict(const Literal& l);
    Theorem processConflict(const Theorem& thm);
    //@}

    //! Auxiliary function for unit propagation
    bool propagate(const Clause &c, int idx, bool& wpUpdated);
    //! Do the unit propagation for the clause
    void unitPropagation(const Clause &c, unsigned idx);
    //! Analyse the conflict, find the UIPs, etc.
    void analyzeUIPs(const Theorem &falseThm, int conflictScope);

    /////////////////////////////
    // New convenience methods //
    /////////////////////////////

    //! Go through all the clauses and check the watch pointers (for debugging)
    IF_DEBUG(void fullCheck();)
    //! Set up the watch pointers for the new clause
    void addNewClause(Clause &c);
    //! Process a new derived fact (auxiliary function)
    void recordFact(const Theorem& thm);

    //! First pass in conflict analysis; takes a theorem of FALSE
    void traceConflict(const Theorem& conflictThm);
    //! Private helper function for checkValid and restart
    QueryResult checkValidMain(const Expr& e2);

  public:
    //! The main Constructor
    SearchEngineFast(TheoryCore* core);
    //! Destructor
    virtual ~SearchEngineFast();

    const std::string& getName() { return d_name; }
    //! Implementation of the API call
    virtual QueryResult checkValidInternal(const Expr& e);
    virtual QueryResult restartInternal(const Expr& e);
    //! Redefine the counterexample generation.
    virtual void getCounterExample(std::vector<Expr>& assertions);
    //! Notify the search engine about a new literal fact.
    void addLiteralFact(const Theorem& thm);
    //! Notify the search engine about a new non-literal fact.
    void addNonLiteralFact(const Theorem& thm);
    /*! @brief Redefine newIntAssumption(): we need to add the new theorem
      to the appropriate Literal */
    virtual Theorem newIntAssumption(const Expr& e);
    virtual bool isAssumption(const Expr& e);
    void addSplitter(const Expr& e, int priority);

  /*! @} */ // end of addtogroup SE_Fast

    //! Return next clause whose satisfiability is unknown
    //virtual Clause nextClause();
    //! Return next non-clause which does not reduce to false
    //virtual Expr nextNonClause();
  };
/*! @} */ // end of SE_Fast
}


#endif
