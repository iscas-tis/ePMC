#ifndef CoCoA_UPoly_H
#define CoCoA_UPoly_H

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
#include "CoCoA/ring.H"

#include <vector>
//using std::ostream;

namespace CoCoA
{

  RingElem ChebyshevPoly(long n, ConstRefRingElem x); // first kind
  RingElem ChebyshevPoly2(long n, ConstRefRingElem x);// second kind

  void HornerRecursiveIterQQ2(BigInt& AnsN, const std::vector<BigInt>& c, const BigInt& n, const BigInt& d);

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/UPoly.H,v 1.1 2014/03/07 14:20:21 abbott Exp $
// $Log: UPoly.H,v $
// Revision 1.1  2014/03/07 14:20:21  abbott
// Summary: Added new UPoly (DUPZZ??) code
// Author: JAA
//
//

#endif
