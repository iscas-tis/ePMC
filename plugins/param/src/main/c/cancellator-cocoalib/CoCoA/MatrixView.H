#ifndef CoCoA_MatrixView_H
#define CoCoA_MatrixView_H

//   Copyright (c)  2005,2008,2014  John Abbott

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
// using std::vector;


namespace CoCoA
{

  class BigInt;           // fwd decl -- defined in BigInt.H
  class BigRat;           // fwd decl -- defined in BigRat.H
  class ConstMatrixView;  // fwd decl -- defined in matrix.H
  class RingElemAlias;    // fwd decl -- defined in ring.H
  typedef const RingElemAlias& ConstRefRingElem;
  class MachineInt;       // fwd decl -- defined in MachineInt.H
  class MatrixView;       // fwd decl -- defined in matrix.H
  class RingElem;         // fwd decl -- defined in ring.H
  class ring;             // fwd decl -- defined in ring.H

  MatrixView ZeroMat(const ring& R, long NumRows, long NumCols); // NumCols, NumCols > 0
  MatrixView IdentityMat(const ring& R, long dim); // dim > 0

  MatrixView transpose(MatrixView M);
  ConstMatrixView transpose(ConstMatrixView M);

  MatrixView submat(MatrixView M, const std::vector<long>& rows, const std::vector<long>& cols);
  ConstMatrixView submat(ConstMatrixView M, const std::vector<long>& rows, const std::vector<long>& cols);

  MatrixView ColMat(std::vector<RingElem>& v);
  ConstMatrixView ColMat(const std::vector<RingElem>& v);
  MatrixView RowMat(std::vector<RingElem>& v);
  ConstMatrixView RowMat(const std::vector<RingElem>& v);
  MatrixView DiagMat(std::vector<RingElem>& DiagEntries);
  ConstMatrixView DiagMat(const std::vector<RingElem>& DiagEntries);

  MatrixView MatByRows(long nrows, long ncols, std::vector<RingElem>& v);
  ConstMatrixView MatByRows(long nrows, long ncols, const std::vector<RingElem>& v);
  MatrixView MatByCols(long nrows, long ncols, std::vector<RingElem>& v);
  ConstMatrixView MatByCols(long nrows, long ncols, const std::vector<RingElem>& v);

//   MatrixView MatByRows(std::vector< std::vector<RingElem> >& vv);
//   ConstMatrixView MatByRows(const std::vector< std::vector<RingElem> >& vv);
//   MatrixView MatByCols(std::vector< std::vector<RingElem> >& vv);
//   ConstMatrixView MatByCols(const std::vector< std::vector<RingElem> >& vv);

  /***************************************************************************/
  // BlockMat2x2 -- block matrices  ( A  B )
  //                                ( C  D )
  MatrixView BlockMat2x2(MatrixView A, MatrixView B, MatrixView C, MatrixView D);
  ConstMatrixView BlockMat2x2(ConstMatrixView A, ConstMatrixView B, ConstMatrixView C, ConstMatrixView D);

  MatrixView ConcatVer(MatrixView M1, MatrixView M2);
  ConstMatrixView ConcatVer(ConstMatrixView M1, ConstMatrixView M2);
  MatrixView ConcatHor(MatrixView M1, MatrixView M2);
  ConstMatrixView ConcatHor(ConstMatrixView M1, ConstMatrixView M2);
  MatrixView ConcatDiag(MatrixView M1, MatrixView M2);
  ConstMatrixView ConcatDiag(ConstMatrixView M1, ConstMatrixView M2);
  MatrixView ConcatAntiDiag(MatrixView M1, MatrixView M2);
  ConstMatrixView ConcatAntiDiag(ConstMatrixView M1, ConstMatrixView M2);

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/MatrixView.H,v 1.1 2014/04/17 13:40:16 abbott Exp $
// $Log: MatrixView.H,v $
// Revision 1.1  2014/04/17 13:40:16  abbott
// Summary: Renamed from MatrixViews.H
// Author: JAA
//
// Revision 1.14  2014/04/10 15:41:11  abbott
// Summary: Removed FilledMat (matrix view)
// Author: JAA
//
// Revision 1.13  2013/05/31 14:52:03  abbott
// Added new views "MatByRows" and "MatByCols" (impl. is untested &  may be incomplete).
//
// Revision 1.12  2013/05/31 12:42:02  bigatti
// changed BlockMat into BlockMat2x2
// changed MultiBlockMat into BlockMat
//
// Revision 1.11  2012/10/24 11:42:39  abbott
// Revised fwd decl of ConstRefRingElem.
//
// Revision 1.10  2012/07/04 12:25:56  abbott
// Removed two useless comments.
//
// Revision 1.9  2011/11/09 13:47:56  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.8  2011/08/24 10:20:28  bigatti
// -- renamed QQ --> BigRat
//
// Revision 1.7  2011/08/14 15:52:18  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.6  2011/03/21 13:20:09  bigatti
// -- added FilledMat and necessary fwd declaration
//
// Revision 1.5  2011/03/09 07:58:19  bigatti
// -- changed row/col args into long (instead of MachineInt)
// -- added comments for row/col args "> 0"
//
// Revision 1.4  2011/03/04 10:22:42  bigatti
// -- added fwd def of MachineInt
//
// Revision 1.3  2011/03/03 13:50:22  abbott
// Replaced several occurrences of std::size_t by long; there's still more
// work to do though!
//
// Revision 1.2  2009/02/13 15:20:33  bigatti
// -- comment fix: BlockMatrix --> BlockMat
//
// Revision 1.1  2008/04/18 15:35:57  abbott
// (long overdue) Major revision to matrices
//
//

#endif
