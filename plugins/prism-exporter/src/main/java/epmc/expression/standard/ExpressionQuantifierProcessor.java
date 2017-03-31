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

package epmc.expression.standard;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class ExpressionQuantifierProcessor implements JANI2PRISMProcessorStrict {

	private ExpressionQuantifier quantifier = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionQuantifier; 
		
		quantifier = (ExpressionQuantifier) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert quantifier != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		Expression quantified = quantifier.getQuantified();
        if (quantified instanceof ExpressionSteadyState) {
            prism.append("S");
        } else if (quantified instanceof ExpressionReward) {
            prism.append("R");
            
            Expression rewardStructure = ((ExpressionReward) quantified).getReward().getExpression();
			processor = ProcessorRegistrar.getProcessor(rewardStructure);
			prism.append("{").append(processor.toPRISM().toString()).append("}");
        } else {
            prism.append("P");
        }
		
		prism.append(quantifier.getDirType().toString());

		CmpType cmpType = quantifier.getCompareType();
		prism.append(cmpType.toString());
		if (cmpType != CmpType.IS) {
			Expression cmpExp = quantifier.getCompare();
			processor = ProcessorRegistrar.getProcessor(cmpExp);
			prism.append(processor.toPRISM().toString());
		}
		
		prism.append("[");
		
		processor = ProcessorRegistrar.getProcessor(quantified);
		prism.append(processor.toPRISM().toString());
		
		prism.append("]");
		
		return prism;
	}
}
