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

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class AssignmentSimpleProcessor implements JANI2PRISMProcessorStrict {

	private AssignmentSimple assignment = null;
	private String prefix = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof AssignmentSimple; 
		
		assignment = (AssignmentSimple) obj;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert assignment != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		if (prefix != null)	{
			prism.append(prefix);
		}
		prism.append("(");
		
		Variable variable = assignment.getRef();
		processor = ProcessorRegistrar.getProcessor(variable);
		prism.append(processor.toPRISM().toString());
		
		prism.append("'=");
		
		Expression update = assignment.getValue();
		processor = ProcessorRegistrar.getProcessor(update);
		prism.append(processor.toPRISM().toString());
		
		prism.append(")");
		return prism;
	}

	@Override
	public void validateTransientVariables() throws EPMCException {
		assert assignment != null;
		
		ensure(!ProcessorRegistrar.getProcessor(assignment.getValue()).usesTransientVariables(), 
				ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_FOR_NORMAL_VARIABLE, 
				assignment.getRef().getName());
	}
	
	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert assignment != null;
		
		boolean usesTransient = false;
		usesTransient |= ProcessorRegistrar.getProcessor(assignment.getRef()).usesTransientVariables();
		usesTransient |= ProcessorRegistrar.getProcessor(assignment.getValue()).usesTransientVariables();
		
		return usesTransient;
	}	
}
