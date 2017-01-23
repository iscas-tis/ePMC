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

package epmc.multiobjective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionMultiObjective;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorNot;
import epmc.value.OperatorSubtract;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;

/**
 * Class to compute normalised form of multi-objective property.
 * The normalised form is described in TODO cite.
 * In addition to the normalised property, auxiliary data structures required
 * to map back the results to results for the original properties are computed.
 * TODO a few more words
 * 
 * @author Ernst Moritz Hahn
 */
final class PropertyNormaliser {
	private ExpressionMultiObjective property;
    private ValueAlgebra subtractNumericalFrom;
    private BitSet invertedRewards;
	private ExpressionMultiObjective normalisedProperty;
	private ExpressionToType expressionToType;

	PropertyNormaliser() {
	}
	
	PropertyNormaliser setExpressionToType(ExpressionToType expressionToType) {
		this.expressionToType = expressionToType;
		return this;
	}
	
	PropertyNormaliser setOriginalProperty(ExpressionMultiObjective property) {
		this.property = property;
		return this;
	}
	
	PropertyNormaliser build() throws EPMCException {
        subtractNumericalFrom = newValueWeight();
        invertedRewards = UtilBitSet.newBitSetUnbounded();
        assert property != null;
        assert subtractNumericalFrom != null;
        ContextValue contextValue = subtractNumericalFrom.getType().getContext();
        assert subtractNumericalFrom.getType().getContext() == contextValue;
        assert subtractNumericalFrom.getType().canImport(TypeWeight.get(contextValue));
        subtractNumericalFrom.set(TypeWeight.asWeight(subtractNumericalFrom.getType()).getPosInf());
        List<Expression> newQuantifiersQuantitative = new ArrayList<>();
        List<Expression> newQuantifiersQualitative = new ArrayList<>();
        Set<Expression> invert = new HashSet<>();
        for (Expression objective : property.getOperands()) {
        	ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            assert !isQuantEq(objectiveQuantifier);
            assert !isQuantGt(objectiveQuantifier);
            assert !isQuantLt(objectiveQuantifier);
            assert isTrue(objectiveQuantifier.getCondition());
            assert !(quantified instanceof ExpressionReward)
            || isRewardCumulative(quantified);
            if (isIs(objectiveQuantifier) && isDirMax(objectiveQuantifier)) {
                newQuantifiersQuantitative.add(objective);
            } else if (isQuantGe(objectiveQuantifier)) {
                newQuantifiersQualitative.add(objective);
            } else if (isIs(objectiveQuantifier) && !isDirMax(objectiveQuantifier) && !(quantified instanceof ExpressionReward)) {
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                		.setContext(contextValue)
                		.setDirType(DirType.MAX)
                		.setCmpType(CmpType.IS)
                		.setQuantified(not(expressionToType.getContextValue(), quantified))
                		.setCondition(objectiveQuantifier.getCondition())
                		.build();
                newQuantifiersQuantitative.add(newQuantifier);
                subtractNumericalFrom.set(subtractNumericalFrom.getType().getOne());
            } else if (isQuantLe(objectiveQuantifier) && !(quantified instanceof ExpressionReward)) {
                Expression newCompare = subtract(ExpressionLiteral.getOne(getContextValue()), objectiveQuantifier.getCompare());
                newCompare = new ExpressionLiteral.Builder()
                		.setValue(evaluateValue(newCompare))
                		.build();
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                		.setDirType(DirType.NONE)
                		.setCmpType(CmpType.GE)
                		.setQuantified(not(expressionToType.getContextValue(), quantified))
                		.setCompare(newCompare)
                		.setCondition(objectiveQuantifier.getCondition())
                		.build();
                newQuantifiersQualitative.add(newQuantifier);
            } else if (isIs(objectiveQuantifier) && !isDirMax(objectiveQuantifier) && quantified instanceof ExpressionReward) {
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                		.setDirType(DirType.MAX)
                		.setCmpType(CmpType.IS)
                		.setQuantified(quantified)
                		.setCondition(objectiveQuantifier.getCondition())
                		.build();
                invert.add(newQuantifier);
                newQuantifiersQuantitative.add(newQuantifier);
                subtractNumericalFrom.set(subtractNumericalFrom.getType().getZero());
            } else if (isQuantLe(objectiveQuantifier) && quantified instanceof ExpressionReward) {
                Expression newCompare = new ExpressionOperator.Builder()
                        .setOperator(contextValue.getOperator(OperatorAddInverse.IDENTIFIER))
                        .setOperands(objectiveQuantifier.getCompare()).build();
                newCompare = new ExpressionLiteral.Builder()
                		.setValue(evaluateValue(newCompare))
                		.build();
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                		.setDirType(DirType.NONE)
                		.setCmpType(CmpType.GE)
                		.setQuantified(quantified)
                		.setCompare(newCompare)
                		.setCondition(objectiveQuantifier.getCondition())
                		.build();
                newQuantifiersQualitative.add(newQuantifier);
            }
        }
        List<Expression> newQuantifiers = new ArrayList<>();
        newQuantifiers.addAll(newQuantifiersQuantitative);
        newQuantifiers.addAll(newQuantifiersQualitative);
        invertedRewards.clear();
        for (int prop = 0; prop < newQuantifiers.size(); prop++) {
            if (invert.contains(newQuantifiers.get(prop))) {
                invertedRewards.set(prop);
            }
        }
        normalisedProperty = new ExpressionMultiObjective.Builder()
                .setOperands(newQuantifiers)
                .build();
        return this;
	}
	
	ExpressionMultiObjective getNormalisedProperty() {
		return normalisedProperty;
	}
	
	Value getSubtractNumericalFrom() {
		return subtractNumericalFrom;
	}
	
	BitSet getInvertedRewards() {
		return invertedRewards;
	}
	
	private ContextValue getContextValue() {
		return expressionToType.getContextValue();
	}
	
	private ValueAlgebra newValueWeight() {
		return TypeWeight.get(getContextValue()).newValue();
	}
	
    private Expression subtract(Expression a, Expression b) {
    	return new ExpressionOperator.Builder()
    			.setOperator(getContextValue().getOperator(OperatorSubtract.IDENTIFIER))
    			.setOperands(a, b)
    			.build();
    }
    
    private static Expression not(ContextValue contextValue, Expression expression) {
    	return new ExpressionOperator.Builder()
        	.setOperator(contextValue.getOperator(OperatorNot.IDENTIFIER))
        	.setOperands(expression)
        	.build();
    }
    
    private static boolean isRewardCumulative(Expression expression) {
    	if (!(expression instanceof ExpressionReward)) {
    		return false;
    	}
    	ExpressionReward expressionReward = (ExpressionReward) expression;
    	return expressionReward.getRewardType().isCumulative();
    }
    
    private Value evaluateValue(Expression expression) throws EPMCException {
        assert expression != null;
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(expression, expressionToType, new Expression[0]);
        return evaluator.evaluate();
    }
    
    private static boolean isDirMax(Expression expression) {
    	assert expression != null;
    	if (!(expression instanceof ExpressionQuantifier)) {
    		return false;
    	}
    	ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
    	return expressionQuantifier.getDirType() == DirType.MAX;
    }

    private static boolean isIs(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
        return expressionQuantifier.getCompareType() == CmpType.IS;
    }
    
    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(expressionLiteral.getValue());
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

    private static boolean isQuantEq(Expression expression) {
        if (!(expression instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
        return expressionQuantifier.getCompareType().isEq();
    }

}
