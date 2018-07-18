#ifndef CoCoA_MatrixSpecial_H
#define CoCoA_MatrixSpecial_H

//   Copyright (c)  2011 Anna Bigatti

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


#include <cstddef>
//using std::size_t;
#include <vector>
// using std::vector

namespace CoCoA
{

  class matrix;          // forward declaration -- defined in matrix.H
  class RingElem;        // forward declaration -- defined in ring.H

  matrix jacobian(const std::vector<RingElem>& polys);
  matrix jacobian(const std::vector<RingElem>& polys, const std::vector<RingElem>& inds);

  matrix TensorMat(ConstMatrixView A, ConstMatrixView B); // or Kronecker product


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/MatrixSpecial.H,v 1.2 2011/03/21 07:58:30 bigatti Exp $
// $Log: MatrixSpecial.H,v $
// Revision 1.2  2011/03/21 07:58:30  bigatti
// -- added TensorMat
// -- changed size into len
//
// Revision 1.1  2011/02/10 15:30:14  bigatti
// -- first import: jacobian
//


#endif // CoCoA_MatrixSpecial_H
