#ifndef CoCoA_JBOptions_H
#define CoCoA_JBOptions_H

#include <iostream>
#include <string>
#include "CoCoA/BigInt.H"
#include "CoCoA/IntOperations.H"
#include "CoCoA/ring.H"
#include "CoCoA/SparsePolyRing.H"
#include "CoCoA/symbol.H"
#include "CoCoA/TmpJBDatastructure.H"
#include "CoCoA/TmpJBSets.H"
#include "CoCoA/RingQQ.H"
#include "CoCoA/FreeModule.H"
#include "CoCoA/RingZZ.H"
#include "CoCoA/ModuleOrdering.H"
#include "CoCoA/submodule.H"
#include "CoCoA/bool3.H"
#include "CoCoA/RingHom.H"
#include "CoCoA/QuotientRing.H"
#include <set>
#include <vector>
#include <bitset>
#include <map>

namespace CoCoA
{

  // different strategys
  enum StrategyFlag {TQDegree, TQBlockHigh, TQBlockLow}; 

  // the output
  enum ResOutputFlag{GB,JB};

  /****************************************************************************************************************************/
  /*!\brief This class compares the LPP of the polynoms in Janet Triples
   */
  /*****************************************************************************************************************************/
  struct IsLowerLPP
  {
    /**
     * The "compare" function
     * @param t1 JanetTriple
     * @param t2 JanetTriple
     * The output is: LPP(t1.myPolynom)<LPP(t2.myPolynom)
     **/
    bool operator() (const JanetTriple& t1, const JanetTriple& t2)
    {
      return ((LPP(t1.myPolynom)<LPP(t2.myPolynom)) || ((LPP(t1.myPolynom) == LPP(t2.myPolynom)) && (t1.myAncestor < t2.myAncestor)));
    }
  };


  /****************************************************************************************************************************/
  /*!\brief This class stores the infos about the ring which we use
   * It contains the following informations:
   * The SparsePolyRing
   * The PPMonoid
   * The CoefficientRing
   */
  /*****************************************************************************************************************************/
  class JBEnv
  {
  public:
    //several constructors
    JBEnv(SparsePolyRing PolyRing, PPMonoid PPM, ring CoefRing): myPolyRing(PolyRing), myPPMValue(PPM), myCoefRing(CoefRing)
    {
    }

    JBEnv(SparsePolyRing PolyRing, PPMonoid PPM):myPolyRing(PolyRing), myPPMValue(PPM), myCoefRing(CoeffRing(PolyRing))
    {
    }

    JBEnv(SparsePolyRing PolyRing):myPolyRing(PolyRing), myPPMValue(PPM(PolyRing)), myCoefRing(CoeffRing(PolyRing))
    {
    }

    JBEnv(ring CoefRing, long numIndets): myPolyRing(NewPolyRing(CoefRing,numIndets)), myPPMValue(PPM(myPolyRing)), myCoefRing(CoefRing)
    {
    }

    JBEnv(ring CoefRing, PPOrdering ordering): myPolyRing(NewPolyRing(CoefRing, SymbolRange("x",0,NumIndets(ordering)-1), ordering)), myPPMValue(PPM(myPolyRing)), myCoefRing(CoefRing)
    {
    }
 
    SparsePolyRing myPolyRing;
    PPMonoid myPPMValue;
    ring myCoefRing;

  };


  /****************************************************************************************************************************/
  /*!\brief This class stores informations about the options which we use in the computation
   * It contains the following informations:
   * Information about the basic ring
   * The algorithm which we shall use
   * When we use algorithm "strategy 2" the selectionStrategy
   * Which criteria we want to use
   * If we want to store some computations in the normalform (This option will not be implemented in this bachelorthesis)
   * Which output
   */
  /********************************************************************************************************************************/
  class JBFlag
  {
  public:

    //constructor
    JBFlag(JBEnv options, std::bitset<3> crit = std::bitset<3>(), ResOutputFlag res = GB): 
      myRingOptions(options), myCriteria(crit), myResult(res)
    {
    }

    JBEnv myRingOptions;
    std::bitset<3> myCriteria;
    ResOutputFlag myResult;
  };


  /****************************************************************************************************************************/
  /*!\brief This class stores the datas during the computation
   * It contains the following datas:
   * JBFlag with JBEnv
   * the input set (as std::vector<RingElem>)
   * the list setT (for the TQ-Algorithms)
   * the multiset setQ (for the TQ-Algorithms (noStrategy)
   * the multiset P (for the algorithm 1 and 2)
   * the janetTree
   * // a counting varibale (for debugging) //
   *
   * Also there are two functions which return the JB or GB as std::vector<RingElem> 
   */
  /*****************************************************************************************************************************/
  class JBMill
  {
  public:
      /**
       * Constructor. If there is an empty or zero input raises an error
       */
      JBMill(std::vector<RingElem> NewInput, JBFlag NewOptions):myOptions(NewOptions), myJTree(myOptions.myRingOptions.myPolyRing, 0, 0)  
      {
        for(std::vector<RingElem>::iterator iter = NewInput.begin(); iter != NewInput.end(); ++iter)
        {
          if(!IsZero(*iter))
          {
            myInput.push_back(*iter);
          }
        }
        if(myInput.empty())
        {
          CoCoA_ERROR("Empty input (or your input only contains zeros)", "JBMill");
        }
      }

    // returns minimal Janet Basis
    std::vector<RingElem> myReturnJB() const;
    // returns reduced Groebner Basis
    std::vector<RingElem> myReturnGB() const;
    


    /**
     * This algorithm computes a Janet basis. It uses the algorithm
     * which is described in the paper "Construction of Janet Bases
     * II. Polynomial Bases"
     */
    void myDegreeTQ();

    /**
     * This algorithm compute a Janet basis. It uses the algorithm
     * which is described in the paper "On Computing Janet Bases 
     * for Degree Compatible Orderings"
     */
    void myBlockTQ(bool updateHigh);

    /**
     * This function print the nonmultiplicative variables of every element in our Janet-basis
     */
    void myPrintNonMultVar() const;

    /*
     * this function computes the multiplicative vars of our Janet-basis
     */
    std::map<PPMonoidElem, std::vector<bool> > myComputeMultVars() const;    

    /*
     * this function computes the non-multiplicative vars of our Janet-basis
     */
    std::map<PPMonoidElem, std::vector<bool> > myComputeNonMultVars() const;    

    /*
     * this function computes the non-multiplicative vars of our Janet-basis
     * but it returns the complete basis element
     */
    std::vector< std::pair<RingElem, std::vector<bool> > > myComputeNonMultVarsWithRingElem() const;

    /*
     * this function computes the multiplicative vars of our Janet-basis
     * but it returns the complete basis element
     */
    std::vector< std::pair<RingElem, std::vector<bool> > > myComputeMultVarsWithRingElem() const;

    /* 
     * this function computes the NonMultVars of the basis element elem. 
     * If the element is not a basis element it returns a vector with all bools true
    */
   std::vector<bool> myNonMultVarsOf(RingElem elem) const;

    /**
     * This function print the multiplicative variables of every element in our Janet-basis
     */
    void myPrintMultVar() const;

    /**
     * This function checks if the Janet-basis is also a Pommaret-basis
     */
    bool IamPommaretBasis() const;

    bool IamPommaretBasisDecide();

    /*
     * This function checks if the ideal is homogenous
     */
    bool IamHomogenous() const;

    bool IamHomogenousDecide();

    /*
     * This function checks if the ideal is a monomial ideal
     */
    bool IamMonomialIdeal() const;

    bool IamMonomialIdealDecide();


    /**
     * This function computes an involutive standard representation of the element r
     * with respect to our given Janet-basis
     */
    std::pair<std::map<PPMonoidElem, RingElem>, RingElem> myStandardRepresentation(RingElem r) const;

    /**
     * As Above but without rest and with returning the whole BasisElement instead of returning only the LPP
     */
    std::vector<std::pair<RingElem, RingElem> >myStandardRepresentationWithoutRest(RingElem r) const;

    /**
     * As Above but without rest and without returning the corresponding BasisElement instead of returning only the LPP
     */
    std::vector<RingElem>myStandardRepresentationWithoutRestShort(RingElem r) const;


    /**
     * Output of the StandardRepresentation above
     */
    void myOutputStandardRepresentation(RingElem r) const;

    /**
     *Computes the HilbertPolynomial
     */
    RingElem myHilbertPol(RingElem s) const;

    /**
     *Computes the Hilbert Function
     */
    BigInt myHilbertFunc(BigInt s) const;

    /**
     * Computes the Hilbert Function. Output is the functional expression
     */
    void myHilbertFunc() const;


    /**
     *Computes the rational function of the Hilbert Series
     *Attention RingElem must be in a fraction field!!!
     */
    RingElem myHilbertSeries(RingElem s) const;

    /*
     * computes the involutive syzygy
     */
    FGModule mySyzygy() const;

    /*
     * Dimension of the P/I where I is generated by the current Janet-Basis
     */
    long myDim() const;

    /*
     *depth of the ideal
     */
    long myDepth() const;

    /*
     * projective dimension
     */
    long myProjDim() const;

    /*
    * the class of an ring element in the setting
    */
    long myCls(PPMonoidElem elem) const;

    /*
    * old definition of the class
    */
    long myOldCls(PPMonoidElem elem) const;

    /*
    * the minimal class of the basis
    */
    long myMinCls() const;

    /*
    * all elements with class cls in the current basis
    */
    std::vector<RingElem> myElementsWithClass(long InputCls) const;


    /*
    * the socle of the current ideal
    */
    std::vector<RingElem> mySocle() const;

    /*
    * extremal betti numbers of our ideal
    */
    std::map<std::pair<long, long>, long> myExtremalBettiNumbers() const;

    /*
     * maximal regular Sequence !!!!mod I!!!!
     */
    std::vector<RingElem> myRegSeq() const; //mod I

    /*
     * unique maximal strongly independent set !!!mod I!!!
     */
    std::vector<RingElem> myMaxStronglyIndependentSet() const;

    /*
     * tests if I is a CohenMacaulay ring
     */
    bool IamCohenMacaulay() const;

    /*
     * regularity
     */
    long myRegularity() const;

    /*
     * the maximal degree of all elements with 
     * class index in the pommaret basis
     */
    long myDegPommaretClass(long index) const;

    /*
     * a map with all classes in the pommaret basis +
     * associated degree (see above)
     */
    std::map<long, long> myDegPommaretClasses() const;

    /*
     * satiety of an ideal
     * if the ideal is not saturated it returns 0
     */
    long mySatiety() const;


    /*
     * complementary decomposition of a monomial ideal
     */
    std::vector< std::pair<PPMonoidElem, std::vector<bool> > > myComplementaryDecomposition() const;

    /*
     * complementary decomposition of a ideal (using only the leading terms!)
     */
    std::vector< std::pair<PPMonoidElem, std::vector<bool> > > myComplementaryDecompositionPolynomial() const;

    /*
     * standard pairs of a monomial ideal
     */
    std::vector< std::pair<PPMonoidElem, std::vector<bool> > > myStandardPairs() const;

    /*
    * Castelnuovo-Mumford-regularity
    */ 
    long myCastelnuovoMumfordRegularity() const;

    /*
    * saturation of the ideal
    */
    std::vector<RingElem> mySaturation() const;

    /**
     * Computes the JNormalForm of a polynomial
     * @param elem, a RingElem.
     * The algorithm returns a RingElem
     */
    RingElem myJNormalForm(const RingElem& elem) const;

    /**
     * Computes the j-divisor of a polynomial.
     * It is only a wrapper to the 'deep' function
     * @param elem, a RingElem
     * It returns a polynomial
     */
    inline RingElem myJDivisor(const RingElem& elem) const
    {
      JanetTriple* DivPtr(myJTree.myJDivisor(LPP(elem)));
      if(DivPtr == 0)
      {
        return zero(myOptions.myRingOptions.myPolyRing);
      }
      return DivPtr->myGetPol();
    }



    /*
    * noether normalization (Robertz: Noether normalization guided by monomial cone decompositions, page 8/9)
    * !!!!!NOT TESTED!!!!! 
    * Only works in a few cases...
    */
    std::pair<RingHom, std::vector<bool> > myNoetherNormalization() const;

    /*
     * Returns the SparsePolyRing
     */
    SparsePolyRing myGetSparsePolyRing() const
    {
      return myOptions.myRingOptions.myPolyRing;
    }

    /*
     * Returns the PPMonoid
     */
    PPMonoid myGetPPMonoid() const
    {
      return myOptions.myRingOptions.myPPMValue;
    }
    
    private: 
    JBFlag myOptions;
    std::vector<RingElem> myInput;
    std::list<JanetTriple> mySetT; 
    std::multiset<JanetTriple,IsLowerLPP> mySetQ; // we save this now in the class JSets -> maybe we should remove this!
    JanetTree myJTree;
    
    bool3 IamPommaret;
    bool3 IamHomog;
    bool3 IamMonomial;


    /*
     * Count all elements in the vector with degree = degree
     */
    long myCountElementsWithDegree(const std::vector<RingElem>& vec, long degree) const;

    /*
    * union of two bool vectors
    */
    std::vector<bool> myUnionBoolVectors(std::vector<bool> v1, std::vector<bool> v2) const;


    /*
    * union of two bool vectors
    */
    RingElem myGreatestMultVar(std::vector<bool> v) const;

    /*
    * check if mon a multiple of the multiplicative vars
    */
    bool IamMultipleOfMultVars(PPMonoidElem ppm, std::vector<bool> v) const;

    /*
     * we need this function for PrintMultVar and PrintNonMultVar. It returns the (non)multiplicative variables
     */
    void myOutputVar(std::map<PPMonoidElem,std::vector<bool> > multVars, bool outputMultVar) const;
    
    
    /*
     * recursive part of computeMultVar
     */
    void myRekComputeMultVar(std::map<PPMonoidElem,std::vector<bool> >& multVars, JanetIterator iter, std::vector<int> currentNonMultVars, int curVar) const; 
    
    /*
     * computes the pommaret multiplicative variables of our Janet basis
     */
    std::vector<bool> myPommaretMultVar(PPMonoidElem pp, long numIndets) const;
        
    /**
     * n!
     */
    long myFac(long n) const;

    /**
     * like the binomial coeffient
     * warning: maybe infinite loop!!!!
     */
    RingElem myBinLike(PolyRing ring, RingElem polAbove, long intBelow) const;

    /*
     * count trues in a std::vector<bool>
     */
    
    long myCountTrues(std::vector<bool> vec) const;

    /*
    * recurscive part of the complementary decomposition
    */
    void myComplementaryDecompositionRecPart(std::vector< std::pair<PPMonoidElem, std::vector<bool> > >& output, JanetIterator JIter) const;

    /*
     * complementary decomposition of the leading ideal
     */
    std::vector< std::pair<PPMonoidElem, std::vector<bool> > > myComplementaryDecompositionLeadingIdeal() const;

    /*
     * computes the multiplicative variables of a JanetIterator
     */
    std::vector<bool> myComputeMultVar(JanetIterator iter) const;

    /*
     * returns a reversed bool vector (true to false, and vica versa)
     */
    std::vector<bool> myReverseBoolVec(std::vector<bool> vec) const;

    /*
     * computes the nonmultiplicative variables of a JanetIerator
     */
    std::vector<bool> myComputeNonMultVar(JanetIterator iter) const;

    FreeModule myMakeNewFreeModuleForSyz() const;

    /*
    * divides the ringElems in classes
    */
    std::multimap<long, RingElem> myPommaretClasses() const;

  };

  /**
   * Computes a Janet Basis for a set of polynomials
   * @param PolyList is a vector of RingElems. Therefore we compute the Janet Basis.
   * @param crit is a bitset of three bits. If a bit is set we compute the corresponding involutive criteria.
   * @param res: This parameter is able to take two values, GB (then the output of this function is the reduced Groebnerbasis) or JB(then the output of this function is the minimal Janet Basis)
   * The algorithm returns a vector of RingElems.
   */
  const std::vector<RingElem> JanetBasis(const std::vector<RingElem>& PolyList, const std::bitset<3> crit, ResOutputFlag res, StrategyFlag algorithm = TQBlockLow);

  /**
   * Computes a Janet Basis for a set of polynomials. It use the first two involutive criteria.
   * @param PolyList is a vector of RingElems. Therefore we compute the Janet Basis.
   * @param res: This parameter is able to take two values, GB (then the output of this function is the reduced Groebnerbasis) or JB(then the output of this function is the minimal Janet Basis)
   * The algorithm returns a vector of RingElem.
   */
  inline const std::vector<RingElem> JanetBasis(const std::vector<RingElem>& PolyList, ResOutputFlag res, StrategyFlag algorithm = TQBlockLow)
  {
    return JanetBasis(PolyList, std::bitset<3>(3), res, algorithm);
  }

  /**
   * Computes a Janet Basis for a set of polynomials. It returns a minimal Janet Basis.
   * @param PolyList is a vector of RingElems. Therefore we compute the Janet Basis.
   * @param crit is a bitset of three bits. If a bit is set we compute the corresponding involutive criteria.
   * The algorithm returns a vector of RingElem.
   */
  inline const std::vector<RingElem> JanetBasis(const std::vector<RingElem>& PolyList, const std::bitset<3> crit, StrategyFlag algorithm=TQBlockLow)
  {
    return JanetBasis(PolyList, crit, JB, algorithm);
  }  

  /**
   * Computes a Janet Basis for a set of polynomials. It returns a minimal Janet Basis and uses the first two involutive criteria.
   * @param PolyList is a vector of RingElems. Therefore we compute the Janet Basis.
   * The algorithm returns a vector of RingElem.
   */
  inline const std::vector<RingElem> JanetBasis(const std::vector<RingElem>& PolyList, StrategyFlag algorithm=TQBlockLow)
  {
    return JanetBasis(PolyList, std::bitset<3>(3), JB, algorithm);
  }

  /**
   * Computes a Janet Basis for an ideal.
   * @param i is an ideal. Therefore we compute the Janet Basis.
   * @param crit is a bitset of three bits. If a bit is set we compute the corresponding involutive criteria.
   * @param res: This parameter is able to take two values, GB (then the output of this function is the reduced Groebnerbasis) or JB(then the output of this function is the minimal Janet Basis)
   * The algorithm returns a vector of RingElem.
   */
  inline const std::vector<RingElem> JanetBasis(const ideal& i, const std::bitset<3> crit, ResOutputFlag res, StrategyFlag algorithm=TQBlockLow)
  {
    return JanetBasis(gens(i),crit,res, algorithm);
  }

  /**
   * Computes a Janet Basis for an ideal. It use the first two involutive criteria.
   * @param i is an ideal. Therefore we compute the Janet Basis.
   * @param res: This parameter is able to take two values, GB (then the output of this function is the reduced Groebnerbasis) or JB(then the output of this function is the minimal Janet Basis)
   * The algorithm returns a vector of RingElem.
   */
  inline const std::vector<RingElem> JanetBasis(const ideal& i, ResOutputFlag res, StrategyFlag algorithm=TQBlockLow)
  {
    return JanetBasis(gens(i), std::bitset<3>(3), res, algorithm);
  }

  /**
   * Computes a Janet Basis for an ideal. It returns a minimal Janet Basis.
   * @param i is an ideal. Therefore we compute the Janet Basis.
   * @param crit is a bitset of three bits. If a bit is set we compute the corresponding involutive criteria.
   * The algorithm returns a vector of RingElem.
   */
  inline const std::vector<RingElem> JanetBasis(const ideal& i, const std::bitset<3> crit, StrategyFlag algorithm=TQBlockLow)
  {
    return JanetBasis(gens(i), crit, JB, algorithm);
  }

  /**
   * Computes a Janet Basis for an ideal. It returns a minimal Janet Basis and uses the first two involutive criteria.
   * @param i is an ideal. Therefore we compute the Janet Basis.
   * The algorithm returns a vector of RingElem.
   */
  inline const std::vector<RingElem> JanetBasis(const ideal& i, StrategyFlag algorithm=TQBlockLow)
  {
    return JanetBasis(gens(i), std::bitset<3>(3), JB, algorithm);
  }

  /**
   * These four functions are similar to the the functions JanetBasis, but the return a JBMill
   */
  JBMill ExtendedJanetBasis(const std::vector<RingElem>& PolyList, const std::bitset<3> crit, StrategyFlag algorithm);


  inline JBMill ExtendedJanetBasis(const std::vector<RingElem>& PolyList, StrategyFlag algorithm=TQBlockLow)
  {
    return ExtendedJanetBasis(PolyList, std::bitset<3>(3), algorithm);
  }


  inline JBMill ExtendedJanetBasis(const ideal& i, const std::bitset<3> crit, StrategyFlag algorithm=TQBlockLow)
  {
    return ExtendedJanetBasis(gens(i), crit, algorithm);
  }


  inline JBMill ExtendedJanetBasis(const ideal& i, StrategyFlag algorithm=TQBlockLow)
  {
    return ExtendedJanetBasis(gens(i), std::bitset<3>(3), algorithm);
  }

  /////////////////////////////////////////////////////////////////
  // functions for CoCoA 5                                       //
  /////////////////////////////////////////////////////////////////

    
  inline std::vector<RingElem> JBReturnJB(const JBMill& mill)
  {
    return mill.myReturnJB();
  }

  inline std::vector<RingElem> JBReturnGB(const JBMill& mill)
  {
    return mill.myReturnGB();
  }


  //nicer data type
  inline std::map<PPMonoidElem, std::vector<bool> > JBNonMultVar(const JBMill& mill)
  {
    return mill.myComputeNonMultVars();
  }

  //nicer data type
  inline std::map<PPMonoidElem, std::vector<bool> > JBMultVar(const JBMill& mill)
  {
    return mill.myComputeMultVars();
  }

  inline bool JBIsPommaretBasis(const JBMill& mill)
  {
    return mill.IamPommaretBasis();
  }

  inline bool JBIsHomogenous(const JBMill& mill)
  {
    return mill.IamHomogenous();
  }

  inline bool JBIsMonomialIdeal(const JBMill& mill)
  {
    return mill.IamMonomialIdeal();
  }

  inline RingElem JBHilbertPol(const JBMill& mill, RingElem elem)
  {
    return mill.myHilbertPol(elem);
  }

  inline BigInt JBHilbertFunc(const JBMill& mill, BigInt s)
  {
    return mill.myHilbertFunc(s);
  }

  inline void JBHilbertFunc(const JBMill& mill)
  {
    return mill.myHilbertFunc();
  }

  inline RingElem JBHilbertSeries(const JBMill& mill, ConstRefRingElem elem)
  {
    return mill.myHilbertSeries(elem);
  }

  inline std::pair<std::map<PPMonoidElem, RingElem>, RingElem> JBStandardRepresentation(const JBMill& mill, ConstRefRingElem elem) 
  {
    return mill.myStandardRepresentation(elem);
  }

  inline void JBOutputStandardRepresentation(const JBMill& mill, ConstRefRingElem elem) 
  {
    mill.myOutputStandardRepresentation(elem);
  }

  inline FGModule JBSyzygy(const JBMill& mill)
  {
    return mill.mySyzygy();
  }

  inline long JBDim(const JBMill& mill)
  {
    return mill.myDim();
  }

  inline long JBDepth(const JBMill& mill)
  {
    return mill.myDepth();
  }

  inline long JBProjDim(const JBMill& mill)
  {
    return mill.myProjDim();
  }

  inline long JBCls(const JBMill& mill, ConstRefRingElem elem){
    return mill.myCls(LPP(elem));
  }

  inline long JBCls(const JBMill& mill, PPMonoidElem elem){
    return mill.myCls(elem);
  }

  inline long JBMinCls(const JBMill& mill)
  {
    return mill.myMinCls();
  }

  inline std::vector<RingElem> JBElementsWithClass(const JBMill& mill, BigInt InputCls)
  {
    long InputClsLong;
    IsConvertible(InputClsLong, InputCls);
    return mill.myElementsWithClass(InputClsLong);
  }

  inline std::vector<RingElem> JBSocle(const JBMill& mill)
  {
    return mill.mySocle();
  }

  inline std::map<std::pair<long, long>, long> JBExtremalBettiNumbers(const JBMill& mill)
  {
    return mill.myExtremalBettiNumbers();
  }

  inline std::vector<RingElem> JBRegularSequence(const JBMill& mill)
  {
    return mill.myRegSeq();
  }

  inline std::vector<RingElem> JBMaxStronglyIndependentSet(const JBMill& mill)
  {
    return mill.myMaxStronglyIndependentSet();
  }

  inline bool JBIsCohenMacaulay(const JBMill& mill)
  {
    return mill.IamCohenMacaulay();
  }

  inline long JBRegularity(const JBMill& mill)
  {
    return mill.myRegularity();
  }

  inline long JBDegPommaretClass(const JBMill& mill, BigInt index)
  {
    long IndexLong;
    IsConvertible(IndexLong, index);
    return mill.myDegPommaretClass(IndexLong);
  }

  inline long JBSatiety(const JBMill& mill)
  {
    return mill.mySatiety();
  }

  inline long JBCastelnuovoMumfordRegularity(const JBMill& mill)
  {
    return mill.myCastelnuovoMumfordRegularity();
  }

  inline std::vector<RingElem> JBSaturation(const JBMill& mill)
  {
    return mill.mySaturation();
  }

  inline void JBOutputNonMultVar(const JBMill& mill)
  {
    return mill.myPrintNonMultVar();
  }

  inline void JBOutputMultVar(const JBMill& mill)
  {
    return mill.myPrintMultVar();
  }

  inline std::vector< std::pair<PPMonoidElem, std::vector<bool> > > JBComplementaryDecomposition(const JBMill& mill)
  {
    return mill.myComplementaryDecomposition();
  }

  inline std::vector< std::pair<PPMonoidElem, std::vector<bool> > > JBStandardPairs(const JBMill& mill)
  {
    return mill.myStandardPairs();
  }

  inline RingElem JBNormalForm(const JBMill& mill, ConstRefRingElem elem)
  {
    return mill.myJNormalForm(elem);
  }

  inline RingElem JBDivisor(const JBMill& mill, ConstRefRingElem elem)
  {
    return mill.myJDivisor(elem);
  }

}
#endif
