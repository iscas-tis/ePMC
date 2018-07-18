#ifndef CoCoA_BigRat_H
#define CoCoA_BigRat_H

//   Copyright (c)  2009-2010  John Abbott

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

#include "gmp.h"
// #include <iosfwd> --- already included in BigInt.H
//using std::ostream;
//using std::istream;

namespace CoCoA
{

  class MachineInt;      // fwd decl -- defined in MachineInt.H
  class OpenMathOutput;  // fwd decl -- defined in OpenMath.H
  class OpenMathInput;   // fwd decl -- defined in OpenMath.H

  class BigRat
  {
  private:
    friend mpq_t& mpqref(BigRat& N);
    friend const mpq_t& mpqref(const BigRat& N);
    mpq_t myRep;

  public:
    BigRat();
    explicit BigRat(const mpq_t q); // copies the mpq_t value
    enum ReduceFlag { NotReduced=0, AlreadyReduced=1 };
    BigRat(const MachineInt& n1, const MachineInt& n2, ReduceFlag status=NotReduced);  ///< creates the fraction n1/n2
    BigRat(const MachineInt& n1, const BigInt& N2, ReduceFlag status=NotReduced);  ///< creates the fraction n1/N2
    BigRat(const BigInt& N1, const MachineInt& n2, ReduceFlag status=NotReduced);  ///< creates the fraction N1/n2
    BigRat(const BigInt& N1, const BigInt& N2, ReduceFlag status=NotReduced);  ///< creates the fraction N1/N2
    explicit BigRat(const std::string& str, ReduceFlag status=NotReduced);
    BigRat(const BigRat& from);
// BigRat& clone() const;

    ~BigRat();

  private:
    void myAssign(const BigInt& N1, const BigInt& N2, ReduceFlag status=NotReduced); ///< does the work for the ctors
  public:

    BigRat& operator=(const BigRat& rhs);

    // -------- functions that modify at least one argument or `*this' ----------

    BigRat& operator+=(const BigRat& rhs);
    BigRat& operator-=(const BigRat& rhs);
    BigRat& operator*=(const BigRat& rhs);
    BigRat& operator/=(const BigRat& rhs);
  			
    // Same but with RHS a BigInt...
    BigRat& operator= (const BigInt& rhs);
    BigRat& operator+=(const BigInt& rhs);
    BigRat& operator-=(const BigInt& rhs);
    BigRat& operator*=(const BigInt& rhs);
    BigRat& operator/=(const BigInt& rhs);
  			
    // Same but with RHS a MachineInt...
    BigRat& operator= (const MachineInt& rhs);
    BigRat& operator+=(const MachineInt& rhs);
    BigRat& operator-=(const MachineInt& rhs);
    BigRat& operator*=(const MachineInt& rhs);
    BigRat& operator/=(const MachineInt& rhs);


    const BigRat& operator++( );
    const BigRat& operator--( );
    const BigRat operator++(int); // INEFFICIENT
    const BigRat operator--(int); // INEFFICIENT

  };


  // I/O FUNCTIONS

  std::ostream& operator<<(std::ostream& out, const BigRat& Q);
  std::istream& operator>>(std::istream& in, BigRat& Q);

  OpenMathOutput& operator<<(OpenMathOutput& OMOut, const BigRat& Q);
  OpenMathInput& operator>>(OpenMathInput& OMIn, BigRat& Q);


  // STANDARD ARITHMETIC OPERATIONS

  void swap(BigRat& a, BigRat& b);

  const BigInt num(const BigRat& Q);
  const BigInt den(const BigRat& Q);
  const BigRat abs(const BigRat& Q);
  const BigRat operator-(const BigRat& Q);

  const BigRat operator+(const BigRat& Q1, const BigRat& Q2);
  const BigRat operator-(const BigRat& Q1, const BigRat& Q2);
  const BigRat operator*(const BigRat& Q1, const BigRat& Q2);
  const BigRat operator/(const BigRat& Q1, const BigRat& Q2);

  const BigRat operator+(const BigRat& Q, const BigInt& N);
  const BigRat operator-(const BigRat& Q, const BigInt& N);
  const BigRat operator*(const BigRat& Q, const BigInt& N);
  const BigRat operator/(const BigRat& Q, const BigInt& N);

  const BigRat operator+(const BigInt& N, const BigRat& Q);
  const BigRat operator-(const BigInt& N, const BigRat& Q);
  const BigRat operator*(const BigInt& N, const BigRat& Q);
  const BigRat operator/(const BigInt& N, const BigRat& Q);

  const BigRat operator+(const BigRat& Q, const MachineInt& n);
  const BigRat operator-(const BigRat& Q, const MachineInt& n);
  const BigRat operator*(const BigRat& Q, const MachineInt& n);
  const BigRat operator/(const BigRat& Q, const MachineInt& n);

  const BigRat operator+(const MachineInt& n, const BigRat& Q);
  const BigRat operator-(const MachineInt& n, const BigRat& Q);
  const BigRat operator*(const MachineInt& n, const BigRat& Q);
  const BigRat operator/(const MachineInt& n, const BigRat& Q);

  const BigRat power(const BigRat& base, const BigInt& exponent);
  const BigRat power(const BigRat& base, const MachineInt& exponent);


  bool IsZero(const BigRat& Q);
  bool IsOne(const BigRat& Q);
  bool IsMinusOne(const BigRat& Q);
  bool IsOneNum(const BigRat& Q);
  bool IsOneDen(const BigRat& Q);
  int sign(const BigRat& Q);

  // COMPARISON FUNCTIONS

  int cmp(const BigRat& Q1, const BigRat& Q2);
  int cmp(const BigRat& Q, const BigInt& N);
  int cmp(const BigInt& N, const BigRat& Q);
  int cmp(const BigRat& Q, const MachineInt& n);
  int cmp(const MachineInt& n, const BigRat& Q);

  int CmpAbs(const BigRat& Q1, const BigRat& Q2);
  int CmpAbs(const BigRat& Q, const BigInt& N);
  int CmpAbs(const BigInt& N, const BigRat& Q);
  int CmpAbs(const BigRat& Q, const MachineInt& n);
  int CmpAbs(const MachineInt& n, const BigRat& Q);

  bool operator==(const BigRat& Q1, const BigRat& Q2);
  bool operator!=(const BigRat& Q1, const BigRat& Q2);
  bool operator> (const BigRat& Q1, const BigRat& Q2);
  bool operator>=(const BigRat& Q1, const BigRat& Q2);
  bool operator< (const BigRat& Q1, const BigRat& Q2);
  bool operator<=(const BigRat& Q1, const BigRat& Q2);
			
  bool operator==(const BigRat& Q, const BigInt& N);
  bool operator!=(const BigRat& Q, const BigInt& N);
  bool operator> (const BigRat& Q, const BigInt& N);
  bool operator>=(const BigRat& Q, const BigInt& N);
  bool operator< (const BigRat& Q, const BigInt& N);
  bool operator<=(const BigRat& Q, const BigInt& N);
			
  bool operator==(const BigInt& N, const BigRat& Q);
  bool operator!=(const BigInt& N, const BigRat& Q);
  bool operator> (const BigInt& N, const BigRat& Q);
  bool operator>=(const BigInt& N, const BigRat& Q);
  bool operator< (const BigInt& N, const BigRat& Q);
  bool operator<=(const BigInt& N, const BigRat& Q);
			
  bool operator==(const BigRat& Q, const MachineInt& n);
  bool operator!=(const BigRat& Q, const MachineInt& n);
  bool operator> (const BigRat& Q, const MachineInt& n);
  bool operator>=(const BigRat& Q, const MachineInt& n);
  bool operator< (const BigRat& Q, const MachineInt& n);
  bool operator<=(const BigRat& Q, const MachineInt& n);
      		
  bool operator==(const MachineInt& n, const BigRat& Q);
  bool operator!=(const MachineInt& n, const BigRat& Q);
  bool operator> (const MachineInt& n, const BigRat& Q);
  bool operator>=(const MachineInt& n, const BigRat& Q);
  bool operator< (const MachineInt& n, const BigRat& Q);
  bool operator<=(const MachineInt& n, const BigRat& Q);
			

  // MISCELLANEOUS FUNCTIONS

  double mantissa(const BigRat& Q);
  long exponent(const BigRat& Q);
  double log(const BigRat& Q);
  long ILogBase(const BigRat& Q, const MachineInt& base);
  long ILogBase(const BigRat& Q, BigInt base);

  BigInt floor(const BigRat& Q);   ///< greatest integer <= Q
  BigInt ceil(const BigRat& Q);    ///< least integer >= Q
  BigInt round(const BigRat& Q);   ///< round rational Q to nearest integer (halves round toward +infinity)


  // Inline fns

  inline mpq_t& mpqref(BigRat& Q)
  {
    return Q.myRep;
  }

  inline const mpq_t& mpqref(const BigRat& Q)
  {
    return Q.myRep;
  }


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/BigRat.H,v 1.7 2014/07/15 16:49:34 abbott Exp $
// $Log: BigRat.H,v $
// Revision 1.7  2014/07/15 16:49:34  abbott
// Summary: Corrected include guard capitalization
// Author: JAA
//
// Revision 1.6  2014/06/14 19:25:08  abbott
// Summary: Added new fn CmpAbs (for BigRat)
// Author: JAA
//
// Revision 1.5  2014/05/02 13:55:29  abbott
// Summary: Replaced include by fwd decl
// Author: JAA
//
// Revision 1.4  2014/01/28 09:57:21  abbott
// Added option 2nd arg for conversion string -> BigRat to say whether the result
// should be "canonicalized".
//
// Revision 1.3  2013/05/20 15:50:20  abbott
// Added new ctor for BigRat from mpq_t.
//
// Revision 1.2  2011/11/09 13:38:31  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.1  2011/08/24 10:16:38  bigatti
// -- was file/class QQ
//
// ------- log for old file/class name: QQ.H --------------
// Revision 1.11  2011/08/14 15:52:18  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.10  2011/08/02 11:28:20  abbott
// Fixed typo in comment.
//
// Revision 1.9  2011/06/23 16:01:07  abbott
// Removed single arg ctor QQ(MachineInt), & consequential changes.
//
// Revision 1.8  2011/02/25 12:06:51  abbott
// Added new fn IsOneNum; also some minor code cleaning in QQ.C
//
// Revision 1.7  2010/12/26 13:03:16  abbott
// Added ILogBase function (to ZZ & QQ).
//
// Revision 1.6  2010/05/07 14:57:52  abbott
// Two main changes:
//   power(QQ,ZZ) now allows negative exponent
//   renamed QQ::AlreadyNormalized to QQ::AlreadyReduced
//           (and allowed denoms to be negative; the ctor then makes them positive).
//
// Revision 1.5  2010/03/22 11:49:28  abbott
// Added ctor from a string.
//
// Revision 1.4  2010/03/18 16:34:10  abbott
// Added new pseudo-ctors for QQ with optional flag to indicate that value is already normalized.
// Added OpenMath I/O operators.
//
// Revision 1.3  2009/11/26 16:12:54  bigatti
// -- commented out some includes
//
// Revision 1.2  2009/07/08 12:26:53  abbott
// Added floor and ceil functions for QQs.
// Added example program for QQs.
// Minor correction to convert.C; minor cleaning to ex-ZZ1.C
//
// Revision 1.1  2009/07/02 16:29:42  abbott
// Added new class QQ to represent rational numbers.
// Consequent change to the Makefile.
//
//

#endif
