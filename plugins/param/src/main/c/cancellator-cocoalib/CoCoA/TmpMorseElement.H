#ifndef CoCoA_TmpMorseElement_H
#define CoCoA_TmpMorseElement_H

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

#include "CoCoA/PPMonoid.H"
#include "CoCoA/SparsePolyRing.H"
#include "CoCoA/DynamicBitset.H"
#include "CoCoA/TmpJBEnv.H"

#include <vector>

namespace CoCoA
{
  // index of last "true" bit (or -1)
  long myMaxTrueOfBitset(DynamicBitset bitset);

  // conversions between DynamicBitset, vector<long> & vector<bool>
  DynamicBitset myVectorLongToDynamicBitset(const std::vector<long>& longs, const long& length);
  DynamicBitset myVectorBoolToDynamicBitset(const std::vector<bool>& BoolVector);
  std::vector<long> myDynamicBitsetToLong(const DynamicBitset& DynBitset);


  class StandardRepresentationContainer
  {
  private: // data members
    /*
     * stores all StandardRepresentations sorted after PPMonoidElem, then we have to search
     * matching RingElem
     */

    std::multimap<PPMonoidElem, std::pair<RingElem, std::vector<RingElem> > > myContainer;

    /*
     * mill to compute StandardRepresenations
     */
    JBMill myMill;

  public: // data members (for debugging/optimizing)
    /*
     * Statistics
     */
    long myOriginNormalForms;
    long myReallyNormalForms;

  public:
    StandardRepresentationContainer(const JBMill& mill):
        myMill(mill),
        myOriginNormalForms(0),
        myReallyNormalForms(0)
    {}


    /*
     * First this function searches if we have already computed this StandardRepresentation.
     * If yes, it returns the matching StdRep; if not, it computes it, stores it in the container and 
     * then returns it.
     */
    const std::vector<RingElem>& myComputeStandardRepresentation(ConstRefRingElem r);

  };

  /*
   * The MorseElement
   */
  class MorseElement
  {
  public:
    typedef std::vector< std::pair<RingElem, DynamicBitset> >::const_iterator PommBasisElem;
  private: // data members
    /*
     * A MorseElement consists of a WedgeProduct, a RightFactor, and a BasisElement
     */
    DynamicBitset myWedgeProduct;
    PPMonoidElem myRightFactor;
    PommBasisElem myBasis;

    /*
     * for faster access
     * == myRightFactor * LPP(myBasis->first)
     */
    PPMonoidElem myRightProduct;

    /*
     * for faster access
     * == AsPPMonoidElem(myWedgeProduct) * myRightFactor * LPP(myBasis->first)
     */
    PPMonoidElem myProduct;

    /* 
     * for faster access
     * myWedge as longs
     */
    std::vector<long> myWedgeProductAsLongs;

    friend bool operator <(const MorseElement& m1, const MorseElement& m2);
    friend bool operator ==(const MorseElement& m1, const MorseElement& m2);


  private: // impl details
    /*
     * call by reference!
     * computes the RightMaps of the Basic Graph
     */
    void myComputeRightMaps(std::vector<std::pair<MorseElement, RingElem> >& maps,
                            long i,
                            DynamicBitset NewWedge,
                            const std::pair<PommBasisElem, PommBasisElem>& BasisIters,
                            StandardRepresentationContainer& container,
                            const ring& MapRing) const;

    /*
     * call by reference!
     * computes the LeftMaps of the Basic Graph
     */
    void myComputeLeftMaps(std::vector<std::pair<MorseElement, RingElem> >& maps,
                           long i,
                           DynamicBitset NewWedge) const;

    /*
     * construct a new WedgeProduct with i-th BasisElement in old WedgeProduct removed
     */ 
    DynamicBitset myWedgeWithOneRemoved(const std::vector<long>& WedgeAsLong, long LengthWedge, std::vector<long>::const_iterator it) const;

  public: // public interface
    // constructor
    MorseElement(const DynamicBitset& WedgeProduct, const PommBasisElem basis);
    MorseElement(const DynamicBitset& WedgeProduct, const PPMonoidElem& myRightFactor, const PommBasisElem basis);

    /*
     * get myWedgeProduct
     */
    inline const DynamicBitset& myGetWedgeProduct() const
    { return myWedgeProduct; }

    /*
     * get myWedgeProduct as Longs
     */
    inline const std::vector<long>& myGetWedgeProductAsLongs() const
    { return myWedgeProductAsLongs; }

    /* 
     * get myRightFactor
     */
    inline const PPMonoidElem& myGetRightFactor() const
    { return myRightFactor; }

    /* 
     * get myNCrit
     */
    inline const DynamicBitset& myGetNCrit() const
    { return myBasis->second; }

    /* 
     * get myBasisElement
     */
    inline const RingElem& myGetBasisElement() const
    { return myBasis->first; }

    /*
     * get current SparsePolyRing
     * It is the owner of myBasisElement
     */
    inline SparsePolyRing myGetPolyRing() const
    { return owner(myBasis->first); }

    void mySetWedgeProduct(const DynamicBitset& elem);
    void mySetRightFactor(const PPMonoidElem& elem);


    /*
     * test if MorseElement is an ordinary BasisElement
     * This is the case if the RightFactor is One && 
     * myWedgeProduct is a subset of flip(myNCrit)
     * last condition is equivalent to myWedgeProduct and
     * myNCrit are Disjoint
     */
    inline bool IAmBasisElement() const
    {
      return IsOne(myRightFactor) && IsDisjoint(myWedgeProduct, myBasis->second);
    }

    /*
     * max((supp /\ ncrit) \ I)
     * return -1 if there is no maximum
     */
    long myMaxTypeOne() const; 

    /*
     * max((I \/ supp) /\ ncrit)
     * return -1 if there is no maximum
     */
    long myMaxTypeTwo() const;

    /*
     * assume test,add < len(myWedgeProduct)
     * assume add not in set (not necessary)
     * == -1^max{i| (i in myWedgeProduct + add) and i < test}
     */
    int myEpsilon(long test, long add) const;

    /*
     * Divide indeterminate x[i] from RightProduct
     * no check if this is possible!
     */
    void myDivideRightProductWith(long i);


    /*
     * computes the basic maps for a basic Morse Graph
     */
    std::vector<std::pair<MorseElement, RingElem> > myComputeBasicMaps(const std::pair<PommBasisElem, PommBasisElem>& BasisIters, StandardRepresentationContainer& container) const;

    /*
     * computes the basic maps for a basic constant Morse Graph 
     * e.g. we only compute the constant part of the maps
     */
    std::vector<std::pair<MorseElement, RingElem> > myComputeBasicConstantMaps(const std::pair<PommBasisElem, PommBasisElem>& BasisIters, StandardRepresentationContainer& container) const;


    /*
     * number of BasisElements in WedgeProduct
     */
    inline long myCountWedgeBasis() const
    { return myWedgeProductAsLongs.size(); }

    /*
     * total degree of MorseElement
     */
    inline long myDegree() const
    {
      return myCountWedgeBasis() + deg(myBasis->first) + deg(myRightFactor);
    }

    /*
     * checks if i is multiplicative
     */
    inline bool IamMultiplicativeIndex(long i) const
    { return (myBasis->second).Iam1At(i); }

  }; // end of class MorseElement


  inline bool operator ==(const MorseElement& m1, const MorseElement& m2)
  {
    return (m1.myWedgeProduct == m2.myWedgeProduct) &&
      (m1.myRightFactor == m2.myRightFactor) &&
      (*(m1.myBasis) == *(m2.myBasis));
  }



    /* operator contains the following rules
     * 1. NumTrues(m1) < NumTrues(m2)
     * 2. AsPPM(m1.myWedgeProduct) * m1.myRightFactor * LPP(m1.myBasisElement)
     *    < AsPPM(m2.myWedgeProduct) * m2.myRightFactor * LPP(m2.myBasisElement)
     * 3. m1.myNCrit contains m2.myNCrit
     * 4. The i-th index of myNCrit let be the i-th true entry.
     *    let i be the minimal index of m1.myNCrit and m2.myNcrit for which
     *    m1.myNCrit[i] != m2.myNCrit[i]. Then
     *    m1.myNCrit[i] < m2.myNCrit[i].
     * 5. m1.myRightFactor * LPP(m1.myBasisElement) < m2.myRightFactor * LPP(m2.myBasisElement)
     */
  bool operator <(const MorseElement& m1, const MorseElement& m2);
  bool operator <=(const MorseElement& m1, const MorseElement& m2);
  bool operator >(const MorseElement& m1, const MorseElement& m2);
  bool operator >=(const MorseElement& m1, const MorseElement& m2);



} // end of namespace CoCoA

#endif

