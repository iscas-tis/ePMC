/*****************************************************************************/
/*!
 *\file search_sat.h
 *\brief Search engine that uses an external SAT engine
 *
 * Author: Clark Barrett
 *
 * Created: Mon Dec  5 17:52:05 2005
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

#ifndef _cvc3__include__search_sat_h_
#define _cvc3__include__search_sat_h_

#include <vector>
#include <set>
#include "search.h"
#include "smartcdo.h"
#include "cdlist.h"
#include "cnf_manager.h"
#include "expr.h"
#include "dpllt.h"
#include "theory_core.h"
#include "formula_value.h"

namespace CVC3 {

//! Search engine that connects to a generic SAT reasoning module
/*! \ingroup SE */
class SearchSat :public SearchEngine {

  //! Name of search engine
  std::string d_name;

  //! Bottom scope for current query
  CDO<int> d_bottomScope;

  //! Last expr checked for validity
  CDO<Expr> d_lastCheck;

  /*! @brief Theorem from the last successful checkValid call.  It is
    used by getProof and getAssumptions. */
  CDO<Theorem> d_lastValid;

  //! List of all user assumptions
  CDList<Theorem> d_userAssumptions;

  //! List of all internal assumptions
  CDList<Theorem> d_intAssumptions;

  //! Index to where unprocessed assumptions start
  CDO<unsigned> d_idxUserAssump;

  TheoryCore::CoreSatAPI* d_coreSatAPI;

  //! Pointer to DPLLT implementation
  SAT::DPLLT* d_dpllt;

  //! Implementation of TheoryAPI for DPLLT
  SAT::DPLLT::TheoryAPI* d_theoryAPI;

  //! Implementation of Decider for DPLLT
  SAT::DPLLT::Decider* d_decider;

  //! Store of theorems for expressions sent to DPLLT
  CDMap<Expr, Theorem> d_theorems;

  //! Manages CNF formula and its relationship to original Exprs and Theorems
  SAT::CNF_Manager *d_cnfManager;

  //! Callback for CNF_Manager
  SAT::CNF_Manager::CNFCallback *d_cnfCallback;

  //! Cached values of variables
  std::vector<SAT::Var::Val> d_vars;

  //! Whether we are currently in a call to dpllt->checkSat
  bool d_inCheckSat;

  //! CNF Formula used for theory lemmas
  SAT::CD_CNF_Formula d_lemmas;

  //! Lemmas waiting to be translated since last call to getNewClauses()
  std::vector<std::pair<Theorem, int> > d_pendingLemmas;

  //! Scope parameter for lemmas waiting to be translated since last call to getNewClauses()
  std::vector<bool> d_pendingScopes;

  //! Backtracking size of d_pendingLemmas
  CDO<unsigned> d_pendingLemmasSize;

  //! Backtracking next item in d_pendingLemmas
  CDO<unsigned> d_pendingLemmasNext;

  //! Current position in d_lemmas
  CDO<unsigned> d_lemmasNext;

  //! List for backtracking var values
  std::vector<unsigned> d_varsUndoList;

  //! Backtracking size of d_varsUndoList
  CDO<unsigned> d_varsUndoListSize;

public:
  //! Pair of Lit and priority of this Lit
  class LitPriorityPair {
    SAT::Lit d_lit;
    int d_priority;
    LitPriorityPair() {}
  public:
    LitPriorityPair(SAT::Lit lit, int priority)
      : d_lit(lit), d_priority(priority) {}
    SAT::Lit getLit() const { return d_lit; }
    int getPriority() const { return d_priority; }
    friend bool operator<(const LitPriorityPair& p1,
                          const LitPriorityPair& p2);
  };

private:
  //! Used to determine order to find splitters
  std::set<LitPriorityPair> d_prioritySet;
  //! Current position in prioritySet
  CDO<std::set<LitPriorityPair>::const_iterator> d_prioritySetStart;
  //! Backtracking size of priority set entries
  CDO<unsigned> d_prioritySetEntriesSize;
  //! Entries in priority set in insertion order (so set can be backtracked)
  std::vector<std::set<LitPriorityPair>::iterator> d_prioritySetEntries;
  //! Backtracking size of priority set entries at bottom scope
  std::vector<unsigned> d_prioritySetBottomEntriesSizeStack;
  //! Current size of bottom entries
  unsigned d_prioritySetBottomEntriesSize;
  //! Entries in priority set in insertion order (so set can be backtracked)
  std::vector<std::set<LitPriorityPair>::iterator> d_prioritySetBottomEntries;

  //! Last Var registered with core theory
  CDO<unsigned> d_lastRegisteredVar;

  //! Whether it's OK to call DPLLT solver from the current scope
  CDO<bool> d_dplltReady;

  CDO<unsigned> d_nextImpliedLiteral;

  //! Helper class for resetting DPLLT engine
  /*! We need to be notified when the scope goes below the scope from
   *  which the last invalid call to checkValid originated.  This helper class
   *  ensures that this happens.
   */
  friend class Restorer;
  class Restorer :public ContextNotifyObj {
    SearchSat* d_ss;
    public:
      Restorer(Context* context, SearchSat* ss)
        : ContextNotifyObj(context), d_ss(ss) {}
    void notifyPre() { d_ss->restorePre(); }
    void notify() { d_ss->restore(); }
  };
  //! Instance of Restorer class
  Restorer d_restorer;

private:

  //! Get rid of bottom scope entries in prioritySet
  void restorePre();
  //! Get rid of entries in prioritySet and pendingLemmas added since last push
  void restore();
  //! Helper for addLemma and check
  bool recordNewRootLit(SAT::Lit lit, int priority = 0, bool atBottomScope = false);

  friend class SearchSatCoreSatAPI;
  friend class SearchSatCNFCallback;
  //! Core theory callback which adds a new lemma from the core theory
  void addLemma(const Theorem& thm, int priority = 0, bool atBotomScope = false);
  //! Core theory callback which asks for the bottom scope for current query
  int getBottomScope() { return d_bottomScope; }
  //! Core theory callback which suggests a splitter
  void addSplitter(const Expr& e, int priority);

  friend class SearchSatTheoryAPI;
  //! DPLLT callback to inform theory that a literal has been assigned
  void assertLit(SAT::Lit l);
  //! DPLLT callback to ask if theory has detected inconsistency.
  SAT::DPLLT::ConsistentResult checkConsistent(SAT::CNF_Formula& cnf, bool fullEffort);
  //! DPLLT callback to get theory propagations.
  SAT::Lit getImplication();
  //! DPLLT callback to explain a theory propagation.
  void getExplanation(SAT::Lit l, SAT::CNF_Formula& cnf);
  //! DPLLT callback to get more general theory clauses.
  bool getNewClauses(SAT::CNF_Formula& cnf);

  friend class SearchSatDecider;
  //! DPLLT callback to decide which literal to split on next
  SAT::Lit makeDecision();

  //! Recursively traverse DAG looking for a splitter
  /*! Returns true if a splitter is found, false otherwise.  The splitter is
   * returned in lit (lit should be set to true).  Nodes whose current value is
   * fully justified are marked by calling setFlag to avoid searching them in
   * the future.
   */
  bool findSplitterRec(SAT::Lit lit, SAT::Var::Val value,
                       SAT::Lit* litDecision);

  //! Get the value of a CNF Literal
  SAT::Var::Val getValue(SAT::Lit c) {
    DebugAssert(!c.isNull() &&
                (!c.isVar() || unsigned(c.getVar()) < d_vars.size()),
                "Lit out of bounds in getValue");
    return c.isPositive() ? d_vars[c.getVar()] :
      c.isInverted() ? SAT::Var::invertValue(d_vars[c.getVar()]) :
      c.isTrue() ? SAT::Var::TRUE_VAL : SAT::Var::FALSE_VAL;
  }

  SAT::Var::Val getValue(SAT::Var v) {
    DebugAssert(v.isVar() && unsigned(v) < d_vars.size(), "Var out of bounds in getValue");
    return d_vars[v];
  }

  //! Set the value of a variable
  void setValue(SAT::Var v, SAT::Var::Val val) {
    DebugAssert(!v.isNull(), "expected non-null Var");
    DebugAssert(unsigned(v) < d_vars.size(), "Var out of bounds in getValue");
    DebugAssert(d_vars[v] == SAT::Var::UNKNOWN, "Expected unknown");
    DebugAssert(val != SAT::Var::UNKNOWN, "Expected set to non-unknown");
    d_vars[v] = val;
    DebugAssert(d_varsUndoListSize == d_varsUndoList.size(), "Size mismatch");
    d_varsUndoList.push_back(unsigned(v));
    d_varsUndoListSize = d_varsUndoListSize + 1;
  }

  //! Check whether this variable's value is justified
  bool checkJustified(SAT::Var v)
    { return d_cnfManager->concreteLit(SAT::Lit(v)).isJustified(); }

  //! Mark this variable as justified
  void setJustified(SAT::Var v)
    { d_cnfManager->concreteLit(SAT::Lit(v)).setJustified(); }

  //! Main checking procedure shared by checkValid and restart
  QueryResult check(const Expr& e, Theorem& result, bool isRestart = false);

  //! Helper for newUserAssumptionInt
  void newUserAssumptionIntHelper(const Theorem& thm, SAT::CNF_Formula_Impl& cnf,
                                  bool atBottomScope);
  //! Helper for newUserAssumption
  Theorem newUserAssumptionInt(const Expr& e, SAT::CNF_Formula_Impl& cnf,
                               bool atBottomScope);

public:

  //! Constructor
  //! name is the name of the dpllt engine to use, as returned by getName()
  SearchSat(TheoryCore* core, const std::string& name);

  //! Destructor
  virtual ~SearchSat();

  // Implementation of virtual SearchEngine methods
  virtual const std::string& getName() { return d_name; }
  virtual void registerAtom(const Expr& e);
  virtual Theorem getImpliedLiteral();
  virtual void push() { d_dpllt->push(); }
  virtual void pop() { d_dpllt->pop(); }
  virtual QueryResult checkValid(const Expr& e, Theorem& result)
    { return check(e, result); }
  virtual QueryResult restart(const Expr& e, Theorem& result)
    { return check(e, result, true); }
  virtual void returnFromCheck();
  virtual Theorem lastThm() { return d_lastValid; }
  virtual Theorem newUserAssumption(const Expr& e);
  virtual void getUserAssumptions(std::vector<Expr>& assumptions);
  virtual void getInternalAssumptions(std::vector<Expr>& assumptions);
  virtual void getAssumptions(std::vector<Expr>& assumptions);
  virtual bool isAssumption(const Expr& e);
  virtual void getCounterExample(std::vector<Expr>& assertions,
                                 bool inOrder = true);
  virtual Proof getProof();

  //:ALEX:
  virtual FormulaValue getValue(const CVC3::Expr& e) {
    SAT::Lit l = d_cnfManager->getCNFLit(e);

    if (l.isNull()) {
      //:DEBUG:
      std::cout << "No lit for expr: " << e.toString() << std::endl;
      FatalAssert(false, "getValue");
      return UNKNOWN_VAL;
    } else {
      switch (getValue(l)) {
	case SAT::Var::TRUE_VAL: return TRUE_VAL;
	case SAT::Var::FALSE_VAL: return FALSE_VAL;
	case SAT::Var::UNKNOWN: return UNKNOWN_VAL;
      default:
	DebugAssert(false, "unreachable");
	return UNKNOWN_VAL;
      }
    }
  }

};

inline bool operator<(const SearchSat::LitPriorityPair& p1,
                      const SearchSat::LitPriorityPair& p2)
{
  if (p1.d_priority > p2.d_priority) return true;
  if (p1.d_priority < p2.d_priority) return false;
  return abs(p1.d_lit.getID()) < abs(p2.d_lit.getID()) ||
    (abs(p1.d_lit.getID()) == abs(p2.d_lit.getID()) && p1.d_lit.getID() > 0 && (!(p2.d_lit.getID() > 0)));
}

}

#endif
