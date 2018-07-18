#ifndef CoCoA_IntOperations_H
#define CoCoA_IntOperations_H

//   Copyright (c)  2012  John Abbott

//   This file is part of the source of CoCoALib, the CoCoA Library.

//   CoCoALib is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.

//   CoCoALib is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.

//   You should have received a copy of the GNU General Public License
//   along with CoCoALib.  If not, see <http://www.gnu.org/licenses/>.
//   -------------------------------------------------------------------------

#include "CoCoA/BigInt.H"
#include "CoCoA/MachineInt.H"


namespace CoCoA
{

  // STANDARD ARITHMETIC OPERATIONS

  int sign(const BigInt& N);
  int sign(const MachineInt& n);
  const BigInt abs(const BigInt& N);
  // unsigned long abs(const MachineInt& n); // see MachineInt.H
  const BigInt operator-(const BigInt& N);
  void negate(BigInt& N);

  const BigInt operator+(const BigInt& N1, const BigInt& N2);
  const BigInt operator+(const BigInt& N1, const MachineInt& n2);
  const BigInt operator+(const MachineInt& n1, const BigInt& N2);

  const BigInt operator-(const BigInt& N1, const BigInt& N2);
  const BigInt operator-(const BigInt& N1, const MachineInt& n2);
  const BigInt operator-(const MachineInt& n1, const BigInt& N2);

  const BigInt operator*(const BigInt& N1, const BigInt& N2);
  const BigInt operator*(const BigInt& N1, const MachineInt& n2);
  const BigInt operator*(const MachineInt& n1, const BigInt& N2);

  const BigInt operator/(const BigInt& N1, const BigInt& N2);
  const BigInt operator/(const BigInt& N1, const MachineInt& n2);
  const BigInt operator/(const MachineInt& n1, const BigInt& N2);

  const BigInt operator%(const BigInt& N1, const BigInt& N2);
  long operator%(const BigInt& N1, const MachineInt& n2);
  const BigInt operator%(const MachineInt& n1, const BigInt& N2);
  const BigInt LeastNNegRemainder(const BigInt& N1, const BigInt& N2);
  const BigInt LeastNNegRemainder(const BigInt& N1, const MachineInt& n2);
  const BigInt LeastNNegRemainder(const MachineInt& n1, const BigInt& N2);
  const BigInt LeastNNegRemainder(const MachineInt& n1, const MachineInt& n2);
  const BigInt SymmRemainder(const BigInt& N1, const BigInt& N2);
  long SymmRemainder(const BigInt& N1, const MachineInt& n2);
  const BigInt SymmRemainder(const MachineInt& n1, const BigInt& N2);
  long SymmRemainder(const MachineInt& n1, const MachineInt& n2);

  const BigInt power(const BigInt& base, const BigInt& exponent);
  const BigInt power(const BigInt& base, const MachineInt& exponent);
  const BigInt power(const MachineInt& base, const BigInt& exponent);
  const BigInt power(const MachineInt& base, const MachineInt& exponent);
  long SmallPower(const MachineInt& base, const MachineInt& exponent); ///< base^exponent (assuming it fits into a long)


  // Efficient arithmetic procedures...

  void add(BigInt& lhs, const BigInt& N1, const BigInt& N2);
  void add(BigInt& lhs, const BigInt& N1, const MachineInt& n2);
  void add(BigInt& lhs, const MachineInt& n1, const BigInt& N2);

  void sub(BigInt& lhs, const BigInt& N1, const BigInt& N2);
  void sub(BigInt& lhs, const BigInt& N1, const MachineInt& n2);
  void sub(BigInt& lhs, const MachineInt& n1, const BigInt& N2);

  void mul(BigInt& lhs, const BigInt& N1, const BigInt& N2);
  void mul(BigInt& lhs, const BigInt& N1, const MachineInt& n2);
  void mul(BigInt& lhs, const MachineInt& n1, const BigInt& N2);

  void div(BigInt& lhs, const BigInt& N1, const BigInt& N2);
  void div(BigInt& lhs, const BigInt& N1, const MachineInt& n2);
  void div(BigInt& lhs, const MachineInt& n1, const BigInt& N2);

  void mod(BigInt& lhs, const BigInt& N1, const BigInt& N2);
  void mod(BigInt& lhs, const BigInt& N1, const MachineInt& n2);
  void mod(BigInt& lhs, const MachineInt& n1, const BigInt& N2);

  void quorem(BigInt& quo, BigInt& rem, const BigInt& num, const BigInt& den);
  void quorem(BigInt& quo, BigInt& rem, const BigInt& num, const MachineInt& den); // why is rem not of type MachineInt???
  void quorem(BigInt& quo, BigInt& rem, const MachineInt& num, const BigInt& den);

  void divexact(BigInt& lhs, const BigInt& N1, const BigInt& N2);
//???  void divexact(BigInt& lhs, const MachineInt& N1, const BigInt& N2);
//???  void divexact(BigInt& lhs, const BigInt& N1, const MachineInt& N2);


  // SUNDRY PREDICATES.

  bool IsZero(const BigInt& N);
  bool IsOne(const BigInt& N);
  bool IsMinusOne(const BigInt& N);

  bool IsOdd(const BigInt& N);
  bool IsOdd(const MachineInt& n);
  bool IsEven(const BigInt& N);
  bool IsEven(const MachineInt& n);
  bool IsPowerOf2(const BigInt& N);
  bool IsPowerOf2(const MachineInt& n);

  bool IsDivisible(const MachineInt& N, const MachineInt& D); // is N divisibile by D?
  bool IsDivisible(const MachineInt& N, const BigInt& D);     // is N divisibile by D?
  bool IsDivisible(const BigInt& N, const MachineInt& D);     // is N divisibile by D?
  bool IsDivisible(const BigInt& N, const BigInt& D);         // is N divisibile by D?
//bool IsPerfectSquare(const BigInt& N);
// bool IsPerfectSquare(const MachineInt& n);


  // COMPARISON FUNCTIONS

  int cmp(const BigInt& N1, const BigInt& N2);
  int cmp(const BigInt& N1, const MachineInt& n2);
  int cmp(const MachineInt& n1, const BigInt& N2);
  int cmp(const MachineInt& n1, const MachineInt& n2);

  int CmpAbs(const BigInt& N1, const BigInt& N2);
  int CmpAbs(const BigInt& N1, const MachineInt& n2);
  int CmpAbs(const MachineInt& n1, const BigInt& N2);
  int CmpAbs(const MachineInt& n1, const MachineInt& n2);

  bool operator==(const BigInt& N1, const BigInt& N2);
  bool operator==(const BigInt& N1, const MachineInt& n2);
  bool operator==(const MachineInt& n1, const BigInt& N2);

  bool operator!=(const BigInt& N1, const BigInt& N2);
  bool operator!=(const BigInt& N1, const MachineInt& n2);
  bool operator!=(const MachineInt& n1, const BigInt& N2);

  bool operator> (const BigInt& N1, const BigInt& N2);
  bool operator> (const BigInt& N1, const MachineInt& n2);
  bool operator> (const MachineInt& n1, const BigInt& N2);

  bool operator>=(const BigInt& N1, const BigInt& N2);
  bool operator>=(const BigInt& N1, const MachineInt& n2);
  bool operator>=(const MachineInt& n1, const BigInt& N2);

  bool operator< (const BigInt& N1, const BigInt& N2);
  bool operator< (const BigInt& N1, const MachineInt& n2);
  bool operator< (const MachineInt& n1, const BigInt& N2);

  bool operator<=(const BigInt& N1, const BigInt& N2);
  bool operator<=(const BigInt& N1, const MachineInt& n2);
  bool operator<=(const MachineInt& n1, const BigInt& N2);
			

  // MISCELLANEOUS FUNCTIONS

  double mantissa(const BigInt& N);
  long exponent(const BigInt& N);

  long NumDigits(const BigInt& N, long base=2);
  double log(const BigInt& N);
  // std::log works for machine integers
  long ILogBase(const MachineInt& n, const MachineInt& base);
  long ILogBase(const MachineInt& n, const BigInt& base);
  long ILogBase(const BigInt& N,     const MachineInt& base);
  long ILogBase(const BigInt& N,     const BigInt& base);

  BigInt RangeFactorial(const MachineInt& lo, const MachineInt& hi); // computes lo*(lo+1)*(lo+2)*...*hi
  const BigInt factorial(const BigInt& N);
  const BigInt factorial(const MachineInt& n);
  double LogFactorial(const MachineInt& n);
  double LogFactorial(const BigInt& n);
  const BigInt binomial(const BigInt& N,     const BigInt& R);
  const BigInt binomial(const MachineInt& n, const BigInt& R);
  const BigInt binomial(const BigInt& N,     const MachineInt& r);
  const BigInt binomial(const MachineInt& n, const MachineInt& r);
  const BigInt fibonacci(const BigInt& N);
  const BigInt fibonacci(const MachineInt& n);


  const BigInt RoundDiv(const BigInt& num,     const BigInt& den);     ///< round num/den to an integer (halves round toward 0)
  const BigInt RoundDiv(const MachineInt& num, const BigInt& den);     ///< round num/den to an integer (halves round toward 0)
  const BigInt RoundDiv(const BigInt& num,     const MachineInt& den); ///< round num/den to an integer (halves round toward 0)
  long         RoundDiv(const MachineInt& num, const MachineInt& den); ///< round num/den to an integer (halves round toward 0)

  const BigInt iroot(const MachineInt& n, const MachineInt& r);
  const BigInt iroot(const MachineInt& n, const BigInt& R);
  const BigInt iroot(const BigInt& N,     const MachineInt& r);
  const BigInt iroot(const BigInt& N,     const BigInt& R);

  bool IsExactIroot(long& ans,   const MachineInt& n, const MachineInt& r);
  bool IsExactIroot(BigInt& ans, const MachineInt& n, const MachineInt& r);
  bool IsExactIroot(long& ans,   const MachineInt& n, const BigInt& R);
  bool IsExactIroot(BigInt& ans, const MachineInt& n, const BigInt& R);
//???  bool IsExactIroot(long& ans, const BigInt& N, const MachineInt& r);
  bool IsExactIroot(BigInt& ans, const BigInt& N,     const MachineInt& r);
//???  bool IsExactIroot(long& ans, const BigInt& N, const BigInt& R);
  bool IsExactIroot(BigInt& ans, const BigInt& N,     const BigInt& R);

  long isqrt(const MachineInt& n);
  const BigInt isqrt(const BigInt& N);

  bool IsSquare(const MachineInt& n);
  bool IsSquare(const BigInt& N);
  bool IsPower(const MachineInt& n);
  bool IsPower(const BigInt& N);


/*
  friend void           SqrtRem(BigInt& sqrt, BigInt& rem, const BigInt& b);
BigInt         Div2Exp(const BigInt& b, const MachineInt& exponent_of_2);
BigInt         Mod2Exp(const BigInt& b, const MachineInt& exponent_of_2);
BigInt         Mul2Exp(const BigInt& b, const MachineInt& exponent_of_2);
*/


  // Inline impls

  inline int sign(const MachineInt& n)
  { if (IsNegative(n)) return -1; if (IsZero(n)) return 0; return 1; }

  inline int sign(const BigInt& N)
  { return mpz_sgn(mpzref(N)); }


  inline void negate(BigInt& lhs)
  { mpz_neg(mpzref(lhs), mpzref(lhs)); }


  inline bool IsOdd(const BigInt& N)
  { return mpz_odd_p(mpzref(N)); }

  inline bool IsOdd(const MachineInt& n)
  { return abs(n)&1; }


  inline bool IsEven(const BigInt& N)
  { return mpz_even_p(mpzref(N)); }

  inline bool IsEven(const MachineInt& n)
  { return !IsOdd(n); }


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/IntOperations.H,v 1.4 2014/04/08 13:04:13 abbott Exp $
// $Log: IntOperations.H,v $
// Revision 1.4  2014/04/08 13:04:13  abbott
// Summary: Added new fn IsPowerOf2
// Author: JAA
//
// Revision 1.3  2013/05/20 15:48:13  abbott
// Added new fn LogFactorial (placed in IntOperations).
//
// Revision 1.2  2012/12/04 09:55:47  abbott
// Added new fns LeastNNegRemainder and SymmRemainder (with doc & new test).
// Some minor corrections to the doc (for operator/ and operator%).
//
// Revision 1.1  2012/05/28 09:18:21  abbott
// Created IntOperations which gathers together all operations on
// integers (both big and small).  Many consequential changes.
//
//

#endif
