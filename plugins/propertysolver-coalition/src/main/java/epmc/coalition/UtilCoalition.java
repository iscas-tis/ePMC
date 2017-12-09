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

import static epmc.expression.standard.ExpressionPropositional.is;

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
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.operator.Operator;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorNe;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;

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
        if (is(expression)) {
            return Collections.singleton(expression);
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionOperator.is(expression)) {
            ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
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
        ValueBoolean cmpOne = TypeBoolean.get().newValue();
        ValueBoolean cmpZero = TypeBoolean.get().newValue();
        OperatorEvaluator isOne = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE, compareTo.getType());
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, compareTo.getType());
        isOne.apply(cmpOne, compareTo);
        isZero.apply(cmpZero, compareTo);
        return isQuantGe(property) && cmpZero.getBoolean()
                || isQuantLe(property) && cmpOne.getBoolean();
    }

    public static boolean isTrivialFalse(ExpressionCoalition property) {
        assert property != null;
        ValueAlgebra compareTo = getValue(property);
        ValueBoolean cmpOne = TypeBoolean.get().newValue();
        ValueBoolean cmpZero = TypeBoolean.get().newValue();
        OperatorEvaluator isOne = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE, compareTo.getType());
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, compareTo.getType());
        isOne.apply(cmpOne, compareTo);
        isZero.apply(cmpZero, compareTo);
        return isQuantLt(property) && cmpZero.getBoolean()
                || isQuantGt(property) && cmpOne.getBoolean();
    }

    public static boolean isStrictEven(ExpressionCoalition property) {
        assert property != null;
        return !isQuantGt(property) && !isQuantLt(property);
    }

    public static boolean isQualitative(ExpressionCoalition property) {
        assert property != null;
        ValueAlgebra compareTo = getValue(property);
        ValueBoolean cmpOne = TypeBoolean.get().newValue();
        ValueBoolean cmpZero = TypeBoolean.get().newValue();
        OperatorEvaluator isOne = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE, compareTo.getType());
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, compareTo.getType());
        isOne.apply(cmpOne, compareTo);
        isZero.apply(cmpZero, compareTo);
        return compareTo != null && (cmpZero.getBoolean() || cmpOne.getBoolean());
    }

    private static boolean isQuantLe(Expression expression) {
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
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
        return ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(expressionLiteral));
    }

    public static ExpressionQuantifier getQuantifier(Expression expression) {
        assert expression != null;
        ExpressionCoalition expressionCoalition = ExpressionCoalition.as(expression);
        if (expressionCoalition != null) {
            return getQuantifier(expressionCoalition.getInner());
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        if (expressionQuantifier != null) {
            return expressionQuantifier;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        if (expressionOperator != null) {
            return getQuantifier(expressionOperator);
        }
        throw new RuntimeException(expression.toString());
    }

    public static ExpressionQuantifier getQuantifier(ExpressionOperator expression) {
        assert expression != null;
        assert expression.getOperands().size() == 2;
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression.getOperand1());
        if (expressionQuantifier == null) {
            expressionQuantifier = ExpressionQuantifier.as(expression.getOperand2());
        }
        assert expressionQuantifier != null;
        return expressionQuantifier;
    }

    public static ExpressionLiteral getLiteral(Expression expression) {
        assert expression != null;
        ExpressionCoalition expressionCoalition = ExpressionCoalition.as(expression);
        if (expressionCoalition != null) {
            return getLiteral(expressionCoalition.getInner());
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        if (expressionQuantifier != null) {
            return ExpressionLiteral.as(expressionQuantifier.getCompare());
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        if (expressionOperator != null) {
            return getLiteral(expressionOperator);
        }
        throw new RuntimeException(expression.toString());
    }

    public static ExpressionLiteral getLiteral(ExpressionOperator expression) {
        assert expression != null;
        assert expression.getOperands().size() == 2;
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression.getOperand1());
        if (expressionLiteral == null) {
            expressionLiteral = ExpressionLiteral.as(expression.getOperand2());
        }
        assert expressionLiteral != null;
        return expressionLiteral;
    }

    public static CmpType getCompareType(Expression expression) {
        assert expression != null;
        ExpressionCoalition expressionCoalition = ExpressionCoalition.as(expression);
        if (expressionCoalition != null) {
            return getCompareType(expressionCoalition.getInner());
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        if (expressionQuantifier != null) {
            return expressionQuantifier.getCompareType();
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        if (expressionOperator != null) {
            return getCompareType(expressionOperator);
        }
        throw new RuntimeException(expression.toString());
    }

    public static CmpType getCompareType(ExpressionOperator expression) {
        assert expression != null;
        boolean invert = ExpressionQuantifier.is(expression.getOperand2());
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
