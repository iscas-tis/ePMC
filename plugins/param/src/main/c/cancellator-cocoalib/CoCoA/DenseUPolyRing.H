#ifndef CoCoA_DenseUPolyRing_H
#define CoCoA_DenseUPolyRing_H

//   Copyright (c)  2007  Anna Bigatti

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


// Header file for the abstract class DenseUPolyRingBase etc.


#include "CoCoA/error.H"
#include "CoCoA/ideal.H"
#include "CoCoA/PolyRing.H"
#include "CoCoA/RingHom.H"

#include <iosfwd>
//using std::ostream;
#include <vector>
//using std::vector;
#include <memory>
// using std::auto_ptr;


namespace CoCoA
{

  class symbol;  // fwd decl -- defined in symbol.H

  class DenseUPolyRingBase;


  class DenseUPolyRing: public PolyRing
  {
  public:
    DenseUPolyRing(const ring& R);
    explicit DenseUPolyRing(const DenseUPolyRingBase* RingPtr);
    // Default copy ctor works fine.
    // Default dtor works fine.
  private: // disable assignment
    DenseUPolyRing& operator=(const DenseUPolyRing& rhs); ///< NEVER DEFINED -- assignment disabled
  public:
    const DenseUPolyRingBase* operator->() const; ///< allow member fns to be called
  };


  class DenseUPolyRingBase: public PolyRingBase
  {
  public:
    ///@name  Member functions every concrete DenseUPolyRing implementation must have in addition to those of PolyRingBase.
    //@{
    using PolyRingBase::myIndetSymbol;    // disable warnings of overloading
    virtual const symbol& myIndetSymbol() const = 0;
    virtual std::string myImplDetails() const = 0;
    virtual long mySize(ConstRawPtr rawf) const = 0;            ///< returns (memory) size of f
    virtual long myDegPlus1(ConstRawPtr rawf) const = 0;        ///< returns deg of f +1 (0 for zero poly)
    virtual RingElemAlias myCoeff(ConstRawPtr rawf, long d) const = 0;
    virtual void myAddMul(RawPtr rawf, ConstRawPtr rawc, long d, ConstRawPtr rawg) const = 0; ///<  f += c*indet^d*g
    virtual void myMulByXExp(RawPtr rawf, unsigned long d) const = 0;
    virtual void myMulBy1MinusXExp(RawPtr rawf, unsigned long d) const = 0;
    //@}

    ///@name  Functions for creating/building polynomials
    //@{
    virtual RingElem myMonomial(ConstRawPtr rawc, unsigned long d) const; ///< c is element of CoeffRing!!
    virtual void myAssignZeroCoeff(RawPtr rawf, long d) const = 0; ///< f_d = 0, no check on size
    virtual void myAssignNonZeroCoeff(RawPtr rawf, ConstRawPtr rawc, long d) const = 0; ///< f_d = c, no check on size
    virtual void myResize(RawPtr rawf, long NewSize) const = 0;
    virtual void myResetDeg(RawPtr rawf) const = 0;
    //@}

    ///@name  Functions inherited from PolyRing with a unique implementation for all DenseUPolyRing implementations
    //@{
    virtual long myNumIndets() const {return 1;}
    virtual void myIndetPower(RawPtr rawf, long var, long exp) const;
    virtual long myNumTerms(ConstRawPtr rawf) const;
    virtual bool myIsConstant(ConstRawPtr rawf) const;                  ///< true iff rawf == c (possibly 0)
    virtual bool myIsIndet(long& IndetIndex, ConstRawPtr rawf) const;///< true iff rawf is x[IndetIndex]
    virtual bool myIsIndetPosPower(ConstRawPtr rawf) const;///< true iff rawf is x[i]^d for some i and d>0
    virtual bool myIsMonomial(ConstRawPtr rawf) const;                  ///< is it useful for univariate?
    virtual long myStdDeg(ConstRawPtr rawf) const;                      ///< standard degree of f, error for zero poly
    virtual long myDeg(ConstRawPtr rawf, long index) const;   ///< max exponent of index (same as myStdDeg)
    virtual RingElemAlias myLC(ConstRawPtr rawf) const;
    virtual void myContent(RawPtr rawcontent, ConstRawPtr rawf) const;
    virtual void myContentFrF(RawPtr rawcontent, ConstRawPtr rawf) const;
    virtual void myCommonDenom(RawPtr rawcontent, ConstRawPtr rawf) const;
    virtual void myClearDenom(RawPtr rawcontent, ConstRawPtr rawf) const;
    virtual void myRemoveBigContent(RawPtr rawf) const;
    virtual bool myIsValid(ConstRawPtr rawf) const;                    ///< true iff f is a valid representation, for debugging
    //    virtual bool myIsHomog(ConstRawPtr rawf) const;                  ///< true iff f is homogeneous
    virtual const symbol& myIndetSymbol(long idx) const;
    //@}

    ///@name  Functions inherited from ring with a single implementation for all DenseUPolyRing implementations
    //@{
    virtual void myMul(RawPtr rawlhs, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = x*y
    virtual bool myIsDivisible(RawPtr rawlhs, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = x/y, if divisible
    virtual bool myIsInvertible(ConstRawPtr rawx) const;                ///< true iff x is invertible constant
    virtual void myGcd(RawPtr rawlhs, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = gcd(x,y) if TrueGCDDomain;
    virtual void myExgcd(RawPtr rawlhs, RawPtr rawxcofac, RawPtr rawycofac, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = gcd(x,y) = xcofac*x + ycofac*y  if TrueGCDDomain;
    virtual void mySymbols(std::vector<symbol>& SymList) const;         ///< append symbols in ring to SymList
    virtual RingElem mySymbolValue(const symbol& sym) const;            ///< returns the RingElem corresponding to sym
    virtual void myOutput(std::ostream& out, ConstRawPtr rawx) const;   ///< out << x
    virtual bool myIsPrintAtom(ConstRawPtr rawx) const;                 ///< <indet>^n may be printed without parentheses
    using PolyRingBase::myOutputSelf; // disable warnings of overloading
    virtual void myOutputSelf(OpenMathOutput& OMOut) const;
    virtual void myOutput(OpenMathOutput& OMOut, ConstRawPtr rawx) const;///< OMOut << x
    virtual bool myIsZero(ConstRawPtr rawx) const;                      ///< x == 0
    virtual bool myIsOne(ConstRawPtr rawf) const;                       ///< true iff rawf == 1
    virtual bool myIsMinusOne(ConstRawPtr rawf) const;                  ///< true iff rawf == -1
    virtual bool myIsInteger(BigInt& N, ConstRawPtr rawx) const;        ///< true iff x is integer
    virtual bool myIsRational(BigRat& Q, ConstRawPtr rawx) const;           ///< true iff x is rational
    virtual bool myIsEqual(ConstRawPtr rawx, ConstRawPtr rawy) const;   ///< true iff rawx == rawy
    virtual void myPowerSmallExp(RawPtr rawlhs, ConstRawPtr rawx, long n) const;///< lhs = x^n, n>1, x not -1,0,1
    virtual ideal myIdealCtor(const std::vector<RingElem>& gens) const;
    //@}

    ///@name  Functions for homomorphisms
    //@{
    virtual RingHom myHomCtor(const ring& codomain, const RingHom& CoeffHom, const std::vector<RingElem>& IndetImages) const;
    //    RingHom myHomCtor(const ring& codomain, const RingHom& CoeffHom, ConstRefRingElem IndetImage) const;
    RingHom myCompose(const RingHom& phi, const RingHom& theta) const;
    //@}

  protected: // Ideal class for this type of ring.

    class IdealImpl: public IdealBase
    {
    public:
      IdealImpl(const DenseUPolyRing& P, const std::vector<RingElem>& gens);
      // default copy ctor is OK
      virtual IdealBase* myClone() const;
//???    virtual void swap(ideal& other);

      virtual const DenseUPolyRing& myRing() const {return myP;}
      virtual bool IamZero() const;
      // virtual bool IamOne() const;  // default impl
      virtual void myReduceMod(RingElemRawPtr rawx) const; ///< x elem of P, where I is ideal in P
      virtual bool IhaveElem(RingElemConstRawPtr rawx) const;
      virtual void myAdd(const ideal&);
      virtual void myMul(const ideal&);
      virtual void myIntersect(const ideal&);
      virtual void myColon(const ideal&);
      virtual bool myDivMod(RingElemRawPtr rawlhs, RingElemConstRawPtr rawnum, RingElemConstRawPtr rawden) const; ///< ??? lhs = num/den modulo the ideal  (lhs = 0 if quotient does not exist)

      virtual const std::vector<RingElem>& myGens() const {return myGensValue;} ///< gens as specified by user
      virtual const std::vector<RingElem>& myTidyGens() const; ///< tidier set of gens
    protected:
      virtual void myMaximalTest() const;
      virtual void myPrimeTest() const;

    protected: // Data members of DenseUPolyRingBase::IdealImpl
      DenseUPolyRing myP;
      std::vector<RingElem> myGensValue;
      mutable bool myTidyGensIsValid;
      mutable std::vector<RingElem> myTidyGensValue;
    protected: // Auxiliary functions.
      static const DenseUPolyRingBase::IdealImpl* GetPtr(const ideal& J);
    };  // class IdealImpl

  protected:    //  General homomorphism class for this type of ring.
    class HomImpl: public RingHomBase
    {
    public:
      HomImpl(const DenseUPolyRing& domain, const ring& codomain, const RingHom& CoeffHom, ConstRefRingElem IndetImage);
      virtual void myApply(RingElemRawPtr rawimage, RingElemConstRawPtr rawarg) const;
      virtual bool IamPartial() const { return IsPartial(myCoeffHom); }
    private:
      virtual void myOutputSelfDetails(std::ostream& out) const;

    private: // Data members of DenseUPolyRingBase::HomImpl
      const RingHom myCoeffHom;
      RingElem myIndetImage;
    };  // class HomImpl

  protected:    //  Special homomorphism class for this type of ring.
    class CoeffEmbeddingHomImpl: public RingHomEmbeddingBase
    {
    public:
      CoeffEmbeddingHomImpl(const DenseUPolyRing& P);
      virtual void myApply(RingElemRawPtr rawimage, RingElemConstRawPtr rawarg) const;
    };  // class CoeffEmbeddingHomImpl
  };


  bool IsDenseUPolyRing(const ring& R);                         // inline
  bool IsGoodIndetName(const ring& CoeffRing, const symbol& IndetName);

  ///@name  Pseudo constructors for RingElems owned by DenseUPolyRing
  //@{
  RingElem monomial(const DenseUPolyRing& P, ConstRefRingElem c, const MachineInt& exp);      ///< c*indet^exp
  RingElem monomial(const DenseUPolyRing& P, const BigRat& q, const MachineInt& exp);             ///< q*indet^exp
  RingElem monomial(const DenseUPolyRing& P, const BigInt& N, const MachineInt& exp);         ///< N*indet^exp
  RingElem monomial(const DenseUPolyRing& P, const MachineInt& n, const MachineInt& exp); ///< n*indet^exp
  //@}

  ///@name  Special functions on RingElems owned by DenseUPolyRing
  //@{
  RingElemAlias coeff(ConstRefRingElem f, long d);
  //@}

  //----------------------------------------------------------------------
  // Inline functions

  inline const DenseUPolyRingBase* DenseUPolyRingPtr(const ring& R)
  {
    return dynamic_cast<const DenseUPolyRingBase*>(R.myRawPtr());
  }

  inline const DenseUPolyRingBase* DenseUPolyRingPtr(const ring& R, const char* const FnName)
  {
    const DenseUPolyRingBase* ptr = DenseUPolyRingPtr(R);
    if (ptr == 0/*nullptr*/) CoCoA_ERROR(ERR::NotDenseUPolyRing, FnName);
    return ptr;
  }

  inline bool IsDenseUPolyRing(const ring& R)
  {
    return PolyRingPtr(R) != 0/*nullptr*/;
  }


  inline DenseUPolyRing::DenseUPolyRing(const ring& R):
      PolyRing(DenseUPolyRingPtr(R, "DenseUPolyRing ctor"))
  {}

  inline DenseUPolyRing::DenseUPolyRing(const DenseUPolyRingBase* RingPtr):
    PolyRing(RingPtr)
  {}


  inline const DenseUPolyRingBase* DenseUPolyRing::operator->() const
  {
    return static_cast<const DenseUPolyRingBase*>(ring::operator->());
  }
  

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/DenseUPolyRing.H,v 1.39 2014/07/30 14:00:35 abbott Exp $
// $Log: DenseUPolyRing.H,v $
// Revision 1.39  2014/07/30 14:00:35  abbott
// Summary: Changed myAmbientRing into myRing
// Author: JAA
//
// Revision 1.38  2014/07/11 15:41:43  bigatti
// -- added  myOutputSelf (default impl),  myImplDetails()
//
// Revision 1.37  2014/07/09 13:01:17  abbott
// Summary: Removed AsDenseUPolyRing
// Author: JAA
//
// Revision 1.36  2014/05/14 15:57:15  bigatti
// -- added "using" for clang with superpedantic flag
//
// Revision 1.35  2014/05/06 13:20:41  abbott
// Summary: Changed names (my)MaxExponents into (my)Deg
// Author: JAA
//
// Revision 1.34  2013/06/28 17:03:51  abbott
// Modified semantics of IdealBase::myDivMod;
// it now returns a boolean.
// Several consequential changes.
//
// Revision 1.33  2013/05/31 09:15:21  abbott
// Changed arg type of fn "coeff" from MachineInt to long becaue it is an index.
//
// Revision 1.32  2013/02/21 14:15:31  abbott
// First attempt at implementing PartialRingHom -- some problems remain!
//
// Revision 1.31  2012/10/24 13:29:42  abbott
// Changed return type of coeff/myCoeff and myLC.
// Changed type of data member myIndetImage
//
// Revision 1.30  2012/05/24 17:00:27  bigatti
// -- removed old style Doxygen include
//
// Revision 1.29  2012/05/22 10:02:38  abbott
// Removed IsGCDDomain; substituted by IsTrueGCDDomain.
// Added IsFractionFieldOfGCDDomain.
//
// Revision 1.28  2011/11/09 13:38:31  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.27  2011/08/24 10:20:28  bigatti
// -- renamed QQ --> BigRat
//
// Revision 1.26  2011/08/14 15:52:18  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.25  2011/06/23 16:04:47  abbott
// Added IamExact mem fn for rings.
// Added myRecvTwinFloat mem fn for rings.
// Added first imple of RingHom from RingTwinFloat to other rings.
//
// Revision 1.24  2011/05/19 14:44:32  abbott
// Removed commented out decl of old form of myIsRational.
//
// Revision 1.23  2011/03/16 15:38:52  bigatti
// -- added myIsIndetPosPower(f)
//
// Revision 1.22  2011/03/10 16:39:35  abbott
// Replaced (very many) size_t by long in function interfaces (for rings,
// PPMonoids and modules).  Also replaced most size_t inside fn defns.
//
// Revision 1.21  2011/03/01 14:10:47  bigatti
// -- added ClearDenom/myClearDenom
//
// Revision 1.20  2011/01/18 14:35:38  bigatti
// -- moved **_forC5 functions into CoCoA-5/CoCoALibSupplement:
//    myMonomials_forC5, mySupport_forC5, monomials_forC5, support_forC5,
//    LPP_forC5, LT_forC5, LM_forC5
//
// Revision 1.19  2010/11/30 11:32:36  bigatti
// -- moved IndetsCalled into unique implementation in PolyRing
// -- renamed myIndetName --> myIndetSymbol
// -- added myIndetSymbol
//
// Revision 1.18  2010/11/25 12:30:49  bigatti
// -- added myIndetsCalled
//
// Revision 1.17  2010/11/05 16:14:04  bigatti
// -- added monomials_forC5, support_forC5
//
// Revision 1.16  2010/10/01 15:20:33  bigatti
// -- added mySymbolValue
// -- added RingElem(R, sym)
//
// Revision 1.15  2010/06/10 08:00:02  bigatti
// -- fixed naming conventions
//
// Revision 1.14  2010/02/04 10:12:04  bigatti
// -- added "mul" for ideal (implemented only for SparsePolyRing)
//
// Revision 1.13  2009/10/02 13:27:26  bigatti
// -- unique implementation of myDiv in PolyRing.C
//
// Revision 1.12  2009/07/24 12:26:43  abbott
// Added CommonDenom function for polynomials.
//
// Revision 1.11  2009/07/02 16:32:11  abbott
// Consequential changes stemming from new class BigRat, and modified interface to the member
// function RingBase::myIsRational.  Also some new conversion functions.
//
// Revision 1.10  2008/12/17 12:11:52  abbott
// Changed type from long to MachineInt in operations which use a machine integer
// in place of a RingElem.  The change is "superficial" but affects many files.
//
// Revision 1.9  2008/04/21 12:32:54  abbott
// Corrected size_t into std::size_t in several header files; in some cases,
// replaced size_t with MachineInt (with consequent changes to impl files).
//
// Revision 1.8  2008/03/12 16:35:19  bigatti
// -- changed: IsHomogeneous --> IsHomog
// -- changed: ERR:ZeroPoly --> ERR::ZeroRingElem
//
// Revision 1.7  2007/12/21 12:29:08  bigatti
// -- abstract implementation in DenseUPolyRing of myDiv, myIsDivisible, myIsInvertible, myGcd
// -- abstract implementation in DenseUPolyRing of ideal operations
// -- some cleaning
//
// Revision 1.6  2007/12/07 15:27:01  bigatti
// -- default implementation of "IamOne" in ideal.C
//
// Revision 1.5  2007/12/05 11:06:24  bigatti
// -- changed "size_t StdDeg/myStdDeg(f)" into "long"  (and related functions)
// -- changed "log/myLog(f, i)" into "MaxExponent/myMaxExponent(f, i)"
// -- fixed bug in "IsOne(ideal)" in SparsePolyRing.C
//
// Revision 1.4  2007/10/30 17:14:12  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.3  2007/10/15 12:38:52  bigatti
// -- fixed comment
//
// Revision 1.2  2007/10/10 14:02:37  bigatti
// -- added myMulBy1MinusXExp
// -- fixed a few little bugs
//
// Revision 1.1  2007/10/05 15:28:56  bigatti
// -- added abstract class DenseUPolyRing for representing dense
//    univariate polynomials
// -- added concrete class RingDenseUPolyClean, the cleanest
//    implementation
// -- just for testing, still horribly inefficient and incomplete
//

#endif
