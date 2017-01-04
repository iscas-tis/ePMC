/*****************************************************************************/
/*!
 * \file clause.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Fri Mar  7 16:03:38 2003
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
 * Class to represent a clause, which is a disjunction of formulas
 * which are literals for conflict clauses, and possibly more complex
 * formulas for the clauses derived from the user-asserted formulas.
 * 
 * class Clause is implemented as a smart pointer to ClauseValue, so
 * it can be freely copied and assigned with low overhead (like
 * Theorem or Expr).
 */
/*****************************************************************************/

// Include it before ifndef, since it includes this file recursively
#ifndef DOXYGEN
#include "variable.h"
#endif

#ifndef _cvc3__include__clause_h_
#define _cvc3__include__clause_h_

namespace CVC3 {

  class Clause;
  class ClauseOwner;
  class TheoryCore;

  class ClauseValue {
    friend class Clause;
  private:
    //! Ref. counter
    int d_refcount;
    //! Ref. counter of ClauseOwner classes holding it
    int d_refcountOwner;
    // The original clause (disjunction of literals)
    Theorem d_thm;
    // Scope where the clause is valid
    int d_scope;
    // Theorems l_i <=> l'_i for each literal l_i
    // FIXME: more efficient implementation for fixed arrays of CDOs
    std::vector<Literal> d_literals;
    // Disallow copy constructor and assignment (make private)
    ClauseValue(const ClauseValue& c); // Undefined (since it cannot be used)
    ClauseValue& operator=(const ClauseValue& c) { return *this; }
    // Pointers to watched literals (Watch Pointers).  They are not
    // backtrackable.
    size_t d_wp[2];
    // Direction flags for the watch pointers (1 or -1)
    // FIXME: should we use one bit of d_wp{1,2} instead? (efficiency
    // vs. space)
    int d_dir[2];
    // A flag indicating that the clause is shown satisfiable
    CDO<bool> d_sat;
    // Marks the clause as deleted
    bool d_deleted;
    // Creation file and line number (for debugging)
    IF_DEBUG(std::string d_file; int d_line;)
    // Constructor: takes the main clause theorem which must be a
    // disjunction of literals and have no assumptions.
    ClauseValue(TheoryCore* core, VariableManager* vm,
		const Theorem& clause, int scope);
  public:
    // Destructor
    ~ClauseValue();
      
  }; // end of class ClauseValue

  //! A class representing a CNF clause (a smart pointer)
  class Clause {
  private:
    friend class ClauseOwner;
    //! The only value member
    ClauseValue* d_clause;
    //! Export owner refcounting to ClauseOwner
    int& countOwner() {
      DebugAssert(d_clause != NULL, "");
      return d_clause->d_refcountOwner;
    }
  public:
    Clause(): d_clause(NULL) { }
    // Constructors
    Clause(TheoryCore* core, VariableManager* vm, const Theorem& clause,
           int scope, const std::string& file = "", int line = 0)
      : d_clause(new ClauseValue(core, vm, clause, scope)) {
      d_clause->d_refcount++;
      IF_DEBUG(d_clause->d_file = file; d_clause->d_line=line;)
    }
    // Copy constructor
    Clause(const Clause& c): d_clause(c.d_clause) {
      if(d_clause != NULL) d_clause->d_refcount++;
    }
    // Destructor
    ~Clause();
    // Assignment operator
    Clause& operator=(const Clause& c);

    bool isNull() const { return d_clause == NULL; }
    // Other public methods
    size_t size() const {
      return (d_clause == NULL)? 0 : d_clause->d_literals.size();
    }
    // Get the theorem representing the entire clause
    const Theorem& getTheorem() const {
      DebugAssert(!isNull(), "Clause::getTheorem(): Null Clause");
      return d_clause->d_thm;
    }
    // Get the scope where the clause is valid
    int getScope() const {
      if(isNull()) return 0;
      else return d_clause->d_scope;
    }
    // Get the current value of the i-th literal
    const Literal& getLiteral(size_t i) const {
      DebugAssert(!isNull(), "Clause::getLiteral(): Null Clause");
      DebugAssert(i < size(), 
		  "Clause::getLiteral(" + int2string(i)
		  + "): i >= size = " + int2string(size()));
      return d_clause->d_literals[i];
    }
    // Get the current value of the i-th literal
    const Literal& operator[](size_t i) const { return getLiteral(i); }

    // Get the reference to the vector of literals, for fast access
    const std::vector<Literal>& getLiterals() const {
      DebugAssert(!isNull(), "Clause::getLiterals(): Null Clause");
      return d_clause->d_literals;
    }
    // Get the values of watch pointers
    size_t wp(int i) const {
      DebugAssert(!isNull(), "Clause::wp(i): Null Clause");
      DebugAssert(i==0 || i==1, 
		  "wp(i): Watch pointer index is out of bounds: i = "
		  + int2string(i));
      return d_clause->d_wp[i];
    }
    // Get the watched literals
    const Literal& watched(int i) const { return getLiteral(wp(i)); }
    // Set the watch pointers and return the new value
    size_t wp(int i, size_t l) const {
      DebugAssert(!isNull(), "Clause::wp(i,l): Null Clause");
      DebugAssert(i==0 || i==1, 
		  "wp(i,l): Watch pointer index is out of bounds: i = "
		  + int2string(i));
      DebugAssert(l < size(), "Clause::wp(i = " + int2string(i)
		  + ", l = " + int2string(l)
		  + "): l >= size() = " + int2string(size()));
      TRACE("clauses", " **clauses** UPDATE wp(idx="
	    +int2string(i)+", l="+int2string(l),
	    ")\n  clause #: ", id());
      d_clause->d_wp[i] = l;
      return l;
    }
    // Get the direction of the i-th watch pointer
    int dir(int i) const {
      DebugAssert(!isNull(), "Clause::dir(i): Null Clause");
      DebugAssert(i==0 || i==1, 
		  "dir(i): Watch pointer index is out of bounds: i = "
		  + int2string(i));
      return d_clause->d_dir[i];
    }
    // Set the direction of the i-th watch pointer
    int dir(int i, int d) const {
      DebugAssert(!isNull(), "Clause::dir(i,d): Null Clause");
      DebugAssert(i==0 || i==1, 
		  "dir(i="+int2string(i)+",d="+int2string(d)
		  +"): Watch pointer index is out of bounds");
      DebugAssert(d==1 || d==-1, "dir(i="+int2string(i)+",d="+int2string(d)
		  +"): Direction is out of bounds");
      d_clause->d_dir[i] = d;
      return d;
    }
    //! Check if the clause marked as SAT
    bool sat() const {
      DebugAssert(!isNull(), "Clause::sat()");
      return d_clause->d_sat;
    }
    //! Precise version of sat(): check all the literals and cache the result
    bool sat(bool ignored) const {
      DebugAssert(!isNull(), "Clause::sat()");
      bool flag = false;
      if (!d_clause->d_sat) {
        for (size_t i = 0; !flag && i < d_clause->d_literals.size(); ++i)
          if (d_clause->d_literals[i].getValue() == 1)
            flag = true;
      }
      if (flag) {
        //std::cout << "*** Manually marking SAT" << std::endl;
        markSat();
      }
      return d_clause->d_sat;
    }
    // Mark the clause as SAT
    void markSat() const {
      DebugAssert(!isNull(), "Clause::markSat()");
      d_clause->d_sat = true;
    }
    // Check / mark the clause as deleted
    bool deleted() const {
      DebugAssert(!isNull(), "Clause::deleted()");
      return d_clause->d_deleted;
    }
    void markDeleted() const;

    // For debugging: return some kind of unique ID
    size_t id() const { return (size_t) d_clause; }

    // Equality: compare the pointers
    bool operator==(const Clause& c) const { return d_clause == c.d_clause; }

    //! Tell how many owners this clause has (for debugging)
    int owners() const { return d_clause->d_refcountOwner; }
    
    // Printing
    std::string toString() const;

    friend std::ostream& operator<<(std::ostream& os, const Clause& c);

    IF_DEBUG(bool wpCheck() const;)
    IF_DEBUG(const std::string& getFile() const { return d_clause->d_file; })
    IF_DEBUG(int getLine() const { return d_clause->d_line; })

  }; // end of class Clause

  //! Same as class Clause, but when destroyed, marks the clause as deleted
  /*! Needed for backtraking data structures.  When the SAT solver
    backtracks, some clauses will be thrown away automatically, and we
    need to mark those as deleted. */
  class ClauseOwner {
    Clause d_clause;
    //! Disable default constructor
    ClauseOwner() { }
  public:
    //! Constructor from class Clause
    ClauseOwner(const Clause& c): d_clause(c) { d_clause.countOwner()++; }
    //! Construct a new Clause
    ClauseOwner(TheoryCore* core, VariableManager* vm, const Theorem& clause,
		int scope): d_clause(core, vm, clause, scope) {
      d_clause.countOwner()++;
    }
    //! Copy constructor (keep track of refcounts)
    ClauseOwner(const ClauseOwner& c): d_clause(c.d_clause) {
      d_clause.countOwner()++;
    }
    //! Assignment (keep track of refcounts)
    ClauseOwner& operator=(const ClauseOwner& c) {
      if(&c == this) return *this; // Seft-assignment
      DebugAssert(d_clause.countOwner() > 0, "in operator=");
      if(--(d_clause.countOwner()) == 0)
	d_clause.markDeleted();
      d_clause = c.d_clause;
      d_clause.countOwner()++;
      return *this;
    }
    //! Destructor: mark the clause as deleted
    ~ClauseOwner() {
      FatalAssert(d_clause.countOwner() > 0, "in ~ClauseOwner");
      if(--(d_clause.countOwner()) == 0) {
	d_clause.markDeleted();
      }
    }
    //! Automatic type conversion to Clause ref
    operator Clause& () { return d_clause; }
    //! Automatic type conversion to Clause const ref
    operator const Clause& () const { return d_clause; }
  }; // end of class ClauseOwner
    

  // I/O Manipulators

  // Print clause in a compact form: Clause[x=-1@scope, ...], mark
  // watched literals by '*'
  class CompactClause {
  private:
    Clause d_clause;
  public:
    CompactClause(const Clause& c): d_clause(c) { }
    friend std::ostream& operator<<(std::ostream& os, const CompactClause& c);
    std::string toString() const;
  };

} // end of namespace CVC3
    
#endif
