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

import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorAnd;
import epmc.value.OperatorDivide;
import epmc.value.OperatorEq;
import epmc.value.OperatorIte;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.expression.Expression;

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
        return newOperator(OperatorAdd.IDENTIFIER, op1, op2);
    }

    public static Expression opAdd(Expression op1, int op2) {
        assert op1 != null;
        TypeInteger typeInteger = TypeInteger.get();
        
        Expression op2Expr = new ExpressionLiteral.Builder()
        		.setValueProvider(() -> UtilValue.newValue(typeInteger, op2))
        		.build();
        return newOperator(OperatorAdd.IDENTIFIER, op1, op2Expr);
    }

    public static Expression opAddInverse(Expression operand) {
        assert operand != null;
        return newOperator(OperatorAddInverse.IDENTIFIER, operand);
    }

    public static Expression opDivide(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorDivide.IDENTIFIER, op1, op2);
    }

    public static Expression opAnd(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorAnd.IDENTIFIER, op1, op2);
    }

    public static Expression opOr(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorOr.IDENTIFIER, op1, op2);
    }

    public static Expression opMin(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorMin.IDENTIFIER, op1, op2);
    }

    public static Expression opMin(int op1, Expression op2) {
        assert op2 != null;
        TypeInteger typeInteger = TypeInteger.get();
        Expression op1Expr = new ExpressionLiteral.Builder()
                .setValueProvider(() -> UtilValue.newValue(typeInteger, op1))
                .build();
        return newOperator(OperatorMin.IDENTIFIER, op1Expr, op2);
    }

    public static Expression opMax(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorMax.IDENTIFIER, op1, op2);
    }

    public static Expression opEq(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorEq.IDENTIFIER, op1, op2);
    }

    public static Expression opIte(Expression op1, Expression op2, Expression op3) {
        assert op1 != null;
        assert op2 != null;
        assert op3 != null;
        return newOperator(OperatorIte.IDENTIFIER, op1, op2, op3);
    }

    public static Expression opIte(Expression op1, Expression op2, int op3) {
        assert op1 != null;
        assert op2 != null;
        TypeInteger typeInteger = TypeInteger.get();
        Expression op3Expr = new ExpressionLiteral.Builder()
        		.setValueProvider(() -> UtilValue.newValue(typeInteger, op3))
        		.build();
        return newOperator(OperatorIte.IDENTIFIER, op1, op2, op3Expr);
    }

    public static Expression opNot(Expression operand) {
        assert operand != null;
        return newOperator(OperatorNot.IDENTIFIER, operand);
    }

    public static Expression replace(Expression expression, Map<Expression, Expression> replacement) {
        assert expression != null;
        if (replacement.containsKey(expression)) {
            return replacement.get(expression);
        }
        ArrayList<Expression> newChildren = new ArrayList<>();
        for (Expression child : expression.getChildren()) {
            newChildren.add(replace(child, replacement));
        }
        return expression.replaceChildren(newChildren);
    }

    static Expression newOperator(String operator, Expression... operands) {
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
     * Private constructor to prevent instantiation of this class.
     */
    private UtilExpressionStandard() {
    }
}
