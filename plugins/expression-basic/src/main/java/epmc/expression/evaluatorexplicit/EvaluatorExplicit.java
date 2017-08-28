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

package epmc.expression.evaluatorexplicit;

import java.util.Map;

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

        boolean canHandle();
        
        EvaluatorExplicit build();
    }
    
    String getIdentifier();
    
    /**
     * Get the expression which this builder is used to evaluate.
     * This function is meant mainly for debugging purposes.
     * 
     * @return expression which this builder is used to evaluate
     */
    Expression getExpression();
    
    Value evaluate(Value... values);
    
    Value getResultValue();
}
