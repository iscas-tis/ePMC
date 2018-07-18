#ifndef CoCoA_MachineInt_H
#define CoCoA_MachineInt_H

//   Copyright (c)  2007,2008  John Abbott

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


#include "CoCoA/assert.H"
#include "CoCoA/ULong2Long.H"

#include <limits>
//using std::numeric_limits;
#include <iosfwd>
//using std::ostream;

namespace CoCoA
{

  // This class is deliberately inline.
  class MachineInt
  {
  public:
    MachineInt(signed char n): myValue(n), IamNegative(n<0) {}
    MachineInt(unsigned char n): myValue(n), IamNegative(false) {}
    MachineInt(short n): myValue(n), IamNegative(n<0) {}
    MachineInt(unsigned short n): myValue(n), IamNegative(false) {}
    MachineInt(int n): myValue(n), IamNegative(n<0) {}
    MachineInt(unsigned int n): myValue(n), IamNegative(false) {}
    MachineInt(long n): myValue(n), IamNegative(n<0) {}
    MachineInt(unsigned long n): myValue(n), IamNegative(false) {}
    friend bool IsZero(const MachineInt& n);
    friend bool IsNegative(const MachineInt& n);
    friend bool IsSignedLong(const MachineInt& n);
    friend unsigned long AsUnsignedLong(const MachineInt& n);
    friend long AsSignedLong(const MachineInt& n);
    friend unsigned long abs(const MachineInt& n);
  private: // data members
    unsigned long myValue;
    bool IamNegative;
  };

  bool IsInRange(const MachineInt& lwb, const MachineInt& val, const MachineInt& upb);   // Checks that  lwb <= val <= upb

  inline bool IsZero(const MachineInt& n)
  { return n.myValue == 0; }

  inline  bool IsNegative(const MachineInt& n)
  { return n.IamNegative; }

  inline  bool IsSignedLong(const MachineInt& n)
  { return n.IamNegative || (n.myValue <= static_cast<unsigned long>(std::numeric_limits<long>::max())); }

  inline unsigned long AsUnsignedLong(const MachineInt& n)
  {
    CoCoA_ASSERT(!n.IamNegative);
    return n.myValue;
  }

  inline long AsSignedLong(const MachineInt& n)
  {
    CoCoA_ASSERT(IsSignedLong(n));
    return ULong2Long(n.myValue);
  }

  inline unsigned long abs(const MachineInt& n)
  { if (n.IamNegative) return -ULong2Long(n.myValue); else return n.myValue; }

  std::ostream& operator<<(std::ostream& out, const MachineInt& n);

} // end of namespace CoCoA


// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/MachineInt.H,v 1.3 2013/02/15 16:31:19 abbott Exp $
// $Log: MachineInt.H,v $
// Revision 1.3  2013/02/15 16:31:19  abbott
// Moved IsInRange here from "convert".
//
// Revision 1.2  2012/05/28 09:18:21  abbott
// Created IntOperations which gathers together all operations on
// integers (both big and small).  Many consequential changes.
//
// Revision 1.1  2011/11/09 13:38:58  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.10  2011/09/06 13:39:59  abbott
// Changed include directive: now includes ULong2Long.H instead of utils.H
//
// Revision 1.9  2011/08/27 20:48:02  abbott
// Inserted forgotten calls to ULong2Long.
//
// Revision 1.8  2010/03/05 18:39:49  abbott
// Added SmallPower function -- currently undefined behaviour if overflow occurs!!
//
// Revision 1.7  2009/10/08 13:45:58  abbott
// Added new "RoundDiv" function.
//
// Revision 1.6  2009/06/05 12:14:55  abbott
// Major change:
//   created new files NumTheory.H/C  which contain basic number theory operations
//   removed several basic number theory operations from ZZ.H/C
//   removed gcd from MachineInt.H/C
//   changed names of some basic fns:
//      IsPPrime -> IsProbPrime
//      invmod -> InvMod    (changed signature too)
//      powermod -> PowerMod  (changed signature too)
//   added new fns
//      NextProbPrime & PrevProbPrime
//   consequent changes to other code and tests and examples
//
// Revision 1.5  2008/12/12 16:32:49  abbott
// Improved comments.
//
// Revision 1.4  2008/12/11 20:06:47  abbott
// Added IsSignedLong function; needed elsewhere, and should be present anyway.
//
// Revision 1.3  2008/12/11 10:47:36  abbott
// Fixed bug in IsZero (it appeared only when CoCoA_DEBUG was set).
// Some cleaning.
//
// Revision 1.2  2007/10/30 17:14:12  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1  2007/05/21 12:57:28  abbott
// New class for passing machine integers as args; includes some simple
// operations on machine integers (cmp, gcd, IsNegative,...).  Operations
// between ZZ and machine integers modified to use the new class.  Inexact
// integer division (of a ZZ) by a negative value now triggers an error;
// new error for reporting inexact integer division by a negative value.
//
//

#endif
