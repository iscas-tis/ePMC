/*****************************************************************************/
/*!
 * \file theory_core.h
 *
 * Author: Clark Barrett
 *
 * Created: Thu Jan 30 16:58:05 2003
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

#ifndef _cvc3__include__theory_core_h_
#define _cvc3__include__theory_core_h_

#include <queue>
#include "theory.h"
#include "cdmap.h"
#include "statistics.h"
#include <string>
#include "notifylist.h"
//#include "expr_transform.h"

namespace CVC3 {

class ExprTransform;
// class Statistics;
class CoreProofRules;
class Translator;

/*****************************************************************************/
/*!
 *\class TheoryCore
 *\ingroup Theories
 *\brief This theory handles the built-in logical connectives plus equality.
 * It also handles the registration and cooperation among all other theories.
 *
 * Author: Clark Barrett
 *
 * Created: Sat Feb 8 14:54:05 2003
 */
/*****************************************************************************/
class TheoryCore :public Theory {
  friend class Theory;

  //! Context manager
  ContextManager* d_cm;

  //! Theorem manager
  TheoremManager* d_tm;

  //! Core proof rules
  CoreProofRules* d_rules;

  //! Reference to command line flags
  const CLFlags& d_flags;

  //! Reference to statistics
  Statistics& d_statistics;

  //! PrettyPrinter (we own it)
  PrettyPrinter* d_printer;

  //! Type Computer
  ExprManager::TypeComputer* d_typeComputer;

  //! Expr Transformer
  ExprTransform* d_exprTrans;

  //! Translator
  Translator* d_translator;

  //! Assertion queue
  std::queue<Theorem> d_queue;
  //! Queue of facts to be sent to the SearchEngine
  std::vector<Theorem> d_queueSE;

  //! Inconsistent flag
  CDO<bool> d_inconsistent;
  //! The set of reasons for incompleteness (empty when we are complete)
  CDMap<std::string, bool> d_incomplete;

  //! Proof of inconsistent
  CDO<Theorem> d_incThm;
  //! List of all active terms in the system (for quantifier instantiation)
  CDList<Expr> d_terms;
  //! Map from active terms to theorems that introduced those terms

  std::hash_map<Expr, Theorem> d_termTheorems;
  //  CDMap<Expr, Theorem> d_termTheorems;

  //! List of all active non-equality atomic formulas in the system (for quantifier instantiation)
  CDList<Expr> d_predicates;
  //! List of variables that were created up to this point
  std::vector<Expr> d_vars;
  //! Database of declared identifiers
  std::map<std::string, Expr> d_globals;
  //! Bound variable stack: a vector of pairs <name, var>
  std::vector<std::pair<std::string, Expr> > d_boundVarStack;
  //! Map for bound vars
  std::hash_map<std::string, Expr> d_boundVarMap;
  //! Cache for parser
  ExprMap<Expr> d_parseCache;
  //! Cache for tcc's
  ExprMap<Expr> d_tccCache;

  //! Array of registered theories
  std::vector<Theory*> d_theories;

  //! Array mapping kinds to theories
  std::hash_map<int, Theory*> d_theoryMap;

  //! The theory which has the solver (if any)
  Theory* d_solver;

  //! Mapping of compound type variables to simpler types (model generation)
  ExprHashMap<std::vector<Expr> > d_varModelMap;
  //! Mapping of intermediate variables to their valies
  ExprHashMap<Theorem> d_varAssignments;
  //! List of basic variables (temporary storage for model generation)
  std::vector<Expr> d_basicModelVars;
  //! Mapping of basic variables to simplified expressions (temp. storage)
  /*! There may be some vars which simplify to something else; we save
   * those separately, and keep only those which simplify to
   * themselves.  Once the latter vars are assigned, we'll re-simplify
   * the former variables and use the results as concrete values.
  */
  ExprHashMap<Theorem> d_simplifiedModelVars;

  //! Command line flag whether to simplify in place
  const bool* d_simplifyInPlace;
  //! Which recursive simplifier to use
  Theorem (TheoryCore::*d_currentRecursiveSimplifier)(const Expr&);

  //! Resource limit (0==unlimited, 1==no more resources, >=2 - available).
  /*! Currently, it is the number of enqueued facts.  Once the
   * resource is exhausted, the remaining facts will be dropped, and
   * an incomplete flag is set.
   */
  unsigned d_resourceLimit;

  //! Time limit (0==unlimited, >0==total available cpu time in seconds).
  /*! Currently, if exhausted processFactQueue will not perform any more
   * reasoning with FULL effor and an incomplete flag is set.
   */
  unsigned d_timeBase;
  unsigned d_timeLimit;

  bool d_inCheckSATCore;
  bool d_inAddFact;
  bool d_inRegisterAtom;
  bool d_inPP;

  IF_DEBUG(ExprMap<bool> d_simpStack;)


  //! So we get notified every time there's a pop
  friend class CoreNotifyObj;
  class CoreNotifyObj :public ContextNotifyObj {
    TheoryCore* d_theoryCore;
  public:
    CoreNotifyObj(TheoryCore* tc, Context* context)
      : ContextNotifyObj(context), d_theoryCore(tc) {}
    void notify() { d_theoryCore->getEM()->invalidateSimpCache(); }
  };
  CoreNotifyObj d_notifyObj;

  //! List of implied literals, based on registered atomic formulas of interest
  CDList<Theorem> d_impliedLiterals;

  //! Next index in d_impliedLiterals that has not yet been fetched
  CDO<unsigned> d_impliedLiteralsIdx;

  //! List of theorems from calls to update()
  // These are stored here until the equality lists are finished and then
  // processed by processUpdates()
  std::vector<Theorem> d_update_thms;

  //! List of data accompanying above theorems from calls to update()
  std::vector<Expr> d_update_data;

  //! Notify list that gets processed on every equality
  NotifyList d_notifyEq;

  //! Whether we are in the middle of doing updates
  unsigned d_inUpdate;

public:
  /***************************************************************************/
  /*!
   *\class CoreSatAPI
   *\brief Interface class for callbacks to SAT from Core
   *
   * Author: Clark Barrett
   *
   * Created: Mon Dec  5 18:42:15 2005
   */
  /***************************************************************************/
  class CoreSatAPI {
  public:
    CoreSatAPI() {}
    virtual ~CoreSatAPI() {}
    //! Add a new lemma derived by theory core
    virtual void addLemma(const Theorem& thm, int priority = 0,
                          bool atBottomScope = false) = 0;
    //! Add an assumption to the set of assumptions in the current context
    /*! Assumptions have the form e |- e */
    virtual Theorem addAssumption(const Expr& assump) = 0;
    //! Suggest a splitter to the Sat engine
    /*! \param e is a literal.
     * \param priority is between -10 and 10.  A priority above 0 indicates
     * that the splitter should be favored.  A priority below 0 indicates that
     * the splitter should be delayed.
     */
    virtual void addSplitter(const Expr& e, int priority) = 0;
    //! Check the validity of e in the current context
    virtual bool check(const Expr& e) = 0;
  };
private:
  CoreSatAPI* d_coreSatAPI;

private:
  //! Private method to get a new theorem producer.
  /*! This method is used ONLY by the TheoryCore constructor.  The
    only reason to have this method is to separate the trusted part of
    the constructor into a separate .cpp file (Alternative is to make
    the entire constructer trusted, which is not very safe).
    Method is implemented in core_theorem_producer.cpp  */
  CoreProofRules* createProofRules(TheoremManager* tm);

  // Helper functions

  //! Effort level for processFactQueue
  /*! LOW means just process facts, don't call theory checkSat methods
      NORMAL means call theory checkSat methods without full effort
      FULL means call theory checkSat methods with full effort
  */
  typedef enum {
    LOW,
    NORMAL,
    FULL
  } EffortLevel;

  //! A helper function for addFact()
  /*! Returns true if lemmas were added to search engine, false otherwise */
  bool processFactQueue(EffortLevel effort = NORMAL);
  //! Process a notify list triggered as a result of new theorem e
  void processNotify(const Theorem& e, NotifyList* L);
  //! Transitive composition of other rewrites with the above
  Theorem rewriteCore(const Theorem& e);
  //! Helper for registerAtom
  void setupSubFormulas(const Expr& s, const Expr& e, const Theorem& thm);
  //! Process updates recorded by calls to update()
  void processUpdates();
  /*! @brief The assumptions for e must be in H or phi.  "Core" added
   * to avoid conflict with theory interface function name
   */
  void assertFactCore(const Theorem& e);
  //! Process a newly derived formula
  void assertFormula(const Theorem& e);
  //! Check that lhs and rhs of thm have same base type
  IF_DEBUG(void checkRewriteType(const Theorem& thm);)
  /*! @brief Returns phi |= e = rewriteCore(e).  "Core" added to avoid
    conflict with theory interface function name */
  Theorem rewriteCore(const Expr& e);

  //! Set the find pointer of an atomic formula and notify SearchEngine
  /*! \param thm is a Theorem(phi) or Theorem(NOT phi), where phi is
   * an atomic formula to get a find pointer to TRUE or FALSE,
   * respectively.
   */
  void setFindLiteral(const Theorem& thm);
  //! Core rewrites for literals (NOT and EQ)
  Theorem rewriteLitCore(const Expr& e);
  //! Enqueue a fact to be sent to the SearchEngine
  //  void enqueueSE(const Theorem& thm);//yeting
  //! Fetch the concrete assignment to the variable during model generation
  Theorem getModelValue(const Expr& e);

  //! An auxiliary recursive function to process COND expressions into ITE
  Expr processCond(const Expr& e, int i);

  //! Return true if no special parsing is required for this kind
  bool isBasicKind(int kind);

  //! Helper check functions for solve
  void checkEquation(const Theorem& thm);
  //! Helper check functions for solve
  void checkSolved(const Theorem& thm);

  // Time limit exhausted
  bool timeLimitReached();

public:
  //! Constructor
  TheoryCore(ContextManager* cm, ExprManager* em,
             TheoremManager* tm, Translator* tr,
             const CLFlags& flags,
             Statistics& statistics);
  //! Destructor
  ~TheoryCore();

  //! Request a unit of resource
  /*! It will be subtracted from the resource limit.
   *
   * \return true if resource unit is granted, false if no more
   * resources available.
   */
  void getResource() {
      getStatistics().counter("resource")++;
      if (d_resourceLimit > 1) d_resourceLimit--;
  }

  //! Register a SatAPI for TheoryCore
  void registerCoreSatAPI(CoreSatAPI* coreSatAPI) { d_coreSatAPI = coreSatAPI; }

  //! Register a callback for every equality
  void addNotifyEq(Theory* t, const Expr& e) { d_notifyEq.add(t, e); }

  //! Call theory-specific preprocess routines
  Theorem callTheoryPreprocess(const Expr& e);

  ContextManager* getCM() const { return d_cm; }
  TheoremManager* getTM() const { return d_tm; }
  const CLFlags& getFlags() const { return d_flags; }
  Statistics& getStatistics() const { return d_statistics; }
  ExprTransform* getExprTrans() const { return d_exprTrans; }
  CoreProofRules* getCoreRules() const { return d_rules; }
  Translator* getTranslator() const { return d_translator; }

  //! Access to tcc cache (needed by theory_bitvector)
  ExprMap<Expr>& tccCache() { return d_tccCache; }

  //! Get list of terms
  const CDList<Expr>& getTerms() { return d_terms; }

  int getCurQuantLevel();

  //! Set the flag for the preprocessor
  void setInPP() { d_inPP = true; }
  void clearInPP() { d_inPP = false; }

  //! Get theorem which was responsible for introducing this term
  Theorem getTheoremForTerm(const Expr& e);
  //! Get quantification level at which this term was introduced
  unsigned getQuantLevelForTerm(const Expr& e);
  //! Get list of predicates
  const CDList<Expr>& getPredicates() { return d_predicates; }
  //! Whether updates are being processed
  bool inUpdate() { return d_inUpdate > 0; }
  //! Whether its OK to add new facts (for use in rewrites)
  bool okToEnqueue()
    { return d_inAddFact || d_inCheckSATCore || d_inRegisterAtom || d_inPP; }

  // Implementation of Theory API
  /*! Variables of uninterpreted types may be sent here, and they
    should be ignored. */
  void addSharedTerm(const Expr& e) { }
  void assertFact(const Theorem& e);
  void checkSat(bool fullEffort) { }
  Theorem rewrite(const Expr& e);
  /*! Only Boolean constants (TRUE and FALSE) and variables of
    uninterpreted types should appear here (they come from records and
    tuples).  Just ignore them. */
  void setup(const Expr& e) { }
  void update(const Theorem& e, const Expr& d);
  Theorem solve(const Theorem& e);

  Theorem simplifyOp(const Expr& e);
  void checkType(const Expr& e);
  Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                             bool enumerate, bool computeSize);
  void computeType(const Expr& e);
  Type computeBaseType(const Type& t);
  Expr computeTCC(const Expr& e);
  Expr computeTypePred(const Type& t,const Expr& e);
  Expr parseExprOp(const Expr& e);
  ExprStream& print(ExprStream& os, const Expr& e);

  //! Calls for other theories to add facts to refine a coutnerexample.
  void refineCounterExample();
  bool refineCounterExample(Theorem& thm);
  void computeModelBasic(const std::vector<Expr>& v);

  // User-level API methods

  /*! @brief Add a new assertion to the core from the user or a SAT
    solver.  Do NOT use it in a decision procedure; use
    enqueueFact(). */
  /*! \sa enqueueFact */
  void addFact(const Theorem& e);

  //! Top-level simplifier
  Theorem simplify(const Expr& e);

  //! Check if the current context is inconsistent
  bool inconsistent() { return d_inconsistent ; }
  //! Get the proof of inconsistency for the current context
  /*! \return Theorem(FALSE) */
  Theorem inconsistentThm() { return d_incThm; }
  /*! @brief To be called by SearchEngine when it believes the context
   * is satisfiable.
   *
   * \return true if definitely consistent or inconsistent and false if
   * consistency is unknown.
   */
  bool checkSATCore();

  //! Check if the current decision branch is marked as incomplete
  bool incomplete() { return d_incomplete.size() > 0 ; }
  //! Check if the branch is incomplete, and return the reasons (strings)
  bool incomplete(std::vector<std::string>& reasons);

  //! Register an atomic formula of interest.
  /*! If e or its negation is later deduced, we will add the implied
      literal to d_impliedLiterals */
  void registerAtom(const Expr& e, const Theorem& thm);

  //! Return the next implied literal (NULL Theorem if none)
  Theorem getImpliedLiteral(void);

  //! Return total number of implied literals
  unsigned numImpliedLiterals() { return d_impliedLiterals.size(); }

  //! Return an implied literal by index
  Theorem getImpliedLiteralByIndex(unsigned index);

  //! Parse the generic expression.
  /*! This method should be used in parseExprOp() for recursive calls
   *  to subexpressions, and is the method called by the command
   *  processor.
   */
  Expr parseExpr(const Expr& e);
  //! Top-level call to parseExpr, clears the bound variable stack.
  /*! Use it in VCL instead of parseExpr(). */
  Expr parseExprTop(const Expr& e) {
    d_boundVarStack.clear();
    d_parseCache.clear();
    return parseExpr(e);
  }

  //! Assign t a concrete value val.  Used in model generation.
  void assignValue(const Expr& t, const Expr& val);
  //! Record a derived assignment to a variable (LHS).
  void assignValue(const Theorem& thm);

  //! Adds expression to var database
  void addToVarDB(const Expr & e);
  //! Split compound vars into basic type variables
  /*! The results are saved in d_basicModelVars and
   *  d_simplifiedModelVars.  Also, add new basic vars as shared terms
   *  whenever their type belongs to a different theory than the term
   *  itself.
   */
  void collectBasicVars();
  //! Calls theory specific computeModel, results are placed in map
  void buildModel(ExprMap<Expr>& m);
  bool buildModel(Theorem& thm);
  //! Recursively build a compound-type variable assignment for e
  void collectModelValues(const Expr& e, ExprMap<Expr>& m);

  //! Set the resource limit (0==unlimited).
  void setResourceLimit(unsigned limit) { d_resourceLimit = limit; }
  //! Get the resource limit
  unsigned getResourceLimit() { return d_resourceLimit; }
  //! Return true if resources are exhausted
  bool outOfResources() { return d_resourceLimit == 1; }

  //! Initialize base time used for !setTimeLimit
  void initTimeLimit();

  //! Set the time limit in seconds (0==unlimited).
  void setTimeLimit(unsigned limit);

  //! Called by search engine
  Theorem rewriteLiteral(const Expr& e);

private:
  // Methods provided for benefit of theories.  Access is made available through theory.h

  //! Assert a system of equations (1 or more).
  /*! e is either a single equation, or a conjunction of equations
   */
  void assertEqualities(const Theorem& e);

  //! Mark the branch as incomplete
  void setIncomplete(const std::string& reason);

  //! Return a theorem for the type predicate of e
  /*! Note: used only by theory_arith */
  Theorem typePred(const Expr& e);

public:
  // TODO: These should be private
  //! Enqueue a new fact
  /*! not private because used in search_fast.cpp */
  void enqueueFact(const Theorem& e);
  void enqueueSE(const Theorem& thm);//yeting
  // Must provide proof of inconsistency
  /*! not private because used in search_fast.cpp */
  void setInconsistent(const Theorem& e);

  //! Setup additional terms within a theory-specific setup().
  /*! not private because used in theory_bitvector.cpp */
  void setupTerm(const Expr& e, Theory* i, const Theorem& thm);


};

//! Printing NotifyList class
std::ostream& operator<<(std::ostream& os, const NotifyList& l);

}

#endif
