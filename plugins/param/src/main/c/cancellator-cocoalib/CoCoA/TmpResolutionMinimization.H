#ifndef CoCoA_TmpResolutionMinimization_H
#define CoCoA_TmpResolutionMinimization_H

#include "CoCoA/SparsePolyRing.H"

#include <utility>
// using std::pair;
#include <vector>
// using std::vector;

namespace CoCoA
{

  class matrix; // fwd decl -- defined in matrix.H

  class ResolutionMinimization
  {
  public:
    ResolutionMinimization(SparsePolyRing ring, const std::vector<matrix>& matrices):
        myRing(ring),
        myResolution(matrices)
    {}

    /*
     * Reduces the given free resolution to a minimal one.
     * WARNING!  This works only if the length is already minimal.
     */
    void myMinimization();

    /*
     * Returns complete resolution as vector<matrix>
     */
    inline const std::vector<matrix>& myGetResolution() const
    {
      return myResolution;
    }

    /*
     * Returns map from F_i -> F_(i-1) as matrix (starting with zero).
     * No check that index is valid!!!
     */
    inline const matrix& myGetResolution(long i) const
    {
      return myResolution[i];
    }

  private: // impl details
    /*
     * Finds a pivot element for the reduction process.
     * If there is no pivot element it returns (-1, -1).
     */
    std::pair<long, long> myFindPivot(ConstMatrixView m) const;

    /*
     * Manipulating the matrix except row r:
     * Subtract -(PivotColumn[i]/PivotColumn[r]) from i-th row
     * m is a pointer, therefore we only manipulate m without give something back
     */
    void myManipulateMatrix(matrix& m, long r, const std::vector<RingElem>& PivotColumn) const;

    std::vector<RingElem> myGetColumn(ConstMatrixView m, long col) const;

  private: // data members
    SparsePolyRing myRing;

    std::vector<matrix> myResolution;
  };
}
#endif
