package epmc.constraintsolver;

import epmc.expression.Expression;
import epmc.value.Type;
import epmc.value.Value;

public interface SMTSolver {
	
	
	void push();
	void pop();
	Value getModelValue(Expression expression);
	Value getModelValue(int varIdx);
	int addVariable(String name, Type type);           /* return variable index */
	int addVariable(Expression expression, Type type);
	void addConstraint(Value[] row, int[] variables,
			ConstraintType constraintType, Value rightHandSide);
	void addConstraint(Value row, int[] variables,
			ConstraintType constraintType, Value rightHandSide);
	
	void addConstraint(Expression expression);
	void setAssumption(Expression expression);
	
	/* can apply multiple objective, return objective index */
	int setObjective(Expression objective, Direction direction);       
	int setObjective(Value row, int[] variables, Direction direction); 
	int setObjective(Value[] row, int[] variables, Direction direction);
	
	ConstraintSolverResult solveByAssumption(Expression... expressions);
	ConstraintSolverResult solve();
	ConstraintSolverResult solve(Expression... expressions);
	/* get objective value */
	Value getObjectiveValue(int index);
	
	void addExists(Expression[] vars, Expression body);
	
	void close();


}
