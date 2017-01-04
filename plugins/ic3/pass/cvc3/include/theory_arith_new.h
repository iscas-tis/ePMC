/*****************************************************************************/
/*!
 * \file theory_arith_new.h
 * 
 * Author: Dejan Jovanovic
 *
 * Created: Thu Jun 14 13:38:16 2007
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

#ifndef _cvc3__include__theory_arith_new_h_
#define _cvc3__include__theory_arith_new_h_

#include "theory_arith.h"

#include <hash_fun.h>
#include <hash_map.h>
#include <queryresult.h>
#include <map>

namespace CVC3 {


/**
 * This theory handles basic linear arithmetic.
 *
 * @author Clark Barrett
 * 
 * @since Sat Feb  8 14:44:32 2003
 */
class TheoryArithNew :public TheoryArith {
		
  /** For concrete model generation */
  CDList<Theorem> d_diseq;
  /** Index to the next unprocessed disequality */   
  CDO<size_t> d_diseqIdx; 
  ArithProofRules* d_rules;
  CDO<bool> d_inModelCreation;

  /** Data class for the strongest free constant in separation inqualities **/
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
  Expr pickIntEqMonomial(const Expr& right);
  //! processes equalities with 1 or more vars of type REAL
  Theorem processRealEq(const Theorem& eqn);
  //! processes equalities whose vars are all of type INT
  Theorem processIntEq(const Theorem& eqn);
  //! One step of INT equality processing (aux. method for processIntEq())
  Theorem processSimpleIntEq(const Theorem& eqn);
  //! Take an inequality and isolate a variable
  Theorem isolateVariable(const Theorem& inputThm, bool& e1);
  //! Update the statistics counters for the variable with a coeff. c
  void updateStats(const Rational& c, const Expr& var);
  //! Update the statistics counters for the monomial
  void updateStats(const Expr& monomial);
  //! Add an inequality to the input buffer.  See also d_buffer
  void addToBuffer(const Theorem& thm);
  Expr pickMonomial(const Expr& right);
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
  TheoryArithNew(TheoryCore* core);
  ~TheoryArithNew();

  // Trusted method that creates the proof rules class (used in constructor).
  // Implemented in arith_theorem_producer.cpp
  ArithProofRules* createProofRules();

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
  virtual Expr parseExprOp(const Expr& e);

  // DDDDDDDDDDDDDDDDDDDDDDDDEEEEEEEEEEEEEEEEEEEEEEEJJJJJJJJJJJJJJJJJJJJJAAAAAAAAAAAAAAAAAAAAAAANNNNNNNNNNNNNNNNNNNNNNN
  
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
  				 * Returns the floor of the number \f$x = q + k \epsilon\f$ using the following formula
  				 * \f[
  				 * \lfloor \beta(x) \rfloor = \left\{
  				 * \begin{tabular}{ll}
  				 * $\lfloor q \rfloor$ & $\mathrm{if\ } q \notin Z$\\
  				 * $q$ & $\mathrm{if\ } q \in Z \mathrm{\ and\ } k \geq 0$\\
  				 * $q - 1$ & $\mathrm{if\ } q \in Z \mathrm{\ and\ } k < 0$
  				 * \end{tabular}\right.
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
				EpsRational(const Rational q) : type(FINITE), q(q), k(0) {} 
  		
  				/**
  				 * Constructor from a rational and a given epsilon multiplier, constructs a 
  				 * new pair (q, k).
  				 * 
  				 * @param q the rational
  				 * @param k the epsilon multiplier
  				 */
				EpsRational(const Rational q, const Rational k) : type(FINITE), q(q), k(k) {} 
  		
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
  
  		/**
  		 * Registers the atom given from the core. This atoms are stored so that they can be used
  		 * for theory propagation.
  		 * 
  		 * @param e the expression (atom) that is part of the input formula
  		 */ 
 		void registerAtom(const Expr& e);
 
  	private:		
  	  	
  	  	/** A set of all integer variables */
  	  	std::set<Expr> intVariables;
  	  	
  	  	/**
  	  	 * Return a Gomory cut plane derivation of the variable $x_i$. Mixed integer
  	  	 * Gomory cut can be constructed if 
  	  	 * <ul>
  	  	 * <li>\f$x_i\f$ is a integer basic variable with a non-integer value
  	  	 * <li>all non-basic variables in the row of \f$x_i\f$ are assigned to their
  	  	 *     upper or lower bounds
  	  	 * <li>all the values on the right side of the row have rational values (non
  	  	 *     eps-rational values)
  	  	 * </ul>
  	  	 */ 
  	  	Theorem deriveGomoryCut(const Expr& x_i);
  	  	
  	  	/**
  	  	 * Tries to rafine the integer constraint of the theorem. For example,
  	  	 * x < 1 is rewritten as x <= 0, and x <(=) 1.5 is rewritten as x <= 1.
  	  	 * The constraint should be in the normal form.
  	  	 * 
  	  	 * @param thm the derivation of the constraint 
  	  	 */
  	  	Theorem rafineIntegerConstraints(const Theorem& thm);
  	  	
  	  	/** Are we consistent or not */
  	  	CDO<QueryResult> consistent;
  	  	
  	  	/** The theorem explaining the inconsistency */
  	  	Theorem explanation;
  	  	
  	  	/** 
  	  	 * The structure necessaty to hold the bounds.
  	  	 */
  	  	struct BoundInfo {
  	  		/** The bound itself */
  	  		EpsRational bound;
  	  		/** The assertion theoreem of the bound */
  	  		Theorem theorem;
  	  		
  	  		/** Constructor */
  	  		BoundInfo(const EpsRational& bound, const Theorem& thm): bound(bound), theorem(thm) {}
  	  		
  	  		/** The empty constructor for the map */
  	  		BoundInfo(): bound(0), theorem() {}
  	  		
  	  		/** 
  	  		 * The comparator, just if we need it. Compares first by expressions then by bounds 
  	  		 */
  	  		bool operator < (const BoundInfo& bI) const {
  	  			// Get tje expressoins
  	  			const Expr& expr1 = (theorem.isRewrite() ? theorem.getRHS() : theorem.getExpr());
  	  			const Expr& expr2 = (bI.theorem.isRewrite() ? bI.theorem.getRHS() : bI.theorem.getExpr());
  	  			
  	  			std::cout << expr1 << " @ " << expr2 << std::endl;
  	  			
  	  			// Compare first by the expressions (right sides of expressions)
  	  			if (expr1[1] == expr2[1])
  	  				// If the same, just return the bound comparison (plus a trick to order equal bounds, different relations) 
  	  				if (bound == bI.bound && expr1.getKind() != expr2.getKind())
  	  					return expr1.getKind() == LE; // LE before GE -- only case that can happen   	  				  	  					
  	  				else
  	  					return bound < bI.bound;
  	  			else
  	  				// Return the expression comparison
  	  				return expr1[1] < expr2[1];
  	  		}
   	  	};


  	  	/** 
  	  	 * The structure necessaty to hold the bounds on expressions (for theory propagation).
  	  	 */
  	  	struct ExprBoundInfo {
  	  		/** The bound itself */
  	  		EpsRational bound;
  	  		/** The assertion theoreem of the bound */
  	  		Expr e;
  	  		
  	  		/** Constructor */
  	  		ExprBoundInfo(const EpsRational& bound, const Expr& e): bound(bound), e(e) {}
  	  		
  	  		/** The empty constructor for the map */
  	  		ExprBoundInfo(): bound(0) {}
  	  		
  	  		/** 
  	  		 * The comparator, just if we need it. Compares first by expressions then by bounds 
  	  		 */
  	  		bool operator < (const ExprBoundInfo& bI) const {
  	  			
  	  			// Compare first by the expressions (right sides of expressions)
  	  			if (e[1] == bI.e[1])
  	  				// If the same, just return the bound comparison (plus a trick to order equal bounds, different relations) 
  	  				if (bound == bI.bound && e.getKind() != bI.e.getKind())
  	  					return e.getKind() == LE; // LE before GE -- only case that can happen   	  				  	  					
  	  				else
  	  					return bound < bI.bound;
  	  			else
  	  				// Return the expression comparison
  	  				return e[1] < bI.e[1];
  	  		}
   	  	};
  	  	
  	    /** The map from variables to lower bounds (these must be backtrackable) */  
  		CDMap<Expr, BoundInfo> lowerBound;
  		/** The map from variables to upper bounds (these must be backtrackable) */ 
  		CDMap<Expr, BoundInfo> upperBound;
  		/** The current real valuation of the variables (these must be backtrackable for the last succesefull checkSAT!!!) */
  		CDMap<Expr, EpsRational> beta;
  		  	
  		typedef Hash::hash_map<Expr, Theorem> TebleauxMap;
  		//typedef google::sparse_hash_map<Expr, Theorem, Hash::hash<Expr> > TebleauxMap;
  		//typedef std::map<Expr, Theorem> TebleauxMap;
  		
  		typedef std::set<Expr> SetOfVariables;
  		typedef Hash::hash_map<Expr, SetOfVariables> DependenciesMap;
  		
  		/** Maps variables to sets of variables that depend on it in the tableaux */
  		DependenciesMap dependenciesMap;	
  		  	
  	    /** The tableaux, a map from basic variables to the expressions over the non-basic ones (theorems that explain them how we got there) */ 
  		TebleauxMap tableaux;

		/** Additional tableaux map from expressions asserted to the corresponding theorems explaining the introduction of the new variables */
		TebleauxMap freshVariables;
		
		/** A set containing all the unsatisfied basic variables */
		std::set<Expr> unsatBasicVariables;
		
		/** The vector to keep the assignments from fresh variables to expressions they represent */	
		std::vector<Expr> assertedExpr;
		/** The backtrackable number of fresh variables asserted so far */
		CDO<unsigned int> assertedExprCount;
		
		/** A set of BoundInfo objects */
		typedef std::set<ExprBoundInfo> BoundInfoSet;
		
		/** Internal variable to see wheather to propagate or not */
		bool propagate;
		
		/** 
		 * Propagate all that is possible from given assertion and its bound
		 */
		void propagateTheory(const Expr& assertExpr, const EpsRational& bound, const EpsRational& oldBound);		
		
		/**
		 * Store all the atoms from the formula in this set. It is searchable by an expression
		 * and the bound. To get all the implied atoms, one just has to go up (down) and check if the 
		 * atom or it's negation are implied.
		 */
		BoundInfoSet allBounds;
		
		/**
		 * Adds var to the dependencies sets of all the variables in the sum.
		 * 
		 * @param var the variable on the left side 
		 * @param sum the sum that defines the variable
		 */
		void updateDependenciesAdd(const Expr& var, const Expr& sum);
		
		/**
		 * Remove var from the dependencies sets of all the variables in the sum.
		 * 
		 * @param var the variable on the left side 
		 * @param sum the sum that defines the variable
		 */
		void updateDependenciesRemove(const Expr& var, const Expr& sum);
	
		/**
		 * Updates the dependencies if a right side of an expression in the tableaux is changed. For example,
		 * if oldExpr is x + y and newExpr is y + z, var will be added to the dependency list of z, and removed
		 * from the dependency list of x.
		 * 
		 * @param oldExpr the old right side of the tableaux 
		 * @param newExpr the new right side of the tableaux
		 * @param var the variable that is defined by these two expressions
		 * @param skipVar a variable to skip when going through the expressions
		 */
		void updateDependencies(const Expr& oldExpr, const Expr& newExpr, const Expr& var, const Expr& skipVar);
		
		/**
		 * Update the values of variables that have appeared in the tableaux due to backtracking.
		 */
		void updateFreshVariables();
		
		/** 
		 * Updates the value of variable var by computing the value of expression e.
		 * 
		 * @param var the variable to update
		 * @param e the expression to compute
		 */
		void updateValue(const Expr& var, const Expr& e);
		
		/**
		 * Returns a string representation of the tableaux.
		 * 
		 * @return tableaux as string
		 */
		std::string tableauxAsString() const;
		
		/**
		 * Returns a string representation of the unsat variables.
		 * 
		 * @return unsat as string
		 */
		std::string unsatAsString() const;
		
		/**
		 * Returns a string representation of the current bounds.
		 * 
		 * @return tableaux as string
		 */
		std::string boundsAsString();

		/**
		 * Gets the equality of the fresh variable tableaux variable corresponding to this expression. If the expression has already been
		 * asserted, the coresponding variable is returned, othervise a fresh variable is created and the theorem is returned.
		 * 
		 * @param leftSide the left side of the asserted constraint
		 * @return the equality theorem s = leftSide
		 */
		Theorem getVariableIntroThm(const Expr& leftSide);

		/**
		 * Find the coefficient standing by the variable var in the expression expr. Expresion is expected to be
		 * in canonical form, i.e either a rational constant, an arithmetic leaf (i.e. variable or term from some 
		 * other theory), (MULT rat leaf) where rat is a non-zero rational constant, leaf is an arithmetic leaf or
		 * (PLUS \f$const term_0 term_1 ... term_n\f$) where each \f$term_i\f$ is either a leaf or (MULT \f$rat leaf\f$) 
		 * and each leaf in \f$term_i\f$ must be strictly greater than the leaf in \f$term_{i+1}\f$.               
		 * 
		 * @param var the variable
		 * @param expr the expression to search in
		 */  		
  		const Rational& findCoefficient(const Expr& var, const Expr& expr); 
  	
  		/**
  		 * Return true iof the given variable is basic in the tableaux, i.e. it is on the left side, expressed
  		 * in terms of the non-basic variables.
  		 * 
  		 * @param x the variable to be checked
  		 * @return true if the variable is basic, false if the variable is non-basic
  		 */
  		bool isBasic(const Expr& x) const;
  		
  		/**
  		 * Returns the coefficient at a_ij in the current tableaux, i.e. the coefficient 
  		 * at x_j in the row of x_i.
  		 * 
  		 * @param x_i a basic variable
  		 * @param x_j a non-basic variable
  		 * @return the reational coefficient 
  		 */
  		Rational getTableauxEntry(const Expr& x_i, const Expr& x_j); 
  		
  		/**
  		 * Swaps a basic variable \f$x_r\f$ and a non-basic variable \f$x_s\f$ such
  		 * that ars \f$a_{rs} \neq 0\f$. After pivoting, \f$x_s\f$ becomes basic and \f$x_r\f$ becomes non-basic. 
  		 * The tableau is updated by replacing equation \f[x_r = \sum_{x_j \in N}{a_{rj}xj}\f] with
  		 * \f[x_s = \frac{x_r}{a_{rs}} - \sum_{x_j \in N }{\frac{a_{rj}}{a_rs}x_j}\f] and this equation 
  		 * is used to eliminate \f$x_s\f$ from the rest of the tableau by substitution.
  		 * 
  		 * @param x_r a basic variable
  		 * @param x_s a non-basic variable
  		 */
  		void pivot(const Expr& x_r, const Expr& x_s);
  	
  		/**
  		 * Sets the value of a non-basic variable \f$x_i\f$ to \f$v\f$ and adjusts the value of all 
  		 * the basic variables so that all equations remain satisfied.
  		 * 
  		 * @param x_i a non-basic variable
  		 * @param v the value to set the variable \f$x_i\f$ to 
  		 */
  		void update(const Expr& x_i, const EpsRational& v);
  		
  		/**
  		 * Pivots the basic variable \f$x_i\f$ and the non-basic variable \f$x_j\f$. It also sets \f$x_i\f$ to \f$v\f$ and adjusts all basic 
  		 * variables to keep the equations satisfied.
  		 * 
  		 * @param x_i a basic variable
  		 * @param x_j a non-basic variable
  		 * @param v the valie to assign to x_i
  		 */ 
  		void pivotAndUpdate(const Expr& x_i, const Expr& x_j, const EpsRational& v);
  		
  		/**
  		 * Asserts a new upper bound constraint on a variable and performs a simple check for consistency (not complete).
  		 * 
  		 * @param x_i the variable to assert the bound on
  		 * @param c the bound to assert
		 */
  		QueryResult assertUpper(const Expr& x_i, const EpsRational& c, const Theorem& thm);

  		/**
  		 * Asserts a new lower bound constraint on a variable and performs a simple check for consistency (not complete).
  		 * 
  		 * @param x_i the variable to assert the bound on
  		 * @param c the bound to assert
		 */
  		QueryResult assertLower(const Expr& x_i, const EpsRational& c, const Theorem& thm);
  		
  		/**
  		 * Asserts a new equality constraint on a variable by asserting both upper and lower bounds.
  		 * 
  		 * @param x_i the variable to assert the bound on
  		 * @param c the bound to assert
		 */
  		QueryResult assertEqual(const Expr& x_i, const EpsRational& c, const Theorem& thm);  		

		/**
  		 * Type of noramlization GCD = 1 or just first coefficient 1
  		 */
  		typedef enum { NORMALIZE_GCD, NORMALIZE_UNIT } NormalizationType;
  		
  		/**
  		 * Given a canonized term, compute a factor to make all coefficients integer and relatively prime 
  		 */
  		Expr computeNormalFactor(const Expr& rhs, NormalizationType type = NORMALIZE_GCD);
  
  		/**
  		 * Normalize an equation (make all coefficients rel. prime integers)
  		 */
  		Theorem normalize(const Expr& e, NormalizationType type = NORMALIZE_GCD);
  		
  		/**
  		 * Normalize an equation (make all coefficients rel. prime integers) accepts a rewrite theorem over 
  		 * eqn|ineqn and normalizes it and returns a theorem to that effect.
   		 */
  		Theorem normalize(const Theorem& thm, NormalizationType type = NORMALIZE_GCD);
  	
		/**
		 * Canonise the equation using the tebleaux equations, i.e. replace all the tableaux right sides
		 * with the corresponding left sides and canonise the result. 
		 * 
		 * @param eq the equation to canonise
		 * @return the explaining theorem
		 */  	
  		Theorem substAndCanonizeModTableaux(const Theorem& eq);
  		
  		/**
		 * Canonise the sum using the tebleaux equations, i.e. replace all the tableaux right sides
		 * with the corresponding left sides and canonise the result. 
		 * 
		 * @param sum the canonised sum to canonise
		 * @return the explaining theorem
		 */  	
  		Theorem substAndCanonizeModTableaux(const Expr& sum);
  		
  		/**
  		 * Sustitute the given equation everywhere in the tableaux.
  		 * 
  		 * @param eq the equation to use for substitution
  		 */
  		void substAndCanonizeTableaux(const Theorem& eq);
  		
  		/**
  		 * Given an equality eq: \f$\sum a_i x_i = y\f$ and a variable $var$ that appears in
  		 * on the left hand side, pivots $y$ and $var$ so that $y$ comes to the right-hand 
  		 * side.
  		 * 
  		 * @param eq the proof of the equality
  		 * @param var the variable to move to the right-hand side
  		 */
  		Theorem pivotRule(const Theorem& eq, const Expr& var);
  		
  		/**
  		 * Knowing that the tableaux row for \f$x_i\f$ is the problematic one, generate the
  		 * explanation clause. The variables in the row of \f$x_i = \sum_{x_j \in \mathcal{N}}{a_ij x_j}\f$ are separated to
  		 * <ul>
  		 * <li>\f$\mathcal{N}^+ = \left\lbrace x_j \in \mathcal{N} \; | \; a_{ij} > 0 \right\rbrace\f$
		 * <li>\f$\mathcal{N}^- = \left\lbrace  x_j \in \mathcal{N} \; | \; a_{ij} < 0\right\rbrace\f$ 
  		 * </ul>
  		 * Then, the explanation clause to be returned is 
  		 * \f[\Gamma = \left\lbrace x_j \leq u_j \; | \; x_j \in \mathcal{N}^+\right\rbrace \cup \left\lbrace l_j \leq x_j \; | \; 
  		 * x_j \in \mathcal{N}^-\right\rbrace \cup \left\lbrace l_i \leq x_i  \right\rbrace\f]
  		 * 
  		 * @param var_it the variable that caused the clash
  		 * @return the theorem explainang the clash
  		 */ 
  		Theorem getLowerBoundExplanation(const TebleauxMap::iterator& var_it);

  		/**
  		 * Knowing that the tableaux row for \f$x_i\f$ is the problematic one, generate the
  		 * explanation clause. The variables in the row of \f$x_i = \sum_{x_j \in \mathcal{N}}{a_ij x_j}\f$ are separated to
  		 * <ul>
  		 * <li>\f$\mathcal{N}^+ = \left\lbrace x_j \in \mathcal{N} \; | \; a_{ij} > 0 \right\rbrace\f$
		 * <li>\f$\mathcal{N}^- = \left\lbrace  x_j \in \mathcal{N} \; | \; a_{ij} < 0\right\rbrace\f$ 
  		 * </ul>
  		 * Then, the explanation clause to be returned is 
  		 * \f[\Gamma = \left\lbrace x_j \leq u_j \; | \; x_j \in \mathcal{N}^-\right\rbrace \cup \left\lbrace l_j \leq x_j \; | \; 
  		 * x_j \in \mathcal{N}^+\right\rbrace \cup \left\lbrace x_i \leq u_i \right\rbrace\f]
  		 * 
  		 * @param var_it the variable that caused the clash
  		 * @return the theorem explainang the clash
  		 */ 
		Theorem getUpperBoundExplanation(const TebleauxMap::iterator& var_it);
  		
  		Theorem addInequalities(const Theorem& le_1, const Theorem& le_2);
  	  	
  		/**
  		 * Check the satisfiability 
  		 */
  		QueryResult checkSatSimplex();
  		
  		/**
  		 * Check the satisfiability of integer constraints
  		 */
  		QueryResult checkSatInteger();
  		
  		/** 
  		 * The last lemma that we asserted to check the integer satisfiability. We don't do checksat until
  		 * the lemma split has been asserted.
  		 */
  		CDO<Theorem> integer_lemma;
  	  
	public:
  	
  		/**
  		 * Gets the current lower bound on variable x.
  		 * 
  		 * @param x the variable
  		 * @return the current lower bound on x
  		 */
  		EpsRational getLowerBound(const Expr& x) const;
  		
  		/**
  		 * Get the current upper bound on variable x.
  		 * 
  		 * @param x the variable
  		 * @return the current upper bound on x
  		 */
  		EpsRational getUpperBound(const Expr& x) const;

		/**
  		 * Gets the theorem of the current lower bound on variable x.
  		 * 
  		 * @param x the variable
  		 * @return the current lower bound on x
  		 */
  		Theorem getLowerBoundThm(const Expr& x) const;
  		
  		/**
  		 * Get the theorem of the current upper bound on variable x.
  		 * 
  		 * @param x the variable
  		 * @return the current upper bound on x
  		 */
  		Theorem getUpperBoundThm(const Expr& x) const;
  		
  		/**
  		 * Gets the current valuation of variable x (beta).
  		 * 
  		 * @param x the variable
  		 * @return the current value of variable x
  		 */
  		EpsRational getBeta(const Expr& x);
		  	  
  // DDDDDDDDDDDDDDDDDDDDDDDDEEEEEEEEEEEEEEEEEEEEEEEJJJJJJJJJJJJJJJJJJJJJAAAAAAAAAAAAAAAAAAAAAAANNNNNNNNNNNNNNNNNNNNNNN

};

}

#endif
