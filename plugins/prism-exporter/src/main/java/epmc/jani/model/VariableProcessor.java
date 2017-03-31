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
import epmc.expression.Expression;
import epmc.jani.model.type.JANIType;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;
import epmc.prism.exporter.processor.JANIComponentRegistrar;

public class VariableProcessor implements JANI2PRISMProcessorStrict {

	private Variable variable = null;
	private String prefix = null;
	private boolean forDefinition = false;
	private boolean withInitialValue = false;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Variable; 
		
		variable = (Variable) obj;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public void setForDefinition(boolean forDefinition) {
		this.forDefinition = forDefinition;
	}

	@Override
	public void setWithInitialValue(boolean withInitialValue) {
		this.withInitialValue = withInitialValue;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert variable != null;
		
		if (forDefinition) {
			return toPRISMForDefinition();
		} else {
			return new StringBuilder(variable.getName());
		}
	}
	
	private StringBuilder toPRISMForDefinition() throws EPMCException {
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		String comment = variable.getComment();
		if (comment != null) {
			if (prefix != null) {
				prism.append(prefix);
			}
			prism.append("// ").append(comment).append("\n");
		}

		JANIType type = variable.getType();
		processor = ProcessorRegistrar.getProcessor(type);
		if (prefix != null)	{
			prism.append(prefix);
		}
		prism.append(JANIComponentRegistrar.getVariableNameByVariable(variable)).append(" : ").append(processor.toPRISM().toString());
		
		if (withInitialValue) {
			Expression initial = variable.getInitialValueOrNull();
			if (initial != null) {
				processor = ProcessorRegistrar.getProcessor(initial);
				prism.append(" init ").append(processor.toPRISM().toString());
			}
		}
		
		prism.append(";\n");
		
		return prism;
	}
}
