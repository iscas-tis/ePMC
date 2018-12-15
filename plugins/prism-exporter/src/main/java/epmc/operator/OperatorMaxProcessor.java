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

import epmc.expression.standard.ExpressionOperator;
import epmc.prism.exporter.operatorprocessor.JANI2PRISMOperatorProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

/**
 * @author Andrea Turrini
 *
 */
public class OperatorMaxProcessor implements JANI2PRISMOperatorProcessorStrict {

    private ExpressionOperator expressionOperator = null;
    private String prefix = null;
    
    /* (non-Javadoc)
     * @see epmc.prism.exporter.processor.JANI2PRISMOperatorProcessorStrict#setOperatorElement(epmc.operator.Operator, java.lang.Object)
     */
    @Override
    public JANI2PRISMOperatorProcessorStrict setOperatorElement(Operator operator, Object obj) {
        assert operator != null;
        assert obj != null;
        
        assert operator.equals(OperatorMax.MAX);
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
        
        prism.append("max(")
            .append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toPRISM())
            .append(", ")
            .append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand2())
                        .toPRISM())
            .append(")");

        return prism.toString();
    }
}
