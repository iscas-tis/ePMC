#ifndef CoCoA_MatrixOperations_H
#define CoCoA_MatrixOperations_H

//   Copyright (c)  2005,2008,2012  John Abbott

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
// using std::vector

namespace CoCoA
{

  class BigInt;          // fwd decl -- defined in BigInt.H
  class BigRat;          // fwd decl -- defined in BigRat.H
  class ConstMatrixView; // fwd decl -- defined in matrix.H
  class RingElemAlias   ;// fwd decl -- defined in ring.H
  typedef const RingElemAlias& ConstRefRingElem;
  class MachineInt;      // fwd decl -- defined in MachineInt.H
  class MatrixView;      // fwd decl -- defined in matrix.H
  class RingElem;        // fwd decl -- defined in ring.H
  class matrix;          // fwd decl -- defined in matrix.H
  class ring;            // fwd decl -- defined in ring.H

  matrix operator*(ConstMatrixView Mleft, ConstMatrixView Mright);
  matrix operator+(ConstMatrixView Mleft, ConstMatrixView Mright);
  matrix operator-(ConstMatrixView Mleft, ConstMatrixView Mright);
  void mul(matrix& lhs, ConstMatrixView M1, ConstMatrixView M2);
  matrix power(ConstMatrixView M, long n);
  matrix power(ConstMatrixView M, const BigInt& N);

  matrix operator*(ConstRefRingElem x, ConstMatrixView M);
  matrix operator*(const BigRat& x, ConstMatrixView M);
  matrix operator*(const BigInt& x, ConstMatrixView M);
  matrix operator*(const MachineInt& x, ConstMatrixView M);
  matrix operator*(ConstMatrixView M, ConstRefRingElem x);
  matrix operator*(ConstMatrixView M, const BigRat& x);
  matrix operator*(ConstMatrixView M, const BigInt& x);
  matrix operator*(ConstMatrixView M, const MachineInt& x);
  matrix operator-(const ConstMatrixView& M);

  matrix operator/(ConstMatrixView M, ConstRefRingElem x);
  matrix operator/(ConstMatrixView M, const BigRat& x);
  matrix operator/(ConstMatrixView M, const BigInt& x);
  matrix operator/(ConstMatrixView M, const MachineInt& x);

  RingElem FrobeniusNorm2(ConstMatrixView A);
  RingElem OperatorNormInfinity(ConstMatrixView M);
  RingElem OperatorNorm1(ConstMatrixView M);

  RingElem det(const ConstMatrixView& M);
  long rank(const ConstMatrixView& M);
  matrix inverse(ConstMatrixView M);
  matrix adjoint(ConstMatrixView M);
  matrix PseudoInverse(ConstMatrixView M);
  matrix LinSolve(ConstMatrixView M, ConstMatrixView rhs);
  matrix LinSolveByGauss(ConstMatrixView M, ConstMatrixView rhs);
  matrix LinSolveByHNF(ConstMatrixView M, ConstMatrixView rhs);
  matrix LinSolveByModuleRepr(ConstMatrixView M, ConstMatrixView rhs);
  matrix LinKer(ConstMatrixView M);
  matrix LinKerByGauss(ConstMatrixView M);

  void det2x2(RingElem& d, ConstMatrixView M);
  void det3x3(RingElem& d, ConstMatrixView M);
  void DetByGauss(RingElem& d, ConstMatrixView M);
  RingElem DetByBareiss(const ConstMatrixView& M);
  long RankByGauss(std::vector<long>& IndepRows, ConstMatrixView M);
  matrix InverseByGauss(ConstMatrixView M);
  matrix AdjointByDetOfMinors(ConstMatrixView M);
  matrix AdjointByInverse(ConstMatrixView M); // base ring must be integral domain

  bool IsZero(const ConstMatrixView& M);
  bool IsZeroRow(const ConstMatrixView& M, long i);
  bool IsZeroCol(const ConstMatrixView& M, long j);
  bool IsSymmetric(const ConstMatrixView& M);
  bool IsAntiSymmetric(const ConstMatrixView& M);
  bool IsDiagonal(const ConstMatrixView& M);
  bool IsMat0x0(const ConstMatrixView& M);

//   void GramSchmidtRows(MatrixView& M);
//   void GramSchmidtRows(MatrixView& M, long row);

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/MatrixOperations.H,v 1.3 2014/08/26 12:56:14 abbott Exp $
// $Log: MatrixOperations.H,v $
// Revision 1.3  2014/08/26 12:56:14  abbott
// Summary: Added DetByBareiss
// Author: JAA
//
// Revision 1.2  2014/04/17 14:09:46  bigatti
// -- moved Is... functions from matrix.H to here
//
// Revision 1.1  2014/04/11 15:42:37  abbott
// Summary: Renamed from MatrixArith
// Author: JAA
//
// Revision 1.23  2012/10/24 11:42:18  abbott
// Revised fwd decl of ConstRefRingElem.
//
// Revision 1.22  2012/10/16 09:45:45  abbott
// Replaced RefRingElem by RingElem&.
//
// Revision 1.21  2012/07/10 12:00:47  bigatti
// -- added LinKer, LinKerByGauss
//
// Revision 1.20  2012/06/19 15:43:27  abbott
// Added division of a matrix by a scalar.
//
// Revision 1.19  2012/06/11 08:20:33  abbott
// Added multiplication on the right by a scalar.
//
// Revision 1.18  2012/06/10 22:57:31  abbott
// Added negation for matrices -- same as doing (-1)*M.
//
// Revision 1.17  2012/04/27 14:49:34  abbott
// Added LinSolve family (incl. LinSolveByGauss, LinSolveByHNF, LinSolveByModuleRepr).
//
// Revision 1.16  2012/04/13 16:24:35  abbott
// Added solve and SolveByGauss.
//
// Revision 1.15  2011/11/09 13:47:56  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.14  2011/08/24 10:20:28  bigatti
// -- renamed QQ --> BigRat
//
// Revision 1.13  2011/08/14 15:52:18  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.12  2011/05/13 16:47:20  abbott
// Added power fn for matrices: partial impl, cannot yet handle negative powers.
//
// Revision 1.11  2011/03/03 13:50:22  abbott
// Replaced several occurrences of std::size_t by long; there's still more
// work to do though!
//
// Revision 1.10  2011/03/01 14:13:24  bigatti
// -- added f*M
//
// Revision 1.9  2011/02/28 14:08:49  bigatti
// -- added det3x3
// -- using apply mapping matrix (in DetByGauss)
//
// Revision 1.8  2011/02/18 12:54:59  bigatti
// -- commented out GrammSchmidtRows
//
// Revision 1.7  2011/02/09 16:48:27  bigatti
// -- added + and - for matrices
//
// Revision 1.6  2008/04/21 11:23:11  abbott
// Separated functions dealing with matrices and PPOrderings into a new file.
// Added matrix norms, and completed adjoint.
//
// Revision 1.5  2008/04/18 15:35:57  abbott
// (long overdue) Major revision to matrices
//
// Revision 1.4  2008/04/16 17:24:17  abbott
// Further cleaning of the new matrix code.  Updated documentation too.
//
// Revision 1.3  2008/04/08 15:26:42  abbott
// Major revision to matrix implementation: added matrix views.
// Lots of changes.
//
// Revision 1.2  2007/10/30 17:14:12  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.8  2007/03/08 18:42:06  cocoa
// Cleaned up whitespace.
//
// Revision 1.7  2007/03/07 13:07:10  cocoa
// -- minor: added comment for NewMatrixMinimize
//
// Revision 1.6  2006/12/06 17:14:12  cocoa
// -- removed #include "config.H"
//
// Revision 1.5  2006/11/24 17:27:46  cocoa
// -- reorganized includes of header files
//
// Revision 1.4  2006/10/06 14:04:15  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//
// Revision 1.3  2006/08/17 09:39:08  cocoa
// -- added: elimination ordering matrix for non-homogeneous input
//
// Revision 1.2  2006/07/17 16:58:05  cocoa
// -- added: NewMatrixElim(size_t NumIndets, std::vector<size_t> IndetsToElim)
//
// Revision 1.1.1.1  2006/05/30 11:39:37  cocoa
// Imported files
//
// Revision 1.4  2006/04/10 13:20:43  cocoa
// -- fixed buglets for Elimination orderings
//
// Revision 1.3  2006/04/05 17:18:33  cocoa
// -- added IsPositiveGrading
//
// Revision 1.2  2006/03/27 12:21:26  cocoa
// Minor silly changes to reduce number of complaints from some compiler or other.
//
// Revision 1.1.1.1  2005/10/17 10:46:54  cocoa
// Imported files
//
// Revision 1.1.1.1  2005/05/03 15:47:30  cocoa
// Imported files
//
// Revision 1.4  2005/04/19 15:39:55  cocoa
// Matrices now use reference counts.
//
// Revision 1.3  2005/04/19 14:06:04  cocoa
// Added GPL and GFDL licence stuff.
//
// Revision 1.2  2005/03/30 17:15:14  cocoa
// Cleaned the SpecialMatrix code; a simple test compiles and
// runs fine.
//
// Revision 1.1  2005/03/11 18:40:54  cocoa
// -- first import
//

#endif
