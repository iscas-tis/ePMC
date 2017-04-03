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

public class AssignmentsProcessor implements JANI2PRISMProcessorStrict {

	private Assignments assignments = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Assignments; 
		
		assignments = (Assignments) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert assignments != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		boolean remaining = false;
		for (AssignmentSimple assignment : assignments) {
			Variable variable = assignment.getRef();
			if (!variable.isTransient()) {
				processor = ProcessorRegistrar.getProcessor(assignment);
				if (remaining) {
					processor.setPrefix(" & ");
				} else {
					remaining = true;
				}
				prism.append(processor.toPRISM().toString());
			}
		}
		if (!remaining) {
			prism.append("true");
		}
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert assignments != null;
		
		for (Assignment assignment : assignments) {
			ProcessorRegistrar.getProcessor(assignment).validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert assignments != null;
		
		boolean usesTransient = false;
		for (Assignment assignment : assignments) {
			usesTransient |= ProcessorRegistrar.getProcessor(assignment).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}