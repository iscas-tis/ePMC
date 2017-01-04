#ifndef TRANSITION_CONSTRAINT_H
#define TRANSITION_CONSTRAINT_H

namespace pred {

class TransitionConstraint {
public:

	typedef std::vector<CVC3::ExprHashMap<CVC3::Expr> > VariableInstantiation;

	TransitionConstraint(lang::Model& __model);

	void createVariableInstances(int depth);
	CVC3::Expr getInstance(const CVC3::Expr& e, int time);
	void getTransitionConstraint(const lang::Alternative& ass, int time, std::vector<CVC3::Expr>& result);
	void getTransitionConstraint(const lang::Command& c, const std::vector<int>& prob_choices, int time, std::vector<CVC3::Expr>& result);


	const CVC3::ExprHashMap<CVC3::Expr>& getOriginalFromInstance() const { return replace; }
	const VariableInstantiation& getVariableIntantiation()    const { return variable_instantiation_ssa; }
private:

	lang::Model& model;
	/*
		1. to analyse counterexamples
		we compute an SSA (single-static assignment) form
		of counterexample paths

		2. this can be converted into a constraint
		   and passed on to decision procedures for further
		   analysis:
			satisfiability => existence of counterexample
			interpolation  => discovery of new predicates
	*/
	//! for each time frame: mapping from variable to its instantiation at that time frame
	std::vector<CVC3::ExprHashMap<CVC3::Expr> > variable_instantiation;
	/*! for each time frame: mapping from variable to instantiation it was most recently defined at
		to do the following compaction:
		x1 = x0 & x2 = x1 & x3= x2 & x3>0 & x4=x3+1 compacted to x1>0 & x4=x1+1

		variable_instantiation_ssa[0][x] = x1
		variable_instantiation_ssa[1][x] = x1
		variable_instantiation_ssa[2][x] = x1
		variable_instantiation_ssa[3][x] = x1
		variable_instantiation_ssa[4][x] = x4
	*/
	VariableInstantiation variable_instantiation_ssa;

	CVC3::ExprHashMap<CVC3::Expr> replace;

	CVC3::Expr assignToVar(const CVC3::Expr& lhs, const CVC3::Expr& rhs, int time);
	void updateSSA(const CVC3::Expr& lhs, int time, bool change);

};

}

#endif





