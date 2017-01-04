/*****************************************************************************/
/*!
 * \file theory_bitvector.h
 *
 * Author: Vijay Ganesh
 *
 * Created: Wed May 05 18:34:55 PDT 2004
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 *
 * <hr>
 *
 */
/*****************************************************************************/

#ifndef _cvc3__include__theory_bitvector_h_
#define _cvc3__include__theory_bitvector_h_

#include "theory_core.h"
#include "statistics.h"

namespace CVC3 {

  class VCL;
  class BitvectorProofRules;

  typedef enum { //some new BV kinds
    // New constants
    BVCONST = 80,

    BITVECTOR = 8000,

    CONCAT,
    EXTRACT,
    BOOLEXTRACT,

    LEFTSHIFT,
    CONST_WIDTH_LEFTSHIFT,
    RIGHTSHIFT,
    BVSHL,
    BVLSHR,
    BVASHR,
    SX,
    BVREPEAT,
    BVZEROEXTEND,
    BVROTL,
    BVROTR,

    BVAND,
    BVOR,
    BVXOR,
    BVXNOR,
    BVNEG,
    BVNAND,
    BVNOR,
    BVCOMP,

    BVUMINUS,
    BVPLUS,
    BVSUB,
    BVMULT,
    BVUDIV,
    BVSDIV,
    BVUREM,
    BVSREM,
    BVSMOD,

    BVLT,
    BVLE,
    BVGT,
    BVGE,
    BVSLT,
    BVSLE,
    BVSGT,
    BVSGE,

    INTTOBV, // Not implemented yet
    BVTOINT, // Not implemented yet
    // A wrapper for delaying the construction of type predicates
    BVTYPEPRED
  } BVKinds;

/*****************************************************************************/
/*!
 *\class TheoryBitvector
 *\ingroup Theories
 *\brief Theory of bitvectors of known length \
 * (operations include: @,[i:j],[i],+,.,BVAND,BVNEG)
 *
 * Author: Vijay Ganesh
 *
 * Created:Wed May  5 15:35:07 PDT 2004
 */
/*****************************************************************************/
class TheoryBitvector :public Theory {
  BitvectorProofRules* d_rules;
  //! MemoryManager index for BVConstExpr subclass
  size_t d_bvConstExprIndex;
  size_t d_bvPlusExprIndex;
  size_t d_bvParameterExprIndex;
  size_t d_bvTypePredExprIndex;

  //! counts delayed asserted equalities
  StatCounter d_bvDelayEq;
  //! counts asserted equalities
  StatCounter d_bvAssertEq;
  //! counts delayed asserted disequalities
  StatCounter d_bvDelayDiseq;
  //! counts asserted disequalities
  StatCounter d_bvAssertDiseq;
  //! counts type predicates
  StatCounter d_bvTypePreds;
  //! counts delayed type predicates
  StatCounter d_bvDelayTypePreds;
  //! counts bitblasted equalities
  StatCounter d_bvBitBlastEq;
  //! counts bitblasted disequalities
  StatCounter d_bvBitBlastDiseq;

  //! boolean on the fly rewrite flag
  const bool* d_booleanRWFlag;
  //! bool extract cache flag
  const bool* d_boolExtractCacheFlag;
  //! flag which indicates that all arithmetic is 32 bit with no overflow
  const bool* d_bv32Flag;

  //! Cache for storing the results of the bitBlastTerm function
  CDMap<Expr,Theorem> d_bitvecCache;

  //! Cache for pushNegation(). it is ok that this is cache is
  //ExprMap. it is cleared for each call of pushNegation. Does not add
  //value across calls of pushNegation
  ExprMap<Theorem> d_pushNegCache;

  //! Backtracking queue for equalities
  CDList<Theorem> d_eq;
  //! Backtracking queue for unsolved equalities
  CDList<Theorem> d_eqPending;
  //! Index to current position in d_eqPending
  CDO<unsigned int> d_eq_index;
  //! Backtracking queue for all other assertions
  CDList<Theorem> d_bitblast;
  //! Index to current position in d_bitblast
  CDO<unsigned int> d_bb_index;
  //! Backtracking database of subterms of shared terms
  CDMap<Expr,Expr> d_sharedSubterms;
  //! Backtracking database of subterms of shared terms
  CDList<Expr> d_sharedSubtermsList;

  //! Constant 1-bit bit-vector 0bin0
  Expr d_bvZero;
  //! Constant 1-bit bit-vector 0bin0
  Expr d_bvOne;
  //! Return cached constant 0bin0
  const Expr& bvZero() const { return d_bvZero; }
  //! Return cached constant 0bin1
  const Expr& bvOne() const { return d_bvOne; }

  //! Max size of any bitvector we've seen
  int d_maxLength;

  //! Used in checkSat
  CDO<unsigned> d_index1;
  CDO<unsigned> d_index2;

  //! functions which implement the DP strategy for bitblasting
  Theorem bitBlastEqn(const Expr& e);
  //! bitblast disequation
  Theorem bitBlastDisEqn(const Theorem& notE);
  //! function which implements the DP strtagey to bitblast Inequations
  Theorem bitBlastIneqn(const Expr& e);
  //! functions which implement the DP strategy for bitblasting
  Theorem bitBlastTerm(const Expr& t, int bitPosition);

//   //! strategy fucntions for BVPLUS NORMAL FORM
//   Theorem padBVPlus(const Expr& e);
//   //! strategy fucntions for BVPLUS NORMAL FORM
//   Theorem flattenBVPlus(const Expr& e);

//   //! Implementation of the concatenation normal form
//   Theorem normalizeConcat(const Expr& e, bool useFind);
//   //! Implementation of the n-bit arithmetic normal form
//   Theorem normalizeBVArith(const Expr& e, bool useFind);
//   //! Helper method for composing normalizations
//   Theorem normalizeConcat(const Theorem& t, bool useFind) {
//     return transitivityRule(t, normalizeConcat(t.getRHS(), useFind));
//   }
//   //! Helper method for composing normalizations
//   Theorem normalizeBVArith(const Theorem& t, bool useFind) {
//     return transitivityRule(t, normalizeBVArith(t.getRHS(), useFind));
//   }

//   Theorem signExtendBVLT(const Expr& e, int len, bool useFind);

  public:
  Theorem pushNegationRec(const Expr& e);
  private:
  Theorem pushNegation(const Expr& e);

  //! Top down simplifier
  virtual Theorem simplifyOp(const Expr& e);

  //! Internal rewrite method for constants
  //  Theorem rewriteConst(const Expr& e);

  //! Main rewrite method (implements the actual rewrites)
  Theorem rewriteBV(const Expr& e, ExprMap<Theorem>& cache, int n = 1);

  //! Rewrite children 'n' levels down (n==1 means "only the top level")
  Theorem rewriteBV(const Expr& e, int n = 1);

  // Shortcuts for theorems
  Theorem rewriteBV(const Theorem& t, ExprMap<Theorem>& cache, int n = 1) {
     return transitivityRule(t, rewriteBV(t.getRHS(), cache, n));
  }
  Theorem rewriteBV(const Theorem& t, int n = 1) {
    return transitivityRule(t, rewriteBV(t.getRHS(), n));
  }

  //! rewrite input boolean expression e to a simpler form
  Theorem rewriteBoolean(const Expr& e);

/*Beginning of Lorenzo PLatania's methods*/

  Expr multiply_coeff( Rational mult_inv, const Expr& e);

  // extract the min value from a Rational list
  int min(std::vector<Rational> list);

  // evaluates the gcd of two integer coefficients
  //  int gcd(int c1, int c2);

  // converts a bv constant to an integer
  //  int bv2int(const Expr& e);

  // return the odd coefficient of a leaf for which we can solve the
  // equation, or zero if no one has been found
  Rational Odd_coeff( const Expr& e );



  // returns 1 if e is a linear term
  int check_linear( const Expr &e );

  bool isTermIn(const Expr& e1, const Expr& e2);

  Theorem updateSubterms( const Expr& d );

  // returns how many times "term" appears in "e"
  int countTermIn( const Expr& term, const Expr& e);

  Theorem simplifyPendingEq(const Theorem& thm, int maxEffort);
  Theorem generalBitBlast( const Theorem& thm );
/*End of Lorenzo PLatania's methods*/

public:
  TheoryBitvector(TheoryCore* core);
  ~TheoryBitvector();

  ExprMap<Expr> d_bvPlusCarryCacheLeftBV;
  ExprMap<Expr> d_bvPlusCarryCacheRightBV;

  // Trusted method that creates the proof rules class (used in constructor).
  // Implemented in bitvector_theorem_producer.cpp
  BitvectorProofRules* createProofRules();

  // Theory interface
  void addSharedTerm(const Expr& e);
  void assertFact(const Theorem& e);
  void assertTypePred(const Expr& e, const Theorem& pred);
  void checkSat(bool fullEffort);
  Theorem rewrite(const Expr& e);
  Theorem rewriteAtomic(const Expr& e);
  void setup(const Expr& e);
  void update(const Theorem& e, const Expr& d);
  Theorem solve(const Theorem& e);
  void checkType(const Expr& e);
  Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                             bool enumerate, bool computeSize);
  void computeType(const Expr& e);
  void computeModelTerm(const Expr& e, std::vector<Expr>& v);
  void computeModel(const Expr& e, std::vector<Expr>& vars);
  Expr computeTypePred(const Type& t, const Expr& e);
  Expr computeTCC(const Expr& e);
  ExprStream& print(ExprStream& os, const Expr& e);
  Expr parseExprOp(const Expr& e);

  //helper functions

  //! Return the number of bits in the bitvector expression
  int BVSize(const Expr& e);

  Expr rat(const Rational& r) { return getEM()->newRatExpr(r); }
  //!pads e to be of length len
  Expr pad(int len, const Expr& e);

  bool comparebv(const Expr& e1, const Expr& e2);

  //helper functions: functions to construct exprs
  Type newBitvectorType(int i)
    { return newBitvectorTypeExpr(i); }
  Expr newBitvectorTypePred(const Type& t, const Expr& e);
  Expr newBitvectorTypeExpr(int i);

  Expr newBVAndExpr(const Expr& t1, const Expr& t2);
  Expr newBVAndExpr(const std::vector<Expr>& kids);

  Expr newBVOrExpr(const Expr& t1, const Expr& t2);
  Expr newBVOrExpr(const std::vector<Expr>& kids);

  Expr newBVXorExpr(const Expr& t1, const Expr& t2);
  Expr newBVXorExpr(const std::vector<Expr>& kids);

  Expr newBVXnorExpr(const Expr& t1, const Expr& t2);
  Expr newBVXnorExpr(const std::vector<Expr>& kids);

  Expr newBVNandExpr(const Expr& t1, const Expr& t2);
  Expr newBVNorExpr(const Expr& t1, const Expr& t2);
  Expr newBVCompExpr(const Expr& t1, const Expr& t2);

  Expr newBVLTExpr(const Expr& t1, const Expr& t2);
  Expr newBVLEExpr(const Expr& t1, const Expr& t2);
  Expr newSXExpr(const Expr& t1, int len);
  Expr newBVIndexExpr(int kind, const Expr& t1, int len);
  Expr newBVSLTExpr(const Expr& t1, const Expr& t2);
  Expr newBVSLEExpr(const Expr& t1, const Expr& t2);

  Expr newBVNegExpr(const Expr& t1);
  Expr newBVUminusExpr(const Expr& t1);
  Expr newBoolExtractExpr(const Expr& t1, int r);
  Expr newFixedLeftShiftExpr(const Expr& t1, int r);
  Expr newFixedConstWidthLeftShiftExpr(const Expr& t1, int r);
  Expr newFixedRightShiftExpr(const Expr& t1, int r);
  Expr newConcatExpr(const Expr& t1, const Expr& t2);
  Expr newConcatExpr(const Expr& t1, const Expr& t2, const Expr& t3);
  Expr newConcatExpr(const std::vector<Expr>& kids);
  Expr newBVConstExpr(const std::string& s, int base = 2);
  Expr newBVConstExpr(const std::vector<bool>& bits);
  // Lorenzo's wrapper
  // as computeBVConst can not give the BV expr of a negative rational,
  // I use this wrapper to do that
  Expr signed_newBVConstExpr( Rational c, int bv_size);
  // end of Lorenzo's wrapper

  // Construct BVCONST of length 'len', or the min. needed length when len=0.
  Expr newBVConstExpr(const Rational& r, int len = 0);
  Expr newBVZeroString(int r);
  Expr newBVOneString(int r);
  //! hi and low are bit indices
  Expr newBVExtractExpr(const Expr& e,
			int hi, int low);
  Expr newBVSubExpr(const Expr& t1, const Expr& t2);
  //! 'numbits' is the number of bits in the result
  Expr newBVPlusExpr(int numbits, const Expr& k1, const Expr& k2);
  //! 'numbits' is the number of bits in the result
  Expr newBVPlusExpr(int numbits, const std::vector<Expr>& k);
  //! pads children and then builds plus expr
  Expr newBVPlusPadExpr(int bvLength, const std::vector<Expr>& k);
  Expr newBVMultExpr(int bvLength,
		     const Expr& t1, const Expr& t2);
  Expr newBVMultExpr(int bvLength, const std::vector<Expr>& kids);
  Expr newBVMultPadExpr(int bvLength,
                        const Expr& t1, const Expr& t2);
  Expr newBVMultPadExpr(int bvLength, const std::vector<Expr>& kids);
  Expr newBVUDivExpr(const Expr& t1, const Expr& t2);
  Expr newBVURemExpr(const Expr& t1, const Expr& t2);
  Expr newBVSDivExpr(const Expr& t1, const Expr& t2);
  Expr newBVSRemExpr(const Expr& t1, const Expr& t2);
  Expr newBVSModExpr(const Expr& t1, const Expr& t2);

  // Accessors for expression parameters
  int getBitvectorTypeParam(const Expr& e);
  int getBitvectorTypeParam(const Type& t)
    { return getBitvectorTypeParam(t.getExpr()); }
  Type getTypePredType(const Expr& tp);
  const Expr& getTypePredExpr(const Expr& tp);
  int getSXIndex(const Expr& e);
  int getBVIndex(const Expr& e);
  int getBoolExtractIndex(const Expr& e);
  int getFixedLeftShiftParam(const Expr& e);
  int getFixedRightShiftParam(const Expr& e);
  int getExtractHi(const Expr& e);
  int getExtractLow(const Expr& e);
  int getBVPlusParam(const Expr& e);
  int getBVMultParam(const Expr& e);

  unsigned getBVConstSize(const Expr& e);
  bool getBVConstValue(const Expr& e, int i);
  //!computes the integer value of a bitvector constant
  Rational computeBVConst(const Expr& e);
  //!computes the integer value of ~c+1 or BVUMINUS(c)
  Rational computeNegBVConst(const Expr& e);

  int getMaxSize() { return d_maxLength; }

/*Beginning of Lorenzo PLatania's public methods*/

  bool isLinearTerm( const Expr& e );
  void extract_vars( const Expr& e, std::vector<Expr>& vars );
  // checks whether e can be solved in term
  bool canSolveFor( const Expr& term, const Expr& e );

  // evaluates the multipicative inverse
  Rational multiplicative_inverse(Rational r, int n_bits);


  /*End of Lorenzo PLatania's public methods*/

  std::vector<Theorem> additionalRewriteConstraints;

}; //end of class TheoryBitvector


}//end of namespace CVC3
#endif
