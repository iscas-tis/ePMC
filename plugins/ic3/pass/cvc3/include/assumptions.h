/*****************************************************************************/
/*!
 * \file assumptions.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Dec 10 00:37:49 GMT 2002
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
// CLASS: Assumptions
//
// AUTHOR: Sergey Berezin, 12/03/2002
//
// Abstract:
//
// Mathematically, the value of class Assumptions is a set of pairs
// 'u:A' on the LHS of the Theorem's sequent.  Both u and A are Expr.
//
// Null assumptions is almost always treated as the empty set.  The
// only exception: iterators cannot be constructed for Null.
//
// This interface should be used as little as possible by the users of
// Theorem class.
///////////////////////////////////////////////////////////////////////////////
#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__assumptions_h_
#define _cvc3__assumptions_h_

#include "theorem.h"

namespace CVC3 {

  class Assumptions {
  private:
    std::vector<Theorem> d_vector;
    static Assumptions s_empty;

  private:
    // Private constructor for internal use.  Assumes v != NULL.
    //    Assumptions(AssumptionsValue *v);

    // helper function for []
    const Theorem& findTheorem(const Expr& e) const;

    static bool findExpr(const Assumptions& a, const Expr& e, 
			 std::vector<Theorem>& gamma);
    static bool findExprs(const Assumptions& a, const std::vector<Expr>& es, 
			  std::vector<Theorem>& gamma);

    void add(const std::vector<Theorem>& thms);

  public:
    //! Default constructor: no value is created
    Assumptions() { }
    //! Constructor from a vector of theorems
    Assumptions(const std::vector<Theorem>& v);
    //! Constructor for one theorem (common case)
    Assumptions(const Theorem& t) { d_vector.push_back(t); }
    //! Constructor for two theorems (common case)
    Assumptions(const Theorem& t1, const Theorem& t2);

    // Destructor
    ~Assumptions() {}
    // Copy constructor.
    Assumptions(const Assumptions &assump) : d_vector(assump.d_vector) {}
    // Assignment.
    Assumptions &operator=(const Assumptions &assump)
    { d_vector = assump.d_vector; return *this; }

    static const Assumptions& emptyAssump() { return s_empty; }

    void add1(const Theorem& t) {
      DebugAssert(d_vector.empty(), "expected empty vector");
      d_vector.push_back(t);
    }
    void add(const Theorem& t);
    void add(const Assumptions& a) { add(a.d_vector); }
    // clear the set of assumptions
    void clear() { d_vector.clear(); }
    // get the size
    unsigned size() const { return d_vector.size(); }
    bool empty() const { return d_vector.empty(); }

    // needed by TheoremValue
    Theorem& getFirst() {
      DebugAssert(size() > 0, "Expected size > 0");
      return d_vector[0];
    }
    
    // Print functions
    std::string toString() const;
    void print() const;

    // Return Assumption associated with the expression.  The
    // value will be Null if the assumption is not in the set.
    //
    // NOTE: do not try to assign anything to the result, it won't work.
    const Theorem& operator[](const Expr& e) const;

    // find only searches through current set of assumptions, will not recurse
    const Theorem& find(const Expr& e) const;

    //! Iterator for the Assumptions: points to class Theorem.
    /*! Cannot inherit from vector<Theorem>::const_iterator in gcc 2.96 */
    class iterator : public std::iterator<std::input_iterator_tag,Theorem,ptrdiff_t> {
      // Let's be friends
      friend class Assumptions;
    private:
      std::vector<Theorem>::const_iterator d_it;

      iterator(const std::vector<Theorem>::const_iterator& i): d_it(i) { }
    public:
      //! Default constructor
      iterator() { }
      //! Destructor
      ~iterator() { }
      //! Equality
      bool operator==(const iterator& i) const { return (d_it == i.d_it); }
      //! Disequality
      bool operator!=(const iterator& i) const { return (d_it != i.d_it); }
      //! Dereference operator
      const Theorem& operator*() const { return *d_it; }
      //! Member dereference operator
      const Theorem* operator->() const { return &(operator*()); }
      //! Prefix increment
      iterator& operator++() { ++d_it; return *this; }
      //! Proxy class for postfix increment
      class Proxy {
	const Theorem* d_t;
      public:
	Proxy(const Theorem& t) : d_t(&t) { }
	const Theorem& operator*() { return *d_t; }
      };
      //! Postfix increment
      Proxy operator++(int) { return Proxy(*(d_it++)); }
    };

    iterator begin() const { return iterator(d_vector.begin()); }
    iterator end() const { return iterator(d_vector.end()); }

    // Merging assumptions
    //    friend Assumptions operator+(const Assumptions& a1, const Assumptions& a2);

    //! Returns all (recursive) assumptions except e
    friend Assumptions operator-(const Assumptions& a, const Expr& e);
    //! Returns all (recursive) assumptions except those in es
    friend Assumptions operator-(const Assumptions& a,
                                 const std::vector<Expr>& es);

    friend std::ostream& operator<<(std::ostream& os,
                                    const Assumptions &assump);

    friend bool operator==(const Assumptions& a1, const Assumptions& a2)
    { return a1.d_vector == a2.d_vector; }
    friend bool operator!=(const Assumptions& a1, const Assumptions& a2)
    { return a1.d_vector != a2.d_vector; }

  }; // end of class Assumptions

} // end of namespace CVC3

#endif
