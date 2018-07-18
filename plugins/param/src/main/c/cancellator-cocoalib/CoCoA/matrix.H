#ifndef CoCoA_matrix_H
#define CoCoA_matrix_H

//   Copyright (c)  2005,2008  John Abbott

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


#include "CoCoA/BigInt.H"
#include "CoCoA/ring.H"

#include <iosfwd>
//using std::ostream;
#include <vector>
//using std::vector;


namespace CoCoA
{

  class MachineInt; // fwd decl -- defined in MachineInt.H
  class OpenMathOutput; // fwd decl -- defined in OpenMath.H

  // Operations on vectors -- should be ModuleElems?  (of a FreeModule?)
  bool IsZero(const std::vector<RingElem>& v);

  template <typename T>
  bool IsRectangular(const std::vector< std::vector<T> >& vv)
  {
    const std::size_t nrows = vv.size();
    if (nrows == 0) return true;
    const std::size_t ncols = vv[0].size();
    for (std::size_t n=1; n < nrows; ++n)
      if (vv[n].size() != ncols) return false;
    return  true;
  }

  class matrix;              // forward declaration
  class MatrixBase;          // forward declaration
  class MatrixViewBase;      // forward declaration
  class ConstMatrixViewBase; // forward declaration

  class ConstMatrixView
  {
  public:
    explicit ConstMatrixView(const ConstMatrixViewBase* MatPtr);
    // Default copy ctor is OK.
    // Assignment disabled (because SmartPtrIRC has no assignment).
    // Default dtor is OK.
    const ConstMatrixViewBase* operator->() const;
    RingElemAlias operator()(long i, long j) const;
    bool operator==(ConstMatrixView M) const;
    bool operator!=(ConstMatrixView M) const { return !(*this==M); }

  protected: // data member
    SmartPtrIRC<ConstMatrixViewBase> mySmartPtr;
    friend void swap(matrix& M1, matrix& M2);
    friend bool IsZeroMatImpl(ConstMatrixView M);
    friend bool IsIdentityMatImpl(ConstMatrixView M);
    friend bool IsDiagMatImpl(ConstMatrixView M);
    friend bool IsConstDiagMatImpl(ConstMatrixView M);
  };


  class MatrixView: public ConstMatrixView
  {
    // Data member inherited from ConstMatrixView
  public:
    explicit MatrixView(MatrixViewBase* MatPtr);
    // Default copy ctor is OK.
    // Assignment disabled.
    // Default dtor is OK.
    MatrixViewBase* operator->() const;
  };


  class matrix: public MatrixView
  {
    // Data member inherited from ConstMatrixView via MatrixView
  public:
    explicit matrix(MatrixBase* MatPtr);
    matrix(const matrix& M);
    // Default copy ctor is OK.
    // Assignment disabled.
    // Default dtor is OK.
    MatrixBase* operator->() const;

//   protected:
//     MatrixBase* myMatrixPtr(); // Needed by concrete matrix impl classes.
  };





  class ConstMatrixViewBase: protected IntrusiveReferenceCount
  {
    friend class SmartPtrIRC<ConstMatrixViewBase>;

  protected: // Hide default ctor & dtor from public.
    ConstMatrixViewBase() {}
    virtual ~ConstMatrixViewBase() {}

  public:
    typedef std::vector<RingElem> vec; ///???? is this the right thing to do???
    virtual const ring& myRing() const = 0;
    virtual long myNumRows() const = 0;
    virtual long myNumCols() const = 0;
    virtual RingElemAlias myEntry(long i, long j) const = 0;
    virtual bool IamEqual(ConstMatrixView M) const;       ///< default tests all entries
    virtual bool IamSymmetric() const;                    ///< default tests all entries
    virtual bool IamAntiSymmetric() const;                ///< default tests all entries
    virtual bool IamDiagonal() const;                     ///< default tests all entries
    void myCheckRowIndex(long i, const char* where="") const;  // throws if index is too large
    void myCheckColIndex(long j, const char* where="") const;  // throws if index is too large
    virtual bool myIsZeroRow(long i) const;                ///< tests whether row i is zero -- default "dense" definition
    virtual bool myIsZeroCol(long j) const;                ///< tests whether col j is zero -- default "dense" definition
    virtual void myMulByRow(vec& lhs, const vec& v) const; ///< lhs = v*M -- vector-by-matrix product, default "dense" definition
    virtual void myMulByCol(vec& lhs, const vec& v) const; ///< lhs = M*v -- matrix-by-vector product, default "dense" definition
    virtual void myDet(RingElem& d) const;                 ///< default method uses Gaussian reduction
    virtual long myRank() const;                           ///< default method uses Gaussian reduction
    virtual void myOutputSelf(std::ostream& out) const;    ///< has a default "dense" definition
    virtual void myOutputSelf(OpenMathOutput& OMOut) const;///< has a default "dense" definition
  };


  class MatrixViewBase: public ConstMatrixViewBase
  {
  protected: // Hide default ctor & dtor from public.
    MatrixViewBase() {}
    virtual ~MatrixViewBase() {}

  public:
    virtual void myAssignZero() = 0; ///< sets all entries to zero
    virtual bool myIsWritable(long i, long j) = 0; ///< true iff location (i,j) is writable
    virtual RingElemRawPtr myRawEntry(long i, long j) = 0; ///< may be used only if (i,j) is writable
    virtual void mySetEntry(long i, long j, ConstRefRingElem r) = 0;
    virtual void mySetEntry(long i, long j, const MachineInt& n) = 0;
    virtual void mySetEntry(long i, long j, const BigInt& N) = 0;
    virtual void mySetEntry(long i, long j, const BigRat& Q) = 0;
  };


  class MatrixBase: public MatrixViewBase
  {
  protected: // Hide default ctor & dtor from public.
    MatrixBase() {}
    virtual ~MatrixBase() {}

  public:
    virtual MatrixBase* myZeroClone(const ring& R, long NumRows, long NumCols) const = 0;
    virtual MatrixBase* myClone() const = 0;
    typedef std::vector<RingElem> vec;
    virtual void myResize(long NumRows, long NumCols) = 0;
    virtual void myRowMul(long i, ConstRefRingElem c) = 0; ///< row(i) = c*row(i)
    virtual void myColMul(long j, ConstRefRingElem c) = 0; ///< col(j) = c*col(j)
    virtual void myAddRowMul(long i1, long i2, ConstRefRingElem c) = 0; ///< row(i1) += c*row(i2)
    virtual void myAddColMul(long j1, long j2, ConstRefRingElem c) = 0; ///< col(j1) += c*col(j2)
    virtual void mySwapRows(long i1, long i2) = 0;
    virtual void mySwapCols(long j1, long j2) = 0;
  };




  std::ostream& operator<<(std::ostream& out, const ConstMatrixView& M);
  void AssignZero(MatrixView& M);
  void swap(matrix& M1, matrix& M2);
  void SetEntry(MatrixView& M, long i, long j, ConstRefRingElem r);
  void SetEntry(MatrixView& M, long i, long j, const MachineInt& n);
  void SetEntry(MatrixView& M, long i, long j, const BigInt& N);
  void SetEntry(MatrixView& M, long i, long j, const BigRat& Q);
  void SwapRows(matrix& M1, long i1, long i2);
  void SwapCols(matrix& M1, long i1, long i2);
  void DeleteRow(matrix& M, long i);
  void DeleteCol(matrix& M, long i);

  /////////////////////////////////////////////////////////////////////////////
  // Inline functions

  inline ConstMatrixView::ConstMatrixView(const ConstMatrixViewBase* MatPtr):
      mySmartPtr(const_cast<ConstMatrixViewBase*>(MatPtr))
  {}


  inline const ConstMatrixViewBase* ConstMatrixView::operator->() const
  { return mySmartPtr.operator->(); }


  inline bool ConstMatrixView::operator==(ConstMatrixView M) const
  {
    if (mySmartPtr == M.mySmartPtr) return true;
    return mySmartPtr->IamEqual(M);
  }


  inline MatrixView::MatrixView(MatrixViewBase* MatPtr):
      ConstMatrixView(MatPtr)
  {}


  inline MatrixViewBase* MatrixView::operator->() const
  { return dynamic_cast<MatrixViewBase*>(const_cast<ConstMatrixViewBase*>(ConstMatrixView::operator->())); }



  inline matrix::matrix(MatrixBase* MatPtr):
      MatrixView(MatPtr)
  {}


  inline MatrixBase* matrix::operator->() const
  { return dynamic_cast<MatrixBase*>(MatrixView::operator->()); }


//   inline MatrixBase* matrix::myMatrixPtr()
//   {
//     // dynamic_cast would perform needless checks
//     // reinterpret_cast might not be portable?
//     return static_cast<MatrixBase*>(const_cast<MatrixViewBase*>(myPtr));
//   }


  inline const ring& RingOf(const ConstMatrixView& M)
  {
    return M->myRing();
  }


  inline long NumRows(const ConstMatrixView& M)
  {
    return M->myNumRows();
  }


  inline long NumCols(const ConstMatrixView& M)
  {
    return M->myNumCols();
  }


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/matrix.H,v 1.29 2014/08/16 13:42:26 abbott Exp $
// $Log: matrix.H,v $
// Revision 1.29  2014/08/16 13:42:26  abbott
// Summary: Added copy ctor for matrix, and myClone for MatrixBase
// Author: JAA
//
// Revision 1.28  2014/07/30 14:01:55  abbott
// Summary: Changed BaseRing into RingOf
// Author: JAA
//
// Revision 1.27  2014/04/17 14:08:39  bigatti
// -- moved Is... functions to MatrixOperations
//
// Revision 1.26  2014/04/15 14:23:42  abbott
// Summary: Removed IsFilledMatImpl (now obsolete)
// Author: JAA
//
// Revision 1.25  2013/06/17 12:56:57  bigatti
// -- added DeleteRow, DeleteCol
//
// Revision 1.24  2012/11/23 17:25:32  abbott
// Added (inline) fn IsMat0x0.
//
// Revision 1.23  2012/10/24 12:09:26  abbott
// Changed return type of matrix::operator() and of MatrixBase::myEntry.
//
// Revision 1.22  2012/10/16 09:50:40  abbott
// Changed  RefRingElem  into  RingElem&.
// Realigned some comments.
//
// Revision 1.21  2012/10/11 14:57:20  abbott
// Replaced mem fn  myRefEntry  by equivalent new mem fn  myRawEntry;
// this way we remove the need for RefRingElem.
// Also the new name/semantics should discourage casual use.
//
// Revision 1.20  2012/04/04 13:53:32  bigatti
// -- added SwapRows, SwapCols
//
// Revision 1.19  2011/11/09 13:51:29  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.18  2011/11/07 11:10:48  bigatti
// -- just a comment: forward --> fwd
//
// Revision 1.17  2011/10/04 16:45:52  abbott
// Added IsRectangular template fn for vector of vector.
//
// Revision 1.16  2011/08/24 10:22:45  bigatti
// -- renamed QQ --> BigRat
//
// Revision 1.15  2011/08/14 15:52:17  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.14  2011/03/21 13:21:58  bigatti
// -- added friend IsZeroMatImpl
//
// Revision 1.13  2011/03/09 09:09:01  bigatti
// -- removed include MachineInt, added fwd decl
//
// Revision 1.12  2011/03/08 17:26:11  bigatti
// -- changed: args for rows and cols are now  long  instead of  MachineInt
//
// Revision 1.11  2011/03/04 16:22:58  bigatti
// -- changed: functions args of type MachineInt instead of size_t
//             members functions args of type long instead of size_t
//
// Revision 1.10  2011/03/03 13:50:22  abbott
// Replaced several occurrences of std::size_t by long; there's still more
// work to do though!
//
// Revision 1.9  2011/02/22 13:15:10  bigatti
// -- added IsSymmetric, IsAntiSymmetric, IsDiagonal, and tests
//
// Revision 1.8  2011/02/16 17:36:08  bigatti
// -- removed useless doxygen comment
//
// Revision 1.7  2011/02/15 10:05:20  bigatti
// -- added myIsEqual, operator==, operator!=
// -- added friends IsIdentityMatImpl, IsZeroMatImpl, IsConstDiagMatImpl, IsDiagMatImpl
//
// Revision 1.6  2011/01/31 14:10:30  bigatti
// -- added mySetEntry/SetEntry with BigRat entry
//
// Revision 1.5  2008/12/17 12:11:52  abbott
// Changed type from long to MachineInt in operations which use a machine integer
// in place of a RingElem.  The change is "superficial" but affects many files.
//
// Revision 1.4  2008/04/16 17:24:17  abbott
// Further cleaning of the new matrix code.  Updated documentation too.
//
// Revision 1.3  2008/04/08 15:26:42  abbott
// Major revision to matrix implementation: added matrix views.
// Lots of changes.
//
// Revision 1.2  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.5  2006/12/06 17:18:40  cocoa
// -- removed #include "config.H"
//
// Revision 1.4  2006/11/24 17:41:18  cocoa
// -- reorganized includes of header files
//
// Revision 1.3  2006/11/02 13:25:44  cocoa
// Simplification of header files: the OpenMath classes have been renamed.
// Many minor consequential changes.
//
// Revision 1.2  2006/10/06 14:04:15  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//
// Revision 1.1.1.1  2006/05/30 11:39:36  cocoa
// Imported files
//
// Revision 1.4  2006/05/12 16:10:58  cocoa
// Added OpenMathFwd.H, and tidied OpenMath.H.
// Many consequential but trivial changes.
//
// Revision 1.3  2006/04/27 13:45:30  cocoa
// Changed name of NewIdentityRingHom to NewIdentityHom.
// Changed name of member functions which print out their own object
// into myOutputSelf (to distinguish from "transitive" myOutput fns).
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
// Revision 1.11  2005/04/20 15:40:48  cocoa
// Major change: modified the standard way errors are to be signalled
// (now via a macro which records filename and line number).  Updated
// documentation in error.txt accordingly.
//
// Improved the documentation in matrix.txt (still more work to be done).
//
// Revision 1.10  2005/04/19 15:39:55  cocoa
// Matrices now use reference counts.
//
// Revision 1.9  2005/04/19 14:06:04  cocoa
// Added GPL and GFDL licence stuff.
//
// Revision 1.8  2005/03/31 16:59:42  cocoa
// Made special matrix ctors private, so a user has to pass via the
// pseudo-ctors (which do all the arg sanity checking).
//
// Revision 1.7  2005/03/30 17:15:14  cocoa
// Cleaned the SpecialMatrix code; a simple test compiles and
// runs fine.
//
// Revision 1.6  2005/03/29 17:36:47  cocoa
// Just checking in before going home -- test-matrix1 does not yet link!
//
// Revision 1.5  2005/03/29 17:19:30  cocoa
// -- added: rank and myRank
//
// Revision 1.4  2005/03/11 16:44:18  cocoa
// New abstract class structure for matrices.
// New types of special matrix.
//
// Revision 1.3  2005/03/02 18:46:41  cocoa
// Added new types ConstRefMatrix, and RefMatrix following along
// the lines of ConstRefRingElem and RefRingElem.  The semantics
// should be a bit clearer now.
//
// Revision 1.2  2005/02/11 14:15:20  cocoa
// New style ring elements and references to ring elements;
// I hope I have finally got it right!
//
// Revision 1.1.1.1  2005/01/27 15:12:13  cocoa
// Imported files
//
// Revision 1.9  2004/11/29 16:19:02  cocoa
// -- changed syntax for function det
//
// Revision 1.8  2004/11/12 15:49:29  cocoa
// Tidying prior to 0.90 release.
// (a) documentation improved (or marked as poor)
// (b) sundry minor improvements to the code
//

#endif
