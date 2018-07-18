//   Copyright (c) 2007 Alberto Arri

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

#ifndef CoCoA_F5_H
#define CoCoA_F5_H

#include "CoCoA/ring.H"
#include "CoCoA/PPMonoid.H"
#include "CoCoA/SparsePolyRing.H"

namespace CoCoA
{

  namespace F5ns{
    
  struct F5opt_t{
    bool incremental,skip_rows,prev_red, use_NR, GBLT2SYZLT, checkGB, verbose;
    F5opt_t(){
      incremental = true; //default = true
      skip_rows = true;   //default = true
      prev_red = true;    //default = true
      use_NR = false;     //makes sense only if skip_rows = true
      GBLT2SYZLT = true;  //default = true
      checkGB = false;    //default = false
      verbose = false;
    }
  };
    
  }

  void F5(std::vector<CoCoA::RingElem>& GB, const std::vector<CoCoA::RingElem>& I, 
	  const F5ns::F5opt_t &F5opt = F5ns::F5opt_t());

  inline void F5(std::vector<CoCoA::RingElem>& GB, const CoCoA::ideal &I)
  {
    F5(GB, gens(I));
  }

} // end of namespace CoCoA


// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/TmpF5.H,v 1.6 2014/07/31 14:45:16 abbott Exp $
// $Log: TmpF5.H,v $
// Revision 1.6  2014/07/31 14:45:16  abbott
// Summary: Merged io.H and UtilsTemplate.H into new header VectorOperations.H
// Author: JAA
//
// Revision 1.5  2008/07/09 16:07:50  abbott
// Cosmetic tidying.
//
// Revision 1.4  2007/11/20 10:01:26  bigatti
// -- change: TmpF5.C --> TmpF5Mat.C  (by Alberto Arri)
// -- updated and improved test-F5.C
//
// Revision 1.3  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.2  2007/03/13 10:35:30  abbott
// Moved F5 code into namespace CoCoA (previously it was at global level).
// Improved spacing and layout (previously it was haphazard).
//

#endif
