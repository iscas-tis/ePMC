#ifndef CoCoA_PPMonoidEvZZ_H
#define CoCoA_PPMonoidEvZZ_H

//   Copyright (c)  2007  John Abbott

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


#include <vector>
// using vector;

namespace CoCoA
{

  class symbol;         // forward declaration -- defined in symbol.H
  class PPMonoid;       // forward declaration -- defined in PPMonoid.H
  class PPOrdering;     // forward declaration -- defined in PPOrdering.H
  class PPOrderingCtor; // forward declaration -- defined in PPOrdering.H

  // Pseudo-ctor
  PPMonoid NewPPMonoidBigEv(const std::vector<symbol>& IndetNames, const PPOrdering& ord);
  PPMonoid NewPPMonoidBigEv(const std::vector<symbol>& IndetNames, const PPOrderingCtor& ord);

} // end of namespace CoCoA

//----------------------------------------------------------------------
// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/PPMonoidEvZZ.H,v 1.4 2011/08/14 15:52:18 abbott Exp $
// $Log: PPMonoidEvZZ.H,v $
// Revision 1.4  2011/08/14 15:52:18  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.3  2010/02/03 16:13:52  abbott
// Added new single word tags for specifying the ordering in PPMonoid
// pseudo-ctors.
//
// Revision 1.2  2007/10/30 17:14:12  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1  2007/05/03 10:35:23  abbott
// Added new PPMonoidEvZZ with (virtually) unlimited exponents.
// Modified test-PPMonoid1.C accordingly.
// Added warning in doc about silent/unchecked exponent overflow in other
// PPMonoids.
//

#endif
