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

package epmc.jani.model.property;

import epmc.error.EPMCException;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class JANIPropertyEntryProcessor implements JANI2PRISMProcessorStrict {

	private JANIPropertyEntry property = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof JANIPropertyEntry; 
		
		property = (JANIPropertyEntry) obj;
		return this;
	}

	@Override
	public String toPRISM() throws EPMCException {
		assert property != null;
		
		StringBuilder prism = new StringBuilder();
		
		String comment = property.getComment();
		if (comment != null) {
			prism.append("// ")
				 .append(comment)
				 .append("\n");
		}
		
		prism.append(ProcessorRegistrar.getProcessor(property.getExpression())
									   .toPRISM())
			 .append("\n");
		
		return prism.toString();
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert property != null;
		
		ProcessorRegistrar.getProcessor(property.getExpression())
		                  .validateTransientVariables();
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert property != null;
		
		return ProcessorRegistrar.getProcessor(property.getExpression())
					             .usesTransientVariables();
	}	
}
