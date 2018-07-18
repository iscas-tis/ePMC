#ifndef CoCoA_MatrixForOrdering_H
#define CoCoA_MatrixForOrdering_H

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

#include <cstddef>
// using std::size_t;
#include <iosfwd>
//using std::ostream;
#include <vector>
// using std::vector;

namespace CoCoA
{

  class ring; // fwd decl -- defined in ring.H
  class ConstMatrixView; // fwd decl -- defined in matrix.H
  class matrix; // fwd decl -- defined in matrix.H

  bool IsTermOrdering(ConstMatrixView M);
  matrix NewPositiveMat(ConstMatrixView M); ///< returns a matrix with positive entries which defines an equivalent term-ordering
  matrix NewIntegerOrdMat(ConstMatrixView M); ///< returns a matrix with integer entries which defines an equivalent term-ordering
  bool IsPositiveGrading(ConstMatrixView M);  ///< is (all) M a positive grading?
  bool IsPositiveGrading(ConstMatrixView M, long GradingDim);  ///< are the first GradingDim rows of M a positive grading?


  matrix NewMatMinimize(ConstMatrixView M);///< the (ordering) matrix obtained by removing linearly dependent rows
  matrix NewDenseMatXel(long n);
  matrix NewDenseMatRevLex(long n);
  matrix NewDenseMatStdDegRevLex(long n);
  matrix NewDenseMatStdDegLex(long n);

  matrix NewMatCompleteOrd(ConstMatrixView M);
  matrix NewMatCompleteOrd(ConstMatrixView M, ConstMatrixView NewRows);

  matrix ElimMat(long NumIndets, const std::vector<long>& IndetsToElim);
  matrix ElimMat(ConstMatrixView GradingM, const std::vector<long>& IndetsToElim);
  matrix HomogElimMat(ConstMatrixView GradingM, const std::vector<long>& IndetsToElim);

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/MatrixForOrdering.H,v 1.14 2014/07/15 16:53:19 abbott Exp $
// $Log: MatrixForOrdering.H,v $
// Revision 1.14  2014/07/15 16:53:19  abbott
// Summary: Corrected name of include guard
// Author: JAA
//
// Revision 1.13  2013/05/27 13:10:41  abbott
// Removed unnecessary include of config.H
//
// Revision 1.12  2013/02/14 17:34:35  bigatti
// -- cleaned up code for elimination matrices
//
// Revision 1.11  2012/03/30 17:28:09  bigatti
// -- added NewIntegerOrdMat
// -- accepting and returning matrices over QQ
//
// Revision 1.10  2012/02/08 17:21:07  bigatti
// -- changed: Z,Q -> ZZ,QQ
// -- code reorganization (more work to do...)
//
// Revision 1.9  2011/12/05 16:54:40  bigatti
// -- changed: MachineInteger --> MachineInt (just in comment)
//
// Revision 1.8  2011/05/26 11:57:28  bigatti
// -- added IsPositiveGrading with one arg
//
// Revision 1.7  2011/04/26 10:10:31  bigatti
// -- added NewMatCompleteOrd
//
// Revision 1.6  2011/03/23 17:29:54  bigatti
// -- added NewDenseMatStdDegLex
//
// Revision 1.5  2011/03/21 07:50:51  bigatti
// -- added NewDenseMatXel, NewDenseMatStdDegRevLex
//
// Revision 1.4  2011/03/08 16:10:16  abbott
// Changed size_t into long.
//
// Revision 1.3  2009/09/22 13:35:55  bigatti
// -- following coding conventions in function names Matrix --> Mat
// -- forced all matrices to be over RingZ
//
// Revision 1.2  2008/12/17 12:11:52  abbott
// Changed type from long to MachineInt in operations which use a machine integer
// in place of a RingElem.  The change is "superficial" but affects many files.
//
// Revision 1.1  2008/04/21 11:23:11  abbott
// Separated functions dealing with matrices and PPOrderings into a new file.
// Added matrix norms, and completed adjoint.
//
//

#endif
