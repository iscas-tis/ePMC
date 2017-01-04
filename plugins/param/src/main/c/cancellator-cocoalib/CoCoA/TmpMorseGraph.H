#ifndef CoCoA_TmpMorseGraph_H
#define CoCoA_TmpMorseGraph_H

//   Copyright (c)  2013 Mario Albert

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

#include "CoCoA/TmpMorsePaths.H"
#include "CoCoA/matrix.H"

namespace CoCoA
{

  class MorseGraph
  {
  public:
    typedef std::vector<std::pair<RingElem, DynamicBitset> >::const_iterator PommBasisElem;
  private:
    /*
     * the basic datastructure
     * myMill, myRing, myPPM are basic datas
     * myResolution contains the resolution
     * myBasis contains the pommaret basis with corresponding multiplicative variables
     * we using this to create smaller MorseElements (instead of save both of these data in
     * the MorseElement we store only a iterator)
     * myMapRing is usually the same as myRing. But if we only compute Betti Numbers myMapRing is CoeffRing(myRing)
     */
    JBMill myMill;
    SparsePolyRing myRing;
    PPMonoid myPPM;
    std::map<MorseElement, MorsePaths> myResolution;
    std::vector<std::pair<RingElem, DynamicBitset> > myBasis;
    ring myMapRing;

    /*
     * computes the wasteless ranks
     */
    std::vector< std::vector<long> > myComputeWasteRanks(const std::vector< std::vector<long> >& ranks) const;

    /*
     * computes the wasteless rank for the map from counter + 1 to counter
     * call by ref!!
     */
    void myComputeWasteRanksPerMap(std::vector< std::vector<long> >& WasteRanks,
                                   std::map<MorseElement, MorsePaths>::const_iterator& ResIter,
                                   const std::vector< std::vector<long> >& ranks,
                                   long PosInRes) const;

    /*
     * compute the ranks of the free resolution
     */ 
    std::vector< std::vector<long> > myComputeRanks() const;

    /*
     * computes the number of rows in the betti diagram
     */
    long myNumRowsBettiDiagram(const std::vector<std::vector<long> >& ranks) const;


    /* 
     * position of MorseElement with NumberWedges Wedge Basis elements in the MorseGraph as: <MorseElement, Position In NumberWedges>
     */
    std::map<MorseElement, long> myPositionMorseElements(long NumberWedges) const;

    /*
     * position of MorseElement with NumberWedges Wedge Basis elements in the MorseGraph as: <MorseElement, Position In NumberWedges>
     * but this time grade wise
     */
    std::map<std::pair<long, MorseElement>, long> myGradedPositionMorseElements(long NumberWedges) const;

    /*
     * transform the morse pathes to matrices
     * only creates matrices with correct size, but without data!!!
     */
    std::vector<matrix> myInitialMapsAsMatrices() const;

    /*
     * transform the morse pathes from (pos-1) to pos to a matrix
     * only creates matrix with correct size, but without data!!!
     */
    matrix myInitialMapsAsMatrix(long pos) const;

    /*
     * transform rank matrix to betti matrix
     * append first column for beginning of resolution
     */
    matrix myTransformRanksToBettis(const std::vector<std::vector<long> >& ranks) const;


    /*
     * m1 - m2 for these two pseudo matrices
     */
    std::vector<std::vector<long> > myMatrixMinus(std::vector<std::vector<long> > m1, const std::vector<std::vector<long> >& m2) const;

    /*
     * delete element at position iter of myResolution and returns iterator to formerly previous element
     */
    std::map<MorseElement, MorsePaths>::iterator myDeleteAndJumpToPrevious(std::map<MorseElement, MorsePaths>::iterator iter);

    /*
     * transform vector<pair<RingElem, vector<bool> > > to vector<pair<RingElem, DynamicBitset> >
     */
    std::vector<std::pair<RingElem, DynamicBitset> > myTransform(const std::vector<std::pair<RingElem, std::vector<bool> > >& VecWithBool) const;
    
    /* 
     * creates a new basis element (in the next step we compute the standard rep of this basis elemenbt)
     */
    RingElem myCreateNewBasisElement(const MorseElement& m, long IndexMult, long IndexDiv) const;

    /*
     * Morse Reduction the left part
     */
    void myLeftMinimization(const PathMap& paths, DynamicBitset NewWedgeProduct, const MorseElement& origin, long maximum, long LongIter);

    /*
     * Morse Reduction the right part
     */
    void myRightMinimization(const PathMap& paths, DynamicBitset NewWedgeProduct, const MorseElement& origin, long maximum, long LongIter, StandardRepresentationContainer& container);

    /*
     * add maps from origin to maps[i].first to the MorseGraph
     */
    void myAddMapsToResolution(const std::vector<std::pair<MorseElement, RingElem> >& maps, const ConstResIter& origin);

    /*
     * computes WasteRanks per Degree. First it extracts the corresponding submatrices in F_PosInRes
     * then it computes the ranks of these submatrices
     */
    std::vector<std::pair<long, long> > myComputeWasteRanksPerDegree(std::map<MorseElement, MorsePaths>::const_iterator& ResIter, const std::vector<long>& RowRanks, const std::vector<long>& ColRanks, long PosInRes) const;
    
    /*
     * extract submatrix in F_PosInRes of degree degree
     */
    matrix ConstructDegreeMatrix(std::map<MorseElement, MorsePaths>::const_iterator& ResIter, long rows, long cols, long degree, long PosInRes, const std::map<std::pair<long, MorseElement>, long>& identifier) const;

    /*
     * converts a vector<long> to a DynamicBitset of length length
     * assume counting beginning with 0!!!
     * assume longs is subset of {0, ... , length - 1}
     * assume every element is unique
     */

  public:
    /*
     * constructor
     */
    MorseGraph(JBMill mill):
        myMill(mill),
        myRing(mill.myGetSparsePolyRing()),
        myPPM(mill.myGetPPMonoid()),
        myMapRing(myRing)
    {
        myBasis = myTransform(mill.myComputeMultVarsWithRingElem());
    }

    /*
     * get myMill
     */
    inline const JBMill& myGetMill() const { return myMill; }

    /*
     * get myRing
     */
    inline const SparsePolyRing& myGetRing() const { return myRing; }

    /*
     * get the begin and end vector of myBasis
     */
    inline std::pair<PommBasisElem, PommBasisElem> myGetBasisRange() const
    { return std::make_pair(myBasis.begin(), myBasis.end()); }


    /*
     * get myPPMonoid
     */
    inline const PPMonoid& myGetPPMonoid() const { return myPPM; }

    /*
     * get myResolution as map<MorseElement, MorsePaths>
     */
    inline const std::map<MorseElement, MorsePaths>& myGetBasicResolution() const
    { return myResolution; }

    /*
     * returns the 0th matrix e.g the pommaret basis
     */
    matrix myZerothMatrix() const;

    /*
     * computes the general basis of the form: WedgeProduct |x| (1) (BasisElement) 
     */
    std::vector<MorseElement> myComputeGeneralBasis() const;

    /*
     * returns the not minimized resolution as vector<matrix> 
     */
    std::vector<matrix> myGetResolution() const;

    /*
     * computes the general basis of form WedgeProduct (1) (BasisElement)
     * and adds the corresponding maps + new nodes which appear during computation of maps
     */
    void myComputeBasicGraph(const std::vector<MorseElement>& elems, StandardRepresentationContainer& container);

    /*
     * computes the general basis of form WedgeProduct (1) (BasisElement)
     * and adds the corresponding !!!constant!!! maps + new nodes which appear during computation of maps
     */
    void myComputeBasicConstantGraph(const std::vector<MorseElement>& elems, StandardRepresentationContainer& container);


    /*
     * compute all possible wedge products of NonMultVars. length say how long the bitset must be
     */
    std::vector<DynamicBitset> myPossibleWedges(const std::vector<long>& NonMultVars, long length) const;

    /*
     * recursive function if length 0 it add CurrentResult to result
     * if not it adds one element of InputSet to CurrentResult s.t i1 < i2 < i3 < ....
     */
    void myVariationWithoutRepetition(std::vector<std::vector<long> >& result,
                                      const std::vector<long>& CurrentResult,
                                      const std::vector<long>& InputSet,
                                      long length)  const;

    /*
     * computes the Morse Reduction
     */ 
    void myDirectMorseReduction(StandardRepresentationContainer& container);

    /*
     * computes the constant Morse Reduction
     */ 
    void myConstantDirectMorseReduction(StandardRepresentationContainer& container);

    /*
     * computes the graded betti numbers
     */
    matrix myComputeBettiNumbers();

    /*
     * computes the pseudo betti numbers and the 'normal' betti numbers
     */ 
    std::pair<matrix, matrix> myComputePseudoBettiNumbers();

    /*
     * computes the free resolution but without minimalization
     */ 
    void myComputeResolution();

    /*
     * compute the constant part of the general resolution
     * e.g. only the constant maps
     */
    void myComputeConstantResolution();


    /* 
     * transform the morse pathes to matrices
     */
    std::vector<matrix> myMapsAsMatrices() const;

    /*
     * transform the morse pathes to F_pos to a matrix
     */
    matrix myMapsAsMatrix(long pos) const;

    /*
     * computes a minimal resolution
     */
    std::vector<matrix> myComputeMinimalResolution();
  };

  /*
   * computes the graded Betti Numbers
   */
  matrix JBBettiDiagram(JBMill mill);


  /*
   * computes the minimal free Resolution
   */
  std::vector<matrix> JBMinimalResolution(JBMill mill);



  /*
   * computes a free Resolution
   */
  std::vector<matrix> JBResolution(JBMill mill);



} // end of namespace CoCoa
#endif

