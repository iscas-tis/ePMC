#ifndef CoCoA_FloatApprox_H
#define CoCoA_FloatApprox_H

//   Copyright (c)  2014  John Abbott

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

#include <iosfwd>
//using std::ostream;


namespace CoCoA
{

  class BigRat; // fwd decl -- defined in BigRat.H
  class RingElem; // fwd decl -- defined in ring.H

  class MantExp2
  {
  public:
    MantExp2(int s, long e, const BigInt& m, long NumDigits): mySign(s), myExponent(e), myMantissa(m), myNumDigits(NumDigits) {}
    int mySign;
    long myExponent;
    BigInt myMantissa;
    long myNumDigits;

    static const int ourDefaultMantBits; // actual value is in FloatApprox.C
  };

  std::ostream& operator<<(std::ostream& out, const MantExp2& ME);


  MantExp2 MantissaAndExponent2(const MachineInt& n, const MachineInt& MantBits = MantExp2::ourDefaultMantBits);
  MantExp2 MantissaAndExponent2(const BigInt& N, const MachineInt& MantBits = MantExp2::ourDefaultMantBits);
  MantExp2 MantissaAndExponent2(const BigRat& q, const MachineInt& MantBits = MantExp2::ourDefaultMantBits);
  MantExp2 MantissaAndExponent2(const RingElem& x); // only for x in RingTwinFloat, def'd in RingTwinFloat.C

  BigRat FloatApprox(const MachineInt& n, const MachineInt& MantBits = MantExp2::ourDefaultMantBits);
  BigRat FloatApprox(const BigInt& n, const MachineInt& MantBits = MantExp2::ourDefaultMantBits);
  BigRat FloatApprox(const BigRat& n, const MachineInt& MantBits = MantExp2::ourDefaultMantBits);

  //------------------------------------------------------------------
  // Decimal "floating point" representation

  class MantExp10
  {
  public:
    MantExp10(int s, long e, const BigInt& m, long NumDigits): mySign(s), myExponent(e), myMantissa(m), myNumDigits(NumDigits) {}
    int mySign;
    long myExponent;
    BigInt myMantissa;
    long myNumDigits;

    static const int ourDefaultSigFig; // actual value is in decimal.C
  };

  std::ostream& operator<<(std::ostream& out, const MantExp10& ME);

  MantExp10 MantissaAndExponent10(const BigInt& N, const MachineInt& SigFig = MantExp10::ourDefaultSigFig);
  MantExp10 MantissaAndExponent10(const BigRat& N, const MachineInt& SigFig = MantExp10::ourDefaultSigFig);


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/FloatApprox.H,v 1.4 2014/07/30 14:27:15 abbott Exp $
// $Log: FloatApprox.H,v $
// Revision 1.4  2014/07/30 14:27:15  abbott
// Summary: Added MantExp2 for RingElem
// Author: JAA
//
// Revision 1.3  2014/05/14 10:51:16  abbott
// Summary: Added new field myNumDigits to MantExp2 and MantExp10
// Author: JAA
//
// Revision 1.2  2014/04/11 13:32:27  abbott
// Summary: Added MantissaAndExponent2 and MantissaAndExponent10
// Author: JAA
//
// Revision 1.1  2014/04/10 15:32:10  abbott
// Summary: New fn FloatApprox
// Author: JAA
//
//

#endif
