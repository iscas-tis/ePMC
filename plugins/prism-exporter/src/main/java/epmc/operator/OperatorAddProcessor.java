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

package epmc.operator;

import java.util.List;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.prism.exporter.operatorprocessor.JANI2PRISMOperatorProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

/**
 * @author Andrea Turrini
 *
 */
public class OperatorAddProcessor implements JANI2PRISMOperatorProcessorStrict {

    private ExpressionOperator expressionOperator = null;
    private String prefix = null;
    
    /* (non-Javadoc)
     * @see epmc.prism.exporter.processor.JANI2PRISMOperatorProcessorStrict#setOperatorElement(epmc.operator.Operator, java.lang.Object)
     */
    @Override
    public JANI2PRISMOperatorProcessorStrict setOperatorElement(Operator operator, Object obj) {
        assert operator != null;
        assert obj != null;
        
        assert operator.equals(OperatorAdd.ADD);
        assert obj instanceof ExpressionOperator; 
    
        expressionOperator = (ExpressionOperator) obj;
        return this;
    }

    /* (non-Javadoc)
     * @see epmc.prism.exporter.operatorprocessor.JANI2PRISMOperatorProcessorStrict#setPrefix(String)
     */
    @Override
    public JANI2PRISMOperatorProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    /* (non-Javadoc)
     * @see epmc.prism.exporter.operatorprocessor.JANI2PRISMOperatorProcessorStrict#toPRISM()
     */
    @Override
    public String toPRISM() {
        assert expressionOperator != null;

        StringBuilder prism = new StringBuilder();

        if (prefix != null) {
            prism.append(prefix);
        }
        
        List<Expression> children = expressionOperator.getChildren();
        boolean remaining = false;
        for (Expression child : children) {
            boolean needBraces = true;
            if (remaining) {
                prism.append(" + ");
            } else {
                remaining = true;
            }
            if (child instanceof ExpressionOperator) {
                ExpressionOperator childOp = (ExpressionOperator) child;
                if (OperatorAdd.ADD.equals(childOp.getOperator())) {
                    needBraces = false;
                }
                if (OperatorMultiply.MULTIPLY.equals(childOp.getOperator())
                        || OperatorDivideIgnoreZero.DIVIDE_IGNORE_ZERO.equals(childOp.getOperator())) {
                    needBraces = false;
                }
            }
            if (child instanceof ExpressionLiteral || child instanceof ExpressionIdentifier) {
                needBraces = false;
            }
            if (needBraces) {
                prism.append("(");
            }
            prism.append(ProcessorRegistrar.getProcessor(child)
                    .toPRISM());
            if (needBraces) {
                prism.append(")");
            }
        }

        return prism.toString();
    }

}
