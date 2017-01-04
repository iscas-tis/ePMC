#include "util/Util.h"
#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Cube.h"
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"

#include "TransitionConstraint.h"

namespace pred {

TransitionConstraint::TransitionConstraint(lang::Model& __model) : model(__model) {
	variable_instantiation_ssa.resize(1);
	for(HashMap<std::string,CVC3::Expr>::iterator i= model.variables.begin(); i!= model.variables.end();++i) {
		CVC3::Expr lhs = i->second;
		variable_instantiation_ssa[0][lhs] = lang::ExprManager::getVariableInstance(lhs,0);
	}
}



CVC3::Expr TransitionConstraint::getInstance(const CVC3::Expr& e, int time) {
	assert(time < (int)variable_instantiation.size());
	CVC3::ExprHashMap<CVC3::Expr>& instance_Xi =  variable_instantiation_ssa[time];
	return e.substExpr(instance_Xi);
}

// instance the program variables
void TransitionConstraint::createVariableInstances(int depth) {

	variable_instantiation_ssa.resize(1);
	variable_instantiation_ssa.resize(depth);

	for(int time=variable_instantiation.size();time<depth;++time) {
		CVC3::ExprHashMap<CVC3::Expr> current;

		// instantiate each variable
		for(lang::Variables::const_iterator i= model.variables.begin();
							     i!= model.variables.end();++i) {
			CVC3::Expr variable = i->second;
			CVC3::Expr instance = lang::ExprManager::getVariableInstance(variable,time);
			current [variable]  = instance;
			replace[instance] = variable;

		}
		variable_instantiation.push_back(current);

	}
}


void TransitionConstraint::updateSSA(const CVC3::Expr& lhs, int time, bool change) {
	variable_instantiation_ssa[time+1][lhs] = change ? variable_instantiation[time+1][lhs] : variable_instantiation_ssa[time][lhs];
}



CVC3::Expr TransitionConstraint::assignToVar(const CVC3::Expr& lhs, 
						    const CVC3::Expr& rhs,
						    int time) {

	CVC3::Expr lhs_iplus1 = variable_instantiation_ssa[time+1][lhs];
	CVC3::Expr rhs_i = rhs.substExpr(variable_instantiation_ssa[time]);
	return  lhs.getType().isBool() ? lang::vc.iffExpr(lhs_iplus1,rhs_i) : lang::vc.eqExpr(lhs_iplus1,rhs_i);
}


void TransitionConstraint::getTransitionConstraint(const lang::Command& c, const std::vector<int>& prob_choices, int time, std::vector<CVC3::Expr>& result) {
	assert(time < (int)variable_instantiation.size());

	lang::Alternative::Map base;
	std::vector<lang::Alternative::Map> alt;
	c.factorize(prob_choices,base,alt);
	const std::set<CVC3::Expr>& support(c.getSupport());

	CVC3::ExprHashMap<CVC3::Expr>& instance_Xiplus1 =  variable_instantiation[time+1];

	// instantiate each variable
	for(HashMap<std::string,CVC3::Expr>::iterator i= model.variables.begin();
								 i!= model.variables.end();++i) {
		CVC3::Expr lhs = i->second;
		if(support.count(lhs)==0) {
			updateSSA(lhs,time,false);
		} else if(base.count(lhs)>0) {
			lang::Alternative::Map::iterator mit = base.find(lhs);
			CVC3::Expr rhs = (mit!=base.end()) ? mit->second : lhs;
			updateSSA(lhs,time,lhs != rhs);

			CVC3::Expr equality = assignToVar(lhs, rhs, time);
			if ( lhs != rhs )
				result.push_back(equality);
		} else updateSSA(lhs,time,true);
	}

	std::vector<CVC3::Expr> disjunction;
	foreach(lang::Alternative::Map& m,alt) {
		std::vector<CVC3::Expr> intermediate;
	
		for(lang::Alternative::Map::iterator i= m.begin();
  						     i!= m.end();++i) {
			CVC3::Expr lhs = i->first;
			if(support.count(lhs)==0 || base.count(lhs)>0) continue;



			lang::Alternative::Map::iterator mit = m.find(lhs);
			CVC3::Expr rhs = (mit!=base.end()) ? mit->second : lhs;
			CVC3::Expr lhs_iplus1 = variable_instantiation_ssa[time+1][lhs] = instance_Xiplus1[lhs];

			CVC3::Expr equality = assignToVar(lhs, rhs, time);
			intermediate.push_back(equality);
			
		}
		if(intermediate.size() > 0)
		disjunction.push_back(lang::vc.andExpr(intermediate));
	}
	if(disjunction.size() > 0)
	result.push_back(lang::vc.orExpr(disjunction));


	// add an invariant
	const std::vector<CVC3::Expr>& invar = model.getInvar();
	std::vector<CVC3::Expr> instantiated_invariants;
	for(size_t i = 0; i<invar.size(); ++i) {
		CVC3::Expr new_invar = invar[i].substExpr(variable_instantiation_ssa[time]);

		bool add = time == 0 || new_invar != invar[i].substExpr(variable_instantiation_ssa[time-1]);
		if(add)
			result.push_back(new_invar);
	}
}


// generate transition constraint for an assignment at a time frame
// - get R(X_i,X_{i+1})
void TransitionConstraint::getTransitionConstraint(const lang::Alternative& ass, int time, std::vector<CVC3::Expr>& result) {
	assert(time + 1 < (int)variable_instantiation.size());
	const CVC3::ExprHashMap<CVC3::Expr>& ass_map = ass.getMap() ;
	// instantiate each variable
	for(HashMap<std::string,CVC3::Expr>::iterator i= model.variables.begin();
							     i!= model.variables.end();++i) {
		CVC3::Expr lhs = i->second;
		CVC3::ExprHashMap<CVC3::Expr>::const_iterator ass_it = ass_map.find(lhs);
		CVC3::Expr rhs = (ass_it!=ass_map.end()) ? ass_it->second : lhs;
		if(lhs==rhs) {
			updateSSA(lhs,time,false);
		}
		else {
			updateSSA(lhs,time,true);
			CVC3::Expr equality = assignToVar(lhs, rhs, time);
			result.push_back(equality);
		}
	}
	// add an invariant
	const std::vector<CVC3::Expr>& invar = model.getInvar();
	std::vector<CVC3::Expr> instantiated_invariants;

	for(size_t i = 0; i<invar.size(); ++i) {
		CVC3::Expr new_invar = invar[i].substExpr(variable_instantiation_ssa[time]);

		bool add = time == 0 || new_invar != invar[i].substExpr(variable_instantiation_ssa[time-1]);
		if(add)
			result.push_back(new_invar);
	}
}

}

