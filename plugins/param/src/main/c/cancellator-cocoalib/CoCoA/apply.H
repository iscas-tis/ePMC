#ifndef CoCoA_apply_H
#define CoCoA_apply_H

//   Copyright (c)  2008  John Abbott

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

#include "CoCoA/matrix.H"
#include "CoCoA/ring.H"
#include "CoCoA/RingHom.H"

#include <algorithm>
// using std::transform;
#include <functional>
// using std::unary_function;
#include <iterator>
// using std::back_inserter;
#include <vector>
// using std::vector;

namespace CoCoA
{

  inline RingElem apply(const RingHom& phi, ConstRefRingElem x)
  {
    return phi(x);
  }

  // This class used to create last arg to std::transform.
  class ApplyRingHom: public std::unary_function<RingElem, ConstRefRingElem>
  {
  public:
    ApplyRingHom(const RingHom& phi): myHom(phi) {}
    RingElem operator()(ConstRefRingElem x) { return apply(myHom, x); }
  private: // data member
    const RingHom& myHom;
  };

  template<typename T>
  std::vector<T> apply(const RingHom& phi, const std::vector<T>& v)
  {
    std::vector<T> ans;
    std::transform(v.begin(), v.end(), std::back_inserter(ans), ApplyRingHom(phi));
    return ans;
  }

  matrix apply(RingHom phi, ConstMatrixView M);  // result is phi(M)

// NEED SOMETHING MORE GENERAL???
//   template<typename T>
//   matrix apply(const T& fn, ConstMatrixView M);


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/apply.H,v 1.5 2012/01/26 15:58:12 bigatti Exp $
// $Log: apply.H,v $
// Revision 1.5  2012/01/26 15:58:12  bigatti
// -- added #include <iterator>
//
// Revision 1.4  2009/12/03 17:31:55  abbott
// Added missing include directive for <functional>
//
// Revision 1.3  2008/12/16 21:05:54  abbott
// Updated licensing notice from GPL2 to GPL3+ -- evidently I forgot to change these
// files when I updated the others.
//
// Revision 1.2  2008/11/24 17:14:16  abbott
// Corrected definition of ApplyRingHom::operator().
//
// Revision 1.1  2008/11/23 18:33:15  abbott
// First attempt at implementing an apply function for applying maps (RingHoms)
// to pure structures.
//
//

#endif
