package epmc.constraintsolver;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorLe;
import epmc.value.OperatorMultiply;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class UtilConstraintSolver {
	public static Expression linearToExpression(ConstraintSolver solver,
			ValueArray row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
		Expression result = linearToExpression(solver, row, variables);
		Expression rhsExpression = new ExpressionLiteral.Builder()
				.setValue(rightHandSide)
				.build();
		ContextValue contextValue = solver.getContextValue();
		Operator operator = constraintTypeToOperator(contextValue, constraintType);
		result = new ExpressionOperator.Builder()
		        .setOperator(operator)
		        .setOperands(result, rhsExpression)
		        .build();
		return result;
	}

	public static Expression linearToExpression(ConstraintSolver solver,
			Value[] row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
		ContextValue contextValue = solver.getContextValue();
		Expression result = linearToExpression(solver, row, variables);
		Expression rhsExpression = new ExpressionLiteral.Builder()
				.setValue(rightHandSide)
				.build();
		Operator operator = constraintTypeToOperator(contextValue, constraintType);
		result = new ExpressionOperator.Builder()
		        	.setOperator(operator)
		        	.setOperands(result, rhsExpression)
		        	.build();
		return result;
	}

	public static Expression linearToExpression(ConstraintSolver solver,
			ValueArray row, int[] variables) {
		Expression result = null;
		Value entry = row.getType().getEntryType().newValue();
		for (int index = 0; index < variables.length; index++) {
			row.get(entry, index);
			String name = solver.getVariableName(variables[index]);
			Expression variable = new ExpressionIdentifierStandard.Builder()
					.setName(name).build();
			Expression factor = new ExpressionLiteral.Builder()
					.setValue(entry)
					.build();
			Expression term = times(solver.getContextValue(), factor, variable);
			if (result == null) {
				result = term;
			} else {
				result = plus(solver.getContextValue(), result, term);
			}
		}
		return result;
	}
	
	public static Expression linearToExpression(ConstraintSolver solver,
			Value[] row, int[] variables) {
		Expression result = null;
		for (int index = 0; index < variables.length; index++) {
			Value entry = row[index];
			String name = solver.getVariableName(variables[index]);
			Expression variable = new ExpressionIdentifierStandard.Builder()
					.setName(name).build();
			Expression factor = new ExpressionLiteral.Builder()
					.setValue(entry)
					.build();
			Expression term = times(solver.getContextValue(), factor, variable);
			if (result == null) {
				result = term;
			} else {
				result = plus(solver.getContextValue(), result, term);
			}
		}
		return result;
	}
	
	public static Operator constraintTypeToOperator(ContextValue contextValue,
			ConstraintType type) {
		assert type != null;
		Operator operator = null;
		switch (type) {
		case EQ:
			operator = contextValue.getOperator(OperatorEq.IDENTIFIER);
			break;
		case GE:
			operator = contextValue.getOperator(OperatorGe.IDENTIFIER);
			break;
		case LE:
			operator = contextValue.getOperator(OperatorLe.IDENTIFIER);
			break;
		default:
			break;
		}
		return operator;
	}
	
	public LinearExpression expressionToLinear(ConstraintSolver solver,
			Expression expression) {
		// TODO finish
		if (ExpressionLiteral.isLiteral(expression)) {
			
		}
		return null;
	}

    private static Expression times(ContextValue contextValue, Expression a, Expression b) {
        return new ExpressionOperator.Builder()
                .setOperator(contextValue.getOperator(OperatorMultiply.IDENTIFIER))
                .setOperands(a, b)
                .build();
    }

    private static Expression plus(ContextValue contextValue, Expression a, Expression b) {
    	return new ExpressionOperator.Builder()
        	.setOperator(contextValue.getOperator(OperatorAdd.IDENTIFIER))
        	.setOperands(a, b)
        	.build();
    }
    
	private UtilConstraintSolver() {
	}
}
