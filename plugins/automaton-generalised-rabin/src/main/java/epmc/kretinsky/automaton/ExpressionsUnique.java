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

package epmc.kretinsky.automaton;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class ExpressionsUnique {
    private final static class Dummy {
        @Override
        public String toString() {
            return "dummy" + super.hashCode();
        }
    }

    private final Map<Expression,Expression> uniqueMap;
    private final Map<Expression,DD> atomicMap;
    private final Map<DD,Expression> ddToExpressionMap = new HashMap<>();
    private final Map<Expression,DD> expressionToDdMap = new HashMap<>();
    private final ExpressionToDD replacedToDD;
    private final VariableDD[] ddReplacedVariables;
    private final Expression[] replaced;
    private final ContextExpression contextExpression;
    private final ContextDD contextDD;
    private final Expression[] expressions;
    private Value[][] consistentValues;
    private Expression[] consistentExpressions;
    private TObjectIntMap<Value[]> valueToNumber = new TObjectIntCustomHashMap<>(HashingStrategyArrayObject.getInstance());
    private Map<Expression, Expression> replacement;
    private Map<Expression, Expression> replacementInverse;
    private Dummy dummy = new Dummy();

    public ExpressionsUnique(ContextExpression contextExpression, Expression[] expressions) {
        assert contextExpression != null;
        assert expressions != null;
        computeReplacement(expressions);
        this.expressions = expressions;
        this.contextExpression = contextExpression;
        this.contextValue = ContextValue.get();
        this.uniqueMap = contextExpression.newMap();
        this.atomicMap = contextExpression.newMap();
        this.contextDD = contextExpression.getContextDD();
        Map<Expression,VariableDD> variables = contextExpression.newMap();

        this.replaced = new Expression[expressions.length];
        for (int i = 0; i < expressions.length; i++) {
            this.replaced[i] = replacement.get(expressions[i]);
        }

        this.ddReplacedVariables = new VariableDD[expressions.length];
        for (int i = 0; i < replaced.length; i++) {
            Expression repl = replaced[i];
            VariableDD variable = contextDD.newVariable(repl.toString(), repl.getType(), 1);
            variables.put(repl, variable);
            ddReplacedVariables[i] = variable;
        }
        this.replacedToDD = new ExpressionToDD(contextExpression, contextDD, variables);
        enumerateExpressionsValues();
        uniqueMap.put(contextExpression.getFalse(), contextExpression.getFalse());
        uniqueMap.put(contextExpression.getTrue(), contextExpression.getTrue());
        ddToExpressionMap.put(contextDD.newConstant(false), contextExpression.getFalse());
        expressionToDdMap.put(contextExpression.getFalse(), contextDD.newConstant(false));
        ddToExpressionMap.put(contextDD.newConstant(true), contextExpression.getTrue());
        expressionToDdMap.put(contextExpression.getTrue(), contextDD.newConstant(true));
    }

    Expression makeUnique(Expression formula) {
        Expression retrieved = uniqueMap.get(formula);
        if (retrieved != null) {
            return retrieved;
        }
        DD dd = formulaToDD(formula);
        retrieved = ddToExpressionMap.get();
        /*
        if (cmpExprsLanguage && retrieved == null && UtilKretinsky.isTemporal(formula)) {
            Collection<Expression> allExpressions = uniqueMap.values();
            for (Expression compare : allExpressions) {
                if (!UtilKretinsky.isTemporal(compare)) {
                    continue;
                }
                if (UtilKretinsky.ltlfiltEquivalent(compare, formula)) {
                    retrieved = compare;
                    uniqueMap.put(formula, retrieved);
                    break;
                }
            }
        }
         */
        if (retrieved == null) {
            ddToExpressionMap.put(dd.clone(), formula);
            expressionToDdMap.put(formula, dd.clone());
            retrieved = formula;
            uniqueMap.put(formula, retrieved);
        }
        dd.dispose();
        return retrieved;
    }

    DD formulaToDD(Expression formula) {
        DD result;
        if (formula.isLiteral() || formula.isIdentifier()) {
            result = replacedToDD.translate(formula);
        } else if (formula.isAnd()) {
            DD left = formulaToDD(makeUnique(formula.getOperand1()));
            DD right = formulaToDD(makeUnique(formula.getOperand2()));
            //            formula = ddToFormula(left).and(ddToFormula(right));
            result = left.andWith(right);
        } else if (formula.isOr()) {
            DD left = formulaToDD(makeUnique(formula.getOperand1()));
            DD right = formulaToDD(makeUnique(formula.getOperand2()));
            //            formula = ddToFormula(left).or(ddToFormula(right));
            result = left.orWith(right);
        } else if (formula.isNext()) {
            Expression inner = makeUnique(formula.getOperand1());
            Expression atomic = contextExpression.newNext(inner);
            result = atomicExpressionToDD(atomic).clone();
        } else if (formula.isGlobally()) {
            Expression inner = makeUnique(formula.getOperand1());
            Expression atomic = contextExpression.newGlobally(inner);
            result = atomicExpressionToDD(atomic).clone();
        } else if (formula.isFinally()) {
            Expression inner = makeUnique(formula.getOperand1());
            Expression atomic = contextExpression.newFinallyF(inner);
            result = atomicExpressionToDD(atomic).clone();
        } else if (formula.isUntil()) {
            Expression left = makeUnique(formula.getOperand1());
            Expression right = makeUnique(formula.getOperand2());
            Expression atomic = contextExpression.newUntil(left, right);
            result = atomicExpressionToDD(atomic).clone();
        } else if (formula.isRelease()) {
            Expression left = makeUnique(formula.getOperand1());
            Expression right = makeUnique(formula.getOperand2());
            Expression atomic = contextExpression.newRelease(left, right);
            result = atomicExpressionToDD(atomic).clone();
        } else if (formula.isNot()) {
            DD inner = formulaToDD(formula.getOperand1());
            result = inner.notWith();
        } else {
            assert false : formula;
        result = null;
        }
        assert result != null : formula;
        //        if (!formulaUniqueMap.containsKey(result)) {
        //            formulaUniqueMap.put(result, formula);
        //        }
        return result;
    }

    private DD atomicExpressionToDD(Expression expression) {
        DD entry = atomicMap.get();
        if (entry == null) {
            entry = contextDD.newVariable(expression.toString(), contextValue.getTypeBoolean(), 1)
                    .getValueEncoding(0).clone();
            atomicMap.put(expression, entry);
        }
        return entry;
    }

    void simplifyGuard(DD guard, int expression, List<List<Expression>> result, List<Expression> current) {
        if (expression >= replaced.length) {
            if (!guard.isFalse()) {
                result.add(new ArrayList<>(current));
            }
            return;
        }

        Set<VariableDD> support = guard.highLevelSupport();
        Expression repl = replaced[expression];
        VariableDD varDD = ddReplacedVariables[expression];
        if (support.contains(varDD)) {
            Type type = repl.getType();
            for (int valueNr = 0; valueNr < type.getNumValues(); valueNr++) {
                Value value = type.getIthValue(valueNr);
                Expression literal = contextExpression.newLiteral(value);
                Expression eq;
                if (type.isBoolean()) {
                    eq = repl;
                    if (value.isFalse()) {
                        eq = eq.not();
                    }
                } else {
                    eq = repl.eq(literal);
                }
                DD set = guard.clone().andWith(replacedToDD.translate(eq));
                if (!set.isFalse()) {
                    current.add(eq);
                    simplifyGuard(set, expression + 1, result, current);
                    current.remove(current.size() - 1);
                }
                set.dispose();
            }
        } else {
            simplifyGuard(guard, expression + 1, result, current);
        }
    }

    Expression simplifyGuard(Expression guard) {
        DD dd = formulaToDD(guard);
        if (dd.isTrue()) {
            dd.dispose();
            return contextExpression.getTrue();
        } else if (dd.isFalse()) {
            dd.dispose();
            return contextExpression.getFalse();            
        }
        List<List<Expression>> resultList = new ArrayList<>();
        List<Expression> current = new ArrayList<>();
        simplifyGuard(dd, 0, resultList, current);
        dd.dispose();
        Expression result = null;
        for (List<Expression> part : resultList) {
            Expression and = null;
            for (Expression expr : part) {
                and = (and == null) ? expr : and.and(expr);
            }
            result = (result == null) ? and : result.or(and);
        }
        return result;
    }

    void enumerateExpressionsValues() {
        Set<Expression> identifiers = contextExpression.newSet();
        for (Expression expression : expressions) {
            identifiers.addAll(expression.collectIdentifiers());
        }
        Map<Expression,VariableDD> variables = contextExpression.newMap();
        for (Expression identifier : identifiers) {
            variables.put(identifier, contextDD.newVariable(identifier.toString(), identifier.getType(), 1));
        }
        ExpressionToDD checkE2D = new ExpressionToDD(contextExpression, contextDD, variables);

        List<Value[]> values = new ArrayList<>();
        List<Expression> consistentExpressions = new ArrayList<>();
        int maxNumValues = 1;
        for (Expression expression : expressions) {
            Type type = expression.getType();
            maxNumValues *= type.getNumValues();
        }
        for (int entryNr = 0; entryNr < maxNumValues; entryNr++) {
            int usedNr = entryNr;
            DD check = contextDD.newConstant(true);
            boolean invalid = false;
            Value[] entry = new Value[expressions.length];
            for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
                Expression expression = expressions[exprNr];
                Type type = expression.getType();
                int numValues = type.getNumValues();
                int valueNr = usedNr % numValues;
                usedNr /= numValues;
                Value value = type.getIthValue(valueNr);
                entry[exprNr] = value;
                Expression literal = contextExpression.newLiteral(value);
                DD eq = checkE2D.translate(expression.eq(literal));
                check = check.andWith(eq);
                if (check.isFalse()) {
                    invalid = true;
                    break;
                }
            }
            check.dispose();
            if (!invalid) {
                valueToNumber.put(entry, valueToNumber.size());
                values.add(entry);
                consistentExpressions.add(valueEntryToExpression(entry));
            }
        }
        checkE2D.close();
        this.consistentValues = values.toArray(new Value[values.size()][]);
        this.consistentExpressions = consistentExpressions.toArray(new Expression[consistentExpressions.size()]);
    }

    Expression[] getReplaced() {
        return replaced;
    }

    Value[][] getConsistentValues() {
        return consistentValues;
    }

    Expression[] getConsistentExpressions() {
        return consistentExpressions;
    }

    int valueToNumber(Value[] value) {
        int number = valueToNumber.get(value);
        if (number == -1) {
            number = valueToNumber.size();
            Value[] newValue = new Value[value.length];
            for (int i = 0; i < value.length; i++) {
                newValue[i] = value[i].clone();
            }
            valueToNumber.put(newValue, number);
        }
        return number;
    }

    Expression valueEntryToExpression(Value[] succ) {
        Expression result = null;
        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
            Value value = succ[exprNr];
            Expression eq;
            if (value.isBoolean()) {
                eq = getReplaced()[exprNr];
                if (value.isFalse()) {
                    eq = eq.not();
                }
            } else {
                Expression literal = contextExpression.newLiteral(value);
                eq = expressions[exprNr].eq(literal);
            }
            if (result == null) {
                result = eq;
            } else {
                result = result.and(eq);
            }
        }
        if (result == null) {
            result = contextExpression.getTrue();
        }
        return result;
    }

    public Expression[] getExpressions() {
        return expressions;
    }

    public Map<Expression, Expression> getReplacement() {
        return replacement;
    }

    private void computeReplacement(
            Expression[] expressions) {
        assert expressions != null;
        if (expressions.length == 0) {
            replacement = Collections.emptyMap();
            replacementInverse = Collections.emptyMap();
            return;
        }
        replacement = contextExpression.newMap();
        replacementInverse = contextExpression.newMap();
        for (int i = 0; i < expressions.length; i++) {
            Expression replExpr = contextExpression.newIdentifier("{" + expressions[i].toString() + "}", dummy);
            replacement.put(expressions[i], replExpr);
            replacementInverse.put(replExpr, expressions[i]);
            replExpr.setType(expressions[i].getType());
        }
    }

    DD getExpressionDD(Expression expression) {
        assert expression != null;
        assert expressionToDdMap.containsKey(expression);
        return expressionToDdMap.get();
    }

    public Expression toUnreplaced(Expression expression) {
        return expression.replace(replacementInverse);
    }
}
