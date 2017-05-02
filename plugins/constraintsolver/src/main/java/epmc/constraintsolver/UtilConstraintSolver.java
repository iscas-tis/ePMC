/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

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
		ContextValue contextValue = ContextValue.get();
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
		ContextValue contextValue = ContextValue.get();
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
			Expression term = times(ContextValue.get(), factor, variable);
			if (result == null) {
				result = term;
			} else {
				result = plus(ContextValue.get(), result, term);
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
			Expression term = times(ContextValue.get(), factor, variable);
			if (result == null) {
				result = term;
			} else {
				result = plus(ContextValue.get(), result, term);
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
