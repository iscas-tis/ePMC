/*****************************************************************************/
/*!
 * \file expr_value.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Fri Feb  7 15:07:18 2003
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
 * Class ExprValue: the value holding class of Expr.  No one should
 * use it directly; use Expr API instead.  To enforce that, the
 * constructors are made protected, and only Expr, ExprManager, and
 * subclasses can use them.
 */
/*****************************************************************************/

// *** HACK ATTACK *** (trick from Aaron Stump's code)

// In order to inline the Expr constructors (for efficiency), this
// file (expr_value.h) must be included in expr.h.  However, we also
// need to include expr.h here, hence, circular dependency.  A way to
// break it is to include expr_value.h in the middle of expr.h after
// the definition of class Expr, but before the definition of its
// inlined methods.  So, expr.h included below will also suck in
// expr_value.h recursively, meaning that we then should skip the rest
// of the file (since it's already been included). 

// That's why expr.h is outside of #ifndef.  The same is true for
// type.h and theorem.h.

#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__expr_value_h_
#define _cvc3__expr_value_h_

#include "theorem.h"
#include "type.h"

// The prime number used in the hash function for a vector of elements
#define PRIME 131

namespace CVC3 {
  
/*****************************************************************************/
/*!
 *\class ExprValue
 *\brief The base class for holding the actual data in expressions
 * 
 *
 * Author: Sergey Berezin
 *
 * Created: long time ago
 *
 * \anchor ExprValue The base class just holds the operator.
 * All the additional data resides in subclasses.
 * 
 */
/*****************************************************************************/
class CVC_DLL ExprValue {
  friend class Expr;
  friend class Expr::iterator;
  friend class ExprManager;
  friend class ::CInterface;
  friend class ExprApply;
  friend class Theorem;
  friend class ExprClosure;

  //! Unique expression id
  ExprIndex d_index;

  //! Reference counter for garbage collection
  unsigned d_refcount;
  
  //! Cached hash value (initially 0)
  size_t d_hash; 

  //! The find attribute (may be NULL)
  CDO<Theorem>* d_find;

  //! Equality between this term and next term in ring of all terms in the equivalence class
  CDO<Theorem>* d_eqNext;

  //! The cached type of the expression (may be Null)
  Type d_type;

  //! The cached TCC of the expression (may be Null)
  //  Expr d_tcc;

  //! Subtyping predicate for the expression and all subexpressions
  //  Theorem d_subtypePred;

  //! Notify list may be NULL (== no such attribute)
  NotifyList* d_notifyList;

  //! For caching calls to Simplify
  Theorem d_simpCache;

  //! For checking whether simplify cache is valid
  unsigned d_simpCacheTag;

  //! context-dependent bit-vector for flags that are context-dependent
  CDFlags d_dynamicFlags;

  //! Size of dag rooted at this expression
  Unsigned d_size;

  //! Which child has the largest height
  //  int d_highestKid;

  //! Most distant expression we were simplified *from*
  //  Expr d_simpFrom;

  //! Generic flag for marking expressions (e.g. in DAG traversal)
  unsigned d_flag;

protected:
  /*! @brief The kind of the expression.  In particular, it determines which
   * subclass of ExprValue is used to store the expression. */
  int d_kind;

  //! Our expr. manager
  ExprManager* d_em;

  // End of data members

private:

  //! Set the ExprIndex
  void setIndex(ExprIndex idx) { d_index = idx; }

  //! Increment reference counter
  void incRefcount() { ++d_refcount; }

  //! Decrement reference counter
  void decRefcount() {
    // Cannot be DebugAssert, since we are called in a destructor
    // and should not throw an exception
    IF_DEBUG(FatalAssert(d_refcount > 0, "Mis-handled the ref. counting");)
    if((--d_refcount) == 0) d_em->gc(this);
  }

  //! Caching hash function
  /*! Do NOT implement it in subclasses! Implement computeHash() instead.
   */
  size_t hash() const {
    if (d_hash == 0)
      const_cast<ExprValue*>(this)->d_hash = computeHash();
    return d_hash;
  }

  //! Return DAG-size of Expr
  Unsigned getSize() const {
    if (d_flag == d_em->getFlag()) return 0;
    const_cast<ExprValue*>(this)->d_flag = d_em->getFlag();
    return computeSize();
  }

  //! Return child with greatest height
  //  int getHighestKid() const { return d_highestKid; }

  //! Get Expr simplified to obtain this expr
  //  const Expr& getSimpFrom() const { return d_simpFrom; }

  //! Set Expr simplified to obtain this expr
  //  void setSimpFrom(const Expr& simpFrom) { d_simpFrom = simpFrom; }

protected:

  // Static hash functions.  They don't depend on the context
  // (ExprManager and such), so it is still thread-safe to have them
  // static.
  static std::hash<char*> s_charHash;
  static std::hash<long int> s_intHash;

  static size_t pointerHash(void* p) { return s_intHash((long int)p); }
  // Hash function for subclasses with children
  static size_t hash(const int kind, const std::vector<Expr>& kids);
  // Hash function for kinds
  static size_t hash(const int n) { return s_intHash((long int)n); }

  // Size function for subclasses with children
  static Unsigned sizeWithChildren(const std::vector<Expr>& kids);

  //! Return the memory manager (for the benefit of subclasses)
  MemoryManager* getMM(size_t MMIndex) {
    DebugAssert(d_em!=NULL, "ExprValue::getMM()");
    return d_em->getMM(MMIndex);
  }

  //! Make a clean copy of itself using the given ExprManager
  ExprValue* rebuild(ExprManager* em) const
    { return copy(em, 0); }

  //! Make a clean copy of the expr using the given ExprManager
  Expr rebuild(Expr e, ExprManager* em) const
    { return em->rebuildRec(e); }

  // Protected API

  //! Non-caching hash function which actually computes the hash.
  /*! This is the method that all subclasses should implement */
  virtual size_t computeHash() const { return hash(d_kind); }

  //! Non-caching size function which actually computes the size.
  /*! This is the method that all subclasses should implement */
  virtual Unsigned computeSize() const { return 1; }

  //! Make a clean copy of itself using the given ExprManager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx) const;

public:
  //! Constructor
  ExprValue(ExprManager* em, int kind, ExprIndex idx = 0)
    : d_index(idx), d_refcount(0),
      d_hash(0), d_find(NULL), d_eqNext(NULL), d_notifyList(NULL),
      d_simpCacheTag(0),
      d_dynamicFlags(em->getCurrentContext()),
      d_size(0),
      //      d_height(0), d_highestKid(-1),
      d_flag(0), d_kind(kind), d_em(em)
  {
    DebugAssert(em != NULL, "NULL ExprManager is given to ExprValue()");
    DebugAssert(em->isKindRegistered(kind),
                ("ExprValue(kind = " + int2string(kind)
                 + ")): kind is not registered").c_str());
    DebugAssert(kind != APPLY, "Only ExprApply should have APPLY kind");
// #ifdef _CVC3_DEBUG_MODE //added by yeting, just hold a place to put my breakpoints in gdb
//     if(idx != 0){
//       TRACE("expr", "expr created ", idx, "");//the line added by yeting
//       //      char * a;
//       //      a="a";
//       //      a[999999]=255;
//     }
// #endif
  }
  //! Destructor
  virtual ~ExprValue();

  //! Get the kind of the expression
  int getKind() const { return d_kind; }

  //! Overload operator new
  void* operator new(size_t size, MemoryManager* mm)
    { return mm->newData(size); }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }

  //! Overload operator delete
  void operator delete(void*) { }

  //! Get unique memory manager ID
  virtual size_t getMMIndex() const { return EXPR_VALUE; }

  //! Equality between any two ExprValue objects (including subclasses)
  virtual bool operator==(const ExprValue& ev2) const;

  // Testers

  //! Test whether the expression is a generic subclass
  /*!
   * \return 0 for the core classes, and getMMIndex() value for
   * generic subclasses (those defined in DPs)
   */
  virtual const ExprValue* getExprValue() const
    { throw Exception("Illegal call to getExprValue()"); }
  //! String expression tester
  virtual bool isString() const { return false; }
  //! Rational number expression tester
  virtual bool isRational() const { return false; }
  //! Uninterpreted constants
  virtual bool isVar() const { return false; }
  //! Application of another expression
  virtual bool isApply() const { return false; }
  //! Special named symbol
  virtual bool isSymbol() const { return false; }
  //! A LAMBDA-expression or a quantifier
  virtual bool isClosure() const { return false; }
  //! Special Expr holding a theorem
  virtual bool isTheorem() const { return false; }

  //! Get kids: by default, returns a ref to an empty vector
  virtual const std::vector<Expr>& getKids() const
    { return d_em->getEmptyVector(); }

  // Methods to access leaf data in subclasses

  //! Default arity = 0
  virtual unsigned arity() const { return 0; }

  //! Special attributes for uninterpreted functions
  virtual CDO<Theorem>* getSig() const {
    DebugAssert(false, "getSig() is called on ExprValue");
    return NULL;
  }

  virtual CDO<Theorem>* getRep() const {
    DebugAssert(false, "getRep() is called on ExprValue");
    return NULL;
  }

  virtual void setSig(CDO<Theorem>* sig) {
    DebugAssert(false, "setSig() is called on ExprValue");
  }

  virtual void setRep(CDO<Theorem>* rep) {
    DebugAssert(false, "setRep() is called on ExprValue");
  }

  virtual const std::string& getUid() const { 
    static std::string null;
    DebugAssert(false, "ExprValue::getUid() called in base class");
    return null;
  }

  virtual const std::string& getString() const {
    DebugAssert(false, "getString() is called on ExprValue");
    static std::string s("");
    return s;
  }

  virtual const Rational& getRational() const {
    DebugAssert(false, "getRational() is called on ExprValue");
    static Rational r(0);
    return r;
  }

  //! Returns the string name of UCONST and BOUND_VAR expr's.
  virtual const std::string& getName() const {
    static std::string ret = "";
    DebugAssert(false, "getName() is called on ExprValue");
    return ret;
  }

  //! Returns the original Boolean variable (for BoolVarExprValue)
  virtual const Expr& getVar() const {
    DebugAssert(false, "getVar() is called on ExprValue");
    static Expr null;
    return null;
  }

  //! Get the Op from an Apply Expr
  virtual Op getOp() const {
    DebugAssert(false, "getOp() is called on ExprValue");
    return Op(NULL_KIND);
  }

  virtual const std::vector<Expr>& getVars() const  {
    DebugAssert(false, "getVars() is called on ExprValue");
    static std::vector<Expr> null;
    return null;
  }

  virtual const Expr& getBody() const {
    DebugAssert(false, "getBody() is called on ExprValue");
    static Expr null;
    return null;
  }

  virtual void setTriggers(const std::vector<std::vector<Expr> >& triggers) {
    DebugAssert(false, "setTriggers() is called on ExprValue");
  }

  virtual const std::vector<std::vector<Expr> >& getTriggers() const { //by yeting
    DebugAssert(false, "getTrigs() is called on ExprValue");
    static std::vector<std::vector<Expr> > null;
    return null;
  }


  virtual const Expr& getExistential() const {
    DebugAssert(false, "getExistential() is called on ExprValue");
    static Expr null;
    return null;
  }
  virtual int getBoundIndex() const {
    DebugAssert(false, "getIndex() is called on ExprValue");
    return 0;
  }

  virtual const std::vector<std::string>& getFields() const {
    DebugAssert(false, "getFields() is called on ExprValue");
    static std::vector<std::string> null;
    return null;
  }
  virtual const std::string& getField() const {
    DebugAssert(false, "getField() is called on ExprValue");
    static std::string null;
    return null;
  }
  virtual int getTupleIndex() const {
    DebugAssert(false, "getTupleIndex() is called on ExprValue");
    return 0;
  }
  virtual const Theorem& getTheorem() const {
    static Theorem null;
    DebugAssert(false, "getTheorem() is called on ExprValue");
    return null;
  }

}; // end of class ExprValue

// Class ExprNode; it's an expression with children
class CVC_DLL ExprNode: public ExprValue {
  friend class Expr;
  friend class ExprManager;

protected:
  //! Vector of children
  std::vector<Expr> d_children;

  // Special attributes for helping with congruence closure
  CDO<Theorem>* d_sig;
  CDO<Theorem>* d_rep;

private:

  //! Tell ExprManager who we are
  size_t getMMIndex() const { return EXPR_NODE; }

protected:
  //! Return number of children
  unsigned arity() const { return d_children.size(); }

  //! Return reference to children
  std::vector<Expr>& getKids1() { return d_children; }

  //! Return reference to children
  const std::vector<Expr>& getKids() const { return d_children; }

  //! Use our static hash() for the member method
  size_t computeHash() const {
    return ExprValue::hash(d_kind, d_children);
  }

  //! Use our static sizeWithChildren() for the member method
  Unsigned computeSize() const {
    return ExprValue::sizeWithChildren(d_children);
  }

  //! Make a clean copy of itself using the given memory manager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;

public:
  //! Constructor
  ExprNode(ExprManager* em, int kind, ExprIndex idx = 0)
    : ExprValue(em, kind, idx), d_sig(NULL), d_rep(NULL) { }
  //! Constructor
  ExprNode(ExprManager* em, int kind, const std::vector<Expr>& kids,
           ExprIndex idx = 0)
    : ExprValue(em, kind, idx), d_children(kids), d_sig(NULL), d_rep(NULL) { }
  //! Destructor
  virtual ~ExprNode();
    
  //! Overload operator new
  void* operator new(size_t size, MemoryManager* mm)
    { return mm->newData(size); }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }

  //! Overload operator delete
  void operator delete(void*) { }

  //! Compare with another ExprValue
  virtual bool operator==(const ExprValue& ev2) const;

  virtual CDO<Theorem>* getSig() const { return d_sig; }
  virtual CDO<Theorem>* getRep() const { return d_rep; }

  virtual void setRep(CDO<Theorem>* rep) { d_rep = rep; }
  virtual void setSig(CDO<Theorem>* sig) { d_sig = sig; }

}; // end of class ExprNode

// Class ExprNodeTmp; special version of ExprNode for Expr constructor
class ExprNodeTmp: public ExprValue {
  friend class Expr;
  friend class ExprManager;

protected:
  //! Vector of children
  const std::vector<Expr>& d_children;

private:

  //! Tell ExprManager who we are
  size_t getMMIndex() const { return EXPR_NODE; }

protected:
  //! Return number of children
  unsigned arity() const { return d_children.size(); }

  //! Return reference to children
  const std::vector<Expr>& getKids() const { return d_children; }

  //! Use our static hash() for the member method
  size_t computeHash() const {
    return ExprValue::hash(d_kind, d_children);
  }

  //! Use our static sizeWithChildren() for the member method
  Unsigned computeSize() const {
    return ExprValue::sizeWithChildren(d_children);
  }

  //! Make a clean copy of itself using the given memory manager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;

public:
  //! Constructor
  ExprNodeTmp(ExprManager* em, int kind, const std::vector<Expr>& kids)
    : ExprValue(em, kind, 0), d_children(kids) { }

  //! Destructor
  virtual ~ExprNodeTmp() {}
    
  //! Compare with another ExprValue
  virtual bool operator==(const ExprValue& ev2) const;

}; // end of class ExprNodeTmp

// Special version for Expr Constructor
class ExprApplyTmp: public ExprNodeTmp {
  friend class Expr;
  friend class ExprManager;
private:
  Expr d_opExpr;
protected:
  size_t getMMIndex() const { return EXPR_APPLY; }
  size_t computeHash() const {
    return PRIME*ExprNodeTmp::computeHash() + d_opExpr.hash();
  }
  Op getOp() const { return Op(d_opExpr); }
  bool isApply() const { return true; }
  // Make a clean copy of itself using the given memory manager
  ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;
public:
  // Constructor
  ExprApplyTmp(ExprManager* em, const Op& op,
               const std::vector<Expr>& kids)
    : ExprNodeTmp(em, NULL_KIND, kids), d_opExpr(op.getExpr())
  { DebugAssert(!op.getExpr().isNull(), "Expected non-null Op");
    d_kind = APPLY; }
  virtual ~ExprApplyTmp() { }

  bool operator==(const ExprValue& ev2) const;
}; // end of class ExprApply

class CVC_DLL ExprApply: public ExprNode {
  friend class Expr;
  friend class ExprManager;
private:
  Expr d_opExpr;
protected:
  size_t getMMIndex() const { return EXPR_APPLY; }
  size_t computeHash() const {
    return PRIME*ExprNode::computeHash() + d_opExpr.hash();
  }
  Op getOp() const { return Op(d_opExpr); }
  bool isApply() const { return true; }
  // Make a clean copy of itself using the given memory manager
  ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;
public:
  // Constructor
  ExprApply(ExprManager* em, const Op& op, ExprIndex idx = 0)
    : ExprNode(em, NULL_KIND, idx), d_opExpr(op.getExpr())
  { DebugAssert(!op.getExpr().isNull(), "Expected non-null Op");
    d_kind = APPLY; }
  ExprApply(ExprManager* em, const Op& op,
            const std::vector<Expr>& kids, ExprIndex idx = 0)
    : ExprNode(em, NULL_KIND, kids, idx), d_opExpr(op.getExpr())
  { DebugAssert(!op.getExpr().isNull(), "Expected non-null Op");
    d_kind = APPLY; }
  virtual ~ExprApply() { }

  bool operator==(const ExprValue& ev2) const;
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
}; // end of class ExprApply

/*****************************************************************************/
/*!
 *\class NamedExprValue
 *\brief NamedExprValue
 *
 * Author: Clark Barrett
 *
 * Created: Thu Dec  2 23:18:17 2004
 *
 * Subclass of ExprValue for kinds that have a name associated with them.
 */
/*****************************************************************************/

// class NamedExprValue : public ExprNode {
//   friend class Expr;
//   friend class ExprManager;

// private:
//   std::string d_name;

// protected:

//   ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const {
//     return new(em->getMM(getMMIndex()))
//       NamedExprValue(d_em, d_kind, d_name, d_children, idx);
//   }

//   ExprValue* copy(ExprManager* em, const std::vector<Expr>& kids,
//       	    ExprIndex idx = 0) const {
//     return new(em->getMM(getMMIndex()))
//       NamedExprValue(d_em, d_kind, d_name, kids, idx);
//   }

//   size_t computeHash() const {
//     return s_charHash(d_name.c_str())*PRIME + ExprNode::computeHash();
//   }

//   size_t getMMIndex() const { return EXPR_NAMED; }

// public:
//   // Constructor
//   NamedExprValue(ExprManager *em, int kind, const std::string& name,
//                  const std::vector<Expr>& kids, ExprIndex idx = 0)
//     : ExprNode(em, kind, kids, idx), d_name(name) { }
//   // virtual ~NamedExprValue();
//   bool operator==(const ExprValue& ev2) const {
//     if(getMMIndex() != ev2.getMMIndex()) return false;
//     return (getName() == ev2.getName())
//       && ExprNode::operator==(ev2);
//   }

//   const std::string& getName() const { return d_name; }

//   // Memory management
//   void* operator new(size_t size, MemoryManager* mm) {
//     return mm->newData(size);
//   }
//   void operator delete(void*) { }
// }; // end of class NamedExprValue

// Leaf expressions
class ExprString: public ExprValue {
  friend class Expr;
  friend class ExprManager;
private:
  std::string d_str;

  // Hash function for this subclass
  static size_t hash(const std::string& str) {
    return s_charHash(str.c_str());
  }

  // Tell ExprManager who we are
  virtual size_t getMMIndex() const { return EXPR_STRING; }

protected:
  // Use our static hash() for the member method
  virtual size_t computeHash() const { return hash(d_str); }

  virtual bool isString() const { return true; }
  virtual const std::string& getString() const { return d_str; }

  //! Make a clean copy of itself using the given memory manager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;
public:
  // Constructor
  ExprString(ExprManager* em, const std::string& s, ExprIndex idx = 0)
    : ExprValue(em, STRING_EXPR, idx), d_str(s) { }
  // Destructor
  virtual ~ExprString() { }

  virtual bool operator==(const ExprValue& ev2) const;
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
}; // end of class ExprString

class ExprSkolem: public ExprValue {
  friend class Expr;
  friend class ExprManager;
private:
  Expr d_quant; //!< The quantified expression to skolemize
  int d_idx; //!< Variable index in the quantified expression
  const Expr& getExistential() const {return d_quant;}
  int getBoundIndex() const {return d_idx;}

  // Tell ExprManager who we are
  size_t getMMIndex() const { return EXPR_SKOLEM;}

protected:
  size_t computeHash() const {
    size_t res = getExistential().getBody().hash();
    res = PRIME*res + getBoundIndex();
    return res;
  }

  bool operator==(const ExprValue& ev2) const;

  //! Make a clean copy of itself using the given memory manager
  ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;
  bool isVar() const { return true; }
   
public:
  // Constructor
  ExprSkolem(ExprManager* em, int index, const Expr& exist, ExprIndex idx = 0)
    : ExprValue(em, SKOLEM_VAR, idx), d_quant(exist), d_idx(index) { }
  // Destructor
  virtual ~ExprSkolem() { }
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
}; // end of class ExprSkolem

class ExprRational: public ExprValue {
  friend class Expr;
  friend class ExprManager;
private:
  Rational d_r;

  virtual const Rational& getRational() const { return d_r; }

  // Hash function for this subclass
  static size_t hash(const Rational& r) {
    return s_charHash(r.toString().c_str());
  }

  // Tell ExprManager who we are
  virtual size_t getMMIndex() const { return EXPR_RATIONAL; }

protected:

  virtual size_t computeHash() const { return hash(d_r); }
  virtual bool operator==(const ExprValue& ev2) const;
  //! Make a clean copy of itself using the given memory manager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;
  virtual bool isRational() const { return true; }

public:
  // Constructor
  ExprRational(ExprManager* em, const Rational& r, ExprIndex idx = 0)
    : ExprValue(em, RATIONAL_EXPR, idx), d_r(r) { }
  // Destructor
  virtual ~ExprRational() { }
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
}; // end of class ExprRational

// Uninterpreted constants (variables)
class ExprVar: public ExprValue {
  friend class Expr;
  friend class ExprManager;
private:
  std::string d_name;

  virtual const std::string& getName() const { return d_name; }

  // Tell ExprManager who we are
  virtual size_t getMMIndex() const { return EXPR_UCONST; }
protected:

  virtual size_t computeHash() const {
    return s_charHash(d_name.c_str());
  }
  virtual bool isVar() const { return true; }

  //! Make a clean copy of itself using the given memory manager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;

public:
  // Constructor
  ExprVar(ExprManager *em, const std::string& name, ExprIndex idx = 0)
    : ExprValue(em, UCONST, idx), d_name(name) { }
  // Destructor
  virtual ~ExprVar() { }

  virtual bool operator==(const ExprValue& ev2) const;
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
}; // end of class ExprVar

// Interpreted symbols: similar to UCONST, but returns false for isVar().
class ExprSymbol: public ExprValue {
  friend class Expr;
  friend class ExprManager;
private:
  std::string d_name;

  virtual const std::string& getName() const { return d_name; }

  // Tell ExprManager who we are
  virtual size_t getMMIndex() const { return EXPR_SYMBOL; }
protected:

  virtual size_t computeHash() const {
    return s_charHash(d_name.c_str())*PRIME + s_intHash(d_kind);
  }
  //! Make a clean copy of itself using the given memory manager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;
  bool isSymbol() const { return true; }

public:
  // Constructor
  ExprSymbol(ExprManager *em, int kind, const std::string& name,
             ExprIndex idx = 0)
    : ExprValue(em, kind, idx), d_name(name) { }
  // Destructor
  virtual ~ExprSymbol() { }

  virtual bool operator==(const ExprValue& ev2) const;
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
}; // end of class ExprSymbol

class ExprBoundVar: public ExprValue {
  friend class Expr;
  friend class ExprManager;
private:
  std::string d_name;
  std::string d_uid;

  virtual const std::string& getName() const { return d_name; }
  virtual const std::string& getUid() const { return d_uid; }

  // Tell ExprManager who we are
  virtual size_t getMMIndex() const { return EXPR_BOUND_VAR; }
protected:

  virtual size_t computeHash() const {
    return s_charHash(d_name.c_str())*PRIME + s_charHash(d_uid.c_str());
  }
  virtual bool isVar() const { return true; }
  //! Make a clean copy of itself using the given memory manager
  virtual ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;

public:
  // Constructor
  ExprBoundVar(ExprManager *em, const std::string& name,
               const std::string& uid, ExprIndex idx = 0)
    : ExprValue(em, BOUND_VAR, idx), d_name(name), d_uid(uid) { }
  // Destructor
  virtual ~ExprBoundVar() { }

  virtual bool operator==(const ExprValue& ev2) const;
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
}; // end of class ExprBoundVar

/*! @brief A "closure" expression which binds variables used in the
  "body".  Used by LAMBDA and quantifiers. */
class ExprClosure: public ExprValue {
  friend class Expr;
  friend class ExprManager;
private:
  //! Bound variables
  std::vector<Expr> d_vars;
  //! The body of the quantifier/lambda
  Expr d_body;
  //! Manual triggers. // added by yeting
  // Note that due to expr caching, only the most recent triggers specified for a given formula will be used.
  std::vector<std::vector<Expr> > d_manual_triggers;
  //! Tell ExprManager who we are
  virtual size_t getMMIndex() const { return EXPR_CLOSURE; }

  virtual const std::vector<Expr>& getVars() const { return d_vars; }
  virtual const Expr& getBody() const { return d_body; }
  virtual void setTriggers(const std::vector<std::vector<Expr> >& triggers) { d_manual_triggers = triggers; }
  virtual const std::vector<std::vector<Expr> >&  getTriggers() const { return d_manual_triggers; }

protected:

  size_t computeHash() const;
  Unsigned computeSize() const { return d_body.d_expr->getSize() + 1; }
  //! Make a clean copy of itself using the given memory manager
  ExprValue* copy(ExprManager* em, ExprIndex idx = 0) const;

public:
  // Constructor
  ExprClosure(ExprManager *em, int kind, const Expr& var,
              const Expr& body, ExprIndex idx = 0)
    : ExprValue(em, kind, idx), d_body(body) { d_vars.push_back(var); }

  ExprClosure(ExprManager *em, int kind, const std::vector<Expr>& vars,
              const Expr& body, ExprIndex idx = 0)
    : ExprValue(em, kind, idx), d_vars(vars), d_body(body) { }

  ExprClosure(ExprManager *em, int kind, const std::vector<Expr>& vars, 
              const Expr& body, const std::vector<std::vector<Expr> >&  trigs, ExprIndex idx = 0)
    : ExprValue(em, kind, idx), d_vars(vars), d_body(body),  d_manual_triggers(trigs) { }

  // Destructor
  virtual ~ExprClosure() { }

  bool operator==(const ExprValue& ev2) const;
  // Memory management
  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }
  virtual bool isClosure() const { return true; }
}; // end of class ExprClosure


} // end of namespace CVC3

#endif
