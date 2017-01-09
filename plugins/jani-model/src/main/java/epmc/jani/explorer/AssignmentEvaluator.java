package epmc.jani.explorer;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.jani.model.Assignment;
import epmc.jani.model.Variable;

public interface AssignmentEvaluator {
	interface Builder {
		Builder setAssignment(Assignment assignment);
		
		Builder setVariableMap(Map<Variable, Integer> variableMap);
		
		Builder setVariables(Expression[] variables);

		Builder setAutVarToLocal(Map<Expression, Expression> autVarToLocal);

		Builder setExpressionToType(ExpressionToType expressionToType);
		
		boolean canHandle();
		
		AssignmentEvaluator build() throws EPMCException;
	}

	void apply(NodeJANI node, NodeJANI successor) throws EPMCException;
}
