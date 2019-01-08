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
import epmc.operator.Operator;
import epmc.operator.OperatorCeil;
import epmc.operator.OperatorFloor;
import epmc.operator.OperatorLog;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMod;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSqrt;

public interface PRISMExporter_OperatorProcessorStrict {	

    PRISMExporter_OperatorProcessorStrict setExpressionOperator(ExpressionOperator expressionOperator);

    /**
     * Decide whether the child operand needs to be wrapped with parentheses
     * so to generated an unambiguous or even a correct expression according 
     * to the PRISM syntax. 
     * 
     * @param childOperand the child operand
     * @return whether the child operand needs to be wrapped with parentheses
     */
    default boolean needsParentheses(Expression childOperand) {
        if (childOperand instanceof ExpressionLiteral) 
            return false;
        if (childOperand instanceof ExpressionIdentifier) 
            return false;
        if (childOperand instanceof ExpressionOperator) {
            Operator operator = ((ExpressionOperator) childOperand).getOperator();
            if (operator.equals(OperatorCeil.CEIL))
                return false;
            if (operator.equals(OperatorFloor.FLOOR))
                return false;
            if (operator.equals(OperatorLog.LOG))
                return false;
            if (operator.equals(OperatorMax.MAX))
                return false;
            if (operator.equals(OperatorMin.MIN))
                return false;
            if (operator.equals(OperatorMod.MOD))
                return false;
            if (operator.equals(OperatorPow.POW))
                return false;
            if (operator.equals(OperatorSqrt.SQRT))
                return false;
        }
        return true; 
    };
    
    /**
     * Generate a PRISM representation of the component.
     * @return the PRISM representation
     */
    String toPRISM();
}
