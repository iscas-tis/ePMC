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

package epmc.coalition;

import static epmc.expression.standard.ExpressionPropositional.isPropositional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionCoalition;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporal;
import epmc.value.Operator;
import epmc.value.ValueAlgebra;
import epmc.value.operator.OperatorEq;
import epmc.value.operator.OperatorGe;
import epmc.value.operator.OperatorGt;
import epmc.value.operator.OperatorLe;
import epmc.value.operator.OperatorLt;
import epmc.value.operator.OperatorNe;

// TODO documentation

/**
 * Auxiliary static methods for the coalition solver plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilCoalition {
    /**
     * Collects subformulas which need to be solved before solving LTL formula.
     * The expression parameter must not be {@code null}.
     * 
     * @param expression Expression to compute subformulae of
     * @return set of subformulae needing to be solved
     */
    public static Set<Expression> collectLTLInner(Expression expression) {
        assert expression != null;
        if (isPropositional(expression)) {
            return Collections.singleton(expression);
        } else if (expression instanceof ExpressionTemporal) {
            ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionTemporal.getOperands()) {
                result.addAll(collectLTLInner(inner));
            }
            return result;
        } else if (ExpressionOperator.isOperator(expression)) {
            ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionOperator.getOperands()) {
                result.addAll(collectLTLInner(inner));
            }
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }

    /**
     * 
     * Note that this function has a different semantics than
     * {@link ExpressionQuantifier#computeQuantifierDirType(Expression)}.
     * 
     * @param expression
     * @return
     */
    public static DirType computeQuantifierDirType(Expression expression) {
        assert expression != null;
        ExpressionQuantifier expressionQuantifier = getQuantifier(expression);
        DirType dirType = expressionQuantifier.getDirType();
        if (dirType == DirType.NONE) {
            switch (getCompareType(expression)) {
            case IS: case EQ: case NE:
                break;
            case GT: case GE:
                dirType = DirType.MAX;
                break;
            case LT: case LE:
                dirType = DirType.MIN;
                break;
            default:
                assert false;
            }
        }

        return dirType;
    }

    public static boolean isDirTypeMin(Expression expression) {
        assert expression != null;
        return computeQuantifierDirType(expression) == DirType.MIN;
    }

    public static boolean isTrivialTrue(ExpressionCoalition property) {
        assert property != null;
        ValueAlgebra compareTo = getValue(property);
        return isQuantGe(property) && compareTo.isZero()
                || isQuantLe(property) && compareTo.isOne();
    }

    public static boolean isTrivialFalse(ExpressionCoalition property) {
        assert property != null;
        ValueAlgebra compareTo = getValue(property);
        return isQuantLt(property) && compareTo.isZero()
                || isQuantGt(property) && compareTo.isOne();
    }

    public static boolean isStrictEven(ExpressionCoalition property) {
        assert property != null;
        return !isQuantGt(property) && !isQuantLt(property);
    }

    public static boolean isQualitative(ExpressionCoalition property) {
        assert property != null;
        ValueAlgebra compareTo = getValue(property);
        return compareTo != null && (compareTo.isZero() || compareTo.isOne());
    }

    private static boolean isQuantLe(Expression expression) {
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.asQuantifier(expression);
        if (expressionQuantifier == null) {
            return false;
        }
        return expressionQuantifier.getCompareType().isLe();
    }

    private static boolean isQuantGe(Expression expression) {
        assert expression != null;
        return getCompareType(expression).isGe();
    }

    private static boolean isQuantGt(Expression expression) {
        assert expression != null;
        return getCompareType(expression).isGt();
    }

    private static boolean isQuantLt(Expression expression) {
        assert expression != null;
        return getCompareType(expression).isLt();
    }

    private static ValueAlgebra getValue(Expression expression) {
        assert expression != null;
        ExpressionLiteral expressionLiteral = getLiteral(expression);
        return ValueAlgebra.asAlgebra(expressionLiteral.getValue());
    }

    public static ExpressionQuantifier getQuantifier(Expression expression) {
        assert expression != null;
        ExpressionCoalition expressionCoalition = ExpressionCoalition.asCoalition(expression);
        if (expressionCoalition != null) {
            return getQuantifier(expressionCoalition.getInner());
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.asQuantifier(expression);
        if (expressionQuantifier != null) {
            return expressionQuantifier;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        if (expressionOperator != null) {
            return getQuantifier(expressionOperator);
        }
        throw new RuntimeException(expression.toString());
    }

    public static ExpressionQuantifier getQuantifier(ExpressionOperator expression) {
        assert expression != null;
        assert expression.getOperands().size() == 2;
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.asQuantifier(expression.getOperand1());
        if (expressionQuantifier == null) {
            expressionQuantifier = ExpressionQuantifier.asQuantifier(expression.getOperand2());
        }
        assert expressionQuantifier != null;
        return expressionQuantifier;
    }

    public static ExpressionLiteral getLiteral(Expression expression) {
        assert expression != null;
        ExpressionCoalition expressionCoalition = ExpressionCoalition.asCoalition(expression);
        if (expressionCoalition != null) {
            return getLiteral(expressionCoalition.getInner());
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.asQuantifier(expression);
        if (expressionQuantifier != null) {
            return ExpressionLiteral.asLiteral(expressionQuantifier.getCompare());
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        if (expressionOperator != null) {
            return getLiteral(expressionOperator);
        }
        throw new RuntimeException(expression.toString());
    }

    public static ExpressionLiteral getLiteral(ExpressionOperator expression) {
        assert expression != null;
        assert expression.getOperands().size() == 2;
        ExpressionLiteral expressionLiteral = ExpressionLiteral.asLiteral(expression.getOperand1());
        if (expressionLiteral == null) {
            expressionLiteral = ExpressionLiteral.asLiteral(expression.getOperand2());
        }
        assert expressionLiteral != null;
        return expressionLiteral;
    }

    public static CmpType getCompareType(Expression expression) {
        assert expression != null;
        ExpressionCoalition expressionCoalition = ExpressionCoalition.asCoalition(expression);
        if (expressionCoalition != null) {
            return getCompareType(expressionCoalition.getInner());
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.asQuantifier(expression);
        if (expressionQuantifier != null) {
            return expressionQuantifier.getCompareType();
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        if (expressionOperator != null) {
            return getCompareType(expressionOperator);
        }
        throw new RuntimeException(expression.toString());
    }

    public static CmpType getCompareType(ExpressionOperator expression) {
        assert expression != null;
        boolean invert = ExpressionQuantifier.isQuantifier(expression.getOperand2());
        Operator operator = expression.getOperator();
        if (operator.equals(OperatorEq.EQ)) {
            return CmpType.EQ;
        } else if (operator.equals(OperatorNe.NE)) {
            return CmpType.NE;
        } else if (operator.equals(OperatorLt.LT)) {
            return invert ? CmpType.GT : CmpType.LT;
        } else if (operator.equals(OperatorGt.GT)) {
            return invert ? CmpType.LT : CmpType.GT;
        } else if (operator.equals(OperatorLe.LE)) {
            return invert ? CmpType.GE : CmpType.LE;
        } else if (operator.equals(OperatorGe.GE)) {
            return invert ? CmpType.LE : CmpType.GE;
        }
        throw new RuntimeException(expression.toString());
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilCoalition() {
    }
}
