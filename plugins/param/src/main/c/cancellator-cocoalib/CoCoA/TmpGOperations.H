#ifndef CoCoA_GOperations_H
#define CoCoA_GOperations_H

//   Copyright (c)  2005  Massimo Caboara

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

#include "CoCoA/TmpGTypes.H"
#include "CoCoA/TmpGReductor.H"

//#include <vector>
// using vector;

// Operations depending on Groebner Bases


namespace CoCoA
{

  class ConstRefPPMonoidElem; // forward declaration -- defined in PPMonoid.H
  class RingElemAlias;        // forward declaration -- defined in ring.H
  typedef const RingElemAlias& ConstRefRingElem;     // forward declaration -- defined in ring.H
  class ModuleElem;           // forward declaration -- defined in PPMonoid.H
  class RingElem;             // forward declaration -- defined in ring.H

  void ComputeGBasis(PolyList& outGB, PolyList& outMinGens, const PolyList&,const int StatLevel=GReductor::ourDefaultStatLevel);
  void ComputeSATGBasis(PolyList&, const PolyList&,const int StatLevel=GReductor::ourDefaultStatLevel);
  void ComputeSATMixGBasis(PolyList&, const PolyList&,const int StatLevel=GReductor::ourDefaultStatLevel);
  void ComputeGBasis(PolyList& outGB, PolyList& outMinGens, Stats&, const PolyList&,const int StatLevel=GReductor::ourDefaultStatLevel);
  void ComputeSATGBasis(PolyList&, Stats&, const PolyList&,const int StatLevel=GReductor::ourDefaultStatLevel);
  void ComputeSATMixGBasis(PolyList&, Stats&, const PolyList&,const int StatLevel=GReductor::ourDefaultStatLevel);
  
  void ComputeGBasis(VectorList& outGB, VectorList& outMinGens, const VectorList& inGens);
  void ComputeGBasisFrameWork(PolyList&, const PolyList&);

// The second argument is the list of indexes of the
// indeterminates that will be eliminated
void ComputeElim(VectorList&,const VectorList&,ConstRefPPMonoidElem);///< Not yet implemented
void ComputeElim(PolyList&,const PolyList&,ConstRefPPMonoidElem);

  void ComputeSyz(VectorList&, const FreeModule&, const VectorList&);
  void ComputeSyz(VectorList&, const FreeModule&, const PolyList&);

void ComputeIntersection(VectorList&,const VectorList&,const VectorList&);
void ComputeIntersection(PolyList&,const PolyList&,const PolyList&);

void ComputeColonByPrincipal(PolyList&, const VectorList&,const VectorList&);
void ComputeColonByPrincipal(PolyList&, const PolyList&, ConstRefRingElem f);
void ComputeColonByPrincipal(VectorList&, const VectorList&,const PolyList&);

void ComputeCColon(PolyList&,const VectorList&,const VectorList&);
void ComputeCColon(PolyList&,const PolyList&,const PolyList& );
void ComputeCColon(VectorList&,const VectorList&,const PolyList&);

void ComputeSaturationByPrincipal(VectorList&,const VectorList&,const PolyList&);
void ComputeSaturationByPrincipal(PolyList&, const PolyList&, ConstRefRingElem);

void ComputeSSaturation(PolyList&,const PolyList&,const PolyList&);
void ComputeSSaturation(VectorList&,const VectorList&,const PolyList&);

void ComputeHomogenization(VectorList&,const VectorList&,const PolyList&);///< Not yet implemented
void ComputeHomogenization(PolyList&,const PolyList&,const PolyList&);///< Not yet implemented

bool RadicalMembership(const PolyList&,ConstRefRingElem);

void ComputeLT(VectorList&,const VectorList&);///< Not yet implemented
void ComputeLT(PolyList&,const PolyList&);

}// end namespace cocoa


// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/TmpGOperations.H,v 1.12 2014/03/26 15:23:44 bigatti Exp $
// $Log: TmpGOperations.H,v $
// Revision 1.12  2014/03/26 15:23:44  bigatti
// -- added MinGens for submodules
//
// Revision 1.11  2013/06/12 08:54:07  bigatti
// -- added computation of MinGens (in ComputeGBasis)
// -- changed some "the" in "out"/"in" in argument names
//
// Revision 1.10  2013/02/21 17:32:28  bigatti
// -- changed syntax for ComputeSyz
//
// Revision 1.9  2013/01/31 11:43:22  bigatti
// -- added Stats field to ComputeXXXGBasis for returning statistics
//
// Revision 1.8  2012/10/24 12:07:30  abbott
// Revised fwd decl of ConstRefRingElem.
//
// Revision 1.7  2012/10/03 12:22:01  bigatti
// -- added homog
//
// Revision 1.6  2011/12/07 15:54:34  bigatti
// -- renamed ambiguous "operator<" and hidden into anonymous namespace
// -- renamed ambiguous "operator==" into AreEqualLPPs (used by Saturation)
//
// Revision 1.5  2011/12/05 16:32:11  bigatti
// -- fixed bug about saturation (by non-principal ideal)
//
// Revision 1.4  2008/09/19 11:34:16  bigatti
// -- new mechanism for passing verbosity level (or StatLevel)
//    [only partially tested]
//
// Revision 1.3  2007/11/09 10:45:52  bigatti
// -- [caboara] preparation for self-saturating algorithm
//
// Revision 1.2  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1  2007/03/09 18:56:56  bigatti
// -- added Tmp prefix to Groebner related files
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.10  2007/03/08 18:42:06  cocoa
// Cleaned up whitespace.
//
// Revision 1.9  2007/03/07 22:24:03  bigatti
// -- reintroduced TmpGTypes.H (waiting for a better solution)
//
// Revision 1.8  2007/03/07 17:04:31  cocoa
// -- several changes by M.Caboara: more operations on ideals,
//    exception cleaner, coding conventions, WSugar, dynamic
//
// Revision 1.7  2006/12/21 13:48:33  cocoa
// Made all increment/decrement calls prefix (except where the must be postfix).
//
// Revision 1.6  2006/12/06 17:11:16  cocoa
// -- removed #include "config.H"
//
// Revision 1.5  2006/11/24 17:17:58  cocoa
// -- reorganized includes of header files
//
// Revision 1.4  2006/11/09 17:37:50  cocoa
// -- just some doxygen comments
//
// Revision 1.3  2006/10/06 16:46:17  cocoa
// -- syzygies for non-homogenous polynomials (Max)
// -- wip: evolution of Groebner Framework (Max)
//
// Revision 1.2  2006/10/06 14:04:16  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//
// Revision 1.1.1.1  2006/05/30 11:39:36  cocoa
// Imported files
//
// Revision 1.11  2006/05/16 08:59:16  cocoa
// -- added function for interactive Groebner
//
// Revision 1.10  2006/05/11 16:00:22  cocoa
// -- fixed spelling of "homogenize"
//
// Revision 1.9  2006/04/27 14:01:11  cocoa
// -- tidied up include files (using GTypes.H)
//
// Revision 1.8  2006/04/21 16:47:06  cocoa
// -- new syntax for ComputeGBasis by Max
//
// Revision 1.7  2006/04/11 16:22:40  cocoa
// -- added: Elim, LT
//

#endif
