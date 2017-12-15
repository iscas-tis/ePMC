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

import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionMultiObjective;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.expression.standard.ExpressionTypeReal;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorSubtract;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;

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
    private final static String ONE = "1";
    private final static String ZERO = "0";
    
    private ExpressionMultiObjective property;
    private Expression subtractNumericalFrom;
    private BitSet invertedRewards;
    private ExpressionMultiObjective normalisedProperty;

    PropertyNormaliser() {
    }

    PropertyNormaliser setOriginalProperty(ExpressionMultiObjective property) {
        this.property = property;
        return this;
    }

    PropertyNormaliser build() {
        invertedRewards = UtilBitSet.newBitSetUnbounded();
        assert property != null;
        List<Expression> newQuantifiersQuantitative = new ArrayList<>();
        List<Expression> newQuantifiersQualitative = new ArrayList<>();
        Set<Expression> invert = new HashSet<>();
        for (Expression objective : property.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            assert !isQuantEq(objectiveQuantifier) : objectiveQuantifier;
            assert !isQuantGt(objectiveQuantifier) : objectiveQuantifier;
            assert !isQuantLt(objectiveQuantifier) : objectiveQuantifier;
            assert isTrue(objectiveQuantifier.getCondition());
            assert !ExpressionReward.is(quantified)
            || isRewardCumulative(quantified);
            if (isIs(objectiveQuantifier) && isDirMax(objectiveQuantifier)) {
                newQuantifiersQuantitative.add(objective);
            } else if (isQuantGe(objectiveQuantifier)) {
                newQuantifiersQualitative.add(objective);
            } else if (isIs(objectiveQuantifier) && !isDirMax(objectiveQuantifier) && !ExpressionReward.is(quantified)) {
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                        .setDirType(DirType.MAX)
                        .setCmpType(CmpType.IS)
                        .setQuantified(negate(quantified))
                        .setCondition(objectiveQuantifier.getCondition())
                        .build();
                newQuantifiersQuantitative.add(newQuantifier);
                subtractNumericalFrom = new ExpressionLiteral.Builder()
                        .setType(ExpressionTypeReal.TYPE_REAL)
                        .setValue(ONE)
                        .build();
            } else if (isQuantLe(objectiveQuantifier) && !ExpressionReward.is(quantified)) {
                Expression newCompare = subtract(ExpressionLiteral.getOne(), objectiveQuantifier.getCompare());
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                        .setDirType(DirType.NONE)
                        .setCmpType(CmpType.GE)
                        .setQuantified(negate(quantified))
                        .setCompare(newCompare)
                        .setCondition(objectiveQuantifier.getCondition())
                        .build();
                newQuantifiersQualitative.add(newQuantifier);
            } else if (isIs(objectiveQuantifier) && !isDirMax(objectiveQuantifier) && ExpressionReward.is(quantified)) {
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                        .setDirType(DirType.MAX)
                        .setCmpType(CmpType.IS)
                        .setQuantified(quantified)
                        .setCondition(objectiveQuantifier.getCondition())
                        .build();
                invert.add(newQuantifier);
                newQuantifiersQuantitative.add(newQuantifier);
                subtractNumericalFrom = new ExpressionLiteral.Builder()
                        .setType(ExpressionTypeReal.TYPE_REAL)
                        .setValue(ZERO)
                        .build();
            } else if (isQuantLe(objectiveQuantifier) && ExpressionReward.is(quantified)) {
                Expression newCompare = new ExpressionOperator.Builder()
                        .setOperator(OperatorAddInverse.ADD_INVERSE)
                        .setOperands(objectiveQuantifier.getCompare()).build();
                Expression newQuantifier = new ExpressionQuantifier.Builder()
                        .setDirType(DirType.NONE)
                        .setCmpType(CmpType.GE)
                        .setQuantified(quantified)
                        .setCompare(newCompare)
                        .setCondition(objectiveQuantifier.getCondition())
                        .build();
                newQuantifiersQualitative.add(newQuantifier);
                invert.add(newQuantifier);
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
                .setPositional(property.getPositional())
                .build();
        return this;
    }

    ExpressionMultiObjective getNormalisedProperty() {
        return normalisedProperty;
    }

    Expression getSubtractNumericalFrom() {
        return subtractNumericalFrom;
    }

    BitSet getInvertedRewards() {
        return invertedRewards;
    }

    private Expression subtract(Expression a, Expression b) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorSubtract.SUBTRACT)
                .setOperands(a, b)
                .build();
    }

    private static Expression negate(Expression expression) {
        if (ExpressionSteadyState.is(expression)) {
            ExpressionSteadyState expressionSteadyState = ExpressionSteadyState.as(expression);
            Expression operand = expressionSteadyState.getOperand1();
            return new ExpressionSteadyState.Builder()
                    .setStates(not(operand))
                    .setPositional(expression.getPositional())
                    .build();
        } else {
            return not(expression);
        }
    }
    
    private static Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorNot.NOT)
                .setOperands(expression)
                .build();
    }

    private static boolean isRewardCumulative(Expression expression) {
        if (!ExpressionReward.is(expression)) {
            return false;
        }
        ExpressionReward expressionReward = ExpressionReward.as(expression);
        return expressionReward.getRewardType().isCumulative();
    }

    private static boolean isDirMax(Expression expression) {
        assert expression != null;
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        return expressionQuantifier.getDirType() == DirType.MAX;
    }

    private static boolean isIs(Expression expression) {
        assert expression != null;
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        return expressionQuantifier.getCompareType() == CmpType.IS;
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return Boolean.valueOf(expressionLiteral.getValue());
    }

    private static boolean isQuantLe(Expression expression) {
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        return expressionQuantifier.getCompareType().isLe();
    }

    private static boolean isQuantGe(Expression expression) {
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        return expressionQuantifier.getCompareType().isGe();
    }

    private static boolean isQuantGt(Expression expression) {
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        return expressionQuantifier.getCompareType().isGt();
    }

    private static boolean isQuantLt(Expression expression) {
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        return expressionQuantifier.getCompareType().isLt();
    }

    private static boolean isQuantEq(Expression expression) {
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = ExpressionQuantifier.as(expression);
        return expressionQuantifier.getCompareType().isEq();
    }

}
