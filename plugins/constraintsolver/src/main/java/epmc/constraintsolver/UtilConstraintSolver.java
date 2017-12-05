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
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.ExpressionTypeReal;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorMultiply;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

public final class UtilConstraintSolver {
    public static Expression linearToExpression(ConstraintSolver solver,
            ValueArray row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        Expression result = linearToExpression(solver, row, variables);
        Expression rhsExpression = null;
        rhsExpression = valueToExpression(rightHandSide);
        
        Operator operator = constraintTypeToOperator(constraintType);
        result = new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(result, rhsExpression)
                .build();
        return result;
    }

    private static Expression valueToExpression(Value value) {
        if (ValueInteger.is(value)) {
            ValueInteger valueInteger = ValueInteger.as(value);
            return new ExpressionLiteral.Builder()
                    .setValue(Integer.toString(valueInteger.getInt()))
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .build();
        }
        if (ValueDouble.is(value)) {
            ValueDouble valueDouble = ValueDouble.as(value);
            return new ExpressionLiteral.Builder()
                    .setValue(Double.toString(valueDouble.getDouble()))
                    .setType(ExpressionTypeReal.TYPE_REAL)
                    .build();
        }
        if (ValueReal.is(value)) {
            // TODO conversion from value to string this way might
            // loose precision
            ValueReal valueReal = ValueReal.as(value);
            return new ExpressionLiteral.Builder()
                    .setValue(valueReal.toString())
                    .setType(ExpressionTypeReal.TYPE_REAL)
                    .build();
        }
        throw new RuntimeException();
    }

    public static Expression linearToExpression(ConstraintSolver solver,
            Value[] row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        Expression result = linearToExpression(solver, row, variables);
        Expression rhsExpression = valueToExpression(rightHandSide);
        Operator operator = constraintTypeToOperator(constraintType);
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
            Expression factor = valueToExpression(entry);
            Expression term = times(factor, variable);
            if (result == null) {
                result = term;
            } else {
                result = plus(result, term);
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
            Expression factor = valueToExpression(entry);
            Expression term = times(factor, variable);
            if (result == null) {
                result = term;
            } else {
                result = plus(result, term);
            }
        }
        return result;
    }

    public static Operator constraintTypeToOperator(ConstraintType type) {
        assert type != null;
        Operator operator = null;
        switch (type) {
        case EQ:
            operator = OperatorEq.EQ;
            break;
        case GE:
            operator = OperatorGe.GE;
            break;
        case LE:
            operator = OperatorLe.LE;
            break;
        default:
            break;
        }
        return operator;
    }

    public LinearExpression expressionToLinear(ConstraintSolver solver,
            Expression expression) {
        // TODO finish
        if (ExpressionLiteral.is(expression)) {

        }
        return null;
    }

    private static Expression times(Expression a, Expression b) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorMultiply.MULTIPLY)
                .setOperands(a, b)
                .build();
    }

    private static Expression plus(Expression a, Expression b) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorAdd.ADD)
                .setOperands(a, b)
                .build();
    }

    private UtilConstraintSolver() {
    }
}
