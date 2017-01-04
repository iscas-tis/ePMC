#ifndef CoCoA_NumTheory_H
#define CoCoA_NumTheory_H

//   Copyright (c)  2009,2012  John Abbott

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


#include "CoCoA/BigRat.H"
#include "CoCoA/factorization.H"

namespace CoCoA
{
  
  class MachineInt; // fwd decl -- defined in MachineInt.H

  // Frankly, 8 separate cases seems a bit excessive!
  long   PowerMod(const MachineInt& base, const MachineInt& exp, const MachineInt& modulus);
  long   PowerMod(const MachineInt& base, const BigInt& exp,     const MachineInt& modulus);
  long   PowerMod(const BigInt& base,     const MachineInt& exp, const MachineInt& modulus);
  long   PowerMod(const BigInt& base,     const BigInt& exp,     const MachineInt& modulus);
  BigInt PowerMod(const MachineInt& base, const MachineInt& exp, const BigInt& modulus);
  BigInt PowerMod(const BigInt& base,     const MachineInt& exp, const BigInt& modulus);
  BigInt PowerMod(const MachineInt& base, const BigInt& exp,     const BigInt& modulus);
  BigInt PowerMod(const BigInt& base,     const BigInt& exp,     const BigInt& modulus);

  long         gcd(const MachineInt& a, const MachineInt& b);
  const BigInt gcd(const BigInt& A,     const MachineInt& b);
  const BigInt gcd(const MachineInt& a, const BigInt& B);
  const BigInt gcd(const BigInt& A,     const BigInt& B);

  long ExtGcd(long& CofacA, long& CofacB, const MachineInt& a, const MachineInt& b);
//???? signature???  BigInt ExtGcd(BigInt& CoFacA, BigInt& CofacB, const MachineInt& A, const BigInt& B);
//???? signature???  BigInt ExtGcd(BigInt& CoFacA, BigInt& CofacB, const BigInt& A, const MachineInt& B);
  BigInt ExtGcd(BigInt& CoFacA, BigInt& CofacB, const BigInt& A, const BigInt& B);

  long   InvMod(const MachineInt& r, const MachineInt& m);
  long   InvMod(const BigInt& r,     const MachineInt& m);
  BigInt InvMod(const MachineInt& r, const BigInt& m);
  BigInt InvMod(const BigInt& r,     const BigInt& m);

  long         lcm(const MachineInt& a, const MachineInt& b);
  const BigInt lcm(const BigInt& A,     const MachineInt& b);
  const BigInt lcm(const MachineInt& a, const BigInt& B);
  const BigInt lcm(const BigInt& a,     const BigInt& b);


  std::vector<bool> eratosthenes(const MachineInt& n);

  bool IsPrime(const MachineInt& n);
  bool IsPrime(const BigInt& N); // might be very slow!!
  bool IsProbPrime(const MachineInt& n); // probably not very useful
  bool IsProbPrime(const MachineInt& n, const MachineInt& NumIters); // probably not very useful
  bool IsProbPrime(const BigInt& N);
  bool IsProbPrime(const BigInt& N, const MachineInt& NumIters);

  long NextPrime(const MachineInt& n); ///< next prime number after n or 0 if there is none, must have n>0
  long PrevPrime(const MachineInt& n); ///< previous prime number before n or 0 if there is none, must have n>0

  // !!No NextPrime for BigInt!!  Use NextProbPrime and call IsPrime if you want a (costly) guarantee of primality.
  BigInt NextProbPrime(const BigInt& N); ///< next probable prime number after N, requires N > 1
  BigInt NextProbPrime(BigInt N, const MachineInt& NumIters); ///< next probable prime number after N, requires N > 1
  BigInt PrevProbPrime(const BigInt& N); ///< previous probable prime number before N, requires N > 1
  BigInt PrevProbPrime(BigInt N, const MachineInt& NumIters); ///< previous probable prime number before N, requires N > 1


  factorization<long>   SmoothFactor(const MachineInt& N, const MachineInt& TrialLimit);
  factorization<BigInt> SmoothFactor(const BigInt& N,     const MachineInt& TrialLimit);
  factorization<BigInt> SmoothFactor(const BigInt& N,     const BigInt& TrialLimit);

  factorization<long>   factor(const MachineInt& N);
  factorization<BigInt> factor(const BigInt& N);


  long valuation(const MachineInt& p, const MachineInt& n);
  long valuation(const MachineInt& p, BigInt N);
  long valuation(const BigInt& p, BigInt N);
  long   EulerPhi(const MachineInt& n);
  BigInt EulerPhi(const BigInt& N);

  long   MultiplicativeOrderMod(const MachineInt& residue, const MachineInt& modulus);
  long   MultiplicativeOrderMod(const BigInt& residue,     const MachineInt& modulus);
  BigInt MultiplicativeOrderMod(const MachineInt& residue, const BigInt& modulus);
  BigInt MultiplicativeOrderMod(const BigInt& residue,     const BigInt& modulus);
  long PrimitiveRoot(const MachineInt& p);
  long PrimitiveRoot(const BigInt& P);

  //////////////////////////////////////////////////////////////////////
  BigRat SimplestBigRatBetween(const BigRat& lo, const BigRat& hi);

  //////////////////////////////////////////////////////////////////////

  class CRTMill
  {
  public:
    CRTMill(): myR(0), myM(1) {}
    // default copy ctor and dtor are fine
    enum CoprimeFlag { CheckCoprimality=0, CoprimeModulus=1};
    void myAddInfo(const MachineInt& r, const MachineInt& m, CoprimeFlag check=CheckCoprimality);
    void myAddInfo(const BigInt& r, const BigInt& m, CoprimeFlag check=CheckCoprimality);
//    const BigInt& myResidue() const { return myR; }
//    const BigInt& myModulus() const { return myM; }
    friend const BigInt& residue(const CRTMill& CRT) { return CRT.myR; }
    friend const BigInt& modulus(const CRTMill& CRT) { return CRT.myM; }
    friend std::ostream& operator<<(std::ostream& out, const CRTMill& CRT);
  private: // data members
    BigInt myR;
    BigInt myM;
  };

//???  BigInt CRT(const std::vector<long>& R, const std::vector<long>& M);
  const BigInt& residue(const CRTMill& CRT);
  const BigInt& modulus(const CRTMill& CRT);
  std::ostream& operator<<(std::ostream& out, const CRTMill& CRT);

  //////////////////////////////////////////////////////////////////////

  class ContFracIter
  {
  public:
    explicit ContFracIter(const BigRat& Q);
    const BigInt& operator*() const;
    ContFracIter& operator++();
    ContFracIter operator++(int);
    friend bool IsEnded(const ContFracIter& CFIter);
    friend bool IsFinal(const ContFracIter& CFIter);
  private: // data members
    BigRat myFrac;
    BigInt myQuot;
    friend std::ostream& operator<<(std::ostream& out, const ContFracIter& CFIter);
  };

  bool IsEnded(const ContFracIter& CFIter);
  bool IsFinal(const ContFracIter& CFIter);

  inline const BigInt& quot(const ContFracIter& CFIter)
  { return *CFIter; }

  //////////////////////////////////////////////////////////////////

  class ContFracApproximant
  {
  public:
    ContFracApproximant();
    void myAppendQuot(const MachineInt& q);
    void myAppendQuot(const BigInt& q);
    const BigRat& myRational() const { return myCurr; }
  private: // data members
    BigRat myCurr; // WARNING: initial value is anomalous, namely 1/0
    BigRat myPrev;
    BigRat myNext; // Just workspace; used only in operator++
    friend std::ostream& operator<<(std::ostream& out, const ContFracApproximant& CFConv);
  };

  std::ostream& operator<<(std::ostream& out, const ContFracApproximant& CFConv);

  //////////////////////////////////////////////////////////////////

  class CFApproximantsIter
  {
  public:
    explicit CFApproximantsIter(const BigRat& Q);
    explicit CFApproximantsIter(const ContFracIter& CFIter);
    const BigRat& operator*() const { return myApproximant.myRational(); }
    CFApproximantsIter& operator++();
    CFApproximantsIter operator++(int);
    friend bool IsEnded(const CFApproximantsIter& CFAIter) { return IsEnded(CFAIter.myCFIter); }
  private: // data members
    ContFracIter myCFIter;
    ContFracApproximant myApproximant;
    friend std::ostream& operator<<(std::ostream& out, const CFApproximantsIter& CFAIter);
  };

  std::ostream& operator<<(std::ostream& out, const ContFracIter& CFIter);
  std::ostream& operator<<(std::ostream& out, const CFApproximantsIter& CFAIter);

  BigRat CFApprox(const BigRat& q, const BigRat& MaxRelErr);


  //////////////////////////////////////////////////////////////////
  // Heuristic fault-tolerant rational reconstruction

  class RatReconstructByContFrac
  {
  public:
    explicit RatReconstructByContFrac(const MachineInt& threshold);
    explicit RatReconstructByContFrac(const BigInt& threshold);
    void myAddInfo(const MachineInt& r, const MachineInt& m);
    void myAddInfo(const BigInt& R, const BigInt& M);
    friend const BigRat& ReconstructedRat(const RatReconstructByContFrac& ModularImages);
    friend bool IsConvincing(const RatReconstructByContFrac& ModularImages);
    friend std::ostream& operator<<(std::ostream& out, const RatReconstructByContFrac& reconstructor);
  private: // data members
    CRTMill myCRT;
    const BigInt myThresholdValue;
    mutable bool myResultIsUpToDate;
    mutable bool myResultIsConvincing;
    mutable BigRat myResult;
    mutable BigInt myBadFactors;
  private: // impl details
    BigInt myThreshold(const BigInt& M) const;
    static BigInt myCheckThreshold(const BigInt& threshold);
    void myUpdateResult() const;
  };

  const BigRat& ReconstructedRat(const RatReconstructByContFrac& ModularImages);
  bool IsConvincing(const RatReconstructByContFrac& ModularImages);
  std::ostream& operator<<(std::ostream& out, const RatReconstructByContFrac& reconstructor);

  //////////////////////////////////////////////////////////////////
  // Heuristic fault-tolerant rational reconstruction

  class RatReconstructByLattice
  {
  public:
    explicit RatReconstructByLattice(const MachineInt& SafetyFactor); // SafetyFactor==0 --> use default value
    explicit RatReconstructByLattice(const BigInt& SafetyFactor);
    void myAddInfo(const MachineInt& r, const MachineInt& m);
    void myAddInfo(const BigInt& r, const BigInt& m);
    friend const BigRat& ReconstructedRat(const RatReconstructByLattice& ModularImages);
    friend bool IsConvincing(const RatReconstructByLattice& ModularImages);
    friend std::ostream& operator<<(std::ostream& out, const RatReconstructByLattice& reconstructor);
  private: // data members
    CRTMill myCRT;
    const BigInt mySafetyFactor;
    mutable bool myResultIsUpToDate;
    mutable bool myResultIsConvincing;
    mutable BigRat myResult;
  private: // impl details
    static const long ourDefaultSafetyFactor;
    static BigInt myCheckSafetyFactor(const BigInt& SafetyFactor);
    void myUpdateResult() const;
  };

  const BigRat& ReconstructedRat(const RatReconstructByLattice& ModularImages);
  bool IsConvincing(const RatReconstructByLattice& ModularImages);
  std::ostream& operator<<(std::ostream& out, const RatReconstructByLattice& reconstructor);

  BigRat RatReconstructWithBounds(long e, const BigInt& P, const BigInt& Q, const std::vector<long>& res, const std::vector<long>& mod);

  // Repr of an int as sum of binomials:
  std::vector<BigInt> BinomialRepr(BigInt N, long r);
  BigInt BinomialReprShift(BigInt N, long r, long shift1, long shift2);

  BigInt NumPartitions(const MachineInt& n);

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/NumTheory.H,v 1.29 2014/09/16 10:41:41 abbott Exp $
// $Log: NumTheory.H,v $
// Revision 1.29  2014/09/16 10:41:41  abbott
// Summary: Added new fn eratosthenes (with doc, example, test)
// Author: JAA
//
// Revision 1.28  2014/08/29 16:04:55  abbott
// Summary: Added optional 3rd arg to myAddInfo (so coprimality check is skipped)
// Author: JAA
//
// Revision 1.27  2014/05/02 13:54:05  abbott
// Summary: Simplified ctor interface for RatReconstruct* (need explicit arg 0 for default behaviour)
// Author: JAA
//
// Revision 1.26  2014/04/15 13:27:19  abbott
// Summary: Changed rtn type of PrimitiveRoot to long (for CoCoA-5/BuiltinOneLiners)
// Author: JAA
//
// Revision 1.25  2014/01/16 16:09:54  abbott
// Added NumPartitions.
//
// Revision 1.24  2013/10/15 16:20:03  abbott
// Added valuation.
//
// Revision 1.23  2013/05/21 14:31:45  abbott
// Added BinomialRepr and BinomialReprShift to CoCoALib and CoCoA-5.
//
// Revision 1.22  2013/05/20 15:47:09  abbott
// Added new fn BinomialRepr (placed in NumTheory).
//
// Revision 1.21  2013/02/26 11:29:17  abbott
// Added impl of RatReconstructWithBounds
//
// Revision 1.20  2013/02/22 22:43:06  abbott
// Changed syntax for getting result from CRTMill.
//
// Revision 1.19  2013/02/22 18:56:50  abbott
// Added feature that RatReconstructByContFrac & RatReconstructByLattice
// ctors accept arg 0 to mean "use default value".
//
// Revision 1.18  2013/02/19 18:48:15  abbott
// Added printing for CRTMill and RatReconstructByContFrac and RatReconstructByLattice.
//
// Revision 1.17  2013/02/15 17:46:00  abbott
// Added RatReconstructByContFrac and RatReconstructByLattice.
//
// Revision 1.16  2012/12/12 18:24:26  abbott
// Added new fn IsFinal for ContFracIter.
//
// Revision 1.15  2012/12/11 17:30:30  abbott
// Changed name from SimplestRationalInInterval to SimplestBigRatBetween.
// Also fixed a bug in the impl.
//
// Revision 1.14  2012/12/05 15:09:24  abbott
// Added new class ContFracApproximant.
// Added new fn SimplestRationalInInterval (NB name will change).
// Some minor cleaning.
//
// Revision 1.13  2012/12/04 20:14:11  abbott
// Added new class CRTMill.
// Improved impl of ContFracIter class.
// Fixed a bug in CFApproxIter class.
//
// Revision 1.12  2012/05/28 09:18:21  abbott
// Created IntOperations which gathers together all operations on
// integers (both big and small).  Many consequential changes.
//
// Revision 1.11  2012/05/22 10:03:32  abbott
// Improved indentation/layout.
//
// Revision 1.10  2012/03/16 15:40:12  abbott
// Merged contents of NumTheoryQQ (continued fraction functions) into NumTheory.
// Merged the doc too.
//
// Revision 1.9  2011/11/09 13:47:56  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.8  2011/08/14 15:52:18  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.7  2011/03/23 21:00:46  abbott
// Removed FindPrimRoot from NumTheory.H because it was already
// available as PrimitiveRoot (a better name).
// Updated documentation for NumTheory.
//
// Revision 1.6  2011/03/22 20:17:18  abbott
// Added fn FindPrimRoot.
// Merged impls from obsolescent SmallPrime.C.
//
// Revision 1.5  2011/03/16 13:26:36  abbott
// Removed all "unsigned" from fn interfaces, and many unsigned from inside fn impls.
//
// Revision 1.4  2010/03/03 10:43:34  abbott
// Added PrimitiveRoot for big primes (might be very slow).
// Added MultiplicativeOrderMod (currently very SLUGGY implementation).
//
// Revision 1.3  2009/12/29 22:44:32  abbott
// Removed buggy proxy class ZZ::rtn.
// Consequent changes for function prototypes also in NumTheory.
// Corrected some minor buglets in NumTheory.
//
// Revision 1.2  2009/07/02 16:28:10  abbott
// Fairly extensive change to NumTheory (at least internally and philosophically).
// Improved and cleaned NumTheory.  Added documentation.
// Clarified the exact behaviour of most functions.
//
// Revision 1.1  2009/06/05 12:14:55  abbott
// Major change:
//   created new files NumTheory.H/C  which contain basic number theory operations
//   removed several basic number theory operations from ZZ.H/C
//   removed gcd from MachineInt.H/C
//   changed names of some basic fns:
//      IsPPrime -> IsProbPrime
//      invmod -> InvMod    (changed signature too)
//      powermod -> PowerMod  (changed signature too)
//   added new fns
//      NextProbPrime & PrevProbPrime
//   consequent changes to other code and tests and examples
//
//

#endif
