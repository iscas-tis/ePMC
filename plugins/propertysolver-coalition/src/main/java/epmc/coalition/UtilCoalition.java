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
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionCoalition;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporal;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

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
		} else if (expression instanceof ExpressionOperator) {
			ExpressionOperator expressionOperator = (ExpressionOperator) expression;
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
        assert expression instanceof ExpressionQuantifier : expression;
        ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
        DirType dirType = expressionQuantifier.getDirType();
        if (dirType == DirType.NONE) {
            switch (expressionQuantifier.getCompareType()) {
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
	    ExpressionQuantifier quant = property.getQuantifier();
	    Expression compareToExpr = quant.getCompare();
	    assert ExpressionLiteral.isLiteral(compareToExpr);
	    ValueAlgebra compareTo = ValueAlgebra.asAlgebra(getValue(compareToExpr));
	    return isQuantGe(quant) && compareTo.isZero()
	    		|| isQuantLe(quant) && compareTo.isOne();
    }

    public static boolean isTrivialFalse(ExpressionCoalition property) {
    	assert property != null;
	    ExpressionQuantifier quant = property.getQuantifier();
	    Expression compareToExpr = quant.getCompare();
	    assert ExpressionLiteral.isLiteral(compareToExpr);
	    ValueAlgebra compareTo = ValueAlgebra.asAlgebra(getValue(compareToExpr));
	    return isQuantLt(quant) && compareTo.isZero()
	    		|| isQuantGt(quant) && compareTo.isOne();
    }

    public static boolean isStrictEven(ExpressionCoalition property) {
    	assert property != null;
	    ExpressionQuantifier quant = property.getQuantifier();
	    return !isQuantGt(quant) && !isQuantLt(quant);
    }

    public static boolean isQualitative(ExpressionCoalition property) {
    	assert property != null;
	    ExpressionQuantifier quant = property.getQuantifier();
	    Expression compareToExpr = quant.getCompare();
	    assert ExpressionLiteral.isLiteral(compareToExpr);
	    ValueAlgebra compareTo = ValueAlgebra.asAlgebra(getValue(compareToExpr));
	    return compareTo.isZero() || compareTo.isOne();    	
    }
    
    private static boolean isQuantLe(Expression expression) {
    	if (!(expression instanceof ExpressionQuantifier)) {
    		return false;
    	}
    	ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
    	return expressionQuantifier.getCompareType().isLe();
    }

    private static boolean isQuantGe(Expression expression) {
    	if (!(expression instanceof ExpressionQuantifier)) {
    		return false;
    	}
    	ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
    	return expressionQuantifier.getCompareType().isGe();
    }

    private static boolean isQuantGt(Expression expression) {
    	if (!(expression instanceof ExpressionQuantifier)) {
    		return false;
    	}
    	ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
    	return expressionQuantifier.getCompareType().isGt();
    }
    
    private static boolean isQuantLt(Expression expression) {
    	if (!(expression instanceof ExpressionQuantifier)) {
    		return false;
    	}
    	ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
    	return expressionQuantifier.getCompareType().isLt();
    }

    private static Value getValue(Expression expression) {
        assert expression != null;
        assert ExpressionLiteral.isLiteral(expression);
        ExpressionLiteral expressionLiteral = ExpressionLiteral.asLiteral(expression);
        return expressionLiteral.getValue();
    }

	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private UtilCoalition() {
	}
}
