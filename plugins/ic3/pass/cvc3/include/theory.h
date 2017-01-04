/*****************************************************************************/
/*!
 * \file theory.h
 * \brief Generic API for Theories plus methods commonly used by theories
 * 
 * Author: Clark Barrett
 * 
 * Created: Sat Nov 30 23:30:15 2002
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

#ifndef _cvc3__include__theory_h_
#define _cvc3__include__theory_h_

#include "expr_stream.h"
#include "common_proof_rules.h"
#include "cdlist.h"

namespace CVC3 {

class TheoryCore;
class Theorem;
class Type;

/************************************************************************/
/*!
 *\defgroup Theories Theories
 *\ingroup VC
 *\brief Theories
 *@{
 */
/***********************************************************************/

/*****************************************************************************/
/*!
 *\anchor Theory
 *\class Theory
 *\brief Base class for theories
 *
 * Author: Clark Barrett
 *
 * Created: Thu Jan 30 16:37:56 2003
 *
 * This is an abstract class which all theories should inherit from.  In
 * addition to providing an abstract theory interface, it provides access
 * functions to core functionality.  However, in order to avoid duplicating the
 * data structures which implement this functionality, all the functionality is
 * stored in a separate class (which actually derives from this one) called
 * TheoryCore.  These two classes work closely together to provide the core
 * functionality.
 */
/*****************************************************************************/

class Theory {
  friend class TheoryCore;
private:
  ExprManager* d_em;
  TheoryCore* d_theoryCore; //!< Provides the core functionality
  CommonProofRules* d_commonRules; //!< Commonly used proof rules
  std::string d_name; //!< Name of the theory (for debugging)

  //! Private default constructor.
  /*! Everyone besides TheoryCore has to use the public constructor
    which sets up all the provided functionality automatically.
  */
  Theory(void);

protected:
  bool d_theoryUsed; //! Whether theory has been used (for smtlib translator)

public:
  //! Exposed constructor.
  /*! Note that each instance of Theory must have a name (mostly for
    debugging purposes). */
  Theory(TheoryCore* theoryCore, const std::string& name);
  //! Destructor
  virtual ~Theory(void);

  //! Access to ExprManager
  ExprManager* getEM() { return d_em; }

  //! Get a pointer to theoryCore
  TheoryCore* theoryCore() { return d_theoryCore; }

  //! Get a pointer to common proof rules
  CommonProofRules* getCommonRules() { return d_commonRules; }

  //! Get the name of the theory (for debugging purposes)
  const std::string& getName() const { return d_name; }

  //! Set the "used" flag on this theory (for smtlib translator)
  virtual void setUsed() { d_theoryUsed = true; }
  //! Get whether theory has been used (for smtlib translator)
  virtual bool theoryUsed() { return d_theoryUsed; }

  /***************************************************************************/
  /*!
   *\defgroup Theory_API Abstract Theory Interface
   *\anchor theory_api
   *\ingroup Theories
   *\brief Abstract Theory Interface
   *
   * These are the theory-specific methods which provide the decision procedure
   * functionality for a new theory.  At the very least, a theory must
   * implement the checkSat method.  The other methods can be used to make the
   * implementation more convenient.  For more information on this API, see
   * Clark Barrett's PhD dissertation and \ref theory_api_howto.
   *@{
   */
  /***************************************************************************/

  //! Notify theory of a new shared term
  /*! When a term e associated with theory i occurs as a child of an expression
    associated with theory j, the framework calls i->addSharedTerm(e) and
    j->addSharedTerm(e)
  */
  virtual void addSharedTerm(const Expr& e) {}

  //! Assert a new fact to the decision procedure
  /*! Each fact that makes it into the core framework is assigned to exactly
    one theory: the theory associated with that fact.  assertFact is called to
    inform the theory that a new fact has been assigned to the theory.
  */
  virtual void assertFact(const Theorem& e) = 0;

  //! Check for satisfiability in the theory
  /*! \param fullEffort when it is false, checkSat can do as much or
   as little work as it likes, though simple inferences and checks for
   consistency should be done to increase efficiency.  If fullEffort is true,
   checkSat must check whether the set of facts given by assertFact together
   with the arrangement of shared terms (provided by addSharedTerm) induced by
   the global find database equivalence relation are satisfiable.  If
   satisfiable, checkSat does nothing.

   If satisfiability can be acheived by merging some of the shared terms, a new
   fact must be enqueued using enqueueFact (this fact need not be a literal).
   If there is no way to make things satisfiable, setInconsistent must be called.
  */
  virtual void checkSat(bool fullEffort) = 0;

  //! Theory-specific rewrite rules.  
  /*! By default, rewrite just returns a reflexive theorem stating that the
    input expression is equivalent to itself.  However, rewrite is allowed to
    return any theorem which describes how the input expression is equivalent
    to some new expression.  rewrite should be used to perform simplifications,
    normalization, and any other preprocessing on theory-specific expressions
    that needs to be done.
  */
  virtual Theorem rewrite(const Expr& e) { return reflexivityRule(e); }

  //! Theory-specific preprocessing
  /*! This gets called each time a new assumption or query is preprocessed.
    By default it does nothing. */
  virtual Theorem theoryPreprocess(const Expr& e) { return reflexivityRule(e); }

  //! Set up the term e for call-backs when e or its children change.
  /*! setup is called once for each expression associated with the theory.  It
    is typically used to setup theory-specific data for an expression and to
    add call-back information for use with update.
    \sa update
  */
  virtual void setup(const Expr& e) {}

  //! Notify a theory of a new equality
  /*! update is a call-back used by the notify mechanism of the core theory.
    It works as follows.  When an equation t1 = t2 makes it into the core
    framework, the two find equivalence classes for t1 and t2 are merged.  The
    result is that t2 is the new equivalence class representative and t1 is no
    longer an equivalence class representative.  When this happens, the notify
    list of t1 is traversed.  Notify list entries consist of a theory and an
    expression d.  For each entry (i,d), i->update(e, d) is called, where e is
    the theorem corresponding to the equality t1=t2.

    To add the entry (i,d) to a term t1's notify list, a call must be made to
    t1.addNotify(i,d).  This is typically done in setup.

    \sa setup
  */
  virtual void update(const Theorem& e, const Expr& d) {}

  //! An optional solver.
  /*! The solve method can be used to implement a Shostak-style solver.  Since
    solvers do not in general combine, the following technique is used.  One
    theory is designated as the primary solver (in our case, it is the theory
    of arithmetic).  For each equation that enters the core framework, the
    primary solver is called to ensure that the equation is in solved form with
    respect to the primary theory.

    After the primary solver, the solver for the theory associated with the
    equation is called.  This solver can do whatever it likes, as long as the
    result is still in solved form with respect to the primary solver.  This is
    a slight generalization of what is described in my (Clark)'s PhD thesis.
  */
  virtual Theorem solve(const Theorem& e) { return e; }
  //! A debug check used by the primary solver
  virtual void checkAssertEqInvariant(const Theorem& e) { }

  /////////////////////////////////
  // Extensions to original API: //
  /////////////////////////////////

  //! Recursive simplification step
  /*!
   * INVARIANT: the result is a Theorem(e=e'), where e' is a fully
   * simplified version of e.  To simplify subexpressions recursively,
   * call simplify() function.
   *
   * This theory-specific method is called when the simplifier
   * descends top-down into the expression.  Normally, every kid is
   * simplified recursively, and the results are combined into the new
   * parent with the same operator (Op).  This functionality is
   * provided with the default implementation.
   *
   * However, in some expressions some kids may not matter in the
   * result, and can be skipped.  For instance, if the first kid in a
   * long AND simplifies to FALSE, then the entire expression
   * simplifies to FALSE, and the remaining kids do not need to be
   * simplified.
   *
   * This call is a chance for a DP to provide these types of
   * optimizations during the top-down phase of simplification.
   */
  virtual Theorem simplifyOp(const Expr& e);

  //! Check that e is a valid Type expr
  virtual void checkType(const Expr& e)
    { throw Exception("Cannot construct type from expr: "+e.toString()); }

  //! Compute information related to finiteness of types
  /*! Used by the TypeComputer defined in TheoryCore (theories should not call this
   *  funtion directly -- they should use the methods in Type instead).  Each theory
   *  should implement this if it contains any types that could be non-infinite.
   *
   * 1. Returns Cardinality of the type (finite, infinite, or unknown)
   * 2. If cardinality = finite and enumerate is true,
   *    sets e to the nth element of the type if it can
   *    sets e to NULL if n is out of bounds or if unable to compute nth element
   * 3. If cardinality = finite and computeSize is true,
   *    sets n to the size of the type if it can
   *    sets n to 0 otherwise
   */
  virtual Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                                     bool enumerate, bool computeSize)
  { return CARD_INFINITE; }

  //! Compute and store the type of e
  /*!
   * \param e is the expression whose type is computed.  
   *
   * This function computes the type of the top-level operator of e,
   * and recurses into children using getType(), if necessary.
   */
  virtual void computeType(const Expr& e) {}
  //! Compute the base type of the top-level operator of an arbitrary type
  virtual Type computeBaseType(const Type& tp) { return tp; }
  /*! @brief  Theory specific computation of the subtyping predicate for 
   *  type t applied to the expression e.
   */ 
  /*! By default returns true. Each theory needs to compute subtype predicates
   *  for the types associated with it. So, for example, the theory of records
   *  will take a record type [# f1: T1, f2: T2 #] and an expression e
   *  and will return the subtyping predicate for e, namely:
   *  computeTypePred(T1, e.f1) AND computeTypePred(T2, e.f2)
   */ 
  virtual Expr computeTypePred(const Type& t, const Expr& e) 
    { return e.getEM()->trueExpr(); }
  //! Compute and cache the TCC of e.
  /*! 
   * \param e is an expression (term or formula).  This function
   * computes the TCC of e which is true iff the expression is defined.
   *
   * This function computes the TCC or predicate of the top-level
   * operator of e, and recurses into children using getTCC(), if
   * necessary.
   *
   * The default implementation is to compute TCCs recursively for all
   * children, and return their conjunction.
   */
  virtual Expr computeTCC(const Expr& e);

  //! Theory-specific parsing implemented by the DP
  virtual Expr parseExprOp(const Expr& e) { return e; }

  //! Theory-specific pretty-printing.
  /*! By default, print the top node in AST, and resume
    pretty-printing the children.  The same call e.print(os) can be
    used in DP-specific printers to use AST printing for the given
    node.  In fact, it is strongly recommended to add e.print(os) as
    the default for all the cases/kinds that are not handled by the
    particular pretty-printer.
  */
  virtual ExprStream& print(ExprStream& os, const Expr& e) {
    return e.printAST(os);
  }

  //! Add variables from 'e' to 'v' for constructing a concrete model
  /*! If e is already of primitive type, do NOT add it to v. */
  virtual void computeModelTerm(const Expr& e, std::vector<Expr>& v);
  //! Process disequalities from the arrangement for model generation
  virtual void refineCounterExample() {}
  //! Assign concrete values to basic-type variables in v
  virtual void computeModelBasic(const std::vector<Expr>& v) {}
  //! Compute the value of a compound variable from the more primitive ones
  /*! The more primitive variables for e are already assigned concrete
   * values, and are available through getModelValue().
   *
   * The new value for e must be assigned using assignValue() method.
   *
   * \param e is the compound type expression to assign a value;
   *
   * \param vars are the variables actually assigned.  Normally, 'e'
   * is the only element of vars.  However, e.g. in the case of
   * uninterpreted functions, assigning 'f' means assigning all
   * relevant applications of 'f' to constant values (f(0), f(5),
   * etc.).  Such applications might not be known before the model is
   * constructed (they may be of the form f(x), f(y+z), etc., where
   * x,y,z are still unassigned).
   *
   * Populating 'vars' is an opportunity for a DP to change the set of
   * top-level "variables" to assign, if needed.  In particular, it
   * may drop 'e' from the model entirely, if it is already a concrete
   * value by itself.
   */
  virtual void computeModel(const Expr& e, std::vector<Expr>& vars) {
    assignValue(find(e));
    vars.push_back(e);
  }

  //! Receives all the type predicates for the types of the given theory
  /*! Type predicates may be expensive to enqueue eagerly, and DPs may
    choose to postpone them, or transform them to something more
    efficient.  By default, the asserted type predicate is
    immediately enqueued as a new fact.

    Note: Used only by bitvector theory.

    \param e is the expression for which the type predicate is computed
    \param pred is the predicate theorem P(e)
  */
  virtual void assertTypePred(const Expr& e, const Theorem& pred)
    { enqueueFact(pred); }

  //! Theory-specific rewrites for atomic formulas
  /*! The intended use is to convert complex atomic formulas into an
   * equivalent Boolean combination of simpler formulas.  Such
   * conversion may be harmful for algebraic rewrites, and is not
   * always desirable to have in rewrite() method.
   *
   * Note: Used only by bitvector theory and rewriteLiteral in core.
   *
   * However, if rewrite() alone cannot solve the problem, and the SAT
   * solver needs to be envoked, these additional rewrites may ease
   * the job for the SAT solver.
   */
  virtual Theorem rewriteAtomic(const Expr& e) { return reflexivityRule(e); }

  //! Notification of conflict
  /*!
   * Decision procedures implement this method when they want to be
   * notified about a conflict.
   *
   * Note: Used only by quantifier theory
   *
   * \param thm is the theorem of FALSE given to setInconsistent()
   */
  virtual void notifyInconsistent(const Theorem& thm) { }

  virtual void registerAtom(const Expr& e, const Theorem& thm);

  //! Theory-specific registration of atoms
  /*!
   * If a theory wants to implement its own theory propagation, it
   * should implement this method and use it to collect all atoms
   * that the core is interested in.  If the theory can deduce the atom
   * or its negation, it should do so (using enqueueFact).
   */
  virtual void registerAtom(const Expr& e) { }


#ifdef _CVC3_DEBUG_MODE
  //! Theory-specific debug function
  virtual void debug(int i) { }
  //! help function, as debug(int i). yeting
  virtual int help(int i) { return 9999 ;} ;
#endif

  /*@}*/ // End of Theory_API group

  /***************************************************************************/
  /*!
   *\name Core Framework Functionality
   * These methods provide convenient access to core functionality for the
   * benefit of decision procedures.
   *@{
   */
  /***************************************************************************/

  //! Check if the current context is inconsistent
  virtual bool inconsistent();
  //! Make the context inconsistent; The formula proved by e must FALSE.
  virtual void setInconsistent(const Theorem& e);

  //! Mark the current decision branch as possibly incomplete
  /*!
   * This should be set when a decision procedure uses an incomplete
   * algorithm, and cannot guarantee satisfiability after the final
   * checkSat() call with full effort.  An example would be
   * instantiation of universal quantifiers.
   *
   * A decision procedure can provide a reason for incompleteness,
   * which will be reported back to the user.
   */
  virtual void setIncomplete(const std::string& reason);

  //! Simplify a term e and return a Theorem(e==e')
  /*! \sa simplifyExpr() */
  virtual Theorem simplify(const Expr& e);
  //! Simplify a term e w.r.t. the current context
  /*! \sa simplify */
  Expr simplifyExpr(const Expr& e)
    { return simplify(e).getRHS(); }

  //! Submit a derived fact to the core from a decision procedure
  /*! \param e is the Theorem for the new fact 
   */
  virtual void enqueueFact(const Theorem& e);
  virtual void enqueueSE(const Theorem& e);

  //! Handle new equalities (usually asserted through addFact)
  /*!
   * INVARIANT: the Theorem 'e' is an equality e1==e2, where e2 is
   * i-leaf simplified in the current context, or a conjunction of
   * such equalities.
   *
   */
  virtual void assertEqualities(const Theorem& e);

  //! Parse the generic expression.
  /*! This method should be used in parseExprOp() for recursive calls
   *  to subexpressions, and is the method called by the command
   *  processor.
   */
  virtual Expr parseExpr(const Expr& e);

  //! Assigns t a concrete value val.  Used in model generation.
  virtual void assignValue(const Expr& t, const Expr& val);
  //! Record a derived assignment to a variable (LHS).
  virtual void assignValue(const Theorem& thm);

  /*@}*/ // End of Core Framework Functionality

  /***************************************************************************/
  /*!
   *\name Theory Helper Methods
   * These methods provide basic functionality needed by all theories.
   *@{
   */
  /***************************************************************************/

  //! Register new kinds with the given theory
  void registerKinds(Theory* theory, std::vector<int>& kinds);
  //! Unregister kinds for a theory
  void unregisterKinds(Theory* theory, std::vector<int>& kinds);
  //! Register a new theory
  void registerTheory(Theory* theory, std::vector<int>& kinds,
		      bool hasSolver=false);
  //! Unregister a theory
  void unregisterTheory(Theory* theory, std::vector<int>& kinds,
                        bool hasSolver);

  //! Return the number of registered theories
  int getNumTheories();

  //! Test whether a kind maps to any theory
  bool hasTheory(int kind);
  //! Return the theory associated with a kind
  Theory* theoryOf(int kind);
  //! Return the theory associated with a type
  Theory* theoryOf(const Type& e);
  //! Return the theory associated with an Expr
  Theory* theoryOf(const Expr& e);

  //! Return the theorem that e is equal to its find
  Theorem find(const Expr& e);
  //! Return the find as a reference: expr must have a find
  const Theorem& findRef(const Expr& e);

  //! Return find-reduced version of e
  Theorem findReduce(const Expr& e);
  //! Return true iff e is find-reduced
  bool findReduced(const Expr& e);
  //! Return the find of e, or e if it has no find
  inline Expr findExpr(const Expr& e)
    { return e.hasFind() ? find(e).getRHS() : e; }

  //! Compute the TCC of e, or the subtyping predicate, if e is a type
  Expr getTCC(const Expr& e);
  //! Compute (or look up in cache) the base type of e and return the result
  Type getBaseType(const Expr& e);
  //! Compute the base type from an arbitrary type
  Type getBaseType(const Type& tp);
  //! Calls the correct theory to compute a type predicate
  Expr getTypePred(const Type& t, const Expr& e);

  //! Update the children of the term e
  /*! When a decision procedure receives a call to update() because a
    child of a term 'e' has changed, this method can be called to
    compute the new value of 'e'.
    \sa update
  */
  Theorem updateHelper(const Expr& e);
  //! Setup a term for congruence closure (must have sig and rep attributes)
  void setupCC(const Expr& e);
  //! Update a term w.r.t. congruence closure (must be setup with setupCC())
  void updateCC(const Theorem& e, const Expr& d);
  //! Rewrite a term w.r.t. congruence closure (must be setup with setupCC())
  Theorem rewriteCC(const Expr& e);

  /*! @brief Calls the correct theory to get all of the terms that
    need to be assigned values in the concrete model */
  void getModelTerm(const Expr& e, std::vector<Expr>& v);
  //! Fetch the concrete assignment to the variable during model generation
  Theorem getModelValue(const Expr& e);

  //! Suggest a splitter to the SearchEngine
  void addSplitter(const Expr& e, int priority = 0);

  //! Add a global lemma
  void addGlobalLemma(const Theorem& thm, int priority = 0);

  /*@}*/ // End of Theory Helper Methods

  /***************************************************************************/
  /*!
   *\name Core Testers
   *@{
   */
  /***************************************************************************/

  //! Test if e is an i-leaf term for the current theory
  /*! A term 'e' is an i-leaf for a theory 'i', if it is a variable,
    or 'e' belongs to a different theory.  This definition makes sense
    for a larger term which by itself belongs to the current theory
    'i', but (some of) its children are variables or belong to
    different theories. */
  bool isLeaf(const Expr& e) { return e.isVar() || theoryOf(e) != this; }

  //! Test if e1 is an i-leaf in e2
  /*! \sa isLeaf */
  bool isLeafIn(const Expr& e1, const Expr& e2);

  //! Test if all i-leaves of e are simplified
  /*! \sa isLeaf */
  bool leavesAreSimp(const Expr& e);

  /*@}*/ // End of Core Testers

  /***************************************************************************/
  /*!
   *\name Common Type and Expr Methods
   *@{
   */
  /***************************************************************************/

  //! Return BOOLEAN type
  Type boolType() { return Type::typeBool(d_em); }

  //! Return FALSE Expr
  const Expr& falseExpr() { return d_em->falseExpr(); }

  //! Return TRUE Expr
  const Expr& trueExpr() { return d_em->trueExpr(); }

  //! Create a new variable given its name and type
  /*! Add the variable to the database for resolving IDs in parseExpr
   */
  Expr newVar(const std::string& name, const Type& type);
  //! Create a new named expression given its name, type, and definition
  /*! Add the definition to the database for resolving IDs in parseExpr
   */
  Expr newVar(const std::string& name, const Type& type, const Expr& def);

  //! Create a new uninterpreted function
  /*! Add the definition to the database for resolving IDs in parseExpr
   */
  Op newFunction(const std::string& name, const Type& type,
                 bool computeTransClosure);

  //! Look up a function by name.
  /*! Returns the function and sets type to the type of the function if it
   * exists.  If not, returns a NULL Op object.
   */
  Op lookupFunction(const std::string& name, Type* type);

  //! Create a new defined function
  /*! Add the definition to the database for resolving IDs in parseExpr
   */
  Op newFunction(const std::string& name, const Type& type, const Expr& def);

  //! Create and add a new bound variable to the stack, for parseExprOp().
  /*!
   * The stack is popped automatically upon return from the
   * parseExprOp() which used this method.
   *
   * Bound variable names may repeat, in which case the latest
   * declaration takes precedence.
   */
  Expr addBoundVar(const std::string& name, const Type& type);
  //! Create and add a new bound named def to the stack, for parseExprOp().
  /*!
   * The stack is popped automatically upon return from the
   * parseExprOp() which used this method.
   *
   * Bound variable names may repeat, in which case the latest
   * declaration takes precedence.
   *
   * The type may be Null, but 'def' must always be a valid Expr
   */
  Expr addBoundVar(const std::string& name, const Type& type, const Expr& def);

  /*! @brief Lookup variable and return it and its type.  Return NULL Expr if
    it doesn't exist yet. */
  Expr lookupVar(const std::string& name, Type* type);

  //! Create a new uninterpreted type with the given name
  /*! Add the name to the global variable database d_globals
   */
  Type newTypeExpr(const std::string& name);
  //! Lookup type by name.  Return Null if no such type exists.
  Type lookupTypeExpr(const std::string& name);
  //! Create a new type abbreviation with the given name 
  Type newTypeExpr(const std::string& name, const Type& def);

  //! Create a new subtype expression
  Type newSubtypeExpr(const Expr& pred, const Expr& witness);

  //! Resolve an identifier, for use in parseExprOp()
  /*!
   * First, search the bound variable stack, and if the name is not
   * found, search the global constant and type declarations.  
   *
   * \return an expression to use in place of the identifier, or Null
   * if cannot resolve the name.
   */
  Expr resolveID(const std::string& name);

  //! Install name as a new identifier associated with Expr e
  void installID(const std::string& name, const Expr& e);

  Theorem typePred(const Expr& e);

  /*@}*/ // End of Common Type and Expr Methods

  /***************************************************************************/
  /*!
   *\name Commonly Used Proof Rules
   *\anchor theory_api_core_proof_rules
   *@{
   */
  /***************************************************************************/

  //!  ==> a == a
  Theorem reflexivityRule(const Expr& a)
    { return d_commonRules->reflexivityRule(a); }

  //!  a1 == a2 ==> a2 == a1
  Theorem symmetryRule(const Theorem& a1_eq_a2)
    { return d_commonRules->symmetryRule(a1_eq_a2); }

  //! (a1 == a2) & (a2 == a3) ==> (a1 == a3)
  Theorem transitivityRule(const Theorem& a1_eq_a2,
			   const Theorem& a2_eq_a3)
    { return d_commonRules->transitivityRule(a1_eq_a2, a2_eq_a3); }

  //! (c_1 == d_1) & ... & (c_n == d_n) ==> op(c_1,...,c_n) == op(d_1,...,d_n)
  Theorem substitutivityRule(const Op& op,
			     const std::vector<Theorem>& thms)
    { return d_commonRules->substitutivityRule(op, thms); }

  //! Special case for unary operators
  Theorem substitutivityRule(const Expr& e,
                             const Theorem& t)
    { return d_commonRules->substitutivityRule(e, t); }

  //! Special case for binary operators
  Theorem substitutivityRule(const Expr& e,
                             const Theorem& t1,
                             const Theorem& t2)
    { return d_commonRules->substitutivityRule(e, t1, t2); }

  //! Optimized: only positions which changed are included
  Theorem substitutivityRule(const Expr& e,
			     const std::vector<unsigned>& changed,
			     const std::vector<Theorem>& thms)
    { return d_commonRules->substitutivityRule(e, changed, thms); }

  //! Optimized: only a single position changed
  Theorem substitutivityRule(const Expr& e,
                             int changed,
                             const Theorem& thm)
    { return d_commonRules->substitutivityRule(e, changed, thm); }

  //! e1 AND (e1 IFF e2) ==> e2
  Theorem iffMP(const Theorem& e1, const Theorem& e1_iff_e2) {
    return d_commonRules->iffMP(e1, e1_iff_e2);
  }

  //! ==> AND(e1,e2) IFF [simplified expr]
  Theorem rewriteAnd(const Expr& e) {
    return d_commonRules->rewriteAnd(e);
  }

  //! ==> OR(e1,...,en) IFF [simplified expr]
  Theorem rewriteOr(const Expr& e) {
    return d_commonRules->rewriteOr(e);
  }
  
  //! Derived rule for rewriting ITE
  Theorem rewriteIte(const Expr& e);
  
  /*@}*/ // End of Commonly Used Proof Rules


};

/*@}*/ // End of group Theories

}

#endif
