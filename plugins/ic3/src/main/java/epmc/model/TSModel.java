package epmc.model;

import java.util.List;

import epmc.constraintsolver.SMTSolver;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;

public interface TSModel {
	
	Expression getInitialStates();
	
	Expression getInvariants();
	
	Expression getTransitions();
	
	List<Expression> getTransitionsDNF();
	
	Expression getErrorProperty();
	
	Expression getPrimed(Expression expression);

	SMTSolver newSolver();
	
	Expression[] getVariables(boolean isPrimed);
	
	Expression getActionVariables();
	
	ContextExpression getContext();
	
	void close();
	
}
