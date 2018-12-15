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

package epmc.prism.exporter.operatorprocessor;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;

public interface JANI2PRISMOperatorProcessorStrict {	

    JANI2PRISMOperatorProcessorStrict setExpressionOperator(ExpressionOperator expressionOperator);

    /**
     * Decide whether the child operand needs to be wrapped with parentheses
     * so to generated an unambiguous or even a correct expression according 
     * to the PRISM syntax. 
     * 
     * @param childOperand the child operand
     * @return whether the child operand needs to be wrapped with parentheses
     */
    default boolean needsParentheses(Expression childOperand) { 
        return (childOperand instanceof ExpressionLiteral 
            || childOperand instanceof ExpressionIdentifier); 
    };
    
    /**
     * Generate a PRISM representation of the component.
     * @return the PRISM representation
     */
    String toPRISM();
}
