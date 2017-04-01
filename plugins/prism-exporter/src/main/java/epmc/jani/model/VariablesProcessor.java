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

package epmc.jani.model;

import epmc.error.EPMCException;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class VariablesProcessor implements JANI2PRISMProcessorStrict {

	private Variables variables = null;
	private String prefix = null;
	private boolean withInitialValue = false;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Variables; 
		
		variables = (Variables) obj;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public void setWithInitialValue(boolean withInitialValue) {
		this.withInitialValue = withInitialValue;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert variables != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		for (Variable variable : variables) {
			processor = ProcessorRegistrar.getProcessor(variable);
			processor.setPrefix(prefix);
			processor.setForDefinition(true);
			processor.setWithInitialValue(withInitialValue);
			prism.append(processor.toPRISM().toString());
		}
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert variables != null;
		
		for (Variable variable : variables) {
			ProcessorRegistrar.getProcessor(variable).validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert variables != null;
		
		boolean usesTransient = false;
		for (Variable variable : variables) {
			usesTransient |= ProcessorRegistrar.getProcessor(variable).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
