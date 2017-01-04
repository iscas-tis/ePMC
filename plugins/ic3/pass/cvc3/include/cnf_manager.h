/*****************************************************************************/
/*!
 *\file cnf_manager.h
 *\brief Manager for conversion to and traversal of CNF formulas
 *
 * Author: Clark Barrett
 *
 * Created: Thu Dec 15 13:53:16 2005
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 * 
 * <hr>
 */
/*****************************************************************************/

#ifndef _cvc3__include__cnf_manager_h_
#define _cvc3__include__cnf_manager_h_

#include "cnf.h"
#include "expr.h"
#include "expr_map.h"
#include "cdmap.h"
#include "statistics.h"

namespace CVC3 {

class CommonProofRules;
class CNF_Rules;
class ValidityChecker;
class Statistics;

}

namespace SAT {

class CNF_Manager {

  //! For clause minimization
  CVC3::ValidityChecker* d_vc;

  //! Whether to use brute-force clause minimization
  bool d_minimizeClauses;

  //! Common proof rules
  CVC3::CommonProofRules* d_commonRules;

  //! Rules for manipulating CNF
  CVC3::CNF_Rules* d_rules;

  //! Information kept for each CNF variable
  struct Varinfo {
    CVC3::Expr expr;
    std::vector<Lit> fanins;
    std::vector<Var> fanouts;
  };

  //! vector that maps a variable index to information for that variable
  std::vector<Varinfo> d_varInfo;

  //! Map from Exprs to Vars representing those Exprs
  CVC3::ExprHashMap<Var> d_cnfVars;

  //! Cached translation of term-ite-containing expressions
  CVC3::ExprHashMap<CVC3::Theorem> d_iteMap;

  //! Map of possibly useful lemmas
  //  CVC3::ExprMap<int> d_usefulLemmas;

  //! Maps a clause id to the theorem justifying that clause
  /*! Note that clauses created by simple CNF translation are not given id's.
   *  This is because theorems for these clauses can be created lazily later. */
  //  CVC3::CDMap<int, CVC3::Theorem> d_theorems;
  //  CVC3::CDMap<int, CVC3::Theorem> d_theorems;

  //! Next clause id
  int d_clauseIdNext;

  //! Whether expr has already been translated
  //  CVC3::CDMap<CVC3::Expr, bool> d_translated;

  //! Bottom scope in which translation is valid
  int d_bottomScope;

  //! Queue of theorems to translate
  std::deque<CVC3::Theorem> d_translateQueueThms;

  //! Queue of fanouts corresponding to thms to translate
  std::deque<Var> d_translateQueueVars;

  //! Whether thm to translate is "translate only"
  std::deque<bool> d_translateQueueFlags;

  //! Reference to statistics object
  CVC3::Statistics& d_statistics;

  //! Reference to command-line flags
  const CVC3::CLFlags& d_flags;

  //! Reference to null Expr
  const CVC3::Expr& d_nullExpr;

public:
  //! Abstract class for callbacks
  class CNFCallback {
  public:
    CNFCallback() {}
    virtual ~CNFCallback() {}
    //! Register an atom
    virtual void registerAtom(const CVC3::Expr& e,
                              const CVC3::Theorem& thm) = 0;
  };

private:
  //! Instance of CNF_CallBack: must be registered
  CNFCallback* d_cnfCallback;

  CVC3::CNF_Rules* createProofRules(CVC3::TheoremManager* tm, const CVC3::CLFlags&);

  //! Register a new atomic formula
  void registerAtom(const CVC3::Expr& e, const CVC3::Theorem& thm);

  //! Return the expr corresponding to the literal unless the expr is TRUE of FALSE
  CVC3::Expr concreteExpr(const CVC3::Expr& e, const Lit& literal);

  //! Recursively translate e into cnf
  /*! A non-context dependent cache, d_cnfVars is used to remember translations
   * of expressions.  A context-dependent attribute, isTranslated, is used to
   * remember whether an expression has been translated in the current context */
  Lit translateExprRec(const CVC3::Expr& e, CNF_Formula& cnf,
                       const CVC3::Theorem& thmIn);

  //! Recursively traverse an expression with an embedded term ITE
  /*! Term ITE's are handled by introducing a skolem variable for the ITE term
   * and then adding new constraints describing the ITE in terms of the new variable.
   */
  CVC3::Theorem replaceITErec(const CVC3::Expr& e, Var v, bool translateOnly);

  //! Recursively translate e into cnf
  /*! Call translateExprRec.  If additional expressions are queued up,
   * translate them too, until none are left. */
  Lit translateExpr(const CVC3::Theorem& thmIn, CNF_Formula& cnf);

//   bool isTranslated(const CVC3::Expr& e)
//     { CVC3::CDMap<CVC3::Expr, bool>::iterator i = d_translated.find(e);
//       return i != d_translated.end() && (*i).second; }
//   void setTranslated(const CVC3::Expr& e)
//     { DebugAssert(!isTranslated(e),"already set");
//       d_translated.insert(e, true, d_bottomScope); }
//   void clearTranslated(const CVC3::Expr& e)
//     { d_translated.insert(e, false, d_bottomScope); }

public:
  CNF_Manager(CVC3::TheoremManager* tm, CVC3::Statistics& statistics,
              const CVC3::CLFlags& flags);
  ~CNF_Manager();

  //! Register CNF callback
  void registerCNFCallback(CNFCallback* cnfCallback)
    { d_cnfCallback = cnfCallback; }

  //! Set scope for translation
  void setBottomScope(int scope) { d_bottomScope = scope; }

  //! Return the number of variables being managed
  unsigned numVars() { return d_varInfo.size(); }

  //! Return number of fanins for CNF node c
  /*! A CNF node x is a fanin of CNF node y if the expr for x is a child of the
   *  expr for y or if y is an ITE leaf and x is a new CNF node obtained by
   *  translating the ITE leaf y.
   *  \sa isITELeaf()
   */
  unsigned numFanins(Var c) {
    if (!c.isVar()) return 0;
    if (unsigned(c) >= d_varInfo.size()) return 0;
    return d_varInfo[c].fanins.size();
  }

  //! Returns the ith fanin of c.
  Lit getFanin(Var c, unsigned i) {
    DebugAssert(i < numFanins(c), "attempt to access unknown fanin");
    return d_varInfo[c].fanins[i];
  }

  //! Return number of fanins for c
  /*! x is a fanout of y if y is a fanin of x
   *  \sa numFanins
   */
  unsigned numFanouts(Var c) {
    if (!c.isVar()) return 0;
    if (unsigned(c) >= d_varInfo.size()) return 0;
    return d_varInfo[c].fanouts.size();
  }

  //! Returns the ith fanout of c.
  Lit getFanout(Var c, unsigned i) {
    DebugAssert(i < numFanouts(c), "attempt to access unknown fanin");
    return Lit(d_varInfo[c].fanouts[i]);
  }

  //! Convert a CNF literal to an Expr literal
  /*! Returns a NULL Expr if there is no corresponding Expr for l
   */
  const CVC3::Expr& concreteVar(Var v) {
    if (v.isNull()) return d_nullExpr;
    if (unsigned(v) >= d_varInfo.size() ||
        (!d_varInfo[v].expr.isTranslated()))
      return d_nullExpr;
    return d_varInfo[v].expr;
  }

  //! Convert a CNF literal to an Expr literal
  /*! Returns a NULL Expr if there is no corresponding Expr for l
   */
  CVC3::Expr concreteLit(Lit l, bool checkTranslated = true) {
    if (l.isNull()) return d_nullExpr;
    bool inverted = !l.isPositive();
    int index = l.getVar();
    if ((unsigned)index >= d_varInfo.size() ||
        (checkTranslated && !d_varInfo[index].expr.isTranslated()))
      return d_nullExpr;
    return inverted ? !d_varInfo[index].expr : d_varInfo[index].expr;
  }

  //! Look up the CNF literal for an Expr
  /*! Returns a NULL Lit if there is no corresponding CNF literal for e
   */
  Lit getCNFLit(const CVC3::Expr& e) {
    if (e.isFalse()) return Lit::getFalse();
    if (e.isTrue()) return Lit::getTrue();
    if (e.isNot()) return !getCNFLit(e[0]);
    CVC3::ExprHashMap<Var>::iterator i = d_cnfVars.find(e);
    if (!e.isTranslated() || i == d_cnfVars.end()) return Lit();
    return Lit((*i).second);
  }

  void cons(unsigned lb, unsigned ub, const CVC3::Expr& e2, std::vector<unsigned>& newLits);

  //! Convert thm A |- B (A is a set of literals) into one or more clauses
  /*! cnf should be empty -- it will be filled with the result */
  void convertLemma(const CVC3::Theorem& thm, CNF_Formula& cnf);

  //! Given thm of form A |- B, convert B to CNF and add it to cnf
  /*! Returns Lit corresponding to the root of the expression that was
   * translated. */
  Lit addAssumption(const CVC3::Theorem& thm, CNF_Formula& cnf);

  //! Convert thm to CNF and add it to the current formula
  /*! \param thm should be of form A |- B where A is a set of literals.
   * \param cnf the new clauses are added to cnf.
   * Returns Lit corresponding to the root of the expression that was
   * translated. */
  Lit addLemma(CVC3::Theorem thm, CNF_Formula& cnf);

};

}

#endif
