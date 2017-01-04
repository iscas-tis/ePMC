/*****************************************************************************/
/*!
 * \file expr_manager.h
 * \brief Expression manager API
 * 
 * Author: Sergey Berezin
 * 
 * Created: Wed Dec  4 14:20:56 2002
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

// Must be before #ifndef, since expr.h also includes this file (see
// comments in expr_value.h)
#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__include__expr_manager_h_
#define _cvc3__include__expr_manager_h_

#include "os.h"
#include "expr_map.h"
#include <deque>

namespace CVC3 {
  // Command line flags
  class CLFlags;
  class PrettyPrinter;
  class MemoryManager;
  class ExprManagerNotifyObj;
  class TheoremManager;

///////////////////////////////////////////////////////////////////////////////
//! Expression Manager
/*!
  Class: ExprManager
  
  Author: Sergey Berezin
 
  Created: Wed Dec  4 14:26:35 2002

  Description: Global state of the Expr package for a particular
    instance of CVC3.  Each instance of the CVC3 library has
    its own expression manager, for thread-safety.
*/
///////////////////////////////////////////////////////////////////////////////

  class CVC_DLL ExprManager {
    friend class Expr;
    friend class ExprValue;
    friend class Op; // It wants to call rebuildExpr
    friend class HashEV; // Our own private class
    friend class Type;

    ContextManager* d_cm; //!< For backtracking attributes
    TheoremManager* d_tm; //!< Needed for Refl Theorems
    ExprManagerNotifyObj* d_notifyObj; //!< Notification on pop()
    ExprIndex d_index; //!< Index counter for Expr compare()
    unsigned d_flagCounter; //!< Counter for a generic Expr flag

    //! The database of registered kinds
    std::hash_map<int, std::string> d_kindMap;
    //! The set of kinds representing a type
    std::hash_set<int> d_typeKinds;
    //! Private class for hashing strings
    class HashString {
      std::hash<char*> h;
    public:
      size_t operator()(const std::string& s) const {
	return h(const_cast<char*>(s.c_str()));
      }
    };
    //! The reverse map of names to kinds
    std::hash_map<std::string, int, HashString> d_kindMapByName;
    /*! @brief The registered pretty-printer, a connector to
      theory-specific pretty-printers */
    PrettyPrinter *d_prettyPrinter;

    size_t hash(const ExprValue* ev) const;

    // Printing and other options 

    /*! @brief Print upto the given depth, replace the rest with
     "...".  -1==unlimited depth. */
    const int* d_printDepth;
    //! Whether to print with indentation
    const bool* d_withIndentation;
    //! Permanent indentation
    int d_indent;
    //! Transient indentation
    /*! Normally is the same as d_indent, but may temporarily be
      different for printing one single Expr */
    int d_indentTransient;
    //! Suggested line width for printing with indentation
    const int* d_lineWidth;
    //! Input language (printing)
    const std::string* d_inputLang;
    //! Output language (printing)
    const std::string* d_outputLang;
    //! Whether to print Expr's as DAGs
    const bool* d_dagPrinting;
    //! Which memory manager to use (copy the flag value and keep it the same)
    const std::string d_mmFlag;

    //! Private class for d_exprSet
    class HashEV {
      ExprManager* d_em;
    public:
      HashEV(ExprManager* em): d_em(em) { }
      size_t operator()(ExprValue* ev) const { return d_em->hash(ev); }
    };
    //! Private class for d_exprSet
    class EqEV {
    public:
      bool operator()(const ExprValue* ev1, const ExprValue* ev2) const;
    };

    //! Hash set type for uniquifying expressions
    typedef std::hash_set<ExprValue*, HashEV, EqEV> ExprValueSet;
    //! Hash set for uniquifying expressions
    ExprValueSet d_exprSet;
    //! Array of memory managers for subclasses of ExprValue
    std::vector<MemoryManager*> d_mm;

    //! A hash function for hashing pointers
    std::hash<void*> d_pointerHash;
    
    //! Expr constants cached for fast access
    Expr d_bool;
    Expr d_false;
    Expr d_true;
    //! Empty vector of Expr to return by reference as empty vector of children
    std::vector<Expr> d_emptyVec;
    //! Null Expr to return by reference, for efficiency
    Expr d_nullExpr;

    void installExprValue(ExprValue* ev);

    //! Current value of the simplifier cache tag
    /*! The cached values of calls to Simplify are valid as long as
      their cache tag matches this tag.  Caches can then be
      invalidated by incrementing this tag. */
    unsigned d_simpCacheTagCurrent;

    //! Disable garbage collection
    /*! This flag disables the garbage collection.  Normally, it's set
      in the destructor, so that we can delete all remaining
      expressions without GC getting in the way. */
    bool d_disableGC;
    //! Postpone deleting garbage-collected expressions.
    /*! Useful during manipulation of context, especially at the time
     * of backtracking, since we may have objects with circular
     * dependencies (like find pointers).
     *
     * The postponed expressions will be deleted the next time the
     * garbage collector is called after this flag is cleared.
     */
    bool d_postponeGC;
    //! Vector of postponed garbage-collected expressions
    std::vector<ExprValue*> d_postponed;

    //! Flag for whether GC is already running
    bool d_inGC;
    //! Queue of pending exprs to GC
    std::deque<ExprValue*> d_pending;

    //! Rebuild cache
    ExprHashMap<Expr> d_rebuildCache;
    IF_DEBUG(bool d_inRebuild;)

  public:
    //! Abstract class for computing expr type
    class TypeComputer {
    public:
      TypeComputer() {}
      virtual ~TypeComputer() {}
      //! Compute the type of e
      virtual void computeType(const Expr& e) = 0;
      //! Check that e is a valid Type expr
      virtual void checkType(const Expr& e) = 0;
      //! Get information related to finiteness of a type
      virtual Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                                         bool enumerate, bool computeSize) = 0;
    };
  private:
    //! Instance of TypeComputer: must be registered
    TypeComputer* d_typeComputer;

    /////////////////////////////////////////////////////////////////////////
    /*! \defgroup EM_Priv Private methods
     * \ingroup ExprPkg
     * @{
     */
    /////////////////////////////////////////////////////////////////////////

    //! Cached recursive descent.  Must be called only during rebuild()
    Expr rebuildRec(const Expr& e);

    //! Return either an existing or a new ExprValue matching ev
    ExprValue* newExprValue(ExprValue* ev);

    //! Return the current Expr flag counter
    unsigned getFlag() { return d_flagCounter; }
    //! Increment and return the Expr flag counter (this clears all the flags)
    unsigned nextFlag()
      { FatalAssert(++d_flagCounter, "flag overflow"); return d_flagCounter; }

    //! Compute the type of the Expr
    void computeType(const Expr& e);
    //! Check well-formedness of a type Expr
    void checkType(const Expr& e);
    //! Get information related to finiteness of a type
    // 1. Returns Cardinality of the type (finite, infinite, or unknown)
    // 2. If cardinality = finite and enumerate is true,
    //    sets e to the nth element of the type if it can
    //    sets e to NULL if n is out of bounds or if unable to compute nth element
    // 3. If cardinality = finite and computeSize is true,
    //    sets n to the size of the type if it can
    //    sets n to 0 otherwise
    Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                               bool enumerate, bool computeSize);

  public:
    //! Constructor
    ExprManager(ContextManager* cm, const CLFlags& flags);
    //! Destructor
    ~ExprManager();
    //! Free up all memory and delete all the expressions.
    /*!
     * No more expressions can be created after this point, only
     * destructors ~Expr() can be called.
     *
     * This method is needed to dis-entangle the mutual dependency of
     * ExprManager and ContextManager, when destructors of ExprValue
     * (sub)classes need to delete backtracking objects, and deleting
     * the ContextManager requires destruction of some remaining Exprs.
     */
    void clear();
    //! Check if the ExprManager is still active (clear() was not called)
    bool isActive();

    //! Garbage collect the ExprValue pointer 
    /*! \ingroup EM_Priv */
    void gc(ExprValue* ev);
    //! Postpone deletion of garbage-collected expressions.
    /*! \sa resumeGC() */
    void postponeGC() { d_postponeGC = true; }
    //! Resume deletion of garbage-collected expressions.
    /*! \sa postponeGC() */
    void resumeGC();

    /*! @brief Rebuild the Expr with this ExprManager if it belongs to
      another ExprManager */
    Expr rebuild(const Expr& e);

    //! Return the next Expr index
    /*! It should be used only by ExprValue() constructor */
    ExprIndex nextIndex() { return d_index++; }
    ExprIndex lastIndex() { return d_index - 1; }

    //! Clears the generic Expr flag in all Exprs
    void clearFlags() { nextFlag(); }

    // Core leaf exprs
    //! BOOLEAN Expr
    const Expr& boolExpr() { return d_bool; }
    //! FALSE Expr
    const Expr& falseExpr() { return d_false; }
    //! TRUE Expr
    const Expr& trueExpr() { return d_true; }
    //! References to empty objects (used in ExprValue)
    const std::vector<Expr>& getEmptyVector() { return d_emptyVec; }
    //! References to empty objects (used in ExprValue)
    const Expr& getNullExpr() { return d_nullExpr; }

    // Expr constructors

    //! Return either an existing or a new Expr matching ev
    Expr newExpr(ExprValue* ev) { return Expr(newExprValue(ev)); }

    Expr newLeafExpr(const Op& op);
    Expr newStringExpr(const std::string &s);
    Expr newRatExpr(const Rational& r);
    Expr newSkolemExpr(const Expr& e, int i);
    Expr newVarExpr(const std::string &s);
    Expr newSymbolExpr(const std::string &s, int kind);
    Expr newBoundVarExpr(const std::string &name, const std::string& uid);
    Expr newBoundVarExpr(const std::string &name, const std::string& uid,
                         const Type& type);
    Expr newBoundVarExpr(const Type& type);
    Expr newClosureExpr(int kind, const Expr& var, const Expr& body);
    Expr newClosureExpr(int kind, const std::vector<Expr>& vars,
                        const Expr& body);
    Expr newClosureExpr(int kind, const std::vector<Expr>& vars,
                        const Expr& body, const Expr& trig);
    Expr newClosureExpr(int kind, const std::vector<Expr>& vars,
                        const Expr& body, const std::vector<Expr>& trigs);
    Expr newClosureExpr(int kind, const std::vector<Expr>& vars,
                        const Expr& body, const std::vector<std::vector<Expr> >& trigs);

    // Vector of children constructors (vector may be empty)
    Expr andExpr(const std::vector <Expr>& children)
     { return Expr(AND, children, this); }
    Expr orExpr(const std::vector <Expr>& children)
     { return Expr(OR, children, this); }

    // Public methods

    //! Hash function for a single Expr
    size_t hash(const Expr& e) const;
    //! Fetch our ContextManager
    ContextManager* getCM() const { return d_cm; }
    //! Get the current context from our ContextManager
    Context* getCurrentContext() const { return d_cm->getCurrentContext(); }
    //! Get current scope level
    int scopelevel() { return d_cm->scopeLevel(); }

    //! Set the TheoremManager
    void setTM(TheoremManager* tm) { d_tm = tm; }
    //! Fetch the TheoremManager
    TheoremManager* getTM() const { return d_tm; }

    //! Return a MemoryManager for the given ExprValue type
    MemoryManager* getMM(size_t MMIndex) {
      DebugAssert(MMIndex < d_mm.size(), "ExprManager::getMM()");
      return d_mm[MMIndex];
    }
    //! Get the simplifier's cache tag
    unsigned getSimpCacheTag() const { return d_simpCacheTagCurrent; }
    //! Invalidate the simplifier's cache tag
    void invalidateSimpCache() { d_simpCacheTagCurrent++; }

    //! Register type computer
    void registerTypeComputer(TypeComputer* typeComputer)
      { d_typeComputer = typeComputer; }

    //! Get printing depth
    int printDepth() const { return *d_printDepth; }
    //! Whether to print with indentation
    bool withIndentation() const { return *d_withIndentation; }
    //! Suggested line width for printing with indentation
    int lineWidth() const { return *d_lineWidth; }
    //! Get initial indentation
    int indent() const { return d_indentTransient; }
    //! Set initial indentation.  Returns the previous permanent value.
    int indent(int n, bool permanent = false);
    //! Increment the current transient indentation by n
    /*! If the second argument is true, sets the result as permanent.
      \return previous permanent value. */
    int incIndent(int n, bool permanent = false);
    //! Set transient indentation to permanent
    void restoreIndent() { d_indentTransient = d_indent; }
    //! Get the input language for printing
    InputLanguage getInputLang() const;
    //! Get the output language for printing
    InputLanguage getOutputLang() const;
    //! Whether to print Expr's as DAGs
    bool dagPrinting() const { return *d_dagPrinting; }
    /*! @brief Return the pretty-printer if there is one; otherwise
       return NULL. */
    PrettyPrinter* getPrinter() const { return d_prettyPrinter; }
 
  /////////////////////////////////////////////////////////////////////////////
  // Kind registration                                                       //
  /////////////////////////////////////////////////////////////////////////////

    //! Register a new kind.
    /*! The kind may already be registered under the same name, but if
     *  the name is different, it's an error.
     * 
     * If the new kind is supposed to represent a type, set isType to true.
     */
    void newKind(int kind, const std::string &name, bool isType = false);
    //! Register the pretty-printer (can only do it if none registered)
    /*! The pointer is NOT owned by ExprManager. Delete it yourself.
     */
    void registerPrettyPrinter(PrettyPrinter& printer);
    //! Tell ExprManager that the printer is no longer valid
    void unregisterPrettyPrinter();
    /*! @brief Returns true if kind is built into CVC or has been registered
      via newKind. */
    bool isKindRegistered(int kind) { return d_kindMap.count(kind) > 0; }
    //! Check if a kind represents a type
    bool isTypeKind(int kind) { return d_typeKinds.count(kind) > 0; }

    /*! @brief Return the name associated with a kind.  The kind must
      already be registered. */
    const std::string& getKindName(int kind);
    //! Return a kind associated with a name.  Returns NULL_KIND if not found.
    int getKind(const std::string& name);
    //! Register a new subclass of ExprValue
    /*!
     * Takes the size (in bytes) of the new subclass and returns the
     * unique index of that subclass.  Subsequent calls to the
     * subclass's getMMIndex() must return that index.
     */
    size_t registerSubclass(size_t sizeOfSubclass);

    //! Calculate memory usage
    unsigned long getMemory(int verbosity);

  }; // end of class ExprManager


/*****************************************************************************/
/*!
 *\class ExprManagerNotifyObj
 *\brief Notifies ExprManager before and after each pop()
 *
 * Author: Sergey Berezin
 *
 * Created: Tue Mar  1 12:29:14 2005
 *
 * Disables the deletion of Exprs during context restoration
 * (backtracking).  This solves the problem of circular dependencies,
 * e.g. in find pointers.
 */
/*****************************************************************************/
  class ExprManagerNotifyObj: public ContextNotifyObj {
    ExprManager* d_em;
  public:
    //! Constructor
    ExprManagerNotifyObj(ExprManager* em, Context* cxt)
      : ContextNotifyObj(cxt), d_em(em) { }

    void notifyPre(void);
    void notify(void);
    unsigned long getMemory(int verbosity) { return sizeof(ExprManagerNotifyObj); }
  };
    

} // end of namespace CVC3

// Include expr_value here for inline definitions
#include "expr_value.h"

namespace CVC3 {

inline size_t ExprManager::hash(const ExprValue* ev) const {
  DebugAssert(ev!=NULL, "ExprManager::hash() called on a NULL ExprValue");
  return ev->hash();
}

inline Expr ExprManager::newLeafExpr(const Op& op)
{
  if (op.getKind() != APPLY) {
    ExprValue ev(this, op.getKind());
    return newExpr(&ev);
  }
  else {
    DebugAssert(op.getExpr().getEM() == this, "ExprManager mismatch");
    std::vector<Expr> kids;
    ExprApply ev(this, op, kids);
    return newExpr(&ev);
  }
}

inline Expr ExprManager::newStringExpr(const std::string &s)
  { ExprString ev(this, s); return newExpr(&ev); }

inline Expr ExprManager::newRatExpr(const Rational& r)
  { ExprRational ev(this, r); return newExpr(&ev); }

inline Expr ExprManager::newSkolemExpr(const Expr& e, int i)
  { DebugAssert(e.getEM() == this, "ExprManager mismatch");
    ExprSkolem ev(this, i, e); return newExpr(&ev); }

inline Expr ExprManager::newVarExpr(const std::string &s)
  { ExprVar ev(this, s); return newExpr(&ev); }

inline Expr ExprManager::newSymbolExpr(const std::string &s, int kind)
  { ExprSymbol ev(this, kind, s); return newExpr(&ev); }

inline Expr ExprManager::newBoundVarExpr(const std::string &name,
                                         const std::string& uid)
  { ExprBoundVar ev(this, name, uid); return newExpr(&ev); }

inline Expr ExprManager::newBoundVarExpr(const std::string& name,
                                         const std::string& uid,
                                         const Type& type) {
  Expr res = newBoundVarExpr(name, uid);
  DebugAssert(type.getExpr().getKind() != ARROW,"");
  DebugAssert(res.lookupType().isNull(), 
              "newBoundVarExpr: redefining a variable " + name);
  res.setType(type);
  return res;
}

inline Expr ExprManager::newBoundVarExpr(const Type& type) {
  static int nextNum = 0;
  std::string name("_cvc3_");
  std::string uid =  int2string(nextNum++);
  return newBoundVarExpr(name, uid, type);
}

inline Expr ExprManager::newClosureExpr(int kind,
                                        const Expr& var,
                                        const Expr& body)
  { ExprClosure ev(this, kind, var, body); return newExpr(&ev); }

inline Expr ExprManager::newClosureExpr(int kind,
                                        const std::vector<Expr>& vars,
                                        const Expr& body)
  { ExprClosure ev(this, kind, vars, body); return newExpr(&ev); }

inline Expr ExprManager::newClosureExpr(int kind,
                                        const std::vector<Expr>& vars,
                                        const Expr& body,
                                        const std::vector<Expr>& trigs)
  { ExprClosure ev(this, kind, vars, body);
    Expr ret = newExpr(&ev); ret.setTriggers(trigs); return ret; }

inline Expr ExprManager::newClosureExpr(int kind,
                                        const std::vector<Expr>& vars,
                                        const Expr& body,
                                        const std::vector<std::vector<Expr> >& trigs)
  { ExprClosure ev(this, kind, vars, body);
    Expr ret = newExpr(&ev); ret.setTriggers(trigs); return ret; }

inline Expr ExprManager::newClosureExpr(int kind,
                                        const std::vector<Expr>& vars,
                                        const Expr& body,
                                        const Expr& trig)
  { ExprClosure ev(this, kind, vars, body);
    Expr ret = newExpr(&ev); ret.setTrigger(trig); return ret; }

inline bool ExprManager::EqEV::operator()(const ExprValue* ev1,
                                          const ExprValue* ev2) const {
  return (*ev1) == (*ev2);
}

inline size_t ExprManager::hash(const Expr& e) const {
  DebugAssert(!e.isNull(), "ExprManager::hash() called on a Null Expr");
  return e.d_expr->hash();
}
 
} // end of namespace CVC3

#endif

