#ifndef CoCoA_SparsePolyRing_H
#define CoCoA_SparsePolyRing_H

//   Copyright (c)  2005-2007,2009,2012  John Abbott

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


// Header file for the abstract class SparsePolyRingBase etc.

#include "CoCoA/PPMonoid.H"
#include "CoCoA/PolyRing.H"
#include "CoCoA/RingHom.H"
#include "CoCoA/error.H"
#include "CoCoA/ideal.H"

#include <iosfwd>
// using std::ostream;
#include <vector>
// using std::vector;
#include <memory>
// using std::auto_ptr;


namespace CoCoA
{

  class symbol;         // fwd decl -- defined in symbol.H
  class matrix;         // fwd decl -- defined in matrix.H
 
  class SparsePolyIter; // fwd decl -- defined later in this file

  class SparsePolyIterBase
  {
  public:
    virtual ~SparsePolyIterBase() {};
    virtual SparsePolyIterBase* myClone() const = 0;

    virtual bool IamEnded() const = 0;
    virtual void myForward() = 0;
//    virtual SparsePolyIter& operator++() = 0;
//    virtual SparsePolyIter operator++(int) = 0;
    virtual RingElemAlias myCoeff() const = 0;
    virtual ConstRefPPMonoidElem myPP() const = 0;
//    virtual bool operator==(const iter& i2) const = 0;
//    virtual bool operator!=(const iter& i2) const = 0;
  protected:
    virtual const void* myPolyPtr() const = 0;
    virtual const void* myTermPtr() const = 0;
    friend class SparsePolyIter;
    friend bool operator==(const SparsePolyIter& i1, const SparsePolyIter& i2);
  };

  // This is a "smart pointer class" -- it owns the object pointed to.
  class SparsePolyIter
  {
  public:
    SparsePolyIter(SparsePolyIterBase* ptr);
    SparsePolyIter(const SparsePolyIter& copy);
    // Default dtor works OK
    SparsePolyIter& operator=(const SparsePolyIter& rhs);
    SparsePolyIterBase* operator->();
    const SparsePolyIterBase* operator->() const;
    SparsePolyIter& operator++();
    SparsePolyIter operator++(int);
  private:
    std::auto_ptr<SparsePolyIterBase> myIterPtr;
    friend bool IsEnded(const SparsePolyIter& i);
    friend RingElemAlias coeff(const SparsePolyIter& i);
    friend ConstRefPPMonoidElem PP(const SparsePolyIter& i);
    friend bool operator==(const SparsePolyIter& i1, const SparsePolyIter& i2);
  };


  class SparsePolyRingBase;

  /*-----------------------------------------------------------------*/
  /** \include SparsePolyRing.txt  */
  /*-----------------------------------------------------------------*/
  class SparsePolyRing: public PolyRing
  {
  public:
    SparsePolyRing(const ring& R);
    explicit SparsePolyRing(const SparsePolyRingBase* RingPtr);
    // Default copy ctor works fine.
    // Default dtor works fine.
  private: // disable assignment
    SparsePolyRing& operator=(const SparsePolyRing& rhs); ///< NEVER DEFINED -- assignment disabled
  public:
    const SparsePolyRingBase* operator->() const; ///< allow member fns to be called
  };


  class SparsePolyRingBase: public PolyRingBase
  {
  public:
    ///@name Member functions every concrete SparsePolyRing implementation must have in addition to those of PolyRingBase.
    //@{
    virtual const PPMonoid& myPPM() const = 0;
    virtual long myGradingDim() const { return GradingDim(myPPM()); }
    virtual bool IamStdGraded() const { return IsStdGraded(myPPM()); }
//    virtual void myWDeg(degree& d, ConstRawPtr rawf) const = 0;
//    virtual int myCmpWDeg(ConstRawPtr rawf, ConstRawPtr rawg) const = 0; //pure???
    virtual ConstRefPPMonoidElem myLPP(ConstRawPtr rawf) const = 0;
    virtual void myMulByPP(RawPtr rawf, PPMonoidElemConstRawPtr rawpp) const = 0;
    virtual bool myIsHomog(ConstRawPtr rawf) const; ///< true iff f is homogeneous
    virtual bool myIsHomogPartial(ConstRawPtr rawf, long n) const; ///< true iff f is homogeneous wrt the first n rows of weights (assumes 0 <= n <= GradingDim)
    virtual void myHomog(RawPtr rawfHom, ConstRawPtr rawf, ConstRawPtr rawh) const;  ///< fHom = f homogenized with h (indet of wdeg 1, GrDim=1)
    //@}

    ///@name   Functions for creating/building polynomials
    //@{
    virtual RingElem myMonomial(ConstRawPtr rawc, PPMonoidElemConstRawPtr rawpp) const = 0; // c is element of CoeffRing!!
    virtual SparsePolyIter myBeginIter(ConstRawPtr rawf) const = 0;
    virtual SparsePolyIter myEndIter(ConstRawPtr rawf) const = 0;
    virtual void myPushFront(RawPtr rawf, ConstRawPtr rawc, const std::vector<long>& expv) const = 0; /// SHOULD BE vector<BigInt> ????
    virtual void myPushBack(RawPtr rawf, ConstRawPtr rawc, const std::vector<long>& expv) const = 0; /// SHOULD BE vector<BigInt> ????
    virtual void myPushFront(RawPtr rawf, ConstRawPtr rawc, PPMonoidElemConstRawPtr rawpp) const = 0;
    virtual void myPushBack(RawPtr rawf, ConstRawPtr rawc, PPMonoidElemConstRawPtr rawpp) const = 0;
    //@}

    ///@name functions inherited from PolyRing with a unique implementation for all SparsePolyRing implementations
    //@{
    virtual long myStdDeg(ConstRawPtr rawf) const;              ///< returns standard degree of f
    virtual long myDeg(ConstRawPtr rawf, long index) const;  ///< returns the degree of f in x[index]
    virtual void myContent(RawPtr rawcontent, ConstRawPtr rawf) const; ///< exception clean
    virtual void myContentFrF(RawPtr rawcontent, ConstRawPtr rawf) const; ///< exception clean
    virtual void myCommonDenom(RawPtr rawcontent, ConstRawPtr rawf) const; ///< exception clean
    virtual void myClearDenom(RawPtr rawres, ConstRawPtr rawf) const; ///< exception clean
    virtual void myRemoveBigContent(RawPtr rawf) const;                ///< exception guarantee: same as myDivByCoeff
    virtual bool myIsValid(ConstRawPtr rawf) const;                    ///< true iff f is a valid representation, for debugging
    virtual const symbol& myIndetSymbol(long idx) const {return myPPM()->myIndetSymbol(idx);}
    //@}

    ///@name functions inherited from ring with a unique implementation for all SparsePolyRing implementations
    //@{
    using PolyRingBase::myOutputSelf; // disable warnings of overloading
    virtual void myOutputSelf(OpenMathOutput& OMOut) const;
    virtual void myMul(RawPtr rawlhs, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = x*y
    //    virtual void myDiv(RawPtr rawlhs, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = x/y
    virtual bool myIsDivisible(RawPtr rawlhs, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = x/y, if divisible
    virtual void myDeriv(RawPtr rawlhs, ConstRawPtr rawf, ConstRawPtr rawx) const; ///< lhs = deriv(f, x)
    virtual void mySymbols(std::vector<symbol>& SymList) const;            ///< append symbols in ring to SymList
    virtual RingElem mySymbolValue(const symbol& sym) const;               ///< returns the RingElem corresponding to sym
    virtual void myOutput(std::ostream& out, ConstRawPtr rawx) const;      ///< out << x
    virtual bool myIsPrintAtom(ConstRawPtr rawx) const;                    ///< x^n may be printed without parentheses
    virtual bool myIsPrintedWithMinus(ConstRawPtr rawx) const;             ///< x^n may be printed without parentheses
    virtual void myOutput(OpenMathOutput& OMOut, ConstRawPtr rawx) const;  ///< OMOut << x
    virtual bool myIsOne(ConstRawPtr rawf) const;                          ///< true iff rawf == 1
    virtual bool myIsMinusOne(ConstRawPtr rawf) const;                     ///< true iff rawf == -1
    virtual bool myIsConstant(ConstRawPtr rawf) const;                     ///< true iff rawf == c (possibly 0)
    virtual bool myIsIndet(long& IndetIndex, ConstRawPtr rawf) const;      ///< true iff rawf is x[IndetIndex]
    virtual bool myIsIndetPosPower(ConstRawPtr rawf) const;                ///< true iff rawf is x[i]^d for some i and d>0
    virtual bool myIsInvertible(ConstRawPtr rawx) const;                   ///< true iff x is invertible
    virtual void myGcd(RawPtr rawlhs, ConstRawPtr rawx, ConstRawPtr rawy) const; ///< lhs = gcd(x,y) if TrueGCDDomain;
    virtual void myNormalizeFracNoGcd(RawPtr rawnum, RawPtr rawden) const; ///< normalize fraction num/den
    virtual bool myIsInteger(BigInt& N, ConstRawPtr rawx) const;           ///< true iff x is integer
    virtual bool myIsRational(BigRat& Q, ConstRawPtr rawx) const;          ///< true iff x is rational
    virtual ideal myIdealCtor(const std::vector<RingElem>& gens) const;
    virtual void mySquare(RawPtr rawlhs, ConstRawPtr rawx) const;          ///< lhs = x^2
    virtual void myPowerSmallExp(RawPtr rawlhs, ConstRawPtr rawx, long n) const; ///< lhs = x^n, n>1, x not -1,0,1
    //@}

    ///@name  Functions for homomorphisms
    //@{
    virtual RingHom myCoeffEmbeddingHomCtor() const;
    RingHom myHomCtor(const ring& codomain, const RingHom& CoeffHom, const std::vector<RingElem>& IndetImages) const;
    RingHom myCompose(const RingHom& phi, const RingHom& theta) const;

    virtual bool myImageLiesInSubfield(const RingHom& phi) const;
    //@}

    ///@name Special functions on polynomials needed for implementing Buchberger's Algorithm
    //@{
    virtual bool myIsZeroAddLCs(RawPtr rawf, RawPtr rawg) const = 0; ///< f+=LM(g); g-=LM(g); assumes LPP(f)==LPP(g); returns LC(f)+LC(g)==0
    virtual void myMoveLM(RawPtr rawf, RawPtr rawg) const = 0;       ///< f+=LM(g); g-=LM(g); assumes LM(f)<LM(g)
    virtual void myDeleteLM(RawPtr rawf) const = 0;            // ????? right interface
    virtual void myDivLM(RawPtr rawlhs, ConstRawPtr rawf, ConstRawPtr rawg) const = 0; ///< lhs=div(LM(f),LM(g)); assumes f!=0,g!=0
    virtual int  myCmpLPP(ConstRawPtr rawf, ConstRawPtr rawg) const = 0; ///< cmp(LPP(f),LPP(g)); assumes f!=0,g!=0
    virtual void myAddClear(RawPtr rawf, RawPtr rawg) const = 0;     ///< f+=g; g=0;
    virtual void myAppendClear(RawPtr rawf, RawPtr rawg) const = 0;  ///< f+=g; g=0; appends g to f with no checks

    enum SkipLMFlag { SkipLMg, DontSkipLMg }; // used only for AddMul
    virtual void myAddMul(RawPtr rawf, ConstRawPtr rawh, ConstRawPtr rawg) const = 0; ///<  f += LM(h)*g
    virtual void myAddMul(RawPtr rawf, ConstRawPtr rawh, ConstRawPtr rawg, SkipLMFlag) const = 0; ///<  f += LM(h)*g
    virtual void myReductionStep(RawPtr rawf, ConstRawPtr rawg) const = 0;
    // ??? aggiungere coefficiente
    virtual void myReductionStepGCD(RawPtr rawf, ConstRawPtr rawg, RingElem& FScale) const = 0;
    // should it all be in ReductionStep ??? ANNA
    //@}

// need to be friend for SparsePolyRingBase::IdealImpl::ourGetPtr(I);
// not nice, not nice... (AMB 2011-04-19)
    friend bool AreGensMonomial(const ideal& I);
    friend bool AreGensSquareFreeMonomial(const ideal& I);
    friend void SetGBasisAsGens(const ideal& I);
    friend const std::vector<RingElem>& MinGens(const ideal& I);

  protected: // Ideal class for this type of ring.

    class IdealImpl: public IdealBase
    {
    friend const std::vector<RingElem>& MinGens(const ideal& I);
    public:
      IdealImpl(const SparsePolyRing& P, const std::vector<RingElem>& gens);
      // default copy ctor is OK
      virtual IdealBase* myClone() const;
//???    virtual void swap(ideal& other);

      virtual const SparsePolyRing& myRing() const;
      virtual bool IamZero() const;
      // virtual bool IamOne() const;  // default impl
      virtual bool IhaveMonomialGens() const;
      virtual bool IhaveSquareFreeMonomialGens() const;
      virtual void mySetGBasisAsGens() const;
      virtual void myReduceMod(RingElemRawPtr rawx) const; // r elem of R, where I is ideal in R
      virtual bool IhaveElem(RingElemConstRawPtr rawx) const;
      virtual void myClearGBasis();
      virtual void myAdd(const ideal&);
      virtual void myMul(const ideal&);
      virtual void myMulMonId(const ideal&);
      virtual void myIntersect(const ideal&);
      virtual void myIntersectMonId(const ideal&);
      virtual void myColon(const ideal&);
      virtual void myColonMonId(const ideal&);
      virtual void mySaturate(const ideal&);
      virtual void myMinimalize();
      virtual void myElim(const std::vector<RingElem>& ELimIndets);
      virtual void myElimMonId(const std::vector<RingElem>& ELimIndets);
      virtual bool myDivMod(RingElemRawPtr rawlhs, RingElemConstRawPtr rawnum, RingElemConstRawPtr rawden) const; // lhs = num/den modulo the ideal  (lhs = 0 if quotient does not exist)

      virtual const std::vector<RingElem>& myGens() const; // gens as specified by user
      virtual const std::vector<RingElem>& myTidyGens() const; // tidier set of gens
    protected:
      virtual void myMaximalTest() const;
      virtual void myPrimeTest() const;

    protected: // Data members of SparsePolyRingBase::IdealImpl
      SparsePolyRing myP;
      std::vector<RingElem> myGensValue;
      mutable bool3 IhaveMonomialGens3Value; ///< the given gens are monomials
      mutable bool3 IhaveSquareFreeMonomialGens3Value; ///< the given gens are square free monomials
      mutable bool myGBasisIsValid;
      mutable std::vector<RingElem> myGBasisValue;
      mutable std::vector<RingElem> myMinGensValue;
    protected: // Auxiliary functions.
      virtual const std::vector<RingElem>& myGBasis() const;
      virtual const std::vector<RingElem>& myMinGens() const;
      virtual const std::vector<RingElem>& myGBasisMonId() const;
    public:
      static const SparsePolyRingBase::IdealImpl* ourGetPtr(const ideal& J);
    };  // class IdealImpl

  protected:  // General homomorphism class for this type of ring.

    class HomImpl: public RingHomBase
    {
    public:
      HomImpl(const SparsePolyRing& domain, const ring& codomain, const RingHom& CoeffHom, const std::vector<RingElem>& IndetImages);
      virtual void myApply(RingElemRawPtr rawimage, RingElemConstRawPtr rawarg) const;
      virtual bool IamPartial() const { return IsPartial(myCoeffHom); }
    private:
      virtual void myOutputSelfDetails(std::ostream& out) const;

    private: // Data members of SparsePolyRingBase::HomImpl
      const RingHom myCoeffHom;
      const std::vector<RingElem> myIndetImages;
    };  // class HomImpl

  protected:  // Special homomorphism class for this type of ring.

    class CoeffEmbeddingHomImpl: public RingHomEmbeddingBase
    {
    public:
      CoeffEmbeddingHomImpl(const SparsePolyRing& P);
      virtual void myApply(RingElemRawPtr rawimage, RingElemConstRawPtr rawarg) const;
    };  // class CoeffEmbeddingHomImpl

  };


  SparsePolyRing NewPolyRing(const ring& CoeffRing, const PPMonoid& PPM);
  SparsePolyRing NewPolyRing(const ring& CoeffRing, const std::vector<symbol>& IndetNames, const PPOrdering& ord);
  SparsePolyRing NewPolyRing(const ring& CoeffRing, const std::vector<symbol>& IndetNames, const PPOrderingCtor& ord);
  SparsePolyRing NewPolyRing(const ring& CoeffRing, const std::vector<symbol>& IndetNames); ///< Default ordering is StdDegRevLex
  SparsePolyRing NewPolyRing(const ring& CoeffRing, long NumIndets); ///< Default ordering is StdDegRevLex
  SparsePolyRing NewPolyRing(const ring& CoeffRing, long NumIndets, const PPOrderingCtor& ord);
  bool IsSparsePolyRing(const ring& R);                         // inline
  const PPMonoid& PPM(const SparsePolyRing& Rx);                // inline
  long GradingDim(const SparsePolyRing& Rx);             // inline
  bool IsStdGraded(const SparsePolyRing& Rx);            // inline
  matrix OrdMat(const SparsePolyRing& Rx);
  matrix GradingMat(const SparsePolyRing& Rx);
  bool AreGoodIndetNames(const ring& CoeffRing, const std::vector<symbol>& IndetNames);

  ///@name Pseudo constructors for RingElems owned by SparsePolyRing
  //@{
  RingElem monomial(const SparsePolyRing& P, ConstRefRingElem c, ConstRefPPMonoidElem pp);
  RingElem monomial(const SparsePolyRing& P, const MachineInt& n, ConstRefPPMonoidElem pp);
  RingElem monomial(const SparsePolyRing& P, const BigInt& N, ConstRefPPMonoidElem pp);
  RingElem monomial(const SparsePolyRing& P, const BigRat& N, ConstRefPPMonoidElem pp);
  RingElem monomial(const SparsePolyRing& P, ConstRefRingElem c, const std::vector<long>& expv);
  RingElem monomial(const SparsePolyRing& P, const MachineInt& n, const std::vector<long>& expv);
  RingElem monomial(const SparsePolyRing& P, const BigInt& N, const std::vector<long>& expv);
  RingElem monomial(const SparsePolyRing& P, const BigRat& N, const std::vector<long>& expv);
  RingElem& PushFront(RingElem& f, ConstRefRingElem c, const std::vector<long>& expv); /// SHOULD BE vector<BigInt> ????
  RingElem& PushBack(RingElem& f, ConstRefRingElem c, const std::vector<long>& expv); /// SHOULD BE vector<BigInt> ????
  RingElem& PushFront(RingElem& f, ConstRefRingElem c, ConstRefPPMonoidElem pp); ///< with checks
  RingElem& PushBack(RingElem& f, ConstRefRingElem c, ConstRefPPMonoidElem pp); ///< with checks
  //@}

  RingElem ClearDenom(const SparsePolyRing& ZZx, const RingElem& f); // see also PolyRing.H

  ///@name Special functions on RingElems owned by SparsePolyRing
  //@{
  long UnivariateIndetIndex(ConstRefRingElem f);  // returns < 0 if not univariate
  ConstRefPPMonoidElem LPP(ConstRefRingElem f);                 // inline
  degree wdeg(ConstRefRingElem f);
  int CmpWDeg(ConstRefRingElem f1, ConstRefRingElem f2);
  int CmpWDegPartial(ConstRefRingElem f1, ConstRefRingElem f2, long GrDim);  // assumes 0 <= GrDim
  bool IsHomog(ConstRefRingElem f);
  bool IsHomogPartial(ConstRefRingElem f, long GrDim);  // assumes 0 <= GrDim
  RingElem homog(ConstRefRingElem f, ConstRefRingElem h);       ///< homogenize f with indet h (GrDim=1, wdeg(h)=1)
  RingElem LF(ConstRefRingElem f);
  RingElem NR(ConstRefRingElem f, const std::vector<RingElem>& v); ///< Normal Remainder (remainder of the Division Algorithm).  If v is a GBasis it gives the Normal Form.
  SparsePolyIter BeginIter(ConstRefRingElem f);
  SparsePolyIter EndIter(ConstRefRingElem f);
  std::vector<long> indets(ConstRefRingElem f);
  struct CoeffPP
  {
    CoeffPP(const RingElem& c, const PPMonoidElem& t): myCoeff(c), myPP(t) {}
    RingElem myCoeff;
    PPMonoidElem myPP;
  };
  std::ostream& operator<<(std::ostream& out, const CoeffPP& term);

  std::vector<CoeffPP> CoefficientsWRT(ConstRefRingElem f, const std::vector<long>& indets);
  std::vector<CoeffPP> CoefficientsWRT(ConstRefRingElem f, ConstRefRingElem x);
  std::vector<RingElem> CoeffVecWRT(ConstRefRingElem f, ConstRefRingElem x);
  RingElem ContentWRT(ConstRefRingElem f, const std::vector<long>& indets);
  RingElem ContentWRT(ConstRefRingElem f, ConstRefRingElem x);
  //@}

  ///@name Special functions on ideals owned by SparsePolyRing
  //@{
  bool IsZeroDim(const ideal& I);
  bool IsHomog(const ideal& I);
  const std::vector<RingElem>& GBasis(const ideal& I);
  const std::vector<RingElem>& MinGens(const ideal& I);
  std::vector<ideal> PrimaryDecomposition(const ideal& I);
  ideal LT(const ideal& I);
  ideal LF(const ideal& I);
  ideal homog(const ideal& I, ConstRefRingElem x);
  std::vector<PPMonoidElem> QuotientBasis(const ideal& I);
  bool AreGensMonomial(const ideal& I);
  bool AreGensSquareFreeMonomial(const ideal& I);
  void SetGBasisAsGens(const ideal& I);
  //@}

  //----------------------------------------------------------------------
  // Inline functions

  inline const SparsePolyRingBase* SparsePolyRingPtr(const ring& R)
  {
    return dynamic_cast<const SparsePolyRingBase*>(R.myRawPtr());
  }

  inline const PolyRingBase* SparsePolyRingPtr(const ring& R, const char* const FnName)
  {
    const SparsePolyRingBase* ptr = SparsePolyRingPtr(R);
    if (ptr == 0/*nullptr*/) CoCoA_ERROR(ERR::NotSparsePolyRing, FnName);
    return ptr;
  }

  inline bool IsSparsePolyRing(const ring& R)
  {
    return SparsePolyRingPtr(R) != 0/*nullptr*/;
  }


  inline SparsePolyRing::SparsePolyRing(const ring& R):
      PolyRing(SparsePolyRingPtr(R, "SparsePolyRing ctor"))
  {}

  inline SparsePolyRing::SparsePolyRing(const SparsePolyRingBase* RingPtr):
      PolyRing(RingPtr)
  {}


  inline const SparsePolyRingBase* SparsePolyRing::operator->() const
  { return static_cast<const SparsePolyRingBase*>(ring::operator->()); }


  inline const PPMonoid& PPM(const SparsePolyRing& Rx)
  { return Rx->myPPM(); }


  inline long GradingDim(const SparsePolyRing& Rx)
  { return Rx->myGradingDim(); }


  inline bool IsStdGraded(const SparsePolyRing& Rx)
  { return Rx->IamStdGraded(); }


  inline ConstRefPPMonoidElem LPP(ConstRefRingElem f)
  {
    if (!IsSparsePolyRing(owner(f)))
      CoCoA_ERROR(ERR::NotElemSparsePolyRing, "LPP(f)");
    if (IsZero(f)) CoCoA_ERROR(ERR::ZeroRingElem, "LPP(f)");
    return SparsePolyRingPtr(owner(f))->myLPP(raw(f));
  }


  inline SparsePolyIter::SparsePolyIter(SparsePolyIterBase* ptr):
      myIterPtr(ptr)
  {}


  inline SparsePolyIter::SparsePolyIter(const SparsePolyIter& copy):
      myIterPtr(copy.myIterPtr->myClone())
  {}


  inline SparsePolyIter& SparsePolyIter::operator=(const SparsePolyIter& rhs)
  {
//    if (&copy == this.get()) return *this;
    myIterPtr.reset(rhs.myIterPtr->myClone());
    return *this;
  }


  inline SparsePolyIterBase* SparsePolyIter::operator->()
  { return myIterPtr.get(); }


  inline const SparsePolyIterBase* SparsePolyIter::operator->() const
  { return myIterPtr.get(); }


  inline bool IsEnded(const SparsePolyIter& i)
  { return i.myIterPtr->IamEnded(); }


  inline SparsePolyIter& SparsePolyIter::operator++()
  {
    if (myIterPtr->IamEnded()) CoCoA_ERROR(ERR::PolyIterEnded, "++PolyIter");
    myIterPtr->myForward();
    return *this;
  }

  inline SparsePolyIter SparsePolyIter::operator++(int)
  {
    if (myIterPtr->IamEnded()) CoCoA_ERROR(ERR::PolyIterEnded, "PolyIter++");
    SparsePolyIter ans(*this);
    myIterPtr->myForward();
    return ans;
  }


  inline RingElemAlias coeff(const SparsePolyIter& i)
  {
    if (IsEnded(i)) CoCoA_ERROR(ERR::PolyIterEnded, "coeff(PolyIter)");
    return i.myIterPtr->myCoeff();
  }


  inline ConstRefPPMonoidElem PP(const SparsePolyIter& i)
  {
    if (IsEnded(i)) CoCoA_ERROR(ERR::PolyIterEnded, "PP(PolyIter)");
    return i.myIterPtr->myPP();
  }


  inline bool operator==(const SparsePolyIter& i1, const SparsePolyIter& i2)
  {
    if (i1.myIterPtr->myPolyPtr() != i2.myIterPtr->myPolyPtr())
      CoCoA_ERROR(ERR::MixedPolyIters, "PolyIter==PolyIter");
    return i1.myIterPtr->myTermPtr() == i2.myIterPtr->myTermPtr();
  }


  inline bool operator!=(const SparsePolyIter& i1, const SparsePolyIter& i2)
  {
    return !(i1==i2);
  }


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/SparsePolyRing.H,v 1.81 2014/07/31 13:10:46 bigatti Exp $
// $Log: SparsePolyRing.H,v $
// Revision 1.81  2014/07/31 13:10:46  bigatti
// -- GetMatrix(PPO) --> OrdMat(PPO)
// -- added OrdMat and GradingMat to PPOrdering, PPMonoid, SparsePolyRing
//
// Revision 1.80  2014/07/30 14:01:14  abbott
// Summary: Changed myAmbientRing into myRing
// Author: JAA
//
// Revision 1.79  2014/07/28 15:41:47  abbott
// Summary: Added myCoeffEmbeddingHomCtor
// Author: JAA
//
// Revision 1.78  2014/07/11 15:42:54  bigatti
// -- added  myOutputSelf (default impl),  myImplDetails()
//
// Revision 1.77  2014/07/07 17:11:57  abbott
// Summary: [MAJOR CHANGE] Removed AsSparsePolyRing; added SparsePolyRingPtr
// Author: JAA
//
// Revision 1.76  2014/05/06 13:20:41  abbott
// Summary: Changed names (my)MaxExponents into (my)Deg
// Author: JAA
//
// Revision 1.75  2014/04/15 13:28:55  abbott
// Summary: Added new ClearDenom fn (with 2 args)
// Author: JAA
//
// Revision 1.74  2014/03/27 14:57:23  bigatti
// -- added myMinimalize
//
// Revision 1.73  2014/01/28 13:08:32  bigatti
// -- added LF for ideal
//
// Revision 1.72  2013/07/30 15:01:51  bigatti
// -- added IsStdGraded
// -- removed empty lines in inline functions
//
// Revision 1.71  2013/06/28 17:03:51  abbott
// Modified semantics of IdealBase::myDivMod;
// it now returns a boolean.
// Several consequential changes.
//
// Revision 1.70  2013/06/12 08:49:17  bigatti
// -- added MinGens
//
// Revision 1.69  2013/05/30 13:13:58  bigatti
// -- added SetGBasisAsGens
//
// Revision 1.68  2013/05/28 13:29:54  abbott
// Added printing for struct CoeffPP.
//
// Revision 1.67  2013/02/21 12:50:44  abbott
// Added new fn UnivariateIndetIndex.
//
// Revision 1.66  2013/01/21 14:07:08  bigatti
// -- commented out "IamPartial"  (for the time being)
//
// Revision 1.65  2013/01/17 15:11:19  abbott
// Added new fn CoeffVecWRT.
// Added new virt mem fns myImageLiesInSubfield & IamPartial.
//
// Revision 1.64  2012/10/24 13:31:08  abbott
// Changed return type of coeff/myCoeff.
// Minor change to signature of CoefficientsWRT & ContentWRT.
//
// Revision 1.63  2012/10/17 09:40:16  abbott
// Replaced  RefRingElem  by  RingElem&
// (plus a few consequential changes)
//
// Revision 1.62  2012/10/05 10:21:39  bigatti
// -- added LF (leading form)
//
// Revision 1.61  2012/10/02 16:44:04  bigatti
// -- added homog for ideal
//
// Revision 1.60  2012/05/30 13:44:11  bigatti
// -- renamed IhaveMonomialGensB3Value --> IhaveMonomialGens3Value
//
// Revision 1.59  2012/05/22 10:02:37  abbott
// Removed IsGCDDomain; substituted by IsTrueGCDDomain.
// Added IsFractionFieldOfGCDDomain.
//
// Revision 1.58  2012/04/03 16:12:46  abbott
// Changed slightly the fn signatures of CoefficientsWRT.
// Added some things to *.C file -- will complete later.
//
// Revision 1.57  2012/03/02 14:21:46  bigatti
// -- added ContentWRT, CoefficientsWRT(f, x)
//
// Revision 1.56  2012/02/10 17:09:38  abbott
// Added new fns  indets, CoefficientsWRT, ContentWRT.
//
// Revision 1.55  2011/11/09 13:50:01  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.54  2011/08/24 10:21:09  bigatti
// -- renamed QQ --> BigRat
//
// Revision 1.53  2011/08/14 15:52:17  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.52  2011/07/05 15:02:17  bigatti
// -- added AlexanderDual
// -- added ad-hoc functions for colon, elim on monomial ideals
//
// Revision 1.51  2011/06/27 13:30:38  bigatti
// -- new file for monomial ideals:
// -- some functions moved there, but some have to be declared in SparsePolyRing.H
//
// Revision 1.50  2011/06/23 16:04:47  abbott
// Added IamExact mem fn for rings.
// Added myRecvTwinFloat mem fn for rings.
// Added first imple of RingHom from RingTwinFloat to other rings.
//
// Revision 1.49  2011/05/20 16:04:31  bigatti
// -- added QuotientBasis
//
// Revision 1.48  2011/05/19 13:56:12  abbott
// Removed comment out decl of old form of myIsRational.
//
// Revision 1.47  2011/04/19 13:59:49  bigatti
// -- added AreGensMonomial
//
// Revision 1.46  2011/04/12 09:52:15  bigatti
// -- added IsHomog(ideal), LT(ideal)
//
// Revision 1.45  2011/03/16 15:39:44  bigatti
// -- added myIsIndetPosPower(f), IsIndetPosPower(f)
//
// Revision 1.44  2011/03/16 13:21:23  abbott
// Added comments for myIsHomogPartial & myCmpWDegPartial.
// Cleaned up impls of myIsHomog & myIsHomogPartial.
// Corrected typo in impl of myElim.
//
// Revision 1.43  2011/03/11 10:53:38  bigatti
// -- added mySaturate
//
// Revision 1.42  2011/03/10 16:39:35  abbott
// Replaced (very many) size_t by long in function interfaces (for rings,
// PPMonoids and modules).  Also replaced most size_t inside fn defns.
//
// Revision 1.41  2011/03/01 14:10:47  bigatti
// -- added ClearDenom/myClearDenom
//
// Revision 1.40  2011/02/28 14:16:08  bigatti
// -- added GBasis(ideal)  -- only for SparsePolyRing
// -- error for myGcd when CoeffRing is not a field
//
// Revision 1.39  2011/01/28 17:58:07  bigatti
// -- added myElim
//
// Revision 1.38  2011/01/28 11:41:42  bigatti
// -- added IsPrintedWithMinus
// -- improved myOutput
// -- fixed bug in IsMinusOne
//
// Revision 1.37  2011/01/18 14:35:38  bigatti
// -- moved **_forC5 functions into CoCoA-5/CoCoALibSupplement:
//    myMonomials_forC5, mySupport_forC5, monomials_forC5, support_forC5,
//    LPP_forC5, LT_forC5, LM_forC5
//
// Revision 1.36  2010/11/30 11:34:37  bigatti
// -- added virtual myIndetSymbol
//
// Revision 1.35  2010/11/25 12:30:49  bigatti
// -- added myIndetsCalled
//
// Revision 1.34  2010/11/05 16:10:58  bigatti
// -- added myMonomials_forC5, mySupport_forC5
//
// Revision 1.33  2010/10/01 15:20:33  bigatti
// -- added mySymbolValue
// -- added RingElem(R, sym)
//
// Revision 1.32  2010/09/02 13:06:52  bigatti
// -- changed comment "fwd decl"
//
// Revision 1.31  2010/07/14 09:00:44  bigatti
// -- added comment
//
// Revision 1.30  2010/07/13 12:50:55  bigatti
// -- added comment
//
// Revision 1.29  2010/06/10 08:00:02  bigatti
// -- fixed naming conventions
//
// Revision 1.28  2010/03/18 13:55:56  abbott
// Added pseudo-ctors for monomials with BigRat coeffs.
//
// Revision 1.27  2010/03/05 18:43:48  abbott
// Added pseudo-ctors allowing polynomial rings to be created specifying
// the ordering using a PPOrderingCtor object.
//
// Revision 1.26  2010/02/04 09:57:11  bigatti
// -- added "mul" for ideals.  Implemented only for SparsePolyRing
//
// Revision 1.25  2010/01/21 14:12:28  bigatti
// -- swapped position of IhaveMonomialGens and myGBasisIsValid
//
// Revision 1.24  2009/11/26 17:21:34  bigatti
// -- added PushFront/PushBack(f, c, pp)
// -- added in .C inline functions:
// --   CheckCompatible, CheckElemSparsePolyRing, CheckCoeffExpv, CheckCoeffPP
//
// Revision 1.23  2009/10/02 13:27:26  bigatti
// -- unique implementation of myDiv in PolyRing.C
//
// Revision 1.22  2009/09/28 16:19:43  bigatti
// -- unique implementation for myDeriv
//
// Revision 1.21  2009/09/25 13:02:09  bigatti
// -- myDiv with one implementation in SparsePolyRing
//
// Revision 1.20  2009/09/22 14:07:33  bigatti
// -- added CmpWDegPartial and IsHomogPartial
//
// Revision 1.19  2009/07/24 12:26:43  abbott
// Added CommonDenom function for polynomials.
//
// Revision 1.18  2009/07/02 16:32:11  abbott
// Consequential changes stemming from new class BigRat, and modified interface to the member
// function RingBase::myIsRational.  Also some new conversion functions.
//
// Revision 1.17  2008/12/17 12:11:52  abbott
// Changed type from long to MachineInt in operations which use a machine integer
// in place of a RingElem.  The change is "superficial" but affects many files.
//
// Revision 1.16  2008/11/18 15:20:10  bigatti
// -- added const to myGBasis return value
// -- added myIdealCtor to RingWeyl for proper inheritance
//
// Revision 1.15  2008/10/09 15:15:05  bigatti
// -- added "bool3 IhaveMonomialGensValue" member field for ideals
//
// Revision 1.14  2008/04/21 12:32:54  abbott
// Corrected size_t into std::size_t in several header files; in some cases,
// replaced size_t with MachineInt (with consequent changes to impl files).
//
// Revision 1.13  2008/04/15 14:54:28  bigatti
// -- added mySquare
//
// Revision 1.12  2008/04/10 15:00:24  bigatti
// -- added  myPushBack/Front(RawPtr, ConstRawPtr, PPMonoidElemConstRawPtr)
//
// Revision 1.11  2008/03/12 16:35:18  bigatti
// -- changed: IsHomogeneous --> IsHomog
// -- changed: ERR:ZeroPoly --> ERR::ZeroRingElem
//
// Revision 1.10  2007/12/07 15:27:01  bigatti
// -- default implementation of "IamOne" in ideal.C
//
// Revision 1.9  2007/12/05 11:06:24  bigatti
// -- changed "size_t StdDeg/myStdDeg(f)" into "long"  (and related functions)
// -- changed "log/myLog(f, i)" into "MaxExponent/myMaxExponent(f, i)"
// -- fixed bug in "IsOne(ideal)" in SparsePolyRing.C
//
// Revision 1.8  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.7  2007/10/05 15:17:13  bigatti
// -- just a proper sorting on functions
//
// Revision 1.6  2007/05/31 16:31:53  bigatti
// -- adjusted comments
//
// Revision 1.5  2007/05/31 15:43:56  bigatti
// -- added mySymbols and AreGoodIndetNames
//
// Revision 1.3  2007/05/22 22:45:14  abbott
// Changed fn name IsUnit to IsInvertible.
//
// Revision 1.2  2007/03/12 16:00:29  bigatti
// -- moved myLog(F, index) into unique implementation in SparsePolyRing
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.23  2007/03/08 18:42:05  cocoa
// Cleaned up whitespace.
//
// Revision 1.22  2007/03/08 11:07:12  cocoa
// Made pseudo ctors for polynomial rings more uniform.  This allowed me to
// remove an include of CoCoA/symbol.H  from the RingDistrM*.H files, but then
// I had to put the include in several .C files.
//
// Revision 1.21  2007/02/10 18:44:03  cocoa
// Added "const" twice to each test and example.
// Eliminated dependency on io.H in several files.
// Improved BuildInfo, and added an example about how to use it.
// Some other minor cleaning.
//
// Revision 1.20  2007/01/20 14:07:25  bigatti
// -- moved code for homomorphism into common implementation in SparsePolyRing
//
// Revision 1.19  2007/01/15 13:33:34  cocoa
// -- added prefix "raw" to RawPtr arguments names
//
// Revision 1.18  2006/12/07 17:36:19  cocoa
// -- migrated  myRemoveBigContent myContent myPowerSmallExp  into
//    single implementation in SparsePolyRing
// -- removed  content  from DistrMPoly(..)
//
// Revision 1.17  2006/12/06 17:25:43  cocoa
// -- removed #include "config.H"
//
// Revision 1.16  2006/11/23 17:43:53  cocoa
// -- minor change
//
// Revision 1.15  2006/11/22 17:51:31  cocoa
// -- moved printing functions into unified implementation in SparsePolyRing
//
// Revision 1.14  2006/11/21 18:09:24  cocoa
// -- added myIsMonomial
// -- implemented myIsOne, myIsMinusOne, myIsConstant, myIsIndet in SparsePolyRing
// -- removed the 4 functions from DistrMPoly(..) and RingDistrMPoly(..)
// -- changed all names of RawPtr arguments into "raw(..)"
//
// Revision 1.13  2006/11/20 15:55:02  cocoa
// ring is now a class again.  Improved definitions of operator-> in derived classes.
//
// Revision 1.12  2006/11/14 17:36:49  cocoa
// -- fixed implementation for ideal in RingWeyl
//
// Revision 1.11  2006/11/09 17:46:58  cocoa
// -- version 0.9712:
// --   IdealImpl moved to SparsePolyRing from concrete rings
// -- PolyList in GTypes is now vector<RingElem> (was list)
// -- "my" coding convention applied to DistrMPoly
//
// Revision 1.10  2006/11/03 14:01:46  cocoa
// -- changed: reference counting in ring, PPMonoids and OrdvArith now
//    uses SmartPtrIRC
//
// Revision 1.9  2006/11/02 13:25:44  cocoa
// Simplification of header files: the OpenMath classes have been renamed.
// Many minor consequential changes.
//
// Revision 1.8  2006/10/06 14:04:15  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//
// Revision 1.7  2006/08/17 09:45:07  cocoa
// -- added: homogenization
//
// Revision 1.6  2006/08/07 21:23:25  cocoa
// Removed almost all publicly visible references to SmallExponent_t;
// changed to long in all PPMonoid functions and SparsePolyRing functions.
// DivMask remains to sorted out.
//
// Revision 1.5  2006/07/20 17:06:08  cocoa
// -- moved myStdDeg into SparsePolyRing
//
// Revision 1.4  2006/07/20 16:51:38  cocoa
// -- added common implementation of myStdDeg
//
// Revision 1.3  2006/07/17 11:05:53  cocoa
// -- added: myIsValid, myIsHomogeneous, IsHomogeneous
//
// Revision 1.2  2006/06/08 16:45:28  cocoa
// -- RingDistrMPoly*.H  have been "moved" into RingDistrMPoly*.C
// -- some coding conventions fixed in DistrMPoly*
// -- functions wdeg and CmpWDeg have a common implementation in SparsePolyRing
//
// Revision 1.1.1.1  2006/05/30 11:39:37  cocoa
// Imported files
//
// Revision 1.10  2006/05/29 16:22:37  cocoa
// Third time lucky???
// Added myIsInteger member function to all rings (NYI for RingFloat).
//
// Revision 1.9  2006/05/12 17:01:28  cocoa
// -- added myIsUnit, myGcd whose implementation in RingDistr*** were identical
//
// Revision 1.8  2006/04/27 13:06:19  cocoa
// -- just spaces
//
// Revision 1.7  2006/04/26 16:44:53  cocoa
// -- myMul has now a single implementation in SparsePolyRing
// -- myMul and mul in RingDistrMPoly* and DistrMPoly* have been disabled
//
// Revision 1.6  2006/04/21 14:58:04  cocoa
// Removed myWDeg member function: it is no longer needed.
//
// Revision 1.5  2006/03/17 18:13:25  cocoa
// -- changed: myMul --> myMulByPP
//
// Revision 1.4  2006/03/12 21:28:34  cocoa
// Major check in after many changes
//
// Revision 1.3  2006/03/07 10:11:06  cocoa
// -- fixed:  PPMonoidElem myLPP/LPP now return ConstRefPPMonoidElem
//
// Revision 1.2  2006/01/19 16:34:42  cocoa
// -- added NF, myReduceMod functions (not yet tested)
//
// Revision 1.1.1.1  2005/10/17 10:46:54  cocoa
// Imported files
//
// Revision 1.5  2005/09/30 15:03:39  cocoa
// Minor cleaning and tidying.
// DistrMPolyInlPP: use of summands now rather cleaner.
//
// Revision 1.4  2005/08/08 16:36:32  cocoa
// Just checking in before going on holiday.
// Don't really recall what changes have been made.
// Added IsIndet function for RingElem, PPMonoidElem,
// and a member function of OrdvArith.
// Improved the way failed assertions are handled.
//
// Revision 1.3  2005/07/19 15:30:20  cocoa
// A first attempt at iterators over sparse polynomials.
// Main additions are to SparsePolyRing, DistrMPoly*.
// Some consequential changes to PPMonoid*.
//
// Revision 1.2  2005/07/15 16:34:33  cocoa
// Added iterators for sparse polynomials.
// The code compiles (and the old tests still run).
// It'd Friday evening -- I'm going home before
// getting any ideas about making the iterator code run.
//
// Revision 1.1  2005/07/01 16:08:16  cocoa
// Friday check-in.  Major change to structure under PolyRing:
// now SparsePolyRing and DUPolyRing are separated (in preparation
// for implementing iterators).
//
// A number of other relatively minor changes had to be chased through
// (e.g. IndetPower).
//

#endif
