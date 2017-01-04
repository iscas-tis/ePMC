/*****************************************************************************/
/*!
 * \file theory_arith.h
 *
 * Author: Clark Barrett
 *
 * Created: Fri Jan 17 18:34:55 2003
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

#ifndef _cvc3__include__theory_arith_h_
#define _cvc3__include__theory_arith_h_

#include "theory.h"
#include "cdmap.h"

namespace CVC3 {

  class ArithProofRules;

  typedef enum {
    // New constants
    REAL_CONST = 30, // wrapper around constants to indicate that they should be real
    NEGINF = 31,
    POSINF = 32,

    REAL = 3000,
    INT,
    SUBRANGE,

    UMINUS,
    PLUS,
    MINUS,
    MULT,
    DIVIDE,
    POW,
    INTDIV,
    MOD,
    LT,
    LE,
    GT,
    GE,
    IS_INTEGER,
    DARK_SHADOW,
    GRAY_SHADOW

  } ArithKinds;

/*****************************************************************************/
/*!
 *\class TheoryArith
 *\ingroup Theories
 *\brief This theory handles basic linear arithmetic.
 *
 * Author: Clark Barrett
 *
 * Created: Sat Feb  8 14:44:32 2003
 */
/*****************************************************************************/
class TheoryArith :public Theory {
 protected:
  Type d_realType;
  Type d_intType;
  std::vector<int> d_kinds;

 protected:

  //! Canonize the expression e, assuming all children are canonical
  virtual Theorem canon(const Expr& e) = 0;

  //! Canonize the expression e recursively
  Theorem canonRec(const Expr& e);

  //! Print a rational in SMT-LIB format
  void printRational(ExprStream& os, const Rational& r, bool printAsReal = false);

  //! Whether any ite's appear in the arithmetic part of the term e
  bool isAtomicArithTerm(const Expr& e);

  //! simplify leaves and then canonize
  Theorem canonSimp(const Expr& e);

  //! helper for checkAssertEqInvariant
  bool recursiveCanonSimpCheck(const Expr& e);

 public:
  TheoryArith(TheoryCore* core, const std::string& name)
    : Theory(core, name) {}
  ~TheoryArith() {}

  virtual void addMultiplicativeSignSplit(const Theorem& case_split_thm) {};

  /**
   * Record that smaller should be smaller than bigger in the variable order.
   * Should be implemented in decision procedures that support it.
   */
  virtual bool addPairToArithOrder(const Expr& smaller, const Expr& bigger) { return true; };

  // Used by translator
  //! Return whether e is syntactically identical to a rational constant
  bool isSyntacticRational(const Expr& e, Rational& r);
  //! Whether any ite's appear in the arithmetic part of the formula e
  bool isAtomicArithFormula(const Expr& e);
  //! Rewrite an atom to look like x - y op c if possible--for smtlib translation
  Expr rewriteToDiff(const Expr& e);

  /*! @brief Composition of canon(const Expr&) by transitivity: take e0 = e1,
   * canonize e1 to e2, return e0 = e2. */
  Theorem canonThm(const Theorem& thm) {
    return transitivityRule(thm, canon(thm.getRHS()));
  }

  // ArithTheoremProducer needs this function, so make it public
  //! Separate monomial e = c*p1*...*pn into c and 1*p1*...*pn
  virtual void separateMonomial(const Expr& e, Expr& c, Expr& var) = 0;

  // Theory interface
  virtual void addSharedTerm(const Expr& e) = 0;
  virtual void assertFact(const Theorem& e) = 0;
  virtual void refineCounterExample() = 0;
  virtual void computeModelBasic(const std::vector<Expr>& v) = 0;
  virtual void computeModel(const Expr& e, std::vector<Expr>& vars) = 0;
  virtual void checkSat(bool fullEffort) = 0;
  virtual Theorem rewrite(const Expr& e) = 0;
  virtual void setup(const Expr& e) = 0;
  virtual void update(const Theorem& e, const Expr& d) = 0;
  virtual Theorem solve(const Theorem& e) = 0;
  virtual void checkAssertEqInvariant(const Theorem& e) = 0;
  virtual void checkType(const Expr& e) = 0;
  virtual Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                                     bool enumerate, bool computeSize) = 0;
  virtual void computeType(const Expr& e) = 0;
  virtual Type computeBaseType(const Type& t) = 0;
  virtual void computeModelTerm(const Expr& e, std::vector<Expr>& v) = 0;
  virtual Expr computeTypePred(const Type& t, const Expr& e) = 0;
  virtual Expr computeTCC(const Expr& e) = 0;
  virtual ExprStream& print(ExprStream& os, const Expr& e) = 0;
  virtual Expr parseExprOp(const Expr& e) = 0;

  // Arith constructors
  Type realType() { return d_realType; }
  Type intType() { return d_intType;}
  Type subrangeType(const Expr& l, const Expr& r)
    { return Type(Expr(SUBRANGE, l, r)); }
  Expr rat(Rational r) { return getEM()->newRatExpr(r); }
  // Dark and Gray shadows (for internal use only)
  //! Construct the dark shadow expression representing lhs <= rhs
  Expr darkShadow(const Expr& lhs, const Expr& rhs) {
    return Expr(DARK_SHADOW, lhs, rhs);
  }
  //! Construct the gray shadow expression representing c1 <= v - e <= c2
  /*! Alternatively, v = e + i for some i s.t. c1 <= i <= c2
   */
  Expr grayShadow(const Expr& v, const Expr& e,
		  const Rational& c1, const Rational& c2) {
    return Expr(GRAY_SHADOW, v, e, rat(c1), rat(c2));
  }
  bool leavesAreNumConst(const Expr& e);
};

// Arith testers
inline bool isReal(Type t) { return t.getExpr().getKind() == REAL; }
inline bool isInt(Type t) { return t.getExpr().getKind() == INT; }

// Static arith testers
inline bool isRational(const Expr& e) { return e.isRational(); }
inline bool isIntegerConst(const Expr& e)
  { return e.isRational() && e.getRational().isInteger(); }
inline bool isUMinus(const Expr& e) { return e.getKind() == UMINUS; }
inline bool isPlus(const Expr& e) { return e.getKind() == PLUS; }
inline bool isMinus(const Expr& e) { return e.getKind() == MINUS; }
inline bool isMult(const Expr& e) { return e.getKind() == MULT; }
inline bool isDivide(const Expr& e) { return e.getKind() == DIVIDE; }
inline bool isPow(const Expr& e) { return e.getKind() == POW; }
inline bool isLT(const Expr& e) { return e.getKind() == LT; }
inline bool isLE(const Expr& e) { return e.getKind() == LE; }
inline bool isGT(const Expr& e) { return e.getKind() == GT; }
inline bool isGE(const Expr& e) { return e.getKind() == GE; }
inline bool isDarkShadow(const Expr& e) { return e.getKind() == DARK_SHADOW;}
inline bool isGrayShadow(const Expr& e) { return e.getKind() == GRAY_SHADOW;}
inline bool isIneq(const Expr& e)
  { return isLT(e) || isLE(e) || isGT(e) || isGE(e); }
inline bool isIntPred(const Expr& e) { return e.getKind() == IS_INTEGER; }

// Static arith constructors
inline Expr uminusExpr(const Expr& child)
  { return Expr(UMINUS, child); }
inline Expr plusExpr(const Expr& left, const Expr& right)
  { return Expr(PLUS, left, right); }
inline Expr plusExpr(const std::vector<Expr>& children) {
  DebugAssert(children.size() > 0, "plusExpr()");
  return Expr(PLUS, children);
}
inline Expr minusExpr(const Expr& left, const Expr& right)
  { return Expr(MINUS, left, right); }
inline Expr multExpr(const Expr& left, const Expr& right)
  { return Expr(MULT, left, right); }
// Begin Deepak:
//! a Mult expr with two or more children
inline Expr multExpr(const std::vector<Expr>& children) {
  DebugAssert(children.size() > 0, "multExpr()");
  return Expr(MULT, children);
}
//! Power (x^n, or base^{pow}) expressions
inline Expr powExpr(const Expr& pow, const Expr & base)
  { return Expr(POW, pow, base);}
// End Deepak
inline Expr divideExpr(const Expr& left, const Expr& right)
  { return Expr(DIVIDE, left, right); }
inline Expr ltExpr(const Expr& left, const Expr& right)
  { return Expr(LT, left, right); }
inline Expr leExpr(const Expr& left, const Expr& right)
  { return Expr(LE, left, right); }
inline Expr gtExpr(const Expr& left, const Expr& right)
  { return Expr(GT, left, right); }
inline Expr geExpr(const Expr& left, const Expr& right)
  { return Expr(GE, left, right); }

inline Expr operator-(const Expr& child)
  { return uminusExpr(child); }
inline Expr operator+(const Expr& left, const Expr& right)
  { return plusExpr(left, right); }
inline Expr operator-(const Expr& left, const Expr& right)
  { return minusExpr(left, right); }
inline Expr operator*(const Expr& left, const Expr& right)
  { return multExpr(left, right); }
inline Expr operator/(const Expr& left, const Expr& right)
  { return divideExpr(left, right); }

}

#endif
