package epmc.expression.standard.evaluatorexplicit.compile;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;
import epmc.value.Value;

public final class EvaluatorExplicitCompile implements EvaluatorExplicit {
    public final static class Builder implements EvaluatorExplicit.Builder {
        private Expression[] variables;
        private Expression expression;
        private Map<EvaluatorCacheEntry, EvaluatorExplicit> cache;

        @Override
        public String getIdentifier() {
            return IDENTIFIER;
        }

        @Override
        public Builder setVariables(Expression[] variables) {
            this.variables = variables;
            return this;
        }

        @Override
        public Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public Builder setCache(Map<EvaluatorCacheEntry, EvaluatorExplicit> cache) {
            this.cache = cache;
            return this;
        }
        
        @Override
        public boolean canHandle() throws EPMCException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public EvaluatorExplicit build() throws EPMCException {
            // TODO Auto-generated method stub
            return null;
        }

		@Override
		public epmc.expression.evaluatorexplicit.EvaluatorExplicit.Builder setExpressionToType(
				ExpressionToType expressionToType) {
			// TODO Auto-generated method stub
			return null;
		}
        
    }
    
    public final static String IDENTIFIER = "compile";
    public final static String VARIABLES_PARAMETER = "variables";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Expression getExpression() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Value evaluate(Value... values) throws EPMCException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Value getResultValue() {
        // TODO Auto-generated method stub
        return null;
    }

}
