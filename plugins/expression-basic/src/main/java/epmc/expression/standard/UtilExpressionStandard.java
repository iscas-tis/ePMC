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

package epmc.expression.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import epmc.expression.Expression;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;

// TODO probably should get rid of most of these methods

public final class UtilExpressionStandard {    
    /* Moritz: op* methods are methods used to construct new expressions from
     * given ones by a given operator. The reason not to use default methods of
     * the Expression class is that
     * - these methods need not and should not be possibly override,
     * - Expression already has too many methods,
     * - having them here allows for more flexibility, as e.g. allowing null
     *   arguments for opAnd might be equivalent to the other parameter.
     */    
    public static Expression opAdd(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorAdd.ADD, op1, op2);
    }

    public static Expression opAdd(Expression op1, int op2) {
        assert op1 != null;
        Expression op2Expr = new ExpressionLiteral.Builder()
                .setValue(Integer.toString(op2))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build();
        return newOperator(OperatorAdd.ADD, op1, op2Expr);
    }

    public static Expression opDivide(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorDivide.DIVIDE, op1, op2);
    }

    public static Expression opAnd(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorAnd.AND, op1, op2);
    }

    public static Expression opOr(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorOr.OR, op1, op2);
    }

    public static Expression opMin(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorMin.MIN, op1, op2);
    }

    public static Expression opMin(int op1, Expression op2) {
        assert op2 != null;
        Expression op1Expr = new ExpressionLiteral.Builder()
                .setValue(Integer.toString(op1))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build();
        return newOperator(OperatorMin.MIN, op1Expr, op2);
    }

    public static Expression opMax(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorMax.MAX, op1, op2);
    }

    public static Expression opEq(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorEq.EQ, op1, op2);
    }

    public static Expression opIte(Expression op1, Expression op2, Expression op3) {
        assert op1 != null;
        assert op2 != null;
        assert op3 != null;
        return newOperator(OperatorIte.ITE, op1, op2, op3);
    }

    public static Expression opIte(Expression op1, Expression op2, int op3) {
        assert op1 != null;
        assert op2 != null;
        Expression op3Expr = new ExpressionLiteral.Builder()
                .setValue(Integer.toString(op3))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build();
        return newOperator(OperatorIte.ITE, op1, op2, op3Expr);
    }

    public static Expression opNot(Expression operand) {
        assert operand != null;
        return newOperator(OperatorNot.NOT, operand);
    }

    public static Expression replace(Expression expression, Map<Expression, Expression> replacement) {
        assert expression != null;
        if (replacement.containsKey(expression)
                && replacement.get(expression) != null) {
            return replacement.get(expression).replacePositional(expression.getPositional());
        }
        ArrayList<Expression> newChildren = new ArrayList<>();
        for (Expression child : expression.getChildren()) {
            newChildren.add(replace(child, replacement));
        }
        return expression.replaceChildren(newChildren).replacePositional(expression.getPositional());
    }

    static Expression newOperator(Operator operator, Expression... operands) {
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(Arrays.asList(operands))
                .build();
    }

    public static Set<Expression> collectIdentifiers(Expression expression) {
        assert expression != null;
        if (expression instanceof ExpressionIdentifier) {
            return Collections.singleton(expression);
        }
        Set<Expression> result = new HashSet<>();
        for (Expression child : expression.getChildren()) {
            result.addAll(collectIdentifiers(child));
        }
        return result;
    }

    /**
     * Return a representation of the expression which is well readable for
     * humans. If possible, this function returns the string the expression
     * was parsed from. The expression parameter must not be {@code null}.
     * 
     * @param expression expression to get human-readable string of
     * @return human-readable representation of expression
     */
    public static String niceForm(Expression expression) {
        assert expression != null;
        if (expression.getPositional() == null
                || expression.getPositional().getContent() == null) {
            return expression.toString();
        } else {
            return expression.getPositional().getContent();
        }
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilExpressionStandard() {
    }
}
