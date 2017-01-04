/*****************************************************************************/
/*!
 * \file variable.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Fri Apr 25 11:52:17 2003
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
 * A data structure representing a variable in the search engine.  It
 * is a smart pointer with a uniquifying mechanism similar to Expr,
 * and a variable is uniquely determined by its expression.  It can be
 * thought of as an Expr with additional attributes, but the type is
 * different, so it will not be confused with other Exprs.
 */
/*****************************************************************************/

#ifndef _cvc3__variable_h_
#define _cvc3__variable_h_

#include "expr.h"

namespace CVC3 {

  class VariableManager;
  class VariableValue;
  class Clause;
  class SearchEngineRules;

  // The main "smart pointer" class
  class Variable {
  private:
    VariableValue* d_val;
    // Private methods
    Theorem deriveThmRec(bool checkAssump) const;
  public:
    // Default constructor
    Variable(): d_val(NULL) { }
    // Constructor from an Expr; if such variable already exists, it
    // will be found and used.
    Variable(VariableManager* vm, const Expr& e);
    // Copy constructor
    Variable(const Variable& l);
    // Destructor
    ~Variable();
    // Assignment
    Variable& operator=(const Variable& l);

    bool isNull() const { return d_val == NULL; }

    // Accessors

    // Expr is the only constant attribute of a variable; other
    // attributes can be changed.
    const Expr& getExpr() const;
    // The Expr of the inverse of the variable.  This function is
    // caching, so !e is only constructed once.
    const Expr& getNegExpr() const;
    
    // IMPORTANT: Value can be -1 (false), 1 (true), or 0 (unresolved)
    int getValue() const;
    // If the value is set, scope level and either a theorem or
    // an antecedent clause must be defined
    int getScope() const;
    const Theorem& getTheorem() const;
    const Clause& getAntecedent() const;
    // Index of this variable in the antecedent clause; if it is -1,
    // the variable is FALSE, and that clause caused the contradiction
    int getAntecedentIdx() const;
    // Theorem of the form l |- l produced by the 'assump' rule, if
    // this variable is a splitter, or a new intermediate assumption
    // is generated for it.
    const Theorem& getAssumpThm() const;
    // Setting the attributes: it can be either derived from the
    // antecedent clause, or by a theorem.  The scope level is set to
    // the current scope.
    void setValue(int val, const Clause& c, int idx);
    // The theorem's expr must be the same as the variable's expr or
    // its negation, and the scope is the lowest scope where all
    // assumptions of the theorem are true
    void setValue(const Theorem& thm);
    void setValue(const Theorem& thm, int scope);
    
    void setAssumpThm(const Theorem& a, int scope);
    // Derive the theorem for either the variable or its negation.  If
    // the value is set by a theorem, that theorem is returned;
    // otherwise a unit propagation rule is applied to the antecedent
    // clause.
    Theorem deriveTheorem() const;

    // Accessing Chaff counters (read and modified by reference)
    unsigned& count(bool neg);
    unsigned& countPrev(bool neg);
    int& score(bool neg);
    bool& added(bool neg);
    // Read-only versions
    unsigned count(bool neg) const;
    unsigned countPrev(bool neg) const;
    int score(bool neg) const;
    bool added(bool neg) const;
    // Watch pointer access
    std::vector<std::pair<Clause, int> >& wp(bool neg) const;
    // Friend methods
    friend bool operator==(const Variable& l1, const Variable& l2) {
      return l1.d_val == l2.d_val;
    }
    // Printing
    friend std::ostream& operator<<(std::ostream& os, const Variable& l);
    std::string toString() const;
  }; // end of class Variable

  class Literal {
  private:
    Variable d_var;
    bool d_negative;
  public:
    // Constructors: from a variable
    Literal(const Variable& v, bool positive = true)
      : d_var(v), d_negative(!positive) { }
    // Default constructor
    Literal(): d_negative(false) { }
    // from Expr: if e == !e', construct negative literal of e',
    // otherwise positive of e
    Literal(VariableManager* vm, const Expr& e)
      : d_var(vm, (e.isNot())? e[0] : e), d_negative(e.isNot()) { }
    Variable& getVar() { return d_var; }
    const Variable& getVar() const { return d_var; }
    bool isPositive() const { return !d_negative; }
    bool isNegative() const { return d_negative; }
    bool isNull() const { return d_var.isNull(); }
    // Return var or !var
    const Expr& getExpr() const {
      if(d_negative) return d_var.getNegExpr();
      else return d_var.getExpr();
    }
    int getValue() const {
      if(d_negative) return -(d_var.getValue());
      else return d_var.getValue();
    }
    int getScope() const { return getVar().getScope(); }
    // Set the value of the literal
//     void setValue(int val, const Clause& c, int idx) {
//       d_var.setValue(d_negative? -val : val, c, idx);
//     }
    void setValue(const Theorem& thm) {
      d_var.setValue(thm, thm.getScope());
    }
    void setValue(const Theorem& thm, int scope) {
      d_var.setValue(thm, scope);
    }
    const Theorem& getTheorem() const { return d_var.getTheorem(); }
//     const Clause& getAntecedent() const { return d_var.getAntecedent(); }
//     int getAntecedentIdx() const { return d_var.getAntecedentIdx(); }
    // Defined when the literal has a value.  Derives a theorem
    // proving either this literal or its inverse.
    Theorem deriveTheorem() const { return d_var.deriveTheorem(); }
    // Accessing Chaff counters (read and modified by reference)
    unsigned& count() { return d_var.count(d_negative); }
    unsigned& countPrev() { return d_var.countPrev(d_negative); }
    int& score() { return d_var.score(d_negative); }
    bool& added() { return d_var.added(d_negative); }
    // Read-only versions
    unsigned count() const { return d_var.count(d_negative); }
    unsigned countPrev() const { return d_var.countPrev(d_negative); }
    int score() const { return d_var.score(d_negative); }
    bool added() const { return d_var.added(d_negative); }
    // Watch pointer access
    std::vector<std::pair<Clause, int> >& wp() const
      { return d_var.wp(d_negative); }
    // Printing
    friend std::ostream& operator<<(std::ostream& os, const Literal& l);
    std::string toString() const;
    // Equality
    friend bool operator==(const Literal& l1, const Literal& l2) {
      return (l1.d_negative == l2.d_negative && l1.d_var==l1.d_var);
    }
  }; // end of class Literal

  // Non-member methods: negation of Variable and Literal
  inline Literal operator!(const Variable& v) {
    return Literal(v, false);
  }

  inline Literal operator!(const Literal& l) {
    return Literal(l.getVar(), l.isNegative());
  }

  std::ostream& operator<<(std::ostream& os, const Literal& l);

} // end of namespace CVC3

// Clause uses class Variable, have to include it here
#include "clause.h"

namespace CVC3 {

  // The value holding class
  class VariableValue {
    friend class Variable;
    friend class VariableManager;
  private:
    VariableManager* d_vm;
    int d_refcount;

    Expr d_expr;
    // The inverse expression (initally Null)
    Expr d_neg;

    // Non-backtracking attributes (Chaff counters)

    // For positive instances
    unsigned d_count;
    unsigned d_countPrev;
    int d_score;
    // For negative instances
    unsigned d_negCount;
    unsigned d_negCountPrev;
    int d_negScore;
    // Whether the corresponding literal is in the list of active literals
    bool d_added;
    bool d_negAdded;
    // Watch pointer lists
    std::vector<std::pair<Clause, int> > d_wp;
    std::vector<std::pair<Clause, int> > d_negwp;

    // Backtracking attributes

    // Value of the variable: -1 (false), 1 (true), 0 (unresolved)
    CDO<int>* d_val;
    CDO<int>* d_scope; // Scope level where the variable is assigned
    // Theorem of the form (d_expr) or (!d_expr), reflecting d_val
    CDO<Theorem>* d_thm;
    CDO<Clause>* d_ante; // Antecedent clause and index of the variable
    CDO<int>* d_anteIdx;
    CDO<Theorem>* d_assump; // Theorem generated by assump rule, if any
    // Constructor is private; only class Variable can create it
    VariableValue(VariableManager* vm, const Expr& e)
      : d_vm(vm), d_refcount(0), d_expr(e),
      d_count(0), d_countPrev(0), d_score(0),
      d_negCount(0), d_negCountPrev(0), d_negScore(0),
      d_added(false), d_negAdded(false),
      d_val(NULL), d_scope(NULL), d_thm(NULL),
      d_ante(NULL), d_anteIdx(NULL), d_assump(NULL) { }
  public:
    ~VariableValue();
    // Accessor methods
    const Expr& getExpr() const { return d_expr; }

    const Expr& getNegExpr() const {
      if(d_neg.isNull()) {
	const_cast<VariableValue*>(this)->d_neg
	  = d_expr.negate();
      }
      return d_neg;
    }

    int getValue() const {
      if(d_val==NULL) return 0;
      else return d_val->get();
    }
    
    int getScope() const {
      if(d_scope==NULL) return 0;
      else return d_scope->get();
    }

    const Theorem& getTheorem() const {
      static Theorem null;
      if(d_thm==NULL) return null;
      else return d_thm->get();
    }

    const Clause& getAntecedent() const {
      static Clause null;
      if(d_ante==NULL) return null;
      else return d_ante->get();
    }

    int getAntecedentIdx() const {
      if(d_anteIdx==NULL) return 0;
      else return d_anteIdx->get();
    }
    
    const Theorem& getAssumpThm() const {
      static Theorem null;
      if(d_assump==NULL) return null;
      else return d_assump->get();
    }

    // Setting the attributes: it can be either derived from the
    // antecedent clause, or by a theorem
    void setValue(int val, const Clause& c, int idx);
    // The theorem's expr must be the same as the variable's expr or
    // its negation
    void setValue(const Theorem& thm, int scope);

    void setAssumpThm(const Theorem& a, int scope);

    // Chaff counters: read and modified by reference
    unsigned& count(bool neg) {
      if(neg) return d_negCount;
      else return d_count;
    }
    unsigned& countPrev(bool neg) {
      if(neg) return d_negCountPrev;
      else return d_countPrev;
    }
    int& score(bool neg) {
      if(neg) return d_negScore;
      else return d_score;
    }
    bool& added(bool neg) {
      if(neg) return d_negAdded;
      else return d_added;
    }

    // Memory management
    void* operator new(size_t size, MemoryManager* mm) {
      return mm->newData(size);
    }
    void operator delete(void* pMem, MemoryManager* mm) {
      mm->deleteData(pMem);
    }
    void operator delete(void*) { }

    // friend methods
    friend std::ostream& operator<<(std::ostream& os, const VariableValue& v);
    friend bool operator==(const VariableValue& v1, const VariableValue& v2) {
      return v1.d_expr == v2.d_expr;
    }
  }; // end of class VariableValue

    // Accessing Chaff counters (read and modified by reference)
  inline unsigned& Variable::count(bool neg) { return d_val->count(neg); }
  inline unsigned& Variable::countPrev(bool neg)
    { return d_val->countPrev(neg); }
  inline int& Variable::score(bool neg) { return d_val->score(neg); }
  inline bool& Variable::added(bool neg) { return d_val->added(neg); }

  inline unsigned Variable::count(bool neg) const { return d_val->count(neg); }
  inline unsigned Variable::countPrev(bool neg) const
    { return d_val->countPrev(neg); }
  inline int Variable::score(bool neg) const { return d_val->score(neg); }
  inline bool Variable::added(bool neg) const { return d_val->added(neg); }

  inline std::vector<std::pair<Clause, int> >& Variable::wp(bool neg) const {
    if(neg) return d_val->d_negwp;
    else return d_val->d_wp;
  }


  class VariableManagerNotifyObj;

  // The manager class
  class VariableManager {
    friend class Variable;
    friend class VariableValue;
  private:
    ContextManager* d_cm;
    MemoryManager* d_mm;
    SearchEngineRules* d_rules;
    VariableManagerNotifyObj* d_notifyObj;
    //! Disable the garbage collection
    /*! Normally, it's set in the destructor, so that we can delete
     * all remaining variables without GC getting in the way
     */
    bool d_disableGC;
    //! Postpone garbage collection
    bool d_postponeGC;
    //! Vector of variables to be deleted (postponed during pop())
    std::vector<VariableValue*> d_deleted;
    
    // Hash only by the Expr
    class HashLV {
    public:
      size_t operator()(VariableValue* v) const { return v->getExpr().hash(); }
    };
    class EqLV {
    public:
      bool operator()(const VariableValue* lv1, const VariableValue* lv2) const
	{ return lv1->getExpr() == lv2->getExpr(); }
    };

    // Hash set for existing variables
    typedef std::hash_set<VariableValue*, HashLV, EqLV> VariableValueSet;
    VariableValueSet d_varSet;
    
    // Creating unique VariableValue
    VariableValue* newVariableValue(const Expr& e);

  public:
    // Constructor.  mmFlag indicates which memory manager to use.
    VariableManager(ContextManager* cm, SearchEngineRules* rules,
		    const std::string& mmFlag);
    // Destructor
    ~VariableManager();

    //! Garbage collect VariableValue pointer
    void gc(VariableValue* v);
    //! Postpone garbage collection
    void postponeGC() { d_postponeGC = true; }
    //! Resume garbage collection
    void resumeGC();
    // Accessors
    ContextManager* getCM() const { return d_cm; }
    SearchEngineRules* getRules() const { return d_rules; }

  }; // end of class VariableManager

/*****************************************************************************/
/*!
 *\class VariableManagerNotifyObj
 *\brief Notifies VariableManager before and after each pop()
 *
 * Author: Sergey Berezin
 *
 * Created: Tue Mar  1 13:52:28 2005
 *
 * Disables the deletion of VariableValue objects during context
 * restoration (backtracking).  This solves the problem of circular
 * dependencies (e.g. a Variable pointing to its antecedent Clause).
 */
/*****************************************************************************/
  class VariableManagerNotifyObj: public ContextNotifyObj {
    VariableManager* d_vm;
  public:
    //! Constructor
  VariableManagerNotifyObj(VariableManager* vm, Context* cxt)
    : ContextNotifyObj(cxt), d_vm(vm) { }
    
    void notifyPre(void) { d_vm->postponeGC(); }
    void notify(void) { d_vm->resumeGC(); }
  };


} // end of namespace CVC3
#endif
