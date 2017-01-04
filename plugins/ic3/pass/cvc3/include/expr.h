/*****************************************************************************/
/*!
 * \file expr.h
 * \brief Definition of the API to expression package.  See class Expr for details.
 *
 * Author: Clark Barrett
 *
 * Created: Tue Nov 26 00:27:40 2002
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

#ifndef _cvc3__expr_h_
#define _cvc3__expr_h_

#include <stdlib.h>
#include <sstream>
#include <set>
#include <functional>
#include <iterator>
#include <map>

#include "os.h"
#include "compat_hash_map.h"
#include "compat_hash_set.h"
#include "rational.h"
#include "kinds.h"
#include "cdo.h"
#include "cdflags.h"
#include "lang.h"
#include "memory_manager.h"

class CInterface;

namespace CVC3 {

  class NotifyList;
  class Theory;
  class Op;
  class Type;
  class Theorem;

  template<class Data>
  class ExprHashMap;

  class ExprManager;
  // Internal data-holding classes
  class ExprValue;
  class ExprNode;
  // Printing
  class ExprStream;

  //! Type ID of each ExprValue subclass.
  /*! It is defined in expr.h, so that ExprManager can use it before loading
    expr_value.h */
  typedef enum {
    EXPR_VALUE,
    EXPR_NODE,
    EXPR_APPLY, //!< Application of functions and predicates
    EXPR_STRING,
    EXPR_RATIONAL,
    EXPR_SKOLEM,
    EXPR_UCONST,
    EXPR_SYMBOL,
    EXPR_BOUND_VAR,
    EXPR_CLOSURE,
    EXPR_VALUE_TYPE_LAST // The end of list; don't assign it to any subclass
  } ExprValueType;

  //! Enum for cardinality of types
  typedef enum {
    CARD_FINITE,
    CARD_INFINITE,
    CARD_UNKNOWN
  } Cardinality;

  //! Expression index type
  typedef long unsigned ExprIndex;

  /**************************************************************************/
  /*! \defgroup ExprPkg Expression Package
   * \ingroup BuildingBlocks
   */
  /**************************************************************************/
  /*! \defgroup Expr_SmartPointer Smart Pointer Functionality in Expr
   * \ingroup ExprPkg
   */
  /**************************************************************************/

  /**************************************************************************/
  //! Data structure of expressions in CVC3
  /*! \ingroup ExprPkg
   * Class: Expr <br>
   * Author: Clark Barrett <br>
   * Created: Mon Nov 25 15:29:37 2002
   *
   * This class is the main data structure for expressions that all
   * other components should use.  It is actually a <em>smart
   * pointer</em> to the actual data holding class ExprValue and its
   * subclasses.
   *
   * Expressions are represented as DAGs with maximal sharing of
   * subexpressions.  Therefore, testing for equality is a constant time
   * operation (simply compare the pointers).
   *
   * Unused expressions are automatically garbage-collected.  The use is
   * determined by a reference counting mechanism.  In particular, this
   * means that if there is a circular dependency among expressions
   * (e.g. an attribute points back to the expression itself), these
   * expressions will not be garbage-collected, even if no one else is
   * using them.
   *
   * The most frequently used operations are getKind() (determining the
   * kind of the top level node of the expression), arity() (how many
   * children an Expr has), operator[]() for accessing a child, and
   * various testers and methods for constructing new expressions.
   *
   * In addition, a total ordering operator<() is provided.  It is
   * guaranteed to remain the same for the lifetime of the expressions
   * (it may change, however, if the expression is garbage-collected and
   * reborn).
   */
  /**************************************************************************/
class CVC_DLL Expr {
  friend class ExprHasher;
  friend class ExprManager;
  friend class Op;
  friend class ExprValue;
  friend class ExprNode;
  friend class ExprClosure;
  friend class ::CInterface;
  friend class Theorem;

  /*! \addtogroup ExprPkg
   * @{
   */
  //! bit-masks for static flags
  typedef enum {
    //! Whether is valid TYPE expr
    VALID_TYPE = 0x1,
    //! Whether IS_ATOMIC flag is valid (initialized)
    VALID_IS_ATOMIC = 0x2,
    //! Whether the expression is an atomic term or formula
    IS_ATOMIC = 0x4,
    //! Expression is the result of a "normal" (idempotent) rewrite
    REWRITE_NORMAL = 0x8,
    //! Finite type
    IS_FINITE = 0x400,
    //! Well-founded (used in datatypes)
    WELL_FOUNDED = 0x800,
    //! Compute transitive closure (for binary uninterpreted predicates)
    COMPUTE_TRANS_CLOSURE = 0x1000,
    //! Whether expr contains a bounded variable (for quantifier instantiation)
    CONTAINS_BOUND_VAR = 0x00020000,
    //! Whether expr uses CC algorithm that relies on not simplifying an expr that has a find
    USES_CC = 0x00080000,
    //! Whether TERMINALS_CONST flag is valid (initialized)
    VALID_TERMINALS_CONST = 0x00100000,
    //! Whether expr contains only numerical constants at all possible ite terminals
    TERMINALS_CONST = 0x00200000
  } StaticFlagsEnum;

  //! bit-masks for dynamic flags
  // TODO: Registered flags instead of hard-wired
  typedef enum {
    //! Whether expr has been added as an implied literal
    IMPLIED_LITERAL = 0x10,
    IS_USER_ASSUMPTION = 0x20,
    IS_INT_ASSUMPTION = 0x40,
    IS_JUSTIFIED = 0x80,
    IS_TRANSLATED = 0x100,
    IS_USER_REGISTERED_ATOM = 0x200,
    IS_SELECTED = 0x2000,
    IS_STORED_PREDICATE = 0x4000,
    IS_REGISTERED_ATOM = 0x8000,
    IN_USER_ASSUMPTION = 0x00010000,
    //! Whether expr is normalized (in array theory)
    NOT_ARRAY_NORMALIZED = 0x00040000
  } DynamicFlagsEnum;

  //! Convenient null expr
  static Expr s_null;

  /////////////////////////////////////////////////////////////////////////////
  // Private Dynamic Data                                                    //
  /////////////////////////////////////////////////////////////////////////////
  //! The value.  This is the only data member in this class.
  ExprValue* d_expr;

  /////////////////////////////////////////////////////////////////////////////
  // Private methods                                                         //
  /////////////////////////////////////////////////////////////////////////////

  //! Private constructor, simply wraps around the pointer
  Expr(ExprValue* expr);

  Expr recursiveSubst(const ExprHashMap<Expr>& subst,
                      ExprHashMap<Expr>& visited) const;

  Expr recursiveQuantSubst(ExprHashMap<Expr>& subst,
                      ExprHashMap<Expr>& visited) const;
public:
  /////////////////////////////////////////////////////////////////////////////
  // Public Classes and Types                                                //
  /////////////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////////////
  /*!
   * Class: Expr::iterator
   * Author: Sergey Berezin
   * Created: Fri Dec  6 15:38:51 2002
   * Description: STL-like iterator API to the Expr's children.
   * IMPORTANT: the iterator will not be valid after the originating
   * expression is destroyed.
  */
  /////////////////////////////////////////////////////////////////////////////
  class CVC_DLL iterator
    : public std::iterator<std::input_iterator_tag,Expr,ptrdiff_t>
  {
    friend class Expr;
  private:
    std::vector<Expr>::const_iterator d_it;
    // Private constructors (used by Expr only)
    //
    //! Construct an iterator out of the vector's iterator
    iterator(std::vector<Expr>::const_iterator it): d_it(it) { }
    // Public methods
  public:
    //! Default constructor
    iterator() { }
    // Copy constructor and operator= are defined by C++, that's good enough

    //! Equality
    bool operator==(const iterator& i) const {
      return d_it == i.d_it;
    }
    //! Disequality
    bool operator!=(const iterator& i) const { return !(*this == i); }
    //! Dereference operator
    const Expr& operator*() const { return *d_it; }
    //! Dereference and member access
    const Expr* operator->() const { return &(operator*()); }
    //! Prefix increment
    iterator& operator++() {
      ++d_it;
      return *this;
    }
    /*! @brief Postfix increment requires a Proxy object to hold the
     * intermediate value for dereferencing */
    class Proxy {
      const Expr* d_e;
    public:
      Proxy(const Expr& e) : d_e(&e) { }
      Expr operator*() { return *d_e; }
    };
    //! Postfix increment
    /*! \return Proxy with the old Expr.
     *
     * Now, an expression like *i++ will return the current *i, and
     * then advance the iterator.  However, don't try to use Proxy for
     * anything else.
     */
    Proxy operator++(int) {
      Proxy e(*(*this));
      ++(*this);
      return e;
    }
  }; // end of class Expr::iterator

  /////////////////////////////////////////////////////////////////////////////
  // Constructors                                                            //
  /////////////////////////////////////////////////////////////////////////////

  //! Default constructor creates the Null Expr
  Expr(): d_expr(NULL) {}

  /*! @brief Copy constructor and assignment (copy the pointer and take care
    of the refcount) */
  Expr(const Expr& e);
  //! Assignment operator: take care of the refcounting and GC
  Expr& operator=(const Expr& e);

  // These constructors grab the ExprManager from the Op or the first
  // child.  The operator and all children must belong to the same
  // ExprManager.
  Expr(const Op& op, const Expr& child);
  Expr(const Op& op, const Expr& child0, const Expr& child1);
  Expr(const Op& op, const Expr& child0, const Expr& child1,
       const Expr& child2);
  Expr(const Op& op, const Expr& child0, const Expr& child1,
       const Expr& child2, const Expr& child3);
  Expr(const Op& op, const std::vector<Expr>& children,
       ExprManager* em = NULL);

  //! Destructor
  ~Expr();

  // Compound expression constructors
  Expr eqExpr(const Expr& right) const;
  Expr notExpr() const;
  Expr negate() const; // avoid double-negatives
  Expr andExpr(const Expr& right) const;
  Expr orExpr(const Expr& right) const;
  Expr iteExpr(const Expr& thenpart, const Expr& elsepart) const;
  Expr iffExpr(const Expr& right) const;
  Expr impExpr(const Expr& right) const;
  Expr xorExpr(const Expr& right) const;
  //! Create a Skolem constant for the i'th variable of an existential (*this)
  Expr skolemExpr(int i) const;
  //! Create a Boolean variable out of the expression
  //  Expr boolVarExpr() const;
  //! Rebuild Expr with a new ExprManager
  Expr rebuild(ExprManager* em) const;
//    Expr newForall(const Expr& e);
//    Expr newExists(const Expr& e);
  Expr substExpr(const std::vector<Expr>& oldTerms,
                 const std::vector<Expr>& newTerms) const;
  Expr substExpr(const ExprHashMap<Expr>& oldToNew) const;

// by yeting, a special subst function for TheoryQuant
  Expr substExprQuant(const std::vector<Expr>& oldTerms,
		      const std::vector<Expr>& newTerms) const;


  Expr operator!() const { return notExpr(); }
  Expr operator&&(const Expr& right) const { return andExpr(right); }
  Expr operator||(const Expr& right) const { return orExpr(right); }

  /////////////////////////////////////////////////////////////////////////////
  // Public Static Methods                                                   //
  /////////////////////////////////////////////////////////////////////////////

  static size_t hash(const Expr& e);

  /////////////////////////////////////////////////////////////////////////////
  // Read-only (const) methods                                               //
  /////////////////////////////////////////////////////////////////////////////

  size_t hash() const;

  // Core expression testers

  bool isFalse() const { return getKind() == FALSE_EXPR; }
  bool isTrue() const { return getKind() == TRUE_EXPR; }
  bool isBoolConst() const { return isFalse() || isTrue(); }
  bool isVar() const;
  bool isBoundVar() const { return getKind() == BOUND_VAR; }
  bool isString() const;
  bool isClosure() const;
  bool isQuantifier() const;
  bool isLambda() const;
  bool isApply() const;
  bool isSymbol() const;
  bool isTheorem() const;

  bool isConstant() const { return getOpKind() <= MAX_CONST; }
  
  bool isRawList() const {return getKind() == RAW_LIST;}

  //! Expr represents a type
  bool isType() const;
  /*
  bool isRecord() const;
  bool isRecordAccess() const;
  bool isTupleAccess() const;
  */
  //! Provide access to ExprValue for client subclasses of ExprValue *only*
  /*@ Calling getExprValue on an Expr with a built-in ExprValue class will
   * cause an error */
  const ExprValue* getExprValue() const;

  //! Test if e is a term (as opposed to a predicate/formula)
  bool isTerm() const;
  //! Test if e is atomic
  /*! An atomic expression is TRUE or FALSE or one that does not
   *  contain a formula (including not being a formula itself).
   *  \sa isAtomicFormula */
  bool isAtomic() const;
  //! Test if e is an atomic formula
  /*! An atomic formula is TRUE or FALSE or an application of a predicate
    (possibly 0-ary) which does not properly contain any formula.  For
    instance, the formula "x = IF f THEN y ELSE z ENDIF" is not an atomic
    formula, since it contains the condition "f", which is a formula. */
  bool isAtomicFormula() const;
  //! An abstract atomic formua is an atomic formula or a quantified formula
  bool isAbsAtomicFormula() const
    { return isQuantifier() || isAtomicFormula(); }
  //! Test if e is a literal
  /*! A literal is an atomic formula, or its negation.
    \sa isAtomicFormula */
  bool isLiteral() const
  { return (isAtomicFormula() || (isNot() && (*this)[0].isAtomicFormula())); }
  //! Test if e is an abstract literal
  bool isAbsLiteral() const
  { return (isAbsAtomicFormula() || (isNot() && (*this)[0].isAbsAtomicFormula())); }
  //! A Bool connective is one of NOT,AND,OR,IMPLIES,IFF,XOR,ITE (with type Bool)
  bool isBoolConnective() const;
  //! True iff expr is not a Bool connective
  bool isPropAtom() const { return !isTerm() && !isBoolConnective(); }
  //! PropAtom or negation of PropAtom
  bool isPropLiteral() const
    { return (isNot() && (*this)[0].isPropAtom()) || isPropAtom(); }
  //! Return whether Expr contains a non-bool type ITE as a sub-term
  bool containsTermITE() const;


  bool isEq() const { return getKind() == EQ; }
  bool isNot() const { return getKind() == NOT; }
  bool isAnd() const { return getKind() == AND; }
  bool isOr() const { return getKind() == OR; }
  bool isITE() const { return getKind() == ITE; }
  bool isIff() const { return getKind() == IFF; }
  bool isImpl() const { return getKind() == IMPLIES; }
  bool isXor() const { return getKind() == XOR;}

  bool isForall() const { return getKind() == FORALL; }
  bool isExists() const { return getKind() == EXISTS; }

  bool isRational() const { return getKind() == RATIONAL_EXPR; }
  bool isSkolem() const { return getKind() == SKOLEM_VAR;}

  // Leaf accessors - these functions must only be called one expressions of
  // the appropriate kind.

  // For UCONST and BOUND_VAR Expr's
  const std::string& getName() const;
  //! For BOUND_VAR, get the UID
  const std::string& getUid() const;

  // For STRING_EXPR's
  const std::string& getString() const;
  //! Get bound variables from a closure Expr
  const std::vector<Expr>& getVars() const;
  //! Get the existential axiom expression for skolem constant
  const Expr& getExistential() const;
  //! Get the index of the bound var that skolem constant comes from
  int getBoundIndex() const;

  //! Get the body of the closure Expr
  const Expr& getBody() const;

  //! Set the triggers for a closure Expr
  void setTriggers(const std::vector<std::vector<Expr> >& triggers) const;
  void setTriggers(const std::vector<Expr>& triggers) const;
  void setTrigger(const Expr& trigger) const;
  void setMultiTrigger(const std::vector<Expr>& multiTrigger) const;

  //! Get the manual triggers of the closure Expr
  const std::vector<std::vector<Expr> >& getTriggers() const; //by yeting

  //! Get the Rational value out of RATIONAL_EXPR
  const Rational& getRational() const;
  //! Get theorem from THEOREM_EXPR
  const Theorem& getTheorem() const;

  // Get the expression manager.  The expression must be non-null.
  ExprManager *getEM() const;

  // Return a ref to the vector of children.
  const std::vector<Expr>& getKids() const;

  // Get the kind of this expr.
  int getKind() const;

  // Get the index field
  ExprIndex getIndex() const;

  // True if this is the most recently created expression
  bool hasLastIndex() const;

  //! Make the expr into an operator
  Op mkOp() const;

  //! Get operator from expression
  Op getOp() const;

  //! Get expression of operator (for APPLY Exprs only)
  Expr getOpExpr() const;

  //! Get kind of operator (for APPLY Exprs only)
  int getOpKind() const;

  // Return the number of children.  Note, that an application of a
  // user-defined function has the arity of that function (the number
  // of arguments), and the function name itself is part of the
  // operator.
  int arity() const;

  // Return the ith child.  As with arity, it's also the ith argument
  // in function application.
  const Expr& operator[](int i) const;

  //! Remove leading NOT if any
  const Expr& unnegate() const { return isNot() ? (*this)[0] : *this; }

  //! Begin iterator
  iterator begin() const;

  //! End iterator
  iterator end() const;

  // Check if Expr is Null
  bool isNull() const;

  // Check if Expr is not Null
  bool isInitialized() const { return d_expr != NULL; }
  //! Get the memory manager index (it uniquely identifies the subclass)
  size_t getMMIndex() const;

  // Attributes

  // True if the find attribute has been set to something other than NULL.
  bool hasFind() const;

  // Return the attached find attribute for the expr.  Note that this
  // must be called repeatedly to get the root of the union-find tree.
  // Should only be called if hasFind is true.
  const Theorem& getFind() const;
  int getFindLevel() const;
  const Theorem& getEqNext() const;

  // Return the notify list
  NotifyList* getNotify() const;

  //! Get the type.  Recursively compute if necessary
  Type getType() const;
  //! Look up the current type. Do not recursively compute (i.e. may be NULL)
  Type lookupType() const;
  //! Return cardinality of type
  Cardinality typeCard() const;
  //! Return nth (starting with 0) element in a finite type
  /*! Returns NULL Expr if unable to compute nth element
   */
  Expr typeEnumerateFinite(Unsigned n) const;
  //! Return size of a finite type; returns 0 if size cannot be determined
  Unsigned typeSizeFinite() const;

  /*! @brief Return true if there is a valid cached value for calling
      simplify on this Expr. */
  bool validSimpCache() const;

  // Get the cached Simplify of this Expr.
  const Theorem& getSimpCache() const;

  // Return true if valid type flag is set for Expr
  bool isValidType() const;

  // Return true if there is a valid flag for whether Expr is atomic
  bool validIsAtomicFlag() const;

  // Return true if there is a valid flag for whether terminals are const
  bool validTerminalsConstFlag() const;

  // Get the isAtomic flag
  bool getIsAtomicFlag() const;

  // Get the TerminalsConst flag
  bool getTerminalsConstFlag() const;

  // Get the RewriteNormal flag
  bool isRewriteNormal() const;

  // Get the isFinite flag
  bool isFinite() const;

  // Get the WellFounded flag
  bool isWellFounded() const;

  // Get the ComputeTransClosure flag
  bool computeTransClosure() const;

  // Get the ContainsBoundVar flag
  bool containsBoundVar() const;

  // Get the usesCC flag
  bool usesCC() const;

  // Get the notArrayNormalized flag
  bool notArrayNormalized() const;

  // Get the ImpliedLiteral flag
  bool isImpliedLiteral() const;

  // Get the UserAssumption flag
  bool isUserAssumption() const;

  // Get the inUserAssumption flag
  bool inUserAssumption() const;

  // Get the IntAssumption flag
  bool isIntAssumption() const;

  // Get the Justified flag
  bool isJustified() const;

  // Get the Translated flag
  bool isTranslated() const;

  // Get the UserRegisteredAtom flag
  bool isUserRegisteredAtom() const;

  // Get the RegisteredAtom flag
  bool isRegisteredAtom() const;

  // Get the Selected flag
  bool isSelected() const;

  // Get the Stored Predicate flag
  bool isStoredPredicate() const;

  //! Check if the generic flag is set
  bool getFlag() const;
  //! Set the generic flag
  void setFlag() const;
  //! Clear the generic flag in all Exprs
  void clearFlags() const;

  // Printing functions

  //! Print the expression to a string
  std::string toString() const;
  //! Print the expression to a string using the given output language
  std::string toString(InputLanguage lang) const;
  //! Print the expression in the specified format
  void print(InputLanguage lang, bool dagify = true) const;

  //! Print the expression as AST (lisp-like format)
  void print() const { print(AST_LANG); }
  //! Print the expression as AST without dagifying
  void printnodag() const;

  //! Pretty-print the expression
  void pprint() const;
  //! Pretty-print without dagifying
  void pprintnodag() const;

  //! Print a leaf node
  /*@ The top node is pretty-printed if it is a basic leaf type;
   * otherwise, just the kind is printed.  Should only be called on expressions
   * with no children. */
  ExprStream& print(ExprStream& os) const;
  //! Print the top node and then recurse through the children */
  /*@ The top node is printed as an AST with all the information, including
   * "hidden" Exprs that are part of the ExprValue */
  ExprStream& printAST(ExprStream& os) const;
  //! Set initial indentation to n.
  /*! The indentation will be reset to default unless the second
    argument is true.
    \return reference to itself, so one can write `os << e.indent(5)'
  */
  Expr& indent(int n, bool permanent = false);

  /////////////////////////////////////////////////////////////////////////////
  // Other Public methods                                                    //
  /////////////////////////////////////////////////////////////////////////////

  // Attributes

  //! Set the find attribute to e
  void setFind(const Theorem& e) const;

  //! Set the eqNext attribute to e
  void setEqNext(const Theorem& e) const;

  //! Add (e,i) to the notify list of this expression
  void addToNotify(Theory* i, const Expr& e) const;

  //! Set the cached type
  void setType(const Type& t) const;

  // Cache the result of a call to Simplify on this Expr
  void setSimpCache(const Theorem& e) const;

  // Set the valid type flag for this Expr
  void setValidType() const;

  // Set the isAtomicFlag for this Expr
  void setIsAtomicFlag(bool value) const;

  // Set the TerminalsConst flag for this Expr
  void setTerminalsConstFlag(bool value) const;

  // Set or clear the RewriteNormal flag
  void setRewriteNormal() const;
  void clearRewriteNormal() const;

  // Set the isFinite flag
  void setFinite() const;

  // Set the WellFounded flag
  void setWellFounded() const;

  // Set the ComputeTransClosure flag
  void setComputeTransClosure() const;

  // Set the ContainsBoundVar flag
  void setContainsBoundVar() const;

  // Set the UsesCC flag
  void setUsesCC() const;

  // Set the notArrayNormalized flag
  void setNotArrayNormalized() const;

  // Set the impliedLiteral flag for this Expr
  void setImpliedLiteral() const;

  // Set the user assumption flag for this Expr
  void setUserAssumption(int scope = -1) const;

  // Set the in user assumption flag for this Expr
  void setInUserAssumption(int scope = -1) const;

  // Set the internal assumption flag for this Expr
  void setIntAssumption() const;

  // Set the justified flag for this Expr
  void setJustified() const;

  //! Set the translated flag for this Expr
  void setTranslated(int scope = -1) const;

  //! Set the UserRegisteredAtom flag for this Expr
  void setUserRegisteredAtom() const;

  //! Set the RegisteredAtom flag for this Expr
  void setRegisteredAtom() const;

  //! Set the Selected flag for this Expr
  void setSelected() const;

  //! Set the Stored Predicate flag for this Expr
  void setStoredPredicate() const;

  //! Check if the current Expr (*this) is a subexpression of e
  bool subExprOf(const Expr& e) const;
  // Returns the maximum number of Boolean expressions on a path from
  // this to a leaf, including this.

  inline Unsigned getSize() const;

//   inline int getHeight() const;

//   // Returns the index of the highest kid.
//   inline int getHighestKid() const;

//   // Gets/sets an expression that this expression was simplified from
//   // (see newRWTheorem). This is the equivalent of SVC's Sigx.
//   inline bool hasSimpFrom() const;
//   inline const Expr& getSimpFrom() const;
//   inline void setSimpFrom(const Expr& simpFrom);

  // Attributes for uninterpreted function symbols.
  bool hasSig() const;
  bool hasRep() const;
  const Theorem& getSig() const;
  const Theorem& getRep() const;
  void setSig(const Theorem& e) const;
  void setRep(const Theorem& e) const;

  /////////////////////////////////////////////////////////////////////////////
  // Friend methods                                                          //
  /////////////////////////////////////////////////////////////////////////////

  friend CVC_DLL std::ostream& operator<<(std::ostream& os, const Expr& e);

  // The master method which defines some fixed total ordering on all
  // Exprs.  If e1 < e2, e1==e2, and e1 > e2, it returns -1, 0, 1
  // respectively.  A Null expr is always "smaller" than any other
  // expr, but is equal to itself.
  friend int compare(const Expr& e1, const Expr& e2);

  friend bool operator==(const Expr& e1, const Expr& e2);
  friend bool operator!=(const Expr& e1, const Expr& e2);

  friend bool operator<(const Expr& e1, const Expr& e2);
  friend bool operator<=(const Expr& e1, const Expr& e2);
  friend bool operator>(const Expr& e1, const Expr& e2);
  friend bool operator>=(const Expr& e1, const Expr& e2);

  /*!@}*/ // end of group Expr

}; // end of class Expr

} // end of namespace CVC3

// Include expr_value.h here.  We cannot include it earlier, since it
// needs the definition of class Expr.  See comments in expr_value.h.
#ifndef DOXYGEN
#include "expr_op.h"
#include "expr_manager.h"
#endif
namespace CVC3 {

inline Expr::Expr(ExprValue* expr) : d_expr(expr) { d_expr->incRefcount(); }

inline Expr::Expr(const Expr& e) : d_expr(e.d_expr) {
  if (d_expr != NULL) d_expr->incRefcount();
}

inline Expr& Expr::operator=(const Expr& e) {
  if(&e == this) return *this; // Self-assignment
  ExprValue* tmp = e.d_expr;
  if(tmp == d_expr) return *this;
  if (tmp == NULL) {
    d_expr->decRefcount();
  }
  else {
    tmp->incRefcount();
    if(d_expr != NULL) {
      d_expr->decRefcount();
    }
  }
  d_expr = tmp;
  return *this;
}

inline Expr::Expr(const Op& op, const Expr& child) {
  ExprManager* em = child.getEM();
  if (op.getKind() != APPLY) {
    ExprNode ev(em, op.getKind());
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child);
    d_expr = em->newExprValue(&ev);
  } else {
    ExprApply ev(em, op);
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child);
    d_expr = em->newExprValue(&ev);
  }
  d_expr->incRefcount();
}

inline Expr::Expr(const Op& op, const Expr& child0, const Expr& child1) {
  ExprManager* em = child0.getEM();
  if (op.getKind() != APPLY) {
    ExprNode ev(em, op.getKind());
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child0);
    kids.push_back(child1);
    d_expr = em->newExprValue(&ev);
  } else {
    ExprApply ev(em, op);
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child0);
    kids.push_back(child1);
    d_expr = em->newExprValue(&ev);
  }
  d_expr->incRefcount();
}

inline Expr::Expr(const Op& op, const Expr& child0, const Expr& child1,
                  const Expr& child2) {
  ExprManager* em = child0.getEM();
  if (op.getKind() != APPLY) {
    ExprNode ev(em, op.getKind());
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child0);
    kids.push_back(child1);
    kids.push_back(child2);
    d_expr = em->newExprValue(&ev);
  } else {
    ExprApply ev(em, op);
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child0);
    kids.push_back(child1);
    kids.push_back(child2);
    d_expr = em->newExprValue(&ev);
  }
  d_expr->incRefcount();
}

inline Expr::Expr(const Op& op, const Expr& child0, const Expr& child1,
                  const Expr& child2, const Expr& child3) {
  ExprManager* em = child0.getEM();
  if (op.getKind() != APPLY) {
    ExprNode ev(em, op.getKind());
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child0);
    kids.push_back(child1);
    kids.push_back(child2);
    kids.push_back(child3);
    d_expr = em->newExprValue(&ev);
  } else {
    ExprApply ev(em, op);
    std::vector<Expr>& kids = ev.getKids1();
    kids.push_back(child0);
    kids.push_back(child1);
    kids.push_back(child2);
    kids.push_back(child3);
    d_expr = em->newExprValue(&ev);
  }
  d_expr->incRefcount();
}

inline Expr::Expr(const Op& op, const std::vector<Expr>& children,
                  ExprManager* em) {
  if (em == NULL) {
    if (op.getKind() == APPLY) em = op.getExpr().getEM();
    else {
      DebugAssert(children.size() > 0,
                  "Expr::Expr(Op, children): op's EM is NULL and "
                  "no children given");
      em = children[0].getEM();
    }
  }
  if (op.getKind() != APPLY) {
    ExprNodeTmp ev(em, op.getKind(), children);
    d_expr = em->newExprValue(&ev);
  } else {
    ExprApplyTmp ev(em, op, children);
    d_expr = em->newExprValue(&ev);
  }
  d_expr->incRefcount();
}

inline Expr Expr::eqExpr(const Expr& right) const {
  return Expr(EQ, *this, right);
}

inline Expr Expr::notExpr() const {
  return Expr(NOT, *this);
}

inline Expr Expr::negate() const {
  return isNot() ? (*this)[0] : this->notExpr();
}

inline Expr Expr::andExpr(const Expr& right) const {
  return Expr(AND, *this, right);
}

inline Expr andExpr(const std::vector <Expr>& children) {
  DebugAssert(children.size()>0 && !children[0].isNull(),
              "Expr::andExpr(kids)");
  return Expr(AND, children);
}

inline Expr Expr::orExpr(const Expr& right) const {
  return Expr(OR, *this, right);
}

inline Expr orExpr(const std::vector <Expr>& children) {
  DebugAssert(children.size()>0 && !children[0].isNull(),
              "Expr::andExpr(kids)");
  return Expr(OR, children);
}

inline Expr Expr::iteExpr(const Expr& thenpart, const Expr& elsepart) const {
  return Expr(ITE, *this, thenpart, elsepart);
}

inline Expr Expr::iffExpr(const Expr& right) const {
  return Expr(IFF, *this, right);
}

inline Expr Expr::impExpr(const Expr& right) const {
  return Expr(IMPLIES, *this, right);
}

inline Expr Expr::xorExpr(const Expr& right) const {
  return Expr(XOR, *this, right);
}

inline Expr Expr::skolemExpr(int i) const {
  return getEM()->newSkolemExpr(*this, i);
}

inline Expr Expr::rebuild(ExprManager* em) const {
  return em->rebuild(*this);
}

inline Expr::~Expr() {
  if(d_expr != NULL) {
    IF_DEBUG(FatalAssert(d_expr->d_refcount > 0, "Mis-handled the ref. counting");)
    if (--(d_expr->d_refcount) == 0) d_expr->d_em->gc(d_expr);
  }
}

inline size_t Expr::hash(const Expr& e) { return e.getEM()->hash(e); }

/////////////////////////////////////////////////////////////////////////////
// Read-only (const) methods                                               //
/////////////////////////////////////////////////////////////////////////////

inline size_t Expr::hash() const { return getEM()->hash(*this); }

inline const ExprValue* Expr::getExprValue() const
  { return d_expr->getExprValue(); }

// Core Expression Testers

inline bool Expr::isVar() const { return d_expr->isVar(); }
inline bool Expr::isString() const { return d_expr->isString(); }
inline bool Expr::isClosure() const { return d_expr->isClosure(); }
inline bool Expr::isQuantifier() const {
  return (isClosure() && (getKind() == FORALL || getKind() == EXISTS));
}
inline bool Expr::isLambda() const {
  return (isClosure() && getKind() == LAMBDA);
}
inline bool Expr::isApply() const
{ DebugAssert((getKind() != APPLY || d_expr->isApply()) &&
              (!d_expr->isApply() || getKind() == APPLY), "APPLY mismatch");
  return getKind() == APPLY; }
inline bool Expr::isSymbol() const { return d_expr->isSymbol(); }
inline bool Expr::isTheorem() const { return d_expr->isTheorem(); }
inline bool Expr::isType() const { return getEM()->isTypeKind(getOpKind()); }
inline bool Expr::isTerm() const { return !getType().isBool(); }
inline bool Expr::isBoolConnective() const {
  if (!getType().isBool()) return false;
  switch (getKind()) {
    case NOT: case AND: case OR: case IMPLIES: case IFF: case XOR: case ITE:
      return true; }
  return false;
}

inline Unsigned Expr::getSize() const {
  if (d_expr->d_size == 0) {
    clearFlags();
    const_cast<ExprValue*>(d_expr)->d_size = d_expr->getSize();
  }
  return d_expr->d_size;
}

  //inline int Expr::getHeight() const { return d_expr->getHeight(); }
  //inline int Expr::getHighestKid() const { return d_expr->getHighestKid(); }

  //inline bool Expr::hasSimpFrom() const
//   { return !d_expr->getSimpFrom().isNull(); }
// inline const Expr& Expr::getSimpFrom() const
//   { return hasSimpFrom() ? d_expr->getSimpFrom() : *this; }
// inline void Expr::setSimpFrom(const Expr& simpFrom)
//   { d_expr->setSimpFrom(simpFrom); }

// Leaf accessors

inline const std::string& Expr::getName() const {
  DebugAssert(!isNull(), "Expr::getName() on Null expr");
  return d_expr->getName();
}

inline const std::string& Expr::getString() const {
   DebugAssert(isString(),
       	"CVC3::Expr::getString(): not a string Expr:\n  "
       	+ toString(AST_LANG));
   return d_expr->getString();
}

inline const std::vector<Expr>& Expr::getVars() const {
   DebugAssert(isClosure(),
       	"CVC3::Expr::getVars(): not a closure Expr:\n  "
       	+ toString(AST_LANG));
   return d_expr->getVars();
}

inline const Expr& Expr::getBody() const {
   DebugAssert(isClosure(),
       	"CVC3::Expr::getBody(): not a closure Expr:\n  "
       	+ toString(AST_LANG));
   return d_expr->getBody();
}

 inline void Expr::setTriggers(const std::vector< std::vector<Expr> >& triggers) const {
  DebugAssert(isClosure(),
	      "CVC3::Expr::setTriggers(): not a closure Expr:\n  "
	      + toString(AST_LANG));
  d_expr->setTriggers(triggers);
}

inline void Expr::setTriggers(const std::vector<Expr>& triggers) const {
   DebugAssert(isClosure(),
               "CVC3::Expr::setTriggers(): not a closure Expr:\n  "
               + toString(AST_LANG));
   std::vector<std::vector<Expr> > patternvv;
   for(std::vector<Expr>::const_iterator i = triggers.begin(); i != triggers.end(); ++i ) {
     std::vector<Expr> patternv;
     patternv.push_back(*i);
     patternvv.push_back(patternv);
   }
   d_expr->setTriggers(patternvv);
 }

inline void Expr::setTrigger(const Expr& trigger) const {
  DebugAssert(isClosure(),
	      "CVC3::Expr::setTrigger(): not a closure Expr:\n  "
	      + toString(AST_LANG));
  std::vector<std::vector<Expr> > patternvv;
  std::vector<Expr> patternv;
  patternv.push_back(trigger);
  patternvv.push_back(patternv);
  setTriggers(patternvv);
}

inline void Expr::setMultiTrigger(const std::vector<Expr>& multiTrigger) const {
  DebugAssert(isClosure(),
              "CVC3::Expr::setTrigger(): not a closure Expr:\n  "
              + toString(AST_LANG));
  std::vector<std::vector<Expr> > patternvv;
  patternvv.push_back(multiTrigger);
  setTriggers(patternvv);
}

 inline const std::vector<std::vector<Expr> >& Expr::getTriggers() const { //by yeting
  DebugAssert(isClosure(),
	      "CVC3::Expr::getTrigs(): not a closure Expr:\n  "
	      + toString(AST_LANG));
  return d_expr->getTriggers();
}

inline const Expr& Expr::getExistential() const {
  DebugAssert(isSkolem(),
              "CVC3::Expr::getExistential() not a skolem variable");
  return d_expr->getExistential();
}
inline int Expr::getBoundIndex() const {
  DebugAssert(isSkolem(),
              "CVC3::Expr::getBoundIndex() not a skolem variable");
  return d_expr->getBoundIndex();
}


inline const Rational& Expr::getRational() const {
  DebugAssert(isRational(),
       	"CVC3::Expr::getRational(): not a rational Expr:\n  "
       	+ toString(AST_LANG));
   return d_expr->getRational();
}

inline const Theorem& Expr::getTheorem() const {
  DebugAssert(isTheorem(),
       	"CVC3::Expr::getTheorem(): not a Theorem Expr:\n  "
       	+ toString(AST_LANG));
   return d_expr->getTheorem();
}

inline const std::string& Expr::getUid() const {
   DebugAssert(getKind() == BOUND_VAR,
       	"CVC3::Expr::getUid(): not a BOUND_VAR Expr:\n  "
       	+ toString(AST_LANG));
   return d_expr->getUid();
}

inline ExprManager* Expr::getEM() const {
  DebugAssert(d_expr != NULL,
              "CVC3::Expr:getEM: on Null Expr (not initialized)");
  return d_expr->d_em;
}

inline const std::vector<Expr>& Expr::getKids() const {
  DebugAssert(d_expr != NULL, "Expr::getKids on Null Expr");
  if(isNull()) return getEM()->getEmptyVector();
  else return d_expr->getKids();
}

inline int Expr::getKind() const {
   if(d_expr == NULL) return NULL_KIND; // FIXME: invent a better Null kind
   return d_expr->d_kind;
 }

inline ExprIndex Expr::getIndex() const { return d_expr->d_index; }

inline bool Expr::hasLastIndex() const
{ return d_expr->d_em->lastIndex() == getIndex(); }

inline Op Expr::mkOp() const {
  DebugAssert(!isNull(), "Expr::mkOp() on Null expr");
  return Op(*this);
}

inline Op Expr::getOp() const {
  DebugAssert(!isNull(), "Expr::getOp() on Null expr");
  if (isApply()) return d_expr->getOp();
  DebugAssert(arity() > 0,
              "Expr::getOp() called on non-apply expr with no children");
  return Op(getKind());
}

inline Expr Expr::getOpExpr() const {
  DebugAssert(isApply(), "getOpExpr() called on non-apply");
  return getOp().getExpr();
}

inline int Expr::getOpKind() const {
  if (!isApply()) return getKind();
  return getOp().getExpr().getKind();
}

inline int Expr::arity() const {
  if(isNull()) return 0;
  else return d_expr->arity();
}

inline const Expr& Expr::operator[](int i) const {
  DebugAssert(i < arity(), "out of bounds access");
  return (d_expr->getKids())[i];
}

inline Expr::iterator Expr::begin() const {
  if (isNull() || d_expr->arity() == 0)
    return Expr::iterator(getEM()->getEmptyVector().begin());
  else return Expr::iterator(d_expr->getKids().begin());
}

inline Expr::iterator Expr::end() const {
  if (isNull() || d_expr->arity() == 0)
    return Expr::iterator(getEM()->getEmptyVector().end());
  else return Expr::iterator(d_expr->getKids().end());
}

inline bool Expr::isNull() const {
  return (d_expr == NULL) || (d_expr->d_kind == NULL_KIND);
}

inline size_t Expr::getMMIndex() const {
  DebugAssert(!isNull(), "Expr::getMMIndex()");
  return d_expr->getMMIndex();
}

inline bool Expr::hasFind() const {
  DebugAssert(!isNull(), "hasFind called on NULL Expr");
  return (d_expr->d_find && !(d_expr->d_find->get().isNull()));
}

inline const Theorem& Expr::getFind() const {
  DebugAssert(hasFind(), "Should only be called if find is valid");
  return d_expr->d_find->get();
}

inline int  Expr::getFindLevel() const {
  DebugAssert(hasFind(), "Should only be called if find is valid");
  return d_expr->d_find->level();
}

inline const Theorem& Expr::getEqNext() const {
  DebugAssert(!isNull(), "getEqNext called on NULL Expr");
  DebugAssert(hasFind(), "Should only be called if find is valid");
  DebugAssert(d_expr->d_eqNext, "getEqNext: d_eqNext is NULL");
  return d_expr->d_eqNext->get();
}

inline NotifyList* Expr::getNotify() const {
  if(isNull()) return NULL;
  else return d_expr->d_notifyList;
}

inline Type Expr::getType() const {
  if (isNull()) return s_null;
  if (d_expr->d_type.isNull()) getEM()->computeType(*this);
  return d_expr->d_type;
}

inline Type Expr::lookupType() const {
  if (isNull()) return s_null;
  return d_expr->d_type;
}

inline Cardinality Expr::typeCard() const {
  DebugAssert(!isNull(), "typeCard called on NULL Expr");
  Expr e(*this);
  Unsigned n;
  return getEM()->finiteTypeInfo(e, n, false, false);
}

inline Expr Expr::typeEnumerateFinite(Unsigned n) const {
  DebugAssert(!isNull(), "typeEnumerateFinite called on NULL Expr");
  Expr e(*this);
  Cardinality card = getEM()->finiteTypeInfo(e, n, true, false);
  if (card != CARD_FINITE) e = Expr();
  return e;
}

inline Unsigned Expr::typeSizeFinite() const {
  DebugAssert(!isNull(), "typeCard called on NULL Expr");
  Expr e(*this);
  Unsigned n;
  Cardinality card = getEM()->finiteTypeInfo(e, n, false, true);
  if (card != CARD_FINITE) n = 0;
  return n;
}

inline bool Expr::validSimpCache() const {
  return d_expr->d_simpCacheTag == getEM()->getSimpCacheTag();
}

inline const Theorem& Expr::getSimpCache() const {
  return d_expr->d_simpCache;
}

inline bool Expr::isValidType() const {
  return d_expr->d_dynamicFlags.get(VALID_TYPE);
}

inline bool Expr::validIsAtomicFlag() const {
  return d_expr->d_dynamicFlags.get(VALID_IS_ATOMIC);
}

inline bool Expr::validTerminalsConstFlag() const {
  return d_expr->d_dynamicFlags.get(VALID_TERMINALS_CONST);
}

inline bool Expr::getIsAtomicFlag() const {
  return d_expr->d_dynamicFlags.get(IS_ATOMIC);
}

inline bool Expr::getTerminalsConstFlag() const {
  return d_expr->d_dynamicFlags.get(TERMINALS_CONST);
}

inline bool Expr::isRewriteNormal() const {
  return d_expr->d_dynamicFlags.get(REWRITE_NORMAL);
}

inline bool Expr::isFinite() const {
  return d_expr->d_dynamicFlags.get(IS_FINITE);
}

inline bool Expr::isWellFounded() const {
  return d_expr->d_dynamicFlags.get(WELL_FOUNDED);
}

inline bool Expr::computeTransClosure() const {
  return d_expr->d_dynamicFlags.get(COMPUTE_TRANS_CLOSURE);
}

inline bool Expr::containsBoundVar() const {
  return d_expr->d_dynamicFlags.get(CONTAINS_BOUND_VAR);
}

inline bool Expr::usesCC() const {
  return d_expr->d_dynamicFlags.get(USES_CC);
}

inline bool Expr::notArrayNormalized() const {
  return d_expr->d_dynamicFlags.get(NOT_ARRAY_NORMALIZED);
}

inline bool Expr::isImpliedLiteral() const {
  return d_expr->d_dynamicFlags.get(IMPLIED_LITERAL);
}

inline bool Expr::isUserAssumption() const {
  return d_expr->d_dynamicFlags.get(IS_USER_ASSUMPTION);
}

inline bool Expr::inUserAssumption() const {
  return d_expr->d_dynamicFlags.get(IN_USER_ASSUMPTION);
}

inline bool Expr::isIntAssumption() const {
  return d_expr->d_dynamicFlags.get(IS_INT_ASSUMPTION);
}

inline bool Expr::isJustified() const {
  return d_expr->d_dynamicFlags.get(IS_JUSTIFIED);
}

inline bool Expr::isTranslated() const {
  return d_expr->d_dynamicFlags.get(IS_TRANSLATED);
}

inline bool Expr::isUserRegisteredAtom() const {
  return d_expr->d_dynamicFlags.get(IS_USER_REGISTERED_ATOM);
}

inline bool Expr::isRegisteredAtom() const {
  return d_expr->d_dynamicFlags.get(IS_REGISTERED_ATOM);
}

inline bool Expr::isSelected() const {
  return d_expr->d_dynamicFlags.get(IS_SELECTED);
}

inline bool Expr::isStoredPredicate() const {
  return d_expr->d_dynamicFlags.get(IS_STORED_PREDICATE);
}

inline bool Expr::getFlag() const {
  DebugAssert(!isNull(), "Expr::getFlag() on Null Expr");
  return (d_expr->d_flag == getEM()->getFlag());
}

inline void Expr::setFlag() const {
  DebugAssert(!isNull(), "Expr::setFlag() on Null Expr");
  d_expr->d_flag = getEM()->getFlag();
}

inline void Expr::clearFlags() const {
  DebugAssert(!isNull(), "Expr::clearFlags() on Null Expr");
  getEM()->clearFlags();
}

inline void Expr::setFind(const Theorem& e) const {
  DebugAssert(!isNull(), "Expr::setFind() on Null expr");
  DebugAssert(e.getLHS() == *this, "bad call to setFind");
  if (d_expr->d_find) d_expr->d_find->set(e);
  else {
    CDO<Theorem>* tmp = new(true) CDO<Theorem>(getEM()->getCurrentContext(), e);
    d_expr->d_find = tmp;
    IF_DEBUG(tmp->setName("CDO[Expr.find]");)
  }
}

inline void Expr::setEqNext(const Theorem& e) const {
  DebugAssert(!isNull(), "Expr::setEqNext() on Null expr");
  DebugAssert(e.getLHS() == *this, "bad call to setEqNext");
  if (d_expr->d_eqNext) d_expr->d_eqNext->set(e);
  else {
    CDO<Theorem>* tmp = new(true) CDO<Theorem>(getEM()->getCurrentContext(), e);
    d_expr->d_eqNext = tmp;
    IF_DEBUG(tmp->setName("CDO[Expr.eqNext]");)
  }
}

inline void Expr::setType(const Type& t) const {
  DebugAssert(!isNull(), "Expr::setType() on Null expr");
  d_expr->d_type = t;
}

inline void Expr::setSimpCache(const Theorem& e) const {
  DebugAssert(!isNull(), "Expr::setSimpCache() on Null expr");
  d_expr->d_simpCache = e;
  d_expr->d_simpCacheTag = getEM()->getSimpCacheTag();
}

inline void Expr::setValidType() const {
  DebugAssert(!isNull(), "Expr::setValidType() on Null expr");
  d_expr->d_dynamicFlags.set(VALID_TYPE, 0);
}

inline void Expr::setIsAtomicFlag(bool value) const {
  DebugAssert(!isNull(), "Expr::setIsAtomicFlag() on Null expr");
  d_expr->d_dynamicFlags.set(VALID_IS_ATOMIC, 0);
  if (value) d_expr->d_dynamicFlags.set(IS_ATOMIC, 0);
  else d_expr->d_dynamicFlags.clear(IS_ATOMIC, 0);
}

inline void Expr::setTerminalsConstFlag(bool value) const {
  DebugAssert(!isNull(), "Expr::setTerminalsConstFlag() on Null expr");
  d_expr->d_dynamicFlags.set(VALID_TERMINALS_CONST, 0);
  if (value) d_expr->d_dynamicFlags.set(TERMINALS_CONST, 0);
  else d_expr->d_dynamicFlags.clear(TERMINALS_CONST, 0);
}

inline void Expr::setRewriteNormal() const {
  DebugAssert(!isNull(), "Expr::setRewriteNormal() on Null expr");
  TRACE("setRewriteNormal", "setRewriteNormal(", *this, ")");
  d_expr->d_dynamicFlags.set(REWRITE_NORMAL, 0);
}

inline void Expr::setFinite() const {
  DebugAssert(!isNull(), "Expr::setFinite() on Null expr");
  d_expr->d_dynamicFlags.set(IS_FINITE, 0);
}

inline void Expr::setWellFounded() const {
  DebugAssert(!isNull(), "Expr::setWellFounded() on Null expr");
  d_expr->d_dynamicFlags.set(WELL_FOUNDED, 0);
}

inline void Expr::setComputeTransClosure() const {
  DebugAssert(!isNull(), "Expr::setComputeTransClosure() on Null expr");
  d_expr->d_dynamicFlags.set(COMPUTE_TRANS_CLOSURE, 0);
}

inline void Expr::setContainsBoundVar() const {
  DebugAssert(!isNull(), "Expr::setContainsBoundVar() on Null expr");
  d_expr->d_dynamicFlags.set(CONTAINS_BOUND_VAR, 0);
}

inline void Expr::setUsesCC() const {
  DebugAssert(!isNull(), "Expr::setUsesCC() on Null expr");
  d_expr->d_dynamicFlags.set(USES_CC, 0);
}

inline void Expr::setNotArrayNormalized() const {
  DebugAssert(!isNull(), "Expr::setContainsBoundVar() on Null expr");
  d_expr->d_dynamicFlags.set(NOT_ARRAY_NORMALIZED);
}

inline void Expr::setImpliedLiteral() const {
  DebugAssert(!isNull(), "Expr::setImpliedLiteral() on Null expr");
  d_expr->d_dynamicFlags.set(IMPLIED_LITERAL);
}

inline void Expr::setUserAssumption(int scope) const {
  DebugAssert(!isNull(), "Expr::setUserAssumption() on Null expr");
  d_expr->d_dynamicFlags.set(IS_USER_ASSUMPTION, scope);
}

inline void Expr::setInUserAssumption(int scope) const {
  DebugAssert(!isNull(), "Expr::setInUserAssumption() on Null expr");
  d_expr->d_dynamicFlags.set(IN_USER_ASSUMPTION, scope);
}

inline void Expr::setIntAssumption() const {
  DebugAssert(!isNull(), "Expr::setIntAssumption() on Null expr");
  d_expr->d_dynamicFlags.set(IS_INT_ASSUMPTION);
}

inline void Expr::setJustified() const {
  DebugAssert(!isNull(), "Expr::setJustified() on Null expr");
  d_expr->d_dynamicFlags.set(IS_JUSTIFIED);
}

inline void Expr::setTranslated(int scope) const {
  DebugAssert(!isNull(), "Expr::setTranslated() on Null expr");
  d_expr->d_dynamicFlags.set(IS_TRANSLATED, scope);
}

inline void Expr::setUserRegisteredAtom() const {
  DebugAssert(!isNull(), "Expr::setUserRegisteredAtom() on Null expr");
  d_expr->d_dynamicFlags.set(IS_USER_REGISTERED_ATOM);
}

inline void Expr::setRegisteredAtom() const {
  DebugAssert(!isNull(), "Expr::setUserRegisteredAtom() on Null expr");
  d_expr->d_dynamicFlags.set(IS_REGISTERED_ATOM);
}

inline void Expr::setSelected() const {
  DebugAssert(!isNull(), "Expr::setSelected() on Null expr");
  d_expr->d_dynamicFlags.set(IS_SELECTED);
}

inline void Expr::setStoredPredicate() const {
  DebugAssert(!isNull(), "Expr::setStoredPredicate() on Null expr");
  d_expr->d_dynamicFlags.set(IS_STORED_PREDICATE);
}

inline void Expr::clearRewriteNormal() const {
  DebugAssert(!isNull(), "Expr::clearRewriteNormal() on Null expr");
  d_expr->d_dynamicFlags.clear(REWRITE_NORMAL, 0);
}

inline bool Expr::hasSig() const {
  return (!isNull()
          && d_expr->getSig() != NULL
          && !(d_expr->getSig()->get().isNull()));
}

inline bool Expr::hasRep() const {
  return (!isNull()
          && d_expr->getRep() != NULL
          && !(d_expr->getRep()->get().isNull()));
}

inline const Theorem& Expr::getSig() const {
  static Theorem nullThm;
  DebugAssert(!isNull(), "Expr::getSig() on Null expr");
  if(d_expr->getSig() != NULL)
    return d_expr->getSig()->get();
  else
    return nullThm;
}

inline const Theorem& Expr::getRep() const {
  static Theorem nullThm;
  DebugAssert(!isNull(), "Expr::getRep() on Null expr");
  if(d_expr->getRep() != NULL)
    return d_expr->getRep()->get();
  else
    return nullThm;
}

inline void Expr::setSig(const Theorem& e) const {
  DebugAssert(!isNull(), "Expr::setSig() on Null expr");
  CDO<Theorem>* sig = d_expr->getSig();
  if(sig != NULL) sig->set(e);
  else {
    CDO<Theorem>* tmp = new(true) CDO<Theorem>(getEM()->getCurrentContext(), e);
    d_expr->setSig(tmp);
    IF_DEBUG(tmp->setName("CDO[Expr.sig] in "+toString());)
  }
}

inline void Expr::setRep(const Theorem& e) const {
  DebugAssert(!isNull(), "Expr::setRep() on Null expr");
  CDO<Theorem>* rep = d_expr->getRep();
  if(rep != NULL) rep->set(e);
  else {
    CDO<Theorem>* tmp = new(true) CDO<Theorem>(getEM()->getCurrentContext(), e);
    d_expr->setRep(tmp);
    IF_DEBUG(tmp->setName("CDO[Expr.rep] in "+toString());)
  }
}

inline bool operator==(const Expr& e1, const Expr& e2) {
  // Comparing pointers (equal expressions are always shared)
  return e1.d_expr == e2.d_expr;
}

inline bool operator!=(const Expr& e1, const Expr& e2)
  { return !(e1 == e2); }

// compare() is defined in expr.cpp

inline bool operator<(const Expr& e1, const Expr& e2)
  { return compare(e1,e2) < 0; }
inline bool operator<=(const Expr& e1, const Expr& e2)
  { return compare(e1,e2) <= 0; }
inline bool operator>(const Expr& e1, const Expr& e2)
  { return compare(e1,e2) > 0; }
inline bool operator>=(const Expr& e1, const Expr& e2)
  { return compare(e1,e2) >= 0; }

} // end of namespace CVC3

#endif
