/*****************************************************************************/
/*!
 * \file rational.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Dec 12 22:00:18 GMT 2002
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
// Class: Rational
// Author: Sergey Berezin, 12/12/2002 (adapted from Bignum)
//
// Description: This is an abstration of a rational with arbitrary
// precision.  It provides a constructor from a pair of ints and
// strings, overloaded operator{+,-,*,/}, assignment, etc.  The
// current implementation uses GMP mpq_class.
///////////////////////////////////////////////////////////////////////////////

#ifndef _cvc3__rational_h_
#define _cvc3__rational_h_

// Do not include <gmpxx.h> here; it contains some depricated C++
// headers.  We only include it in the .cpp file.

#include <vector>
#include "debug.h"

// To be defined only in bignum.cpp
namespace CVC3 {

  class CVC_DLL Rational {
  private:
    class Impl;
    Impl *d_n;
    // Debugging
#ifdef _DEBUG_RATIONAL_
    // Encapsulate static values in a function to guarantee
    // initialization when we need it
    int& getCreated() {
      static int num_created = 0;
      return(num_created);
    }
      
    int& getDeleted() {
      static int num_deleted = 0;
      return(num_deleted);
    }
      
    void printStats();
#endif
    // Private constructor (for internal consumption only)
    Rational(const Impl& t);

  public:
    // Constructors
    Rational();
    // Copy constructor
    Rational(const Rational &n);
    Rational(int n, int d = 1);
    Rational(const char* n, int base = 10);
    Rational(const std::string& n, int base = 10);
    Rational(const char* n, const char* d, int base = 10);
    Rational(const std::string& n, const std::string& d, int base = 10);
    // Destructor
    ~Rational();

    // Assignment
    Rational& operator=(const Rational& n);

    std::string toString(int base = 10) const;

    // Compute hash value (for DAG expression representation)
    size_t hash() const;

    friend CVC_DLL bool operator==(const Rational &n1, const Rational &n2);
    friend CVC_DLL bool operator<(const Rational &n1, const Rational &n2);
    friend CVC_DLL bool operator<=(const Rational &n1, const Rational &n2);
    friend CVC_DLL bool operator>(const Rational &n1, const Rational &n2);
    friend CVC_DLL bool operator>=(const Rational &n1, const Rational &n2);
    friend CVC_DLL bool operator!=(const Rational &n1, const Rational &n2);
    friend CVC_DLL Rational operator+(const Rational &n1, const Rational &n2);
    friend CVC_DLL Rational operator-(const Rational &n1, const Rational &n2);
    friend CVC_DLL Rational operator*(const Rational &n1, const Rational &n2);
    friend CVC_DLL Rational operator/(const Rational &n1, const Rational &n2);
    // 'mod' operator, defined only for integer values of n1 and n2
    friend CVC_DLL Rational operator%(const Rational &n1, const Rational &n2);

    // Unary minus
    Rational operator-() const;
    Rational &operator+=(const Rational &n2);
    Rational &operator-=(const Rational &n2);
    Rational &operator*=(const Rational &n2);
    Rational &operator/=(const Rational &n2);
    //! Prefix increment
    const Rational& operator++() { *this = (*this)+1; return *this; }
    //! Postfix increment
    Rational operator++(int) { Rational x(*this); *this = x+1; return x; }
    //! Prefix decrement
    const Rational& operator--() { *this = (*this)-1; return *this; }
    //! Postfix decrement
    Rational operator--(int) { Rational x(*this); *this = x-1; return x; }

    // Result is integer
    Rational getNumerator() const;
    Rational getDenominator() const;

    // Equivalent to (getDenominator() == 1), but possibly more efficient
    bool isInteger() const;
    // Convert to int; defined only on integer values
    int getInt() const;
    // Equivalent to (*this >= 0 && isInteger())
    bool isUnsigned() const { return (isInteger() && (*this) >= 0); }
    // Convert to unsigned int; defined only on non-negative integer values
    unsigned int getUnsigned() const;

    friend std::ostream &operator<<(std::ostream &os, const Rational &n);

    /* Computes gcd and lcm on *integer* values. Result is always a
       positive integer. */

    friend CVC_DLL Rational gcd(const Rational &x, const Rational &y);
    friend CVC_DLL Rational gcd(const std::vector<Rational> &v);
    friend CVC_DLL Rational lcm(const Rational &x, const Rational &y);
    friend CVC_DLL Rational lcm(const std::vector<Rational> &v);

    friend CVC_DLL Rational abs(const Rational &x);

    //! Compute the floor of x (result is an integer)
    friend CVC_DLL Rational floor(const Rational &x);
    //! Compute the ceiling of x (result is an integer)
    friend CVC_DLL Rational ceil(const Rational &x);
    //! Compute non-negative remainder for *integer* x,y.
    friend CVC_DLL Rational mod(const Rational &x, const Rational &y);
    //! nth root: return 0 if no exact answer (base should be nonzero)
    friend CVC_DLL Rational intRoot(const Rational& base, unsigned long int n);

    // For debugging, to be able to print in gdb
    void print() const;

  }; // Rational class

  //! Raise 'base' into the power of 'pow' (pow must be an integer)
  inline Rational pow(Rational pow, const Rational& base) {
    DebugAssert(pow.isInteger(), "pow("+pow.toString()
		+", "+base.toString()+")");
    FatalAssert(base != 0 || pow >= 0, "Attempt to divide by zero");
    bool neg(pow < 0);
    if(neg) pow = -pow;
    Rational res(1);
    for(; pow > 0; --pow) res *= base;
    if(neg) res = 1/res;
    return res;
  }
  //! take nth root of base, return result if it is exact, 0 otherwise
  // base should not be 0
  inline Rational ratRoot(const Rational& base, unsigned long int n)
  {
    DebugAssert(base != 0, "Expected nonzero base");
    Rational num = base.getNumerator();
    num = intRoot(num, n);
    if (num != 0) {
      Rational den = base.getDenominator();
      den = intRoot(den, n);
      if (den != 0) {
        return num / den;
      }
    }
    return 0;
  }
  
  // Methods creating new Rational values, similar to the
  // constructors, but can be nested
  inline Rational newRational(int n, int d = 1) { return Rational(n, d); }
  inline Rational newRational(const char* n, int base = 10)
    { return Rational(n, base); }
  inline Rational newRational(const std::string& n, int base = 10)
    { return Rational(n, base); }
  inline Rational newRational(const char* n, const char* d, int base = 10)
    { return Rational(n, d, base); }
  inline Rational newRational(const std::string& n, const std::string& d,
			      int base = 10)
    { return Rational(n, d, base); }
    
  // Debugging print
  void printRational(const Rational &x);

  // TODO: implement this properly
  typedef unsigned long Unsigned;

} // end of namespace CVC3

#endif
