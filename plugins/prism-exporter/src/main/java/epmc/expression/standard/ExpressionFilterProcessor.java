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

public class ExpressionFilterProcessor implements JANI2PRISMProcessorStrict {

	private ExpressionFilter filter = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionFilter; 
		
		filter = (ExpressionFilter) obj;
		return this;
	}

	@Override
	public String toPRISM() throws EPMCException {
		assert filter != null;
		
		StringBuilder prism = new StringBuilder();
		
		prism.append("filter(")
			 .append(filter.getFilterType().toString())
			 .append(", ")
			 .append(ProcessorRegistrar.getProcessor(filter.getProp())
					 				   .toPRISM());
		
		Expression states = filter.getStates();
		if (states != null) {
			prism.append(", ")
				 .append(ProcessorRegistrar.getProcessor(states)
						 				   .toPRISM());
		}
		prism.append(")");

		return prism.toString();
	}
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert filter != null;
		
		for (Expression child : filter.getChildren()) {
			ProcessorRegistrar.getProcessor(child)
							  .validateTransientVariables();
		}
	}
	
	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert filter != null;
		
		boolean usesTransient = false;
		for (Expression child : filter.getChildren()) {
			usesTransient |= ProcessorRegistrar.getProcessor(child)
											   .usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
