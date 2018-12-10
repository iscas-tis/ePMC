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

package epmc.expression.standard.evaluatorexplicit;

import java.util.Arrays;
import java.util.Map;

import epmc.error.UtilError;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.expression.standard.ProblemsExpression;
import epmc.expressionevaluator.ExpressionToType;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.Type;
import epmc.value.Value;

public final class UtilEvaluatorExplicit {
    public final static class EvaluatorCacheEntry {
        private final Class<?> returnType;
        private final Expression expression;
        private final Expression[] variables;

        public EvaluatorCacheEntry(Class<?> returnType, Expression expression, Expression[] variables) {
            this.returnType = returnType;
            this.expression = expression;
            this.variables = variables;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            if (returnType != null) {
                hash = returnType.hashCode() + (hash << 6) + (hash << 16) - hash;
            }
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = Arrays.hashCode(variables) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EvaluatorCacheEntry)) {
                return false;
            }
            EvaluatorCacheEntry other = (EvaluatorCacheEntry) obj;
            if ((this.returnType == null) != (other.returnType == null)) {
                return false;
            }
            if (this.returnType != null
                    && !this.returnType.equals(other.returnType)) {
                return false;
            }
            if (!expression.equals(other.expression)) {
                return false;
            }
            if (!Arrays.equals(variables, other.variables)) {
                return false;
            }
            return true;
        }
        
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(returnType);
            builder.append(",");
            builder.append(expression);
            builder.append(",");
            builder.append(Arrays.toString(variables));
            return builder.toString();
        }
    }

    public static EvaluatorExplicitBoolean newEvaluatorBoolean(Expression expression, ExpressionToType expressionToType, Expression[] variables, EvaluatorCache cache) {
        return (EvaluatorExplicitBoolean) newEvaluator(boolean.class, expression, variables, cache, expressionToType);
    }

    public static EvaluatorExplicitBoolean newEvaluatorBoolean(Expression expression, ExpressionToType expressionToType, Expression... variables) {
        EvaluatorCache cache = new EvaluatorCache();
        return (EvaluatorExplicitBoolean) newEvaluator(boolean.class, expression, variables, cache, expressionToType);
    }

    public static EvaluatorExplicit newEvaluator(Class<?> returnType, Expression expression, ExpressionToType expressionToType, Expression... variables) {
        EvaluatorCache cache = new EvaluatorCache();
        return newEvaluator(returnType, expression, variables, cache, expressionToType);
    }

    public static EvaluatorExplicit newEvaluator(Expression expression, ExpressionToType expressionToType, Expression... variables) {
        assert expressionToType != null;
        assert variables != null;
        EvaluatorCache cache = new EvaluatorCache();
        return newEvaluator(null, expression, variables, cache, expressionToType);
    }

    public static Value evaluate(Expression expression, ExpressionToType expressionToType) {
        assert expression != null;
        assert expressionToType != null;
        EvaluatorExplicit evaluator = newEvaluator(expression, expressionToType, new Expression[0]);
        evaluator.setValues(new Value[0]);
        evaluator.evaluate();
        return evaluator.getResultValue();
    }

    public static Value evaluate(Expression expression) {
        assert expression != null;
        return evaluate(expression, new ExpressionToType() {
            
            @Override
            public Type getType(Expression expression) {
                return null;
            }
        });
    }
    
    public static EvaluatorExplicit newEvaluator(
            Class<?> returnType,
            Expression expression,
            Expression[] variables,
            EvaluatorCache cache,
            ExpressionToType expressionToType) {
        //        UtilEvaluatorExplicitCompile.compile(returnType, expression, variables);
        assert expression != null;
        assert variables != null;
        if (cache == null) {
            cache = new EvaluatorCache();
        }
        assert cache != null;
        for (Expression variable : variables) {
            assert variable != null : Arrays.toString(variables);
        }
        assert expressionToType != null;
        EvaluatorCacheEntry entry = new EvaluatorCacheEntry(returnType, expression, variables);
        EvaluatorExplicit already = cache.get(entry);
        if (already != null) {
            return already;
        }

        Options options = Options.get();
        Map<String,Class<? extends EvaluatorExplicit.Builder>> evaluators = options.get(OptionsExpressionBasic.EXPRESSION_EVALUTOR_EXPLICIT_CLASS);
        for (Class<? extends EvaluatorExplicit.Builder> clazz : evaluators.values()) {
            EvaluatorExplicit.Builder builder = Util.getInstance(clazz)
                    .setExpression(expression)
                    .setVariables(variables)
                    .setCache(cache)
                    .setExpressionToType(expressionToType);
            if (builder.canHandle()) {
                EvaluatorExplicit r = builder.build();
                cache.put(entry, r);
                return r;
            }
        }
        UtilError.fail(ProblemsExpression.EXPRESSION_COULD_NOT_EVALUATE, expression.getPositional(), expression);
        return null;
    }
    
    public static boolean assertValues(Value[] values) {
        assert values != null;
        for (Value value : values) {
            assert value != null;
        }
        return true;
    }
}
