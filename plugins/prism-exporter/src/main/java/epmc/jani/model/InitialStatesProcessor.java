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
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class InitialStatesProcessor implements JANI2PRISMProcessorStrict {

	private InitialStates initialStates = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof InitialStates; 
		
		initialStates = (InitialStates) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert initialStates != null;

		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		String comment = initialStates.getComment();
		if (comment != null) {
			prism.append("// ").append(comment).append("\n");
		}
		prism.append("init\n").append(ModelJANIProcessor.INDENT);
		
		Expression exp = initialStates.getExp(); 
		processor = ProcessorRegistrar.getProcessor(exp);
		prism.append(processor.toPRISM().toString());
		
		prism.append("\nendinit\n");
		
		return prism;
	}
}
