package epmc.expression.evaluatorexplicit;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;
import epmc.value.Value;

public interface EvaluatorExplicit {
    interface Builder {
        String getIdentifier();
        
        Builder setVariables(Expression[] variables);
        
        Builder setExpression(Expression expression);
        
        Builder setExpressionToType(ExpressionToType expressionToType);
        
        default Builder setCache(Map<EvaluatorCacheEntry,EvaluatorExplicit> cache) {
            return this;
        }
        
        default void setReturnType(Class<?> returnType) {
        }

        boolean canHandle() throws EPMCException;
        
        EvaluatorExplicit build() throws EPMCException;
    }
    
    String getIdentifier();
    
    /**
     * Get the expression which this builder is used to evaluate.
     * This function is meant mainly for debugging purposes.
     * 
     * @return expression which this builder is used to evaluate
     */
    Expression getExpression();
    
    Value evaluate(Value... values) throws EPMCException;
    
    Value getResultValue();
}
