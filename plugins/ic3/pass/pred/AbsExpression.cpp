/*
 * AbsExpression.cpp
 *
 *  Created on: Sep 7, 2009
 *      Author: bwachter
 */
#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"
#include "util/Cube.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"

#include "dp/SMT.h"
#include "dp/YicesSMT.h"
#include "EncodingManager.h"

#include "Decomposition.h"
#include "AbsExpression.h"

using namespace lang;
using namespace bdd;
using namespace dp;

#ifndef ABSEXPRESSION_CPP_
#define ABSEXPRESSION_CPP_

namespace pred {

struct BDDCubeConsumer : CubeConsumer {
	bdd::DDManager& dd_mgr;
	bdd::BDD& f;
	const std::vector<bdd::BDD>& variables;
public:
	BDDCubeConsumer( bdd::DDManager& __dd_mgr,
			      bdd::BDD& __f,
			      const std::vector<bdd::BDD>& __variables)
		: dd_mgr(__dd_mgr), f ( __f), variables (__variables)
	{}
	virtual void consume(const Cube& c) {
		bdd::BDD cube(dd_mgr,c,variables);
		f |= cube;
	}
};

//compute the overapproximation of an expression
//result: a set of cubes stored in a BDD
bdd::BDD AbsExpression::abstractCover(const CVC3::Expr& e,
				const EncodingManager& em,
				const PredSet& predset,
			    const bdd::BDD& care_set,
			    const std::vector<CVC3::Expr>& invar) {
	MSG(1,"AbsExpression::Abstract("+ e.toString() + "," + predset.toString()+ ")\n");

	if(ExprManager::IsTrue(e)) {
		return em.getDDManager().True();
	}

	std::vector<CVC3::Expr> exprs(predset.size());
	std::vector<bdd::BDD> variables(predset.size());

	for(unsigned i = 0; i<predset.size();++i) {
		const Predicate& pred = predset[i];
		assert(em.existStateVar(pred));
		const StateVar& state_var(em.getStateVar(pred));
		exprs[i] = pred.getExpr();
		variables[i] = state_var.getBDD();
	}


	util::Timer t;
	t.Start();
	bdd::BDD result = em.getDDManager().False();
	YicesSMT smt;
	smt.pushContext();
	smt.Assert(e);
	smt.Assert(invar);

	if(exprs.empty()) {
		return em.getDDManager().True();
	}

	/* compute support */
	bdd::BDD support(em.getDDManager().True());
	foreach(const bdd::BDD& var, variables) {
		support &= var;
	}

	/* simplify the care_set with respect to support */
	bdd::BDD quant(care_set.getSupport().Simplify(support));
	bdd::BDD simplified_care_set(care_set.Exist(quant));
	CVC3::Expr condition (em.getExpr2(simplified_care_set));

	smt.Assert(condition);

	BDDCubeConsumer cube_consumer(em.getDDManager(),result,variables);

	smt.allSat(exprs,cube_consumer,100000);

	smt.popContext();
	t.Stop();
	MSG(1,"AbsExpression::Abstract("+ e.toString() + ",preds) (# minterms " + util::floatToString(result.CountMinterm(exprs.size())) + "# iters "+ util::floatToString(t.Read()*1000) +"ms )\n");
	return result;
}

/*********************************************************************/
//return the predicates
//a guard is different from an ordinary expression
//since we know that the boolean expressions appearing
//in it are predicates
/*********************************************************************/
bdd::BDD AbsExpression::abstractCoverDecomp(const CVC3::Expr& expr,
					 const EncodingManager& em,
				     const PredSet& e_predset,
				     const bdd::BDD& care_set,
				     const std::vector<CVC3::Expr>& invar) {
       bdd::BDD result = em.getDDManager().True();
       CVC3::Expr e = expr;

       if(e == vc.trueExpr()) {
               return em.getDDManager().True();
       } else if(e == vc.falseExpr()) {
               return em.getDDManager().False();
       }

       std::vector<Decomposition> decomp;
       std::vector<const PredSet*> predset_vec;
       predset_vec.push_back(&e_predset);
       CVC3::Expr r;

       lang::Command c;

       c.setGuard(e);

       Decomposition::computeDecompositions(
   			invar,
   			c,
   			predset_vec,
   			decomp,
   			r);

       if(decomp.size()==0) {
    	   result = em.getDDManager().True();

       } else {
    		for(unsigned f=0;f<decomp.size();++f) {
    			Decomposition& cluster = decomp[f];
    			result &= abstractCover(cluster.guard,em,cluster.rel,care_set,cluster.invar);
            }

       }
       MSG(1,"AbsExpression::abstractCoverDecomp("+ e.toString() + ",preds) (# minterms " + util::floatToString(result.CountMinterm(e_predset.size())) + "#\n");
       return result;
}



}


#endif /* ABSEXPRESSION_CPP_ */
