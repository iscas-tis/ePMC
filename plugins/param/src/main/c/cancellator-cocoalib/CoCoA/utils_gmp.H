#ifndef CoCoA_utils_gmp_H
#define CoCoA_utils_gmp_H

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

#include "gmp.h"

namespace CoCoA
{

  int mpq_cmpabs(const mpq_t q1, const mpq_t q2);

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/utils_gmp.H,v 1.2 2014/06/17 10:04:21 abbott Exp $
// $Log: utils_gmp.H,v $
// Revision 1.2  2014/06/17 10:04:21  abbott
// Summary: Corrected include directive (gmp.H -> gmp.h)
// Author: JAA
//
// Revision 1.1  2014/06/13 12:08:39  abbott
// Summary: Header for new GMP fn (CmpAbs for rationals)
// Author: JAA
//
//

#endif
