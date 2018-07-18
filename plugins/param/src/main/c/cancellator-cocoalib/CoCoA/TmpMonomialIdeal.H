#ifndef CoCoA_MonomialIdeal_H
#define CoCoA_MonomialIdeal_H

//   Copyright (c)  2011  Anna Bigatti

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


// Header file for operations on monomial ideals

//#include "CoCoA/PPMonoid.H"
//#include "CoCoA/PolyRing.H"
//#include "CoCoA/RingHom.H"
//#include "CoCoA/error.H"
//#include "CoCoA/ideal.H"

#include <iosfwd>
//using std::ostream;
#include <vector>
//using std::vector;
#include <memory>
// using std::auto_ptr;


namespace CoCoA
{

  class ideal;        // fwd decl -- defined in ideal.H 

// this is just a placeholder:
// the member functions still depend on SparsePolyRing::IdealImpl
// therefore they are declared in SparsePolyRing.H

//  virtual void myIntersectMonId(const ideal&);
//  virtual void myColonMonInd(const ideal&);
//  virtual void myMulMonId(const ideal&);
//  virtual void myElimMonId(const std::vector<RingElem>& ELimIndets);
//  virtual const std::vector<RingElem>& myGBasisMonId() const; // interreduced
  std::vector<ideal> PrimaryDecompositionMonId(const ideal& I);
  ideal AlexanderDual(const ideal& I);

} // end of namespace CoCoA

// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/TmpMonomialIdeal.H,v 1.3 2014/02/25 16:28:30 abbott Exp $
// $Log: TmpMonomialIdeal.H,v $
// Revision 1.3  2014/02/25 16:28:30  abbott
// Summary: Commented out useless includes; added a fwd decl
// Author: JAA
//
// Revision 1.2  2011/07/05 15:02:17  bigatti
// -- added AlexanderDual
// -- added ad-hoc functions for colon, elim on monomial ideals
//
// Revision 1.1  2011/06/27 13:26:51  bigatti
// -- first import (soem functions were in SparsePolyRing)
//

#endif
