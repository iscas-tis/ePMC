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

import static epmc.error.UtilError.ensure;

import java.util.LinkedList;
import java.util.List;

import epmc.expression.standard.ExpressionOperator;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.messages.NonPRISMFeaturesPRISMExporter;
import epmc.prism.exporter.operatorprocessor.PRISMExporter_OperatorProcessorNonPRISM;

/**
 * @author Andrea Turrini
 *
 */
public class PRISMExporter_OperatorUnderflowProcessor implements PRISMExporter_OperatorProcessorNonPRISM {

    private ExpressionOperator expressionOperator = null;
    
    /* (non-Javadoc)
     * @see epmc.prism.exporter.processor.JANI2PRISMOperatorProcessorNonPRISM#setExpressionOperator(epmc.expression.standard.ExpressionOperator)
     */
    @Override
    public PRISMExporter_OperatorProcessorNonPRISM setExpressionOperator(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;
        
        assert expressionOperator.getOperator().equals(OperatorUnderflow.UNDERFLOW);
    
        this.expressionOperator = expressionOperator;
        return this;
    }

    /* (non-Javadoc)
     * @see epmc.prism.exporter.processor.JANI2PRISMOperatorProcessorStrict#toPRISM()
     */
    @Override
    public String toPRISM() {
        assert expressionOperator != null;
        ensure(false, 
                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_UNKNOWN_OPERATOR, 
                expressionOperator.getOperator());
        return null;
    }

    /* (non-Javadoc)
     * @see epmc.prism.exporter.processor.JANI2PRISMOperatorProcessorNonPRISM#getUnsupportedFeature()
     */
    @Override
    public List<String> getUnsupportedFeature() {
        List<String> ll = new LinkedList<>();
        ll.add(NonPRISMFeaturesPRISMExporter.PRISM_EXPORTER_NONPRISM_FEATURE_OPERATOR_UNDERFLOW);
        return ll;
    }
}
