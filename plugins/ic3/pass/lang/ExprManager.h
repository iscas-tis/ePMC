/*! \file Predicate.h
 *  \brief Header file for ExprManager class.
 * \note This class encapsulates the expression manager.
 * @see ExprManager.cpp
 *
 * \author Bjoern Wachter
 * \remarks Copyright (c) 2007 by Saarland University.  All
 * Rights Reserved. This software is for educational purposes only.
 * Permission is given to academic institutions to use, copy, and
 * modify this software and its documentation provided that this
 * introductory message is not removed, that this software and its
 * documentation is used for the institutions' internal research and
 * educational purposes, and that no monies are exchanged. No guarantee
 * is expressed or implied by the distribution of this code. Send
 * bug-reports and/or questions to: bwachter@cs.uni-sb.de.
 */

#ifndef __EXPRMANAGER_H__
#define __EXPRMANAGER_H__

#include "vcl.h"
#include "util/Util.h"
#include "util/Cube.h"

namespace lang {

class Type;
class Value;
class BasicExpr;
class ExtractExpr;


extern CVC3::VCL& vc;

/*! \brief Class that manages all expression and caches results from Decision Procedure calls */
class ExprManager
{
public:

	static void Init();
	static void Done();

	static bool IsTrue(const CVC3::Expr& e) ;

	static bool IsFalse(const CVC3::Expr& e) ;
	/*! \brief Check if UNSAT and return model if SAT */
	static bool IsFalse(const CVC3::Expr& e, const std::vector<CVC3::Expr>& variables, CVC3::ExprHashMap< CVC3::Expr > &model);

	static bool IsFalseWithAssumptions(const std::vector<CVC3::Expr>& v,
					   std::vector<CVC3::Expr>& assumptions,
					   const std::vector<CVC3::Expr>& variables,
					   CVC3::ExprHashMap< CVC3::Expr > &m);

	static bool DisjointWith(const CVC3::Expr& e1, const CVC3::Expr& e2) ;
	static lbool DisjointWithSyntactic(const CVC3::Expr& e1, const CVC3::Expr& e2);

	static bool EquivalentTo(const CVC3::Expr& e1, const CVC3::Expr& e2) ;

	static bool NegationOf(const CVC3::Expr& e1, const CVC3::Expr& e2);

	static bool LvalueCompatible(const CVC3::Expr& e1, const CVC3::Expr& e2) ;
	static void ComputeLvalues(const CVC3::Expr& e, std::set<CVC3::Expr>& result);

	static void getTopLevelConjuncts(const CVC3::Expr& e, std::vector<CVC3::Expr>& result);
	static void getTopLevelConjuncts(const CVC3::Expr& e, std::set<CVC3::Expr>& result);

	static CVC3::Expr Sum(const std::vector<CVC3::Expr>&);
	static CVC3::Expr Conjunction(const std::vector<CVC3::Expr>&);
	static CVC3::Expr Conjunction(const std::set<CVC3::Expr>&);
	static CVC3::Expr Disjunction(const std::vector<CVC3::Expr>&);

	static void CollectExprs(const CVC3::Expr& e, std::hash_set<CVC3::Expr>& exprs);
	static void CollectExprs(const CVC3::Expr& e, std::set<CVC3::Expr>& exprs);

	static void Filter(const std::vector<CVC3::Expr>& vec, const std::hash_set<CVC3::Expr>& exprs, std::vector<CVC3::Expr>& res);

	static void Triage(const std::vector<CVC3::Expr>& vec,
				 const std::hash_set<CVC3::Expr>& exprs,
				 std::vector<CVC3::Expr>& in,
				 std::vector<CVC3::Expr>& out);

	static void SplitExprSet(const std::vector<CVC3::Expr> &arg,std::vector< std::vector<CVC3::Expr> > &res);
	static void SplitExprSet(const std::set<CVC3::Expr> &arg,std::vector< std::set<CVC3::Expr> > &res);
	static void SplitExprSet(const std::vector<CVC3::Expr> &arg,std::vector< std::set<CVC3::Expr> > &res);

	/** \brief decompose an expression into conjuncts with disjoint support */
	static void getDisjointSupportDecomposition(const CVC3::Expr& e,
				     std::vector<CVC3::Expr>& result);

	static CVC3::Expr getExprCube(const Cube& c, const std::vector<CVC3::Expr>& vec);
	static void addExprCube(const Cube& c, const std::vector<CVC3::Expr>& vec, std::vector<CVC3::Expr>& result);


	static bool IsInterval(const CVC3::Expr& e, CVC3::Expr& lower, CVC3::Expr& x, CVC3::Expr& upper);


	static CVC3::Expr getVariableInstance(CVC3::Expr& var, int time);


	static std::string toFOCIString(const CVC3::Expr& e, std::map<std::string,CVC3::Expr>& symbol_table);

	static std::string prettyPrint(const CVC3::Expr& e);

	private:
	static lbool Solve(const CVC3::Expr& e);
	static lbool queryCaches(const CVC3::ExprHashMap<bool>& cache1,
				      const CVC3::ExprHashMap<bool>& cache2,
				      const CVC3::Expr& e);



};

} //namespace lang


#endif //__EXPRMANAGER_H__

/*********************************************************************/
//end of ExprManager.h
/*********************************************************************/
