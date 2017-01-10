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

package epmc.time;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.jani.model.ModelJANI;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAnd;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;

/**
 * Utility methods to work with timed automata and related models.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilTime {
	/**
	 * Adds clock as parsable type to a JANI model.
	 * The model parameter must not be {@code null}.
	 * 
	 * @param model model to add clock type to
	 */
	public static void addClockType(ModelJANI model) {
		assert model != null;
		model.getTypes().put(JANITypeClock.IDENTIFIER, JANITypeClock.class);
	}
	
	public static boolean isClockFree(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		Set<Expression> identifiers = UtilExpressionStandard.collectIdentifiers(expression);
		for (Expression identifier : identifiers) {
			if (TypeClock.isClock(identifier.getType(expressionToType))) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isClockUsageValid(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		expression = normalise(expressionToType, expression);
		if (isClockFree(expressionToType, expression)) {
			return true;
		} else if (isValidClockCompare(expressionToType, expression)) {
			return true;
		} else {
			if (!ExpressionOperator.isOperator(expression)) {
				return false;
			}
			if (!isOr(expression) && !isAnd(expression)) {
				return false;
			}
			for (Expression child : expression.getChildren()) {
				if (!isClockUsageValid(expressionToType, child)) {
					return false;
				}
			}
			return true;
		}
	}

	public static boolean isValidClockCompare(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		if (!isCompare(expression)) {
			return false;
		}
		ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
		if (isClock(expressionToType, expressionOperator.getOperand1())
				&& isClock(expressionToType, expressionOperator.getOperand2())) {
			return true;
		} else if (isClock(expressionToType, expressionOperator.getOperand1())
				&& isClockFree(expressionToType, expressionOperator.getOperand2())) {
			return true;
		} else if (isClockFree(expressionToType, expressionOperator.getOperand1())
				&& isClock(expressionToType, expressionOperator.getOperand2())) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isValidClockCompareNonStrict(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		if (!isCompareNonStrict(expression)) {
			return false;
		}
		ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
		if (isClock(expressionToType, expressionOperator.getOperand1())
				&& isClock(expressionToType, expressionOperator.getOperand2())) {
			return true;
		} else if (isClock(expressionToType, expressionOperator.getOperand1())
				&& isClockFree(expressionToType, expressionOperator.getOperand2())) {
			return true;
		} else if (isClockFree(expressionToType, expressionOperator.getOperand1())
				&& isClock(expressionToType, expressionOperator.getOperand2())) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isClockUsageValidForDigitalClocks(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		expression = normalise(expressionToType, expression);
		if (isClockFree(expressionToType, expression)) {
			return true;
		} else if (isValidClockCompareNonStrict(expressionToType, expression)) {
			return true;
		} else {
			if (!ExpressionOperator.isOperator(expression)) {
				return false;
			}
			if (!isOr(expression) && !isAnd(expression)) {
				return false;
			}
			for (Expression child : expression.getChildren()) {
				if (!isClockUsageValidForDigitalClocks(expressionToType, child)) {
					return false;
				}
			}
			return true;
		}
	}
	
	public static boolean isCompare(Expression expression) {
		assert expression != null;
		return isLt(expression) || isLe(expression) || isEq(expression)
				|| isGe(expression) || isGt(expression) || isNe(expression);
	}

	public static boolean isCompareNonStrict(Expression expression) {
		assert expression != null;
		return isLe(expression) || isEq(expression) || isGe(expression);
	}

	public static boolean isClock(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		if (!ExpressionIdentifier.isIdentifier(expression)) {
			return false;
		}
		if (!TypeClock.isClock(expression.getType(expressionToType))) {
			return false;
		}
		return true;
	}

	public static Expression fixTimeProgressForTick(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		ContextValue contextValue = expressionToType.getContextValue();
		expression = normalise(expressionToType, expression);
		ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
		if (UtilTime.isCompareNonStrict(expression)
				&& UtilTime.isClock(expressionToType, expressionOperator.getOperand1())
				&& !UtilTime.isClock(expressionToType, expressionOperator.getOperand2())
				&& isLe(expression)) {
			return new ExpressionOperator.Builder()
					.setOperator(contextValue.getOperator(OperatorLt.IDENTIFIER))
					.setOperands(expressionOperator.getOperand1(), expressionOperator.getOperand2())
					.build();
		} else if (UtilTime.isCompareNonStrict(expression)
				&& !UtilTime.isClock(expressionToType, expressionOperator.getOperand1())
				&& UtilTime.isClock(expressionToType, expressionOperator.getOperand2())
				&& isGe(expression)) {
			return new ExpressionOperator.Builder()
					.setOperator(contextValue.getOperator(OperatorGt.IDENTIFIER))
					.setOperands(expressionOperator.getOperand1(), expressionOperator.getOperand2())
					.build();
		} else {
			List<Expression> newChildren = new ArrayList<>();
			for (Expression child : expression.getChildren()) {
				newChildren.add(fixTimeProgressForTick(expressionToType, child));
			}
			return expression.replaceChildren(newChildren);
		}
	}

	/**
	 * Tries to transform expression in form usable for TA analysis.
	 * 
	 * @param expression expression to be transformed
	 * @return transformed expression
	 * @throws EPMCException thrown in case of problems
	 */
	public static Expression normalise(ExpressionToType expressionToType, Expression expression) throws EPMCException {
		assert expression != null;
		return normalise(expressionToType, expression, false);
	}

	private static Expression normalise(ExpressionToType expressionToType, Expression expression, boolean negated) throws EPMCException {
		assert expression != null;
		ContextValue contextValue = expressionToType.getContextValue();
		if (isClockFree(expressionToType, expression) && !negated) {
			return expression;
		} else if (isClockFree(expressionToType, expression) && negated) {
			return UtilExpressionStandard.opNot(expressionToType.getContextValue(), expression);
		} else if (isNot(expression) && negated) {
			ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
			return normalise(expressionToType, expressionOperator.getOperand1(), false);
		} else if ((isAnd(expression) || isOr(expression)) && !negated) {
			List<Expression> newChildren = new ArrayList<>();
			for (Expression child : expression.getChildren()) {
				newChildren.add(normalise(expressionToType, child));
			}
			return expression.replaceChildren(newChildren);
		} else if (isAnd(expression) && negated) {
			ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
			Expression opLeft = normalise(expressionToType, expressionOperator.getOperand1(), true);
			Expression opRight = normalise(expressionToType, expressionOperator.getOperand2(), true);
			return new ExpressionOperator.Builder()
					.setOperator(contextValue.getOperator(OperatorOr.IDENTIFIER))
					.setOperands(opLeft, opRight)
					.build();
		} else if (isOr(expression) && negated) {
			ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
			Expression opLeft = normalise(expressionToType, expressionOperator.getOperand1(), true);
			Expression opRight = normalise(expressionToType, expressionOperator.getOperand2(), true);
			return new ExpressionOperator.Builder()
					.setOperator(contextValue.getOperator(OperatorAnd.IDENTIFIER))
					.setOperands(opLeft, opRight)
					.build();
		} else if (isValidClockCompare(expressionToType, expression) && !negated) {
			return expression;
		} else if (isValidClockCompare(expressionToType, expression) && negated) {
			ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
			Operator negOp = negateOperand(expressionOperator.getOperator());
			return new ExpressionOperator.Builder()
			        .setOperator(negOp)
			        .setOperands(expressionOperator.getOperand1(),
			        		expressionOperator.getOperand2())
			        .build();
		} else if (!negated) {
			return expression;
		} else if (negated) {
			return UtilExpressionStandard.opNot(expressionToType.getContextValue(), expression);
		} else {
			assert false;
			return null;
		}
	}

	private static Operator negateOperand(Operator operator) {
		assert operator != null;
		ContextValue context = operator.getContext();
		String identifier = operator.getIdentifier();
		if (identifier.equals(OperatorLt.IDENTIFIER)) {
			return context.getOperator(OperatorGe.IDENTIFIER);
		} else if (identifier.equals(OperatorLe.IDENTIFIER)) {
			return context.getOperator(OperatorGt.IDENTIFIER);
		} else if (identifier.equals(OperatorEq.IDENTIFIER)) {
			return context.getOperator(OperatorNe.IDENTIFIER);
		} else if (identifier.equals(OperatorNe.IDENTIFIER)) {
			return context.getOperator(OperatorEq.IDENTIFIER);
		} else if (identifier.equals(OperatorGe.IDENTIFIER)) {
			return context.getOperator(OperatorLt.IDENTIFIER);			
		} else if (identifier.equals(OperatorGt.IDENTIFIER)) {
			return context.getOperator(OperatorLe.IDENTIFIER);
		}
		assert false;
		return null;
	}

    private static boolean isNe(Expression expression) {
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorNe.IDENTIFIER);
    }
    
    private static boolean isNot(Expression expression) {
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorNot.IDENTIFIER);
    }
    
    private static boolean isOr(Expression expression) {
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorOr.IDENTIFIER);
    }

    private static boolean isAnd(Expression expression) {
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorAnd.IDENTIFIER);
    }

    private static boolean isGe(Expression expression) {
        assert expression != null;
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorGe.IDENTIFIER);
    }

    private static boolean isLe(Expression expression) {
        assert expression != null;
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorLe.IDENTIFIER);
    }

    private static boolean isGt(Expression expression) {
        assert expression != null;
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorGt.IDENTIFIER);
    }
    
    private static boolean isLt(Expression expression) {
        assert expression != null;
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorLt.IDENTIFIER);
    }
    
    private static boolean isEq(Expression expression) {
        assert expression != null;
        if (!ExpressionOperator.isOperator(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.asOperator(expression);
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorEq.IDENTIFIER);
    }

	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private UtilTime() {
	}
}
