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

package epmc.jani.explorer;

import static epmc.error.UtilError.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.EnumerateSAT;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatordd.EvaluatorDD;
import epmc.expression.standard.evaluatordd.UtilEvaluatorDD;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitBoolean;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expression.standard.simplify.ContextExpressionSimplifier;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.Variable;
import epmc.jani.model.Variables;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.value.EvaluatorCache;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnumerable;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnumerable;
import epmc.value.ValueRange;

/**
 * Class to obtain all valid variable assignments for a given expression.
 * 
 * @author Ernst Moritz Hahn
 */
public final class VariableValuesEnumerator {
    EvaluatorCache evaluatorCache = new EvaluatorCache();
    public final static String SPACE = " ";
    /**
     * Enum to specify how to obtain the different values.
     * This enum only decides how values are obtained if no short cuts are
     * applicable.
     * 
     * @author Ernst Moritz Hahn
     */
    public enum EnumeratorType {
        /** Use decision diagrams to enumerate variable assignments. */
        DD,
        /** Try out all combinations and check whether they are valid. */
        BRUTE_FORCE,
    }

    private Variables variables;
    private Expression expression;
    private ExpressionToType expressionToType;
    private ContextExpressionSimplifier simplifier;

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public void setExpressionToType(ExpressionToType expressionToType) {
        this.expressionToType = expressionToType;
        this.simplifier = new ContextExpressionSimplifier(expressionToType, null);
    }

    public List<Map<Variable,Value>> enumerate() {
        assert variables != null;
        assert expression != null;
        List<Map<Variable,Value>> result = computeRecursive(variables, expression);
        return result;
    }

    private List<Map<Variable, Value>> computeRecursive(
            Map<String,Variable> variables,
            Expression expression) {
        assert variables != null;
        assert expression != null;
        if (isFalse(expression)) {
            return Collections.emptyList();
        } else if (isTrue(expression) && variables.size() == 0) {
            return Collections.singletonList(Collections.emptyMap());
        } else if (isTrue(expression) && variables.size() > 0) {
            return enumerateCombinations(variables);
        } else if (isSimpleRestriction(expression)) {
            Map<Variable, Value> r = computeSimpleRestricted(expression);
            Map<String,Variable> remainingVariables = new LinkedHashMap<>(variables);
            for (Variable v : r.keySet()) {
                remainingVariables.remove(v.getName());
            }
            List<Map<Variable, Value>> c = enumerateCombinations(remainingVariables);
            List<Map<Variable, Value>> result = combine(c, r);
            return result;
        } else if (isSingleVariableRestricted(expression)) {
            return computeRestricted(variables, expression);
        } else if (isLeftSingleVariableRestricted(expression)) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            return computeRestricted(variables, expressionOperator.getOperand1(), expressionOperator.getOperand2());
        } else if (isRightSingleVariableRestricted(expression)) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            return computeRestricted(variables, expressionOperator.getOperand2(), expressionOperator.getOperand1());
        } else if (isAnd(expression) && isTrue(((ExpressionOperator) expression).getOperand1())) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            return computeRecursive(variables, expressionOperator.getOperand2());
        } else if (isAnd(expression) && isTrue(
                ((ExpressionOperator) expression).getOperand2())) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            return computeRecursive(variables, expressionOperator.getOperand1());			
        } else {
            return generalEnumerate(variables, expression);
        }
    }

    // TODO continue here

    private boolean isSimpleRestriction(Expression expression) {
        assert expression != null;
        if (isSingleVariableRestricted(expression)) {
            return true;
        }
        if (isTrue(expression)) {
            return true;
        }
        if (isAnd(expression)) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            if (isSimpleRestriction(expressionOperator.getOperand1())
                    && isSimpleRestriction(expressionOperator.getOperand2())) {
                return true;
            }
        }
        return false;
    }

    private Map<Variable,Value> computeSimpleRestricted(Expression expression) {
        assert expression != null;
        if (isSingleVariableRestricted(expression)) {
            ExpressionIdentifierStandard identifier = getIdentifier(expression);
            Expression literal = getLiteral(expression);
            Value value = variables.get(identifier.getName()).getType().toType().newValue();
            Value literalValue = evaluateValue(literal);
            OperatorEvaluator set = evaluatorCache.getEvaluator(OperatorSet.SET, literalValue.getType(), value.getType());
            set.apply(value, literalValue);
            if (!ValueRange.checkRange(value)) {
                return Collections.emptyMap();
            } else {
                Variable variable = variables.get(identifier.getName());
                return Collections.singletonMap(variable, value);
            }
        }
        if (isAnd(expression)) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            Map<Variable,Value> left = computeSimpleRestricted(expressionOperator.getOperand1());
            Map<Variable,Value> right = computeSimpleRestricted(expressionOperator.getOperand2());
            Set<Variable> common = new HashSet<>();
            common.addAll(left.keySet());
            common.retainAll(right.keySet());
            if (!common.isEmpty()) {
                for (Variable entry : common) {
                    OperatorEvaluator eq = evaluatorCache.getEvaluator(OperatorEq.EQ, left.get(entry).getType(), right.get(entry).getType());;
                    ValueBoolean cmp = TypeBoolean.get().newValue();
                    eq.apply(cmp, left.get(entry), right.get(entry));
                    if (!cmp.getBoolean()) {
                        return Collections.emptyMap();
                    }
                }
            }
            Map<Variable,Value> result = new LinkedHashMap<>();
            result.putAll(left);
            result.putAll(right);
            return result;
        }
        if (isTrue(expression)) {
            return Collections.emptyMap();
        }
        assert false;
        return null;
    }

    private List<Map<Variable, Value>> computeRestricted(Map<String, Variable> variables, Expression restriction) {
        ExpressionIdentifierStandard identifier = getIdentifier(restriction);
        Expression literal = getLiteral(restriction);
        Map<String,Variable> remainingVariables = new HashMap<>(variables);
        remainingVariables.remove(identifier.getName());
        Value value = variables.get(identifier.getName()).getType().toType().newValue();
        Value literalValue = evaluateValue(literal);
        OperatorEvaluator set = evaluatorCache.getEvaluator(OperatorSet.SET, literalValue.getType(), value.getType());
        set.apply(value, literalValue);
        if (!ValueRange.checkRange(value)) {
            return Collections.emptyList();
        } else if (remainingVariables.size() == 0) {
            Variable variable = variables.get(identifier.getName());
            return Collections.singletonList(Collections.singletonMap(variable, value));
        } else {
            Expression trueExp = ExpressionLiteral.getTrue();
            List<Map<Variable, Value>> inner = generalEnumerate(remainingVariables, trueExp);
            List<Map<Variable, Value>> result = new ArrayList<>();
            for (Map<Variable, Value> map : inner) {
                Map<Variable, Value> newMap = new HashMap<>(map);
                newMap.put(variables.get(identifier.getName()), evaluateValue(literal));
                result.add(newMap);
            }
            return result;
        }
    }

    private List<Map<Variable, Value>> computeRestricted(Map<String,Variable> variables, Expression restriction, Expression other) {
        ExpressionIdentifierStandard identifier = getIdentifier(restriction);
        Expression literal = getLiteral(restriction);
        Value value = variables.get(identifier.getName()).getType().toType().newValue();
        Value literalValue = evaluateValue(literal);
        OperatorEvaluator set = evaluatorCache.getEvaluator(OperatorSet.SET, literalValue.getType(), value.getType());
        set.apply(value, literalValue);
        if (!ValueRange.checkRange(value)) {
            return Collections.emptyList();
        }
        Map<Expression,Expression> replaceMap =
                Collections.singletonMap(identifier, literal);
        Expression remaining = UtilExpressionStandard.replace(other, replaceMap);
        Map<String,Variable> remainingVariables = new HashMap<>(variables);
        remainingVariables.remove(identifier.getName());
        List<Map<Variable, Value>> inner = computeRecursive(remainingVariables, remaining);
        List<Map<Variable, Value>> result = new ArrayList<>();
        for (Map<Variable, Value> map : inner) {
            Map<Variable, Value> newMap = new HashMap<>(map);
            newMap.put(variables.get(identifier.getName()), evaluateValue(literal));
            result.add(newMap);
        }
        return result;
    }

    private boolean isLeftSingleVariableRestricted(Expression expression) {
        assert expression != null;
        if (!isAnd(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return isSingleVariableRestricted(expressionOperator.getOperand1());
    }

    private boolean isRightSingleVariableRestricted(Expression expression) {
        assert expression != null;
        if (!isAnd(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return isSingleVariableRestricted(expressionOperator.getOperand2());
    }

    private ExpressionIdentifierStandard getIdentifier(Expression expression) {
        assert variables != null;
        assert expression != null;
        assert isEq(expression);
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return (ExpressionIdentifierStandard)
                (expressionOperator.getOperand1() instanceof ExpressionIdentifier
                        ? expressionOperator.getOperand1()
                                : expressionOperator.getOperand2());
    }

    private Expression getLiteral(Expression expression) {
        assert expression != null;
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperand1() instanceof ExpressionLiteral
                ? expressionOperator.getOperand1()
                        : expressionOperator.getOperand2();
    }

    private boolean isSingleVariableRestricted(Expression expression) {
        if (!isEq(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        Expression left = expressionOperator.getOperand1();
        Expression right = expressionOperator.getOperand2();
        left = simplifier.simplify(left);
        right = simplifier.simplify(right);
        if (!(left instanceof ExpressionIdentifier && right instanceof ExpressionLiteral)
                && (!(left instanceof ExpressionLiteral && right instanceof ExpressionIdentifier))) {
            return false;
        }
        return true;
    }

    private List<Map<Variable,Value>> enumerateCombinations(Map<String,Variable> variables) {
        assert variables != null;
        for (Variable variable : variables.values()) {
            ensure(TypeEnumerable.is(variable.getType().toType()),
                    ProblemsJANIExplorer.JANI_EXPLORER_INFINITELY_MANY_INITIAL_STATES);
        }
        Expression[] identifiers = new Expression[variables.size()];
        int index = 0;
        for (Variable variable : variables.values()) {
            identifiers[index] = variable.getIdentifier();
            index++;
        }
        int numValues = 1;
        for (Variable variable : variables.values()) {
            TypeEnumerable type = TypeEnumerable.as(variable.getType().toType());
            numValues *= type.getNumValues();
        }
        ValueEnumerable[] variableValues = new ValueEnumerable[variables.size()];
        index = 0;
        for (Variable variable : variables.values()) {
            variableValues[index] = TypeEnumerable.as(variable.getType().toType()).newValue();
            index++;
        }
        List<TypeEnumerable> variableTypes = new ArrayList<>();
        for (Variable variable : variables.values()) {
            variableTypes.add(TypeEnumerable.as(variable.getType().toType()));
        }
        List<Map<Variable,Value>> result = new ArrayList<>(numValues);
        int variablesSize = variables.size();
        int[] numTypesValues = new int[variablesSize];
        for (index = 0; index < variables.size(); index++) {
            numTypesValues[index] = variableTypes.get(index).getNumValues();
        }
        for (int valueNr = 0; valueNr < numValues; valueNr++) {
            int remaining = valueNr;
            index = 0;
            for (int varNr = 0; varNr < variablesSize; varNr++) {
                int numTypeValues = numTypesValues[varNr];
                int varValueNr = remaining % numTypeValues;
                remaining /= numTypeValues;
                variableValues[index].setValueNumber(varValueNr);
                index++;
            }
            Map<Variable,Value> entry = new HashMap<>(variables.size() * 2);
            index = 0;
            for (Variable variable : variables.values()) {
                entry.put(variable, UtilValue.clone(evaluatorCache, variableValues[index]));
                index++;
            }
            result.add(entry);
        }
        return result;
    }

    private List<Map<Variable,Value>> generalEnumerate(Map<String,Variable> variables, Expression expression) {
        assert variables != null;
        assert expression != null;
        EnumeratorType enumType = Options.get().getEnum(OptionsJANIExplorer.JANI_EXPLORER_INITIAL_ENUMERATOR);
        switch (enumType) {
        case BRUTE_FORCE:
            return generalEnumerateBruteForce(variables, expression);
        case DD:
            return generalEnumerateBDD(variables, expression);
        default:
            assert false;
            return null;
        }
    }

    private List<Map<Variable, Value>> generalEnumerateBruteForce(Map<String, Variable> variables,
            Expression expression) {
        assert variables != null;
        assert expression != null;
        for (Variable variable : variables.values()) {
            ensure(TypeEnumerable.is(variable.getType().toType()),
                    ProblemsJANIExplorer.JANI_EXPLORER_INITIAL_STATES_BRUTE_FORCE_UNBOUNDED);
        }
        Expression[] identifiers = new Expression[variables.size()];
        int index = 0;
        for (Variable variable : variables.values()) {
            identifiers[index] = variable.getIdentifier();
            index++;
        }
        int numValues = 1;
        for (Variable variable : variables.values()) {
            numValues *= TypeEnumerable.as(variable.getType().toType()).getNumValues();
        }
        EvaluatorExplicitBoolean evaluator = UtilEvaluatorExplicit.newEvaluatorBoolean(expression, expressionToType, identifiers);
        ValueEnumerable[] variableValues = new ValueEnumerable[variables.size()];
        index = 0;
        for (Variable variable : variables.values()) {
            variableValues[index] = TypeEnumerable.as(variable.getType().toType()).newValue();
            index++;
        }
        List<Map<Variable,Value>> result = new ArrayList<>();
        for (int valueNr = 0; valueNr < numValues; valueNr++) {
            int remaining = valueNr;
            index = 0;
            for (Variable variable : variables.values()) {
                TypeEnumerable type = TypeEnumerable.as(variable.getType().toType());
                int numTypeValues = type.getNumValues();
                int varValueNr = remaining % numTypeValues;
                remaining /= numTypeValues;
                variableValues[index].setValueNumber(varValueNr);
                index++;
            }
            evaluator.setValues(variableValues);
            if (evaluator.evaluateBoolean()) {
                Map<Variable,Value> entry = new HashMap<>();
                index = 0;
                for (Variable variable : variables.values()) {
                    entry.put(variable, UtilValue.clone(evaluatorCache, variableValues[index]));
                    index++;
                }
                result.add(entry);
            }
        }
        return result;
    }

    private List<Map<Variable, Value>> generalEnumerateBDD(
            Map<String, Variable> variables,
            Expression expression) {
        Map<Expression, VariableDD> bddVariables = new LinkedHashMap<>();
        ContextDD contextDD = ContextDD.get();
        VariableDD[] ddVariableArray = new VariableDD[variables.size()];
        Variable[] variablesArray = new Variable[variables.size()];
        int varNr = 0;
        for (Variable variable : variables.values()) {
            Type variableType = variable.getType().toType();
//            assert TypeBounded.is(variableType) : variable;
            VariableDD variableDD = contextDD.newVariable(variable.getName(),
                    variableType, 1);
            bddVariables.put(variable.getIdentifier(), variableDD);
            ddVariableArray[varNr] = variableDD;
            variablesArray[varNr] = variable;
            varNr++;
        }
        EvaluatorDD evaluator = UtilEvaluatorDD.newEvaluator(expression, bddVariables);
        DD dd = evaluator.getDD().clone();
        EnumerateSAT sat = new EnumerateSAT();
        sat.setBDD(dd);
        sat.setVariables(ddVariableArray);
        List<Map<Variable, Value>> result = new ArrayList<>();
        sat.setCallback((Value[] value) -> {
            Map<Variable, Value> map = new LinkedHashMap<>();
            for (int i = 0; i < value.length; i++) {
                map.put(variablesArray[i], UtilValue.clone(evaluatorCache, value[i]));
            }
            result.add(map);
        });
        sat.enumerate();
        evaluator.close();
        return result;
    }

    private List<Map<Variable, Value>> combine(List<Map<Variable, Value>> c,
            Map<Variable, Value> r) {
        List<Map<Variable,Value>> result = new ArrayList<>();
        for (Map<Variable,Value> lm : c) {
            Map<Variable,Value> nList = new LinkedHashMap<>(lm);
            nList.putAll(r);
            result.add(nList);
        }
        return result;
    }

    private static boolean isAnd(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorAnd.AND);
    }

    private Value evaluateValue(Expression expression) {
        assert expression != null;
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(expression, expressionToType, new Expression[0]);
        evaluator.setValues(new Value[0]);
        evaluator.evaluate();
        return evaluator.getResultValue();
    }

    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return !Boolean.valueOf(expressionLiteral.getValue());
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return Boolean.valueOf(expressionLiteral.getValue());
    }

    private boolean isEq(Expression expression) {
        assert expression != null;
        if (!ExpressionOperator.is(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        return expressionOperator.getOperator().equals(OperatorEq.EQ);
    }
}
