/*****************************************************************************/
/*!
 * \file theory_arith3.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Thu Jun 14 13:22:11 2007
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

#ifndef _cvc3__include__theory_arith3_h_
#define _cvc3__include__theory_arith3_h_

#include "theory_arith.h"

namespace CVC3 {

class TheoryArith3 :public TheoryArith {
  CDList<Theorem> d_diseq;  // For concrete model generation
  CDO<size_t> d_diseqIdx; // Index to the next unprocessed disequality
  ArithProofRules* d_rules;
  CDO<bool> d_inModelCreation;

  //! Data class for the strongest free constant in separation inqualities
  class FreeConst {
  private:
    Rational d_r;
    bool d_strict;
  public:
    FreeConst() { }
    FreeConst(const Rational& r, bool strict): d_r(r), d_strict(strict) { }
    const Rational& getConst() const { return d_r; }
    bool strict() const { return d_strict; }
  };
  //! Printing
  friend std::ostream& operator<<(std::ostream& os, const FreeConst& fc);

  //! Private class for an inequality in the Fourier-Motzkin database
  class Ineq {
  private:
    Theorem d_ineq; //!< The inequality
    bool d_rhs; //!< Var is isolated on the RHS
    const FreeConst* d_const; //!< The max/min const for subsumption check
    //! Default constructor is disabled
    Ineq() { }
  public:
    //! Initial constructor.  'r' is taken from the subsumption database.
    Ineq(const Theorem& ineq, bool varOnRHS, const FreeConst& c):
      d_ineq(ineq), d_rhs(varOnRHS), d_const(&c) { }
    //! Get the inequality
    const Theorem& ineq() const { return d_ineq; }
    //! Get the max/min constant
    const FreeConst& getConst() const { return *d_const; }
    //! Flag whether var is isolated on the RHS
    bool varOnRHS() const { return d_rhs; }
    //! Flag whether var is isolated on the LHS
    bool varOnLHS() const { return !d_rhs; }
    //! Auto-cast to Theorem
    operator Theorem() const { return d_ineq; }
  };
  //! Printing
  friend std::ostream& operator<<(std::ostream& os, const Ineq& ineq);

  //! Database of inequalities with a variable isolated on the right
  ExprMap<CDList<Ineq> *> d_inequalitiesRightDB;

  //! Database of inequalities with a variable isolated on the left
  ExprMap<CDList<Ineq> *> d_inequalitiesLeftDB;

  //! Mapping of inequalities to the largest/smallest free constant
  /*! The Expr is the original inequality with the free constant
   * removed and inequality converted to non-strict (for indexing
   * purposes).  I.e. ax<c+t becomes ax<=t.  This inequality is mapped
   * to a pair<c,strict>, the smallest (largest for c+t<ax) constant
   * among inequalities with the same 'a', 'x', and 't', and a boolean
   * flag indicating whether the strongest inequality is strict.
   */
  CDMap<Expr, FreeConst> d_freeConstDB;

  // Input buffer to store the incoming inequalities
  CDList<Theorem> d_buffer; //!< Buffer of input inequalities

  CDO<size_t> d_bufferIdx; //!< Buffer index of the next unprocessed inequality

  const int* d_bufferThres; //!< Threshold when the buffer must be processed

  // Statistics for the variables

  /*! @brief Mapping of a variable to the number of inequalities where
    the variable would be isolated on the right */
  CDMap<Expr, int> d_countRight;

  /*! @brief Mapping of a variable to the number of inequalities where
    the variable would be isolated on the left */
  CDMap<Expr, int> d_countLeft;

  //! Set of shared terms (for counterexample generation)
  CDMap<Expr, bool> d_sharedTerms;

  //! Set of shared integer variables (i-leaves)
  CDMap<Expr, bool> d_sharedVars;

  //Directed Acyclic Graph representing partial variable ordering for
  //variable projection over inequalities.
  class VarOrderGraph {
    ExprMap<std::vector<Expr> > d_edges;
    ExprMap<bool> d_cache;
    bool dfs(const Expr& e1, const Expr& e2);
  public:
    void addEdge(const Expr& e1, const Expr& e2);
    //returns true if e1 < e2, false otherwise.
    bool lessThan(const Expr& e1, const Expr& e2);
    //selects those variables which are largest and incomparable among
    //v1 and puts it into v2
    void selectLargest(const std::vector<Expr>& v1, std::vector<Expr>& v2);
    //selects those variables which are smallest and incomparable among
    //v1, removes them from v1 and  puts them into v2. 
    void selectSmallest( std::vector<Expr>& v1, std::vector<Expr>& v2);
  };
  
  VarOrderGraph d_graph;

  // Private methods

  //! Check the term t for integrality.  
  /*! \return a theorem of IS_INTEGER(t) or Null. */
  Theorem isIntegerThm(const Expr& e);

  //! A helper method for isIntegerThm()
  /*! Check if IS_INTEGER(e) is easily derivable from the given 'thm' */
  Theorem isIntegerDerive(const Expr& isIntE, const Theorem& thm);

  //! Extract the free constant from an inequality
  const Rational& freeConstIneq(const Expr& ineq, bool varOnRHS);

  //! Update the free constant subsumption database with new inequality
  /*! \return a reference to the max/min constant.
   *
   * Also, sets 'subsumed' argument to true if the inequality is
   * subsumed by an existing inequality.
   */
  const FreeConst& updateSubsumptionDB(const Expr& ineq, bool varOnRHS,
				      bool& subsumed);
  //! Check if the kids of e are fully simplified and canonized (for debugging)
  bool kidsCanonical(const Expr& e);

  //! Canonize the expression e, assuming all children are canonical
  Theorem canon(const Expr& e);

  /*! @brief Canonize and reduce e w.r.t. union-find database; assume
   * all children are canonical */
  Theorem canonSimplify(const Expr& e);

  /*! @brief Composition of canonSimplify(const Expr&) by
   * transitivity: take e0 = e1, canonize and simplify e1 to e2,
   * return e0 = e2. */
  Theorem canonSimplify(const Theorem& thm) {
    return transitivityRule(thm, canonSimplify(thm.getRHS()));
  }

  //! Canonize predicate (x = y, x < y, etc.)
  Theorem canonPred(const Theorem& thm);

  //! Canonize predicate like canonPred except that the input theorem
  //! is an equivalent transformation.
  Theorem canonPredEquiv(const Theorem& thm);

  //! Solve an equation and return an equivalent Theorem in the solved form
  Theorem doSolve(const Theorem& e);

  //! takes in a conjunction equivalence Thm and canonizes it.
  Theorem canonConjunctionEquiv(const Theorem& thm);

  //! picks the monomial with the smallest abs(coeff) from the input
  //integer equation.
  bool pickIntEqMonomial(const Expr& right, Expr& isolated, bool& nonlin);

  //! processes equalities with 1 or more vars of type REAL
  Theorem processRealEq(const Theorem& eqn);

  //! processes equalities whose vars are all of type INT
  Theorem processIntEq(const Theorem& eqn);

  //! One step of INT equality processing (aux. method for processIntEq())
  Theorem processSimpleIntEq(const Theorem& eqn);

  //! Process inequalities in the buffer
  void processBuffer();

  //! Take an inequality and isolate a variable
  Theorem isolateVariable(const Theorem& inputThm, bool& e1);

  //! Update the statistics counters for the variable with a coeff. c
  void updateStats(const Rational& c, const Expr& var);

  //! Update the statistics counters for the monomial
  void updateStats(const Expr& monomial);

  //! Add an inequality to the input buffer.  See also d_buffer
  void addToBuffer(const Theorem& thm);

  /*! @brief Given a canonized term, compute a factor to make all
    coefficients integer and relatively prime */
  Expr computeNormalFactor(const Expr& rhs);

  //! Normalize an equation (make all coefficients rel. prime integers)
  Theorem normalize(const Expr& e);

  //! Normalize an equation (make all coefficients rel. prime integers)
  /*! accepts a rewrite theorem over eqn|ineqn and normalizes it
   *  and returns a theorem to that effect.
   */
  Theorem normalize(const Theorem& thm);

  Expr pickMonomial(const Expr& right);

  void getFactors(const Expr& e, std::set<Expr>& factors);

 public: // ArithTheoremProducer needs this function, so make it public
  //! Separate monomial e = c*p1*...*pn into c and 1*p1*...*pn
  void separateMonomial(const Expr& e, Expr& c, Expr& var);
  //! Check the term t for integrality (return bool)
  bool isInteger(const Expr& e) { return !(isIntegerThm(e).isNull()); }


 private:

  bool lessThanVar(const Expr& isolatedVar, const Expr& var2);

  //! Check if the term expression is "stale"
  bool isStale(const Expr& e);

  //! Check if the inequality is "stale" or subsumed
  bool isStale(const Ineq& ineq);

  void projectInequalities(const Theorem& theInequality,bool isolatedVarOnRHS);

  void assignVariables(std::vector<Expr>&v);

  void findRationalBound(const Expr& varSide, const Expr& ratSide, 
			 const Expr& var,
			 Rational &r);

  bool findBounds(const Expr& e, Rational& lub, Rational&  glb);

  Theorem normalizeProjectIneqs(const Theorem& ineqThm1,
				const Theorem& ineqThm2);

  //! Take a system of equations and turn it into a solved form
  Theorem solvedForm(const std::vector<Theorem>& solvedEqs);

  /*! @brief Substitute all vars in term 't' according to the
   * substitution 'subst' and canonize the result.
   */
  Theorem substAndCanonize(const Expr& t, ExprMap<Theorem>& subst);

  /*! @brief Substitute all vars in the RHS of the equation 'eq' of
   * the form (x = t) according to the substitution 'subst', and
   * canonize the result.
   */
  Theorem substAndCanonize(const Theorem& eq, ExprMap<Theorem>& subst);

  //! Traverse 'e' and push all the i-leaves into 'vars' vector
  void collectVars(const Expr& e, std::vector<Expr>& vars,
		   std::set<Expr>& cache);

  /*! @brief Check if alpha <= ax & bx <= beta is a finite interval
   *  for integer var 'x', and assert the corresponding constraint
   */
  void processFiniteInterval(const Theorem& alphaLEax,
			     const Theorem& bxLEbeta);

  //! For an integer var 'x', find and process all constraints A <= ax <= A+c 
  void processFiniteIntervals(const Expr& x);

  //! Recursive setup for isolated inequalities (and other new expressions)
  void setupRec(const Expr& e);

public:
  TheoryArith3(TheoryCore* core);
  ~TheoryArith3();

  // Trusted method that creates the proof rules class (used in constructor).
  // Implemented in arith_theorem_producer.cpp
  ArithProofRules* createProofRules3();

  // Theory interface
  void addSharedTerm(const Expr& e);
  void assertFact(const Theorem& e);
  void refineCounterExample();
  void computeModelBasic(const std::vector<Expr>& v);
  void computeModel(const Expr& e, std::vector<Expr>& vars);
  void checkSat(bool fullEffort);
  Theorem rewrite(const Expr& e);
  void setup(const Expr& e);
  void update(const Theorem& e, const Expr& d);
  Theorem solve(const Theorem& e);
  void checkAssertEqInvariant(const Theorem& e);
  void checkType(const Expr& e);
  Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                             bool enumerate, bool computeSize);
  void computeType(const Expr& e);
  Type computeBaseType(const Type& t);
  void computeModelTerm(const Expr& e, std::vector<Expr>& v);
  Expr computeTypePred(const Type& t, const Expr& e);
  Expr computeTCC(const Expr& e);
  ExprStream& print(ExprStream& os, const Expr& e);
  Expr parseExprOp(const Expr& e);

private:
	
	/** Map from variables to the maximal (by absolute value) of one of it's coefficients */	
	CDMap<Expr, Rational> maxCoefficientLeft;
	CDMap<Expr, Rational> maxCoefficientRight;
  
	/** Map from variables to the fixed value of one of it's coefficients */
	CDMap<Expr, Rational> fixedMaxCoefficient;	
	
	/** 
	 * Returns the current maximal coefficient of the variable.
	 * 
	 * @param var the variable.
	 */
	Rational currentMaxCoefficient(Expr var);
	
	/**
	 * Fixes the current max coefficient to be used in the ordering. If the maximal coefficient
	 * changes in the future, it will not be used in the ordering.
	 * 
	 * @param variable the variable
	 * @param max the value to set it to
	 */ 
	void fixCurrentMaxCoefficient(Expr variable, Rational max);
	
	/**
	 * Among given input variables, select the smallest ones with respect to the coefficients.
	 */
	void selectSmallestByCoefficient(std::vector<Expr> input, std::vector<Expr>& output);
};

}

#endif
