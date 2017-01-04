/*****************************************************************************/
/*!
 *\file expr_transform.h
 *\brief Generally Useful Expression Transformations
 *
 * Author: Clark Barrett
 *
 * Created: Fri Aug  5 16:11:51 2005
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

#ifndef _cvc3__include__expr_transform_h_
#define _cvc3__include__expr_transform_h_

#include "expr.h"

namespace CVC3 {

  class VCL;
  class TheoryCore;
  class CommonProofRules;
  class CoreProofRules;
  class TheoryArith;

class ExprTransform {

  TheoryCore* d_core;
  TheoryArith* d_theoryArith;
  CommonProofRules* d_commonRules;
  CoreProofRules* d_rules;

  //! Cache for pushNegation()
  ExprMap<Theorem> d_pushNegCache;
  //! Cache for newPP
  ExprMap<Theorem> d_newPPCache;
  //! Budget limit for newPP
  int d_budgetLimit;

public:
  ExprTransform(TheoryCore* core);
  ~ExprTransform() {}

  void setTheoryArith(TheoryArith* arith) { d_theoryArith = arith; }
  
// <UFTeam Junk>

  class CParameter;
  //void get_atoms(std::set< Expr >& atoms, const Expr& e);
  //Theorem do_static_learn(const Expr& e);
 

  typedef std::map< std::pair< Expr, ExprTransform::CParameter >, Expr > T_name_map;
  typedef std::map< Expr, std::set< ExprTransform::CParameter >* > T_ack_map;
  typedef std::map< Expr, Type> T_type_map;
  typedef std::map< std::pair< Expr, Expr>, Expr > B_name_map;
  typedef std::map<Expr, Type> B_type_map;
  typedef std::map< Expr, std::set<Expr>*> T_generator_map;
  typedef std::map<Expr, std::vector<Expr>*> B_Term_map;
  typedef std::map< Expr, Expr> T_ITE_map;
  typedef std::map< Expr, int> B_formula_map;
  typedef std::map<Expr, std::set<int>*> NEW_formula_map;
  typedef std::vector<Expr> T_ITE_vec;
  std::string NewBryantVar(const int a, const int b);
  std::string NewVar(const int a, const int b);
  B_name_map BryantNames(T_generator_map& generator_map, B_type_map& type_map);
  Expr ITE_generator(Expr& Orig, Expr& Value, B_Term_map& Creation_map, B_name_map& name_map,
								      T_ITE_map& ITE_map);
  void Get_ITEs(B_formula_map& instance_map, std::set<Expr>& Not_replaced_set, B_Term_map& P_term_map, T_ITE_vec& ITE_vec, B_Term_map& Creation_map, 
							  B_name_map& name_map, T_ITE_map& ITE_map); 

  
  void PredConstrainTester(std::set<Expr>& Not_replaced_set, const Expr& e, B_name_map& name_map, std::vector<Expr>& Pred_vec, std::set<Expr>& Constrained_set, std::set<Expr>& P_constrained_set, T_generator_map& Constrained_map);

  void PredConstrainer(std::set<Expr>& Not_replaced_set, const Expr& e, const Expr& Pred, int location, B_name_map& name_map, std::set<Expr>& SeenBefore, std::set<Expr>& Constrained_set, T_generator_map& Constrained_map, std::set<Expr>& P_constrained_set);


  Expr ConstrainedConstraints(std::set<Expr>& Not_replaced_set, T_generator_map& Constrained_map, B_name_map& name_map, B_Term_map& Creation_map, std::set<Expr>& Constrained_set, std::set<Expr>& UnConstrained_set, std::set<Expr>& P_constrained_set);

  void RemoveFunctionApps(const Expr& orig, std::set<Expr>& Not_replaced_set, std::vector<Expr>& Old, std::vector<Expr>& New, T_ITE_map& ITE_map, std::set<Expr>& SeenBefore);
  void GetSortedOpVec(B_Term_map& X_generator_map, B_Term_map& X_term_map, B_Term_map& P_term_map, std::set<Expr>& P_terms, std::set<Expr>& G_terms, std::set<Expr>& X_terms, std::vector<Expr>& sortedOps, std::set<Expr>& SeenBefore);
  void GetFormulaMap(const Expr& e, std::set<Expr>& formula_map, std::set<Expr>& G_terms, int& size, int negations);
  void GetGTerms2(std::set<Expr>& formula_map, std::set<Expr>& G_terms);
  void GetSub_vec(T_ITE_vec& ITE_vec, const Expr& e, std::set<Expr>& ITE_Added);
  //void GetOrderedTerms(B_Term_map& X_term_map, T_ITE_vec& ITE_vec, std::set<Expr>& G_terms, std::set<Expr>& X_terms, std::vector<Expr>& Pred_vec, std::vector<Expr>& sortedOps, std::vector<Expr>& Constrained_vec, std::vector<Expr>& UnConstrained_vec, B_Term_map& G_term_map, B_Term_map& P_term_map, std::set<Expr>& SeenBefore, std::set<Expr>& ITE_Added);
    void GetOrderedTerms(B_formula_map& instance_map, B_name_map& name_map, B_Term_map& X_term_map, T_ITE_vec& ITE_vec, std::set<Expr>& G_terms, std::set<Expr>& X_terms, std::vector<Expr>& Pred_vec, std::vector<Expr>& sortedOps, std::vector<Expr>& Constrained_vec, std::vector<Expr>& UnConstrained_vec, std::set<Expr>& Constrained_set, std::set<Expr>& UnConstrained_set, B_Term_map& G_term_map, B_Term_map& P_term_map, std::set<Expr>& SeenBefore, std::set<Expr>& ITE_Added);
    void GetPEqs(const Expr& e, B_name_map& name_map, std::set<Expr>& P_constrained_set, std::set<Expr>& Constrained_set, T_generator_map& Constrained_map, std::set<Expr>& SeenBefore);
    
  Expr ConstrainedConstraints(T_generator_map& Constrained_map, B_name_map& name_map, B_Term_map& Creation_map, std::set<Expr>& Constrained_set, std::set<Expr>& UnConstrained_set, std::set<Expr>& P_constrained_set);


 
  void BuildBryantMaps(const Expr& e, T_generator_map& generator_map, B_Term_map& X_generator_map, B_type_map& type_map, std::vector<Expr>& Pred_vec, std::set<Expr>& P_terms, std::set<Expr>& G_terms, B_Term_map& P_term_map, B_Term_map& G_term_map, std::set< Expr >& SeenBefore, std::set<Expr>& ITE_Added);
  int CountSubTerms(const Expr& e, int& counter);
  void GetOrdering(B_Term_map& X_generator_map, B_Term_map& G_term_map, B_Term_map& P_Term_map);
  
  void B_Term_Map_Deleter(B_Term_map& Map);
  void T_generator_Map_Deleter(T_generator_map& Map);


  Theorem dobryant(const Expr& T);

 
  
  T_name_map ANNames(T_ack_map& ack_map, T_type_map& type_map);
  Expr AckConstraints(T_ack_map& ack_map, T_name_map& name_map);
  void GetAckSwap(const Expr& orig, std::vector<Expr>& OldAck, std::vector<Expr>& NewAck, T_name_map& name_map, T_ack_map& ack_map, std::set<Expr>& SeenBefore);
  void BuildMap(const Expr& e, T_ack_map& ack_map, T_type_map& type_map, std::set< Expr >& SeenBefore);
  Theorem doackermann(const Expr& T);
 
   // <UFTeam Junk>



  //! Simplification that avoids stack overflow
  /*! Stack overflow is avoided by traversing the expression to depths that are
    multiples of 5000 until the bottom is reached.  Then, simplification is done
    bottom-up.
   */
  Theorem smartSimplify(const Expr& e, ExprMap<bool>& cache);
  Theorem preprocess(const Expr& e);
  Theorem preprocess(const Theorem& thm);
  //! Push all negations down to the leaves
  Theorem pushNegation(const Expr& e);
  //! Auxiliary recursive function for pushNegation().
  Theorem pushNegationRec(const Expr& e, bool neg);
  //! Its version for transitivity
  Theorem pushNegationRec(const Theorem& e, bool neg);
  //! Push negation one level down.  Takes 'e' which is 'NOT e[0]'
  Theorem pushNegation1(const Expr& e);
  //! Helper for newPP
  Theorem specialSimplify(const Expr& e, ExprHashMap<Theorem>& cache);
  //! new preprocessing code
  Theorem newPP(const Expr& e, int& budget);
  //! main new preprocessing code
  Theorem newPPrec(const Expr& e, int& budget);

private:
  //! Helper for simplifyWithCare
  void updateQueue(ExprMap<std::set<Expr>* >& queue,
                   const Expr& e,
                   const std::set<Expr>& careSet);
  //! Helper for simplifyWithCare
  Theorem substitute(const Expr& e,
                     ExprHashMap<Theorem>& substTable,
                     ExprHashMap<Theorem>& cache);
public:
  //! ITE simplification from Burch paper
  Theorem simplifyWithCare(const Expr& e);

  /*@}*/ // end of preprocessor stuff

};

}

#endif
