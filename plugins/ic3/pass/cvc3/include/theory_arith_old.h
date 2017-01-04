/*****************************************************************************/
/*!
 * \file theory_arith_old.h
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

#ifndef _cvc3__include__theory_arith_old_h_
#define _cvc3__include__theory_arith_old_h_

#include "theory_arith.h"
#include <set>
#include <list>

namespace CVC3 {

class TheoryArithOld :public TheoryArith {
  CDList<Theorem> d_diseq;  // For concrete model generation
  CDO<size_t> d_diseqIdx; // Index to the next unprocessed disequality
  CDMap<Expr, bool> diseqSplitAlready; // Have we eplit on this disequality already
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
    const Theorem ineq() const { return d_ineq; }
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

//  /** Is the problem only difference logic */
//  CDO<bool> isDL;
//  CDO<int> total_buf_size;
//  CDO<int> processed;
//
  // Input buffer to store the incoming inequalities
  CDList<Theorem> d_buffer_0; //!< Buffer of input inequalities (high priority)
  CDList<Theorem> d_buffer_1; //!< Buffer of input inequalities (one variable)
  CDList<Theorem> d_buffer_2; //!< Buffer of input inequalities (small constraints)
  CDList<Theorem> d_buffer_3; //!< Buffer of input inequalities (big constraint)

  CDO<size_t> d_bufferIdx_0; //!< Buffer index of the next unprocessed inequality
  CDO<size_t> d_bufferIdx_1; //!< Buffer index of the next unprocessed inequality
  CDO<size_t> d_bufferIdx_2; //!< Buffer index of the next unprocessed inequality
  CDO<size_t> d_bufferIdx_3; //!< Buffer index of the next unprocessed inequality

  CDO<size_t> diff_logic_size; //!< Number of queries that are just difference logic

  const int* d_bufferThres; //!< Threshold when the buffer must be processed

  const bool* d_splitSign; // Whether to split on the signs of non-trivial nonlinear products

  const int* d_grayShadowThres; //!< Threshold on gray shadow size (ignore it and set incomplete)

  // Statistics for the variables

  /*! @brief Mapping of a variable to the number of inequalities where
    the variable would be isolated on the right */
  CDMap<Expr, int> d_countRight;

  /*! @brief Mapping of a variable to the number of inequalities where
    the variable would be isolated on the left */
  CDMap<Expr, int> d_countLeft;

  //! Set of shared terms (for counterexample generation)
  CDMap<Expr, bool> d_sharedTerms;
  CDList<Expr> d_sharedTermsList;

  //! Set of shared integer variables (i-leaves)
  CDMap<Expr, bool> d_sharedVars;

  //Directed Acyclic Graph representing partial variable ordering for
  //variable projection over inequalities.
  class VarOrderGraph {
    ExprMap<std::vector<Expr> > d_edges;
    ExprMap<bool> d_cache;
    bool dfs(const Expr& e1, const Expr& e2);
    void dfs(const Expr& e1, std::vector<Expr>& output_list);
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
    //returns the list of vertices in the topological order
    void getVerticesTopological(std::vector<Expr>& output_list);
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

  //! Canonize the expression e, assuming,  all children are canonical
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
  bool addToBuffer(const Theorem& thm, bool priority = false);

  /*! @brief Given a canonized term, compute a factor to make all
    coefficients integer and relatively prime */
  Expr computeNormalFactor(const Expr& rhs, bool normalizeConstants);

  //! Normalize an equation (make all coefficients rel. prime integers)
  Theorem normalize(const Expr& e);

  //! Normalize an equation (make all coefficients rel. prime integers)
  /*! accepts a rewrite theorem over eqn|ineqn and normalizes it
   *  and returns a theorem to that effect.
   */
  Theorem normalize(const Theorem& thm);

  Expr pickMonomial(const Expr& right);

  void getFactors(const Expr& e, std::set<Expr>& factors);

 public: // ArithTheoremProducer needs these functions, so make them public
  //! Separate monomial e = c*p1*...*pn into c and 1*p1*...*pn
  void separateMonomial(const Expr& e, Expr& c, Expr& var);

  //! Check the term t for integrality (return bool)
  bool isInteger(const Expr& e)
  { return
      isInt(e.getType()) ? true :
      (isReal(e.getType()) ? false : !(isIntegerThm(e).isNull())); }

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
  TheoryArithOld(TheoryCore* core);
  ~TheoryArithOld();

  // Trusted method that creates the proof rules class (used in constructor).
  // Implemented in arith_theorem_producer.cpp
  ArithProofRules* createProofRulesOld();

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
	ExprMap<Rational> maxCoefficientLeft;
	ExprMap<Rational> maxCoefficientRight;

	/** Map from variables to the fixed value of one of it's coefficients */
	ExprMap<Rational> fixedMaxCoefficient;

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
	void selectSmallestByCoefficient(const std::vector<Expr>& input, std::vector<Expr>& output);

	/**
	 * Given an inequality theorem check if it is on integers and get rid of the non-integer
	 * constants.
	 */
	Theorem rafineInequalityToInteger(const Theorem& thm);

	/**
	 * Given an equality theorem check if it is on integers with a non-integer constant. If
	 * yes, return a theorem 0 = 1
	 */
	Theorem checkIntegerEquality(const Theorem& thm);

	/** Keep the expressions that are already in the buffer */
	CDMap<Expr, Theorem> bufferedInequalities;

	/** Strict lower bounds on terms, so that we don't add inequalities to the buffer */
	CDMap<Expr, Rational> termLowerBound;
	CDMap<Expr, Theorem> termLowerBoundThm;
	/** Strict upper bounds on terms, so that we don't add inequalities to the buffer */
	CDMap<Expr, Rational> termUpperBound;
	CDMap<Expr, Theorem> termUpperBoundThm;

	/**
	 * Which inequalities have already been projected (on which monomial).
	 *  - if we get an update of an inequality that's not been projected, we don't care
	 *    it will get projected (it's find)
	 *  - when projecting, project the finds, not the originals
	 *  - when done projecting add here, both original and the find
	 */
	CDMap<Expr, Expr> alreadyProjected;

	/**
	 * Sometimes we know an inequality is in the buffer (as a find of something) and
	 * we don't want it in the buffer, but we do want to pre-process it, so we put it
	 * here.
	 */
	CDMap<Expr, bool> dontBuffer;

	/**
	 * Are we doing only difference logic?
	 */
	CDO<bool> diffLogicOnly;

	/**
	 * Takes an inequality theorem and substitutes the rhs for it's find. It also get's normalized.
	 */
	Theorem inequalityToFind(const Theorem& inequalityThm, bool normalizeRHS);

	// x -y <= c
	struct GraphEdge {
		Expr x;
		Expr y;
		Rational c;
	};

	/**
	 * Take inequality of the form 0 op t and extract the c1, t1, c2 and t2, such that
	 * c1 <= t1 and t2 <= c2, where c1 and c2 are constants, and t1 and t2 are either
	 * sums of monomials or a monomial.
	 *
	 * @return the number of variables in terms t1 and t2
	 */
	int extractTermsFromInequality(const Expr& inequality,
			Rational& c1, Expr& t1,
			Rational& c2, Expr& t2);

	void registerAtom(const Expr& e);

	typedef ExprMap< std::set< std::pair<Rational, Expr> > > AtomsMap;

	/** Map from terms to their lower bound (and the original formula expression) */
	AtomsMap formulaAtomLowerBound;

	/** Map from terms to their upper bound (and the original formula expression) */
	AtomsMap formulaAtomUpperBound;

	/** Map of all the atoms in the formula */
	ExprMap<bool> formulaAtoms;

	class DifferenceLogicGraph {

		public:

			/**
	  		 * EpsRational class ecapsulates the rationals with a symbolic small \f$\epsilon\f$ added. Each rational
	  		 * number is presented as a pair \f$(q, k) = q + k\epsilon\f$, where \f$\epsilon\f$ is treated symbolically.
	  		 * The operations on the new rationals are defined as
	  		 * <ul>
	  		 * 	<li>\f$(q_1, k_1) + (q_2, k_2) \equiv (q_1 + q_2, k_1 + k_2)\f$
	  		 *  <li>\f$a \times (q, k) \equiv (a \times q, a \times k)\f$
	  		 *  <li>\f$(q_1, k_1) \leq (q_2, k_2) \equiv (q_1 < q_2) \vee (q_1 = q_2 \wedge k_1 \leq k_2)\f$
	  		 * </ul>
	  		 *
	  		 * Note that the operations on the infinite values are not defined, as they are never used currently. Infinities can
	  		 * only be asigned or compared.
	  		 */
	  		class EpsRational {

	  			protected:

	  				/** Type of rationals, normal and the two infinities */
	  				typedef enum { FINITE, PLUS_INFINITY, MINUS_INFINITY } RationalType;

	  				/** The type of this rational */
	  				RationalType type;

	  				/** The rational part */
	  				Rational q;

	  				/** The epsilon multiplier */
	  				Rational k;

	  				/**
	  				 * Private constructor to construt infinities.
	  				 */
	  				EpsRational(RationalType type) : type(type) {}

	  			public:

	  				/**
	  				 * Returns if the numbe is finite.
	  				 */
	  				inline bool isFinite() const { return type == FINITE; }

	  				/**
	  				 * Returns if the number is a plain rational.
	  				 *
	  				 * @return true if rational, false otherwise
	  				 */
	  				inline bool isRational() const { return k == 0; }

	  				/**
	  				 * Returns if the number is a plain integer.
	  				 *
	  				 * @return true if rational, false otherwise
	  				 */
	  				inline bool isInteger() const { return k == 0 && q.isInteger(); }

	  				/**
	  				 * Returns the floor of the number \f$x = q + k \epsilon\f$ using the following fomula
	  				 * \f[
	  				 * \lfloor \beta(x) \rfloor =
	  				 * \begin{cases}
	  				 * \lfloor q \rfloor & \text{ if } q \notin Z\\
	  				 * q & \text{ if } q \in Z \text{ and } k \geq 0\\
	  				 * q - 1 & \text{ if } q \in Z \text{ and } k < 0
	  				 * \end{cases}
	  				 * \f]
	  				 */
	  				inline Rational getFloor() const {
	  					if (q.isInteger()) {
	  						if (k >= 0) return q;
	  						else return q - 1;
	  					} else
	  						// If not an integer, just floor it
	  						return floor(q);
	  				}


					/**
					 * Returns the rational part of the number
					 *
					 * @return the rational
					 */
					inline Rational getRational() const { return q; }

					/**
					 * Returns the epsilon part of the number
					 *
					 * @return the epsilon
					 */
					inline Rational getEpsilon() const { return k; }

					/** The infinity constant */
					static const EpsRational PlusInfinity;
					/** The negative infinity constant */
					static const EpsRational MinusInfinity;
					/** The zero constant */
					static const EpsRational Zero;


					/** The blank constructor */
					EpsRational() : type(FINITE), q(0), k(0) {}

					/** Copy constructor */
					EpsRational(const EpsRational& r) : type(r.type), q(r.q), k(r.k) {}

	  				/**
	  				 * Constructor from a rational, constructs a new pair (q, 0).
	  				 *
	  				 * @param q the rational
	  				 */
					EpsRational(const Rational& q) : type(FINITE), q(q), k(0) {}

	  				/**
	  				 * Constructor from a rational and a given epsilon multiplier, constructs a
	  				 * new pair (q, k).
	  				 *
	  				 * @param q the rational
	  				 * @param k the epsilon multiplier
	  				 */
					EpsRational(const Rational& q, const Rational& k) : type(FINITE), q(q), k(k) {}

	  				/**
	  				 * Addition operator for two EpsRational numbers.
	  				 *
	  				 * @param r the number to be added
	  				 * @return the sum as defined in the class
	  				 */
	  				inline EpsRational operator + (const EpsRational& r) const {
	  					DebugAssert(type == FINITE, "EpsRational::operator +, adding to infinite number");
	  					DebugAssert(r.type == FINITE, "EpsRational::operator +, adding an infinite number");
	  					return EpsRational(q + r.q, k + r.k);
	  				}

	  				/**
	  				 * Addition operator for two EpsRational numbers.
	  				 *
	  				 * @param r the number to be added
	  				 * @return the sum as defined in the class
	  				 */
	  				inline EpsRational& operator += (const EpsRational& r) {
	  					DebugAssert(type == FINITE, "EpsRational::operator +, adding to infinite number");
	  					q = q + r.q;
	  					k = k + r.k;
	  					return *this;
	  				}

	  				/**
	  				 * Subtraction operator for two EpsRational numbers.
	  				 *
	  				 * @param r the number to be added
	  				 * @return the sum as defined in the class
	  				 */
	  				inline EpsRational operator - (const EpsRational& r) const {
	 					DebugAssert(type == FINITE, "EpsRational::operator -, subtracting from infinite number");
	  					DebugAssert(r.type == FINITE, "EpsRational::operator -, subtracting an infinite number");
	  					return EpsRational(q - r.q, k - r.k);
	  				}

	  				/**
	  				 * Unary minus operator
	  				 */
	  				inline EpsRational operator - () {
	 					DebugAssert(type == FINITE, "EpsRational::operator -, subtracting from infinite number");
	  					q = -q;
	  					k = -k;
	 					return *this;
	  				}


	  				/**
	  				 * Multiplication operator EpsRational number and a rational number.
	  				 *
	  				 * @param a the number to be multiplied
	  				 * @return the product as defined in the class
	  				 */
	  				inline EpsRational operator * (const Rational& a) const {
	 					DebugAssert(type == FINITE, "EpsRational::operator *, multiplying an infinite number");
	  					return EpsRational(a * q, a * k);
	  				}

	  				/**
	  				 * Division operator EpsRational number and a rational number.
	  				 *
	  				 * @param a the number to be multiplied
	  				 * @return the product as defined in the class
	  				 */
	  				inline EpsRational operator / (const Rational& a) const {
	 					DebugAssert(type == FINITE, "EpsRational::operator *, dividing an infinite number");
	  					return EpsRational(q / a, k / a);
	  				}

	  				/**
	  				 * Equality comparison operator.
	  				 */
	  				inline bool operator == (const EpsRational& r) const { return (q == r.q && k == r.k);	}

	  				/**
	  				 * Less than or equal comparison operator.
	  				 */
	  				inline bool operator <= (const EpsRational& r) const {
	  					switch (r.type) {
	  						case FINITE:
	  							if (type == FINITE)
	  								// Normal comparison
	  								return (q < r.q || (q == r.q && k <= r.k));
	  							else
	  								// Finite number is bigger only of the negative infinity
	  								return type == MINUS_INFINITY;
	  						case PLUS_INFINITY:
	  							// Everything is less then or equal than +inf
	  							return true;
	  						case MINUS_INFINITY:
	  							// Only -inf is less then or equal than -inf
	  							return (type == MINUS_INFINITY);
	  						default:
	  							// Ohohohohohoooooo, whats up
	  							FatalAssert(false, "EpsRational::operator <=, what kind of number is this????");
	  					}
	  					return false;
	  				}

	  				/**
	  				 * Less than comparison operator.
	  				 */
	  				inline bool operator < (const EpsRational& r) const { return !(r <= *this); }

	  				/**
	  				 * Greater than comparison operator.
	  				 */
	  				inline bool operator > (const EpsRational& r) const { return !(*this <= r); }

	  				/**
	  				 * Returns the string representation of the number.
	  				 *
	  				 * @return the string representation of the number
	  				 */
	  				std::string toString() const {
	  					switch (type) {
	  						case FINITE:
	  							return "(" + q.toString() + ", " + k.toString() + ")";
	  						case PLUS_INFINITY:
	  							return "+inf";
	  						case MINUS_INFINITY:
	  							return "-inf";
	  						default:
	  							FatalAssert(false, "EpsRational::toString, what kind of number is this????");
	  					}
	  					return "hm, what am I?";
	  				}
	  		};

			struct EdgeInfo {
				/** The length of this edge */
				EpsRational length;
				/** The number of edges in this path */
				int path_length_in_edges;
				/** If this is a summary edge, a vertex in the path */
				Expr in_path_vertex;
				/** If this is an original edge, the theorem that explains it */
				Theorem explanation;

				/** Returnes if the edge is well define (i.e. not +infinity) */
				bool isDefined() const { return path_length_in_edges != 0; }

				EdgeInfo(): path_length_in_edges(0) {}
			};

			/**
			 * Given two vertices in the graph and an path edge, reconstruct all the theorems and put them
			 * in the output vector
			 */
			void getEdgeTheorems(const Expr& x, const Expr& y, const EdgeInfo& edgeInfo, std::vector<Theorem>& outputTheorems);

			/**
			 * Returns the current weight of the edge.
			 */
			EpsRational getEdgeWeight(const Expr& x, const Expr& y);

			/**
			 * Returns whether a vertex has incoming edges.
			 */
			bool hasIncoming(const Expr& x);

			/**
			 * Returns whether a vertex has outgoing edges.
			 */
			bool hasOutgoing(const Expr& x);

		protected:

			/** Threshold on path length to process (ignore bigger than and set incomplete) */
			const int* d_pathLenghtThres;

			/** The arithmetic that's using this graph */
			TheoryArithOld* arith;

			/** The core theory */
			TheoryCore* core;

			/** The arithmetic that is using u us */
			ArithProofRules* rules;

			/** The unsat theorem if available */
			CDO<Theorem> unsat_theorem;

			/** The biggest epsilon from EpsRational we used in paths */
			CDO<Rational> biggestEpsilon;

			/** The smallest rational difference we used in path relaxation */
			CDO<Rational> smallestPathDifference;

			/** The graph itself, maps expressions (x-y) to the edge information */
			typedef CDMap<Expr, EdgeInfo> Graph;

			/** Graph of <= paths */
			Graph leGraph;

			typedef ExprMap<CDList<Expr>*> EdgesList;

			/** List of vertices adjacent backwards to a vertex */
			EdgesList incomingEdges;
			/** List of vertices adjacent forward to a vertex */
			EdgesList outgoingEdges;

			/**
			 * Returns the edge (path) info for the given kind
			 *
			 * @param x the starting vertex
			 * @param y the ending vertex
			 * @return the edge information
			 */
			Graph::ElementReference getEdge(const Expr& x, const Expr& y);

			/**
			 * Try to update the shortest path from x to z using y.
			 */
			bool tryUpdate(const Expr& x, const Expr& y, const Expr& z);

		public:

			void writeGraph(std::ostream& out);

			/**
			 * Fills the vector with all the variables (vertices) in the graph
			 */
			void getVariables(std::vector<Expr>& variables);

			void setRules(ArithProofRules* rules) {
				this->rules = rules;
			}

			void setArith(TheoryArithOld* arith) {
				this->arith = arith;
			}

			/**
			 * Class constructor.
			 */
			DifferenceLogicGraph(TheoryArithOld* arith, TheoryCore* core, ArithProofRules* rules, Context* context);

			/**
			 * Destructor
			 */
			~DifferenceLogicGraph();

			/**
			 * Returns the reference to the unsat theorem if there is a negative
			 * cycle in the graph.
			 *
			 * @return the unsat theorem
			 */

			Theorem getUnsatTheorem();

			/**
			 * Returns true if there is a negative cycle in the graph.
			 */
			bool isUnsat();

			void computeModel();

			Rational getValuation(const Expr& x);


			/**
			 * Adds an edge corresponding to the constraint x - y <= c.
			 *
			 * @param x variable x::Difference
			 * @param y variable y
			 * @param c rational c
			 * @param edge_thm the theorem for this edge
			 */
			void addEdge(const Expr& x, const Expr& y, const Rational& c, const Theorem& edge_thm);

			/**
			 * Check if there is an edge from x to y
			 */
			bool existsEdge(const Expr& x, const Expr& y);

			/**
			 * Check if x is in a cycle
			 */
			bool inCycle(const Expr& x);

			/**
			 * Given a shared integer term expand it into the gray shadow on the bounds (if bounded from both sides).
			 */
			void expandSharedTerm(const Expr& x);

		protected:

			/** Whether the variable is in a cycle */
			CDMap<Expr, bool> varInCycle;

			Expr sourceVertex;

			/**
			 * Produced the unsat theorem from a cycle x --> x of negative length
			 *
			 * @param x the variable to use for the conflict
			 * @param kind the kind of edges to consider
			 */
			void analyseConflict(const Expr& x, int kind);
	};

	/** The graph for difference logic */
	DifferenceLogicGraph diffLogicGraph;

	Expr zero;

	/** Index for expanding on shared term equalities */
	CDO<unsigned> shared_index_1;
	/** Index for expanding on shared term equalities */
	CDO<unsigned> shared_index_2;

	std::vector<Theorem> multiplicativeSignSplits;

	int termDegree(const Expr& e);

	bool canPickEqMonomial(const Expr& right);

private:

	// Keeps all expressions that are bounded for disequality splitting and shared term comparisons
	CDMap<Expr, DifferenceLogicGraph::EpsRational> termUpperBounded;
	CDMap<Expr, DifferenceLogicGraph::EpsRational> termLowerBounded;

	// Keeps all expressions that are constrained
	CDMap<Expr, bool> termConstrainedBelow;
	CDMap<Expr, bool> termConstrainedAbove;

	enum BoundsQueryType {
		/** Query the bounds/constrained using cache for leaves */
		QueryWithCacheLeaves,
		/** Query the bounds/constrained using cashe for leaves, but also see if the value is constrained */
		QueryWithCacheLeavesAndConstrainedComputation,
		/** Query the bounds/constrained by only querying the cache, don't try to figure it out */
		QueryWithCacheAll
	};

	/**
	 * Check if the term is bounded. If the term is non-linear, just returns false.
	 */
	bool isBounded(const Expr& t, BoundsQueryType queryType = QueryWithCacheLeaves);
	bool hasLowerBound(const Expr& t, BoundsQueryType queryType = QueryWithCacheLeaves) { return getLowerBound(t, queryType).isFinite(); }
	bool hasUpperBound(const Expr& t, BoundsQueryType queryType = QueryWithCacheLeaves) { return getUpperBound(t, queryType).isFinite(); }

	bool isConstrained(const Expr& t, bool intOnly = true, BoundsQueryType queryType = QueryWithCacheLeaves);
	bool isConstrainedAbove(const Expr& t, BoundsQueryType queryType = QueryWithCacheLeaves);
	bool isConstrainedBelow(const Expr& t, BoundsQueryType queryType = QueryWithCacheLeaves);

	/**
	 * Check if the term is bounded from above. If the term is non-linear, just returns false.
	 */
	DifferenceLogicGraph::EpsRational getUpperBound(const Expr& t, BoundsQueryType queryType = QueryWithCacheLeaves);

	/**
	 * Check if the term is bouned from below. If the term is non-linear, just return false.
	 */
	DifferenceLogicGraph::EpsRational getLowerBound(const Expr& t, BoundsQueryType queryType = QueryWithCacheLeaves);

	/**
	 * See whether and which terms are bounded.
	 */
	int computeTermBounds();

public:

	void tryPropagate(const Expr& x, const Expr& y, const DifferenceLogicGraph::EdgeInfo& x_y_edge, int kind);

	void addMultiplicativeSignSplit(const Theorem& case_split_thm);

	bool addPairToArithOrder(const Expr& smaller, const Expr& bigger);

	bool nonlinearSignSplit() const { return *d_splitSign; }

	/**
	 * Check if equation is nonlinear. An equation is nonlinear if there is at least one nonlinear term in the sum
	 * on either side of the equation.
	 */
	bool isNonlinearEq(const Expr& e);

	/**
	 * Check if a sum term is nonlinear
	 */
	bool isNonlinearSumTerm(const Expr& term);

	/**
	 * Check if the equality is of the form c + power1^n - power2^n = 0;
	 */
	bool isPowersEquality(const Expr& nonlinearEq, Expr& power1, Expr& power2);

	/**
	 * Check if the equality is of the form c - x^n = 0
	 */
	bool isPowerEquality(const Expr& nonlinearEq, Rational& constant, Expr& power1);

};

}

#endif
