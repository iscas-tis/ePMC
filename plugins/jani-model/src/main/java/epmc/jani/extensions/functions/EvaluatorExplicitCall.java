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

package epmc.jani.extensions.functions;

import java.util.ArrayList;
import java.util.Map;

import epmc.value.ValueBoolean;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitBoolean;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.JANIIdentifier;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitCall implements EvaluatorExplicit, EvaluatorExplicitBoolean {
    public final static class Builder implements EvaluatorExplicit.Builder {
        private Expression[] variables;
        private Expression expression;
        private EvaluatorCache cache;
        private ExpressionToType expressionToType;
        private Class<?> returnType;

        @Override
        public String getIdentifier() {
            return IDENTIFIER;
        }

        @Override
        public Builder setVariables(Expression[] variables) {
            this.variables = variables;
            return this;
        }

        private Expression[] getVariables() {
            return variables;
        }

        @Override
        public Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        private Expression getExpression() {
            return expression;
        }

        @Override
        public Builder setCache(EvaluatorCache cache) {
            this.cache = cache;
            return this;
        }

        private EvaluatorCache getCache() {
            return cache;
        }

        @Override
        public boolean canHandle() {
            assert expression != null;
            if (!ExpressionCall.is(expression)) {
                return false;
            }
            for (Expression variable : variables) {
                if (expression.equals(variable)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void setReturnType(Class<?> returnType) {
            this.returnType = returnType;
        }
        
        @Override
        public EvaluatorExplicit build() {
            EvaluatorCacheEntry entry = new EvaluatorCacheEntry(returnType, expression, variables);
            EvaluatorExplicit already = cache.get(entry);
            if (already != null) {
                return already;
            }
            return new EvaluatorExplicitCall(this);
        }

        @Override
        public EvaluatorExplicit.Builder setExpressionToType(
                ExpressionToType expressionToType) {
            this.expressionToType = expressionToType;
            return this;
        }

        private ExpressionToType getExpressionToType() {
            return expressionToType;
        }
    }

    private final static class ExpressionToTypeCall implements ExpressionToType {
        private final ExpressionToType base;
        private final Map<String, JANIIdentifier> parameters;

        private ExpressionToTypeCall(ExpressionToType base, Map<String, JANIIdentifier> parameters) {
            assert base != null;
            assert parameters != null;
            this.base = base;
            this.parameters = parameters;
        }
        
        @Override
        public Type getType(Expression expression) {
            Type result = base.getType(expression);
            if (result != null) {
                return result;
            }
            if (ExpressionIdentifierStandard.is(expression)) {
                ExpressionIdentifierStandard identifier = ExpressionIdentifierStandard.as(expression);
                if (parameters.containsKey(identifier.getName())) {
                    return parameters.get(identifier.getName()).getType().toType();
                }
            }
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    public final static String IDENTIFIER = "call";
    private final ExpressionCall expression;
    private final Value[] extendedValues;
    private final int regSize;

    private final EvaluatorExplicit bodyEvaluator;
    private final EvaluatorExplicit[] operatorEvaluators;

    private EvaluatorExplicitCall(Builder builder) {
        // TODO check for overlapping parameter names
        // TODO recursion won't work
        // TODO this thing is not very well tested
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        // TODO support automata-specific functions
        expression = (ExpressionCall) builder.getExpression();
        JANIFunction function = findFunction(builder);
        Expression[] currentVariables = builder.getVariables();
        Expression[] extendedVariables = new Expression[currentVariables.length
                                                        + function.getParameters().size()];
        for (int index = 0; index < currentVariables.length; index++) {
            extendedVariables[index] = currentVariables[index];
        }
        int index = currentVariables.length;
        for (JANIIdentifier janiIdentifier : function.getParameters().values()) {
            extendedVariables[index] = janiIdentifier.getIdentifier();
            index++;
        }
        
        Expression body = function.getBody();
        body = function.getModel().replaceConstants(body);
        EvaluatorCacheEntry entry = new EvaluatorCacheEntry(builder.returnType, builder.expression, builder.variables);
        builder.cache.put(entry, this);
        ExpressionToType exprToTypeBody = computeExpressionToTypeBody(builder.getExpressionToType(), function.getParameters());
        bodyEvaluator = UtilEvaluatorExplicit.newEvaluator(null, body, extendedVariables, builder.getCache(), exprToTypeBody);
        operatorEvaluators = new EvaluatorExplicit[function.getParameters().size()];
        index = 0;
        for (Expression expression : expression.getOperands()) {
            operatorEvaluators[index] = UtilEvaluatorExplicit.newEvaluator(null, expression, currentVariables, builder.getCache(), builder.getExpressionToType());
            index++;
        }
        extendedValues = new Value[currentVariables.length
                                   + function.getParameters().size()];
        regSize = currentVariables.length;
    }

    private ExpressionToType computeExpressionToTypeBody(ExpressionToType expressionToType,
            Map<String, JANIIdentifier> parameters) {
        return new ExpressionToTypeCall(expressionToType, parameters);
        // TODO Auto-generated method stub
//        return null;
    }

    private static JANIFunction findFunction(Builder builder) {
        ModelExtensionFunctions extensionFunctions = (ModelExtensionFunctions) builder.cache.getAux(ModelExtensionFunctions.class);
        ArrayList<JANIFunction> functions = extensionFunctions.getModelFunctions().getFunctions();
        String callWhat = ((ExpressionCall) builder.expression).getFunction();
        JANIFunction function = null;
        for (JANIFunction f : functions) {
            if (f.getName().equals(callWhat)) {
                function = f;
                break;
            }
        }
        return function;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public void setValues(Value... values) {
        for (int i = 0; i < operatorEvaluators.length; i++) {
            operatorEvaluators[i].setValues(values);
        }
        for (int i = 0; i < values.length; i++) {
            extendedValues[i] = values[i];
        }
//        bodyEvaluator.setValues(extendedValues);
    }
    
    @Override
    public void evaluate() {
        for (int i = 0; i < operatorEvaluators.length; i++) {
            operatorEvaluators[i].evaluate();
        }
        for (int i = 0; i < operatorEvaluators.length; i++) {
            extendedValues[regSize + i] = operatorEvaluators[i].getResultValue();
        }
        bodyEvaluator.setValues(extendedValues);
        bodyEvaluator.evaluate();
    }

    @Override
    public Value getResultValue() {
        return bodyEvaluator.getResultValue();
    }

    @Override
    public boolean evaluateBoolean() {
        evaluate();
        return ValueBoolean.as(bodyEvaluator.getResultValue()).getBoolean();
    }
}
