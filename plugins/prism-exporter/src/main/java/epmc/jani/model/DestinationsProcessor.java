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

public class DestinationsProcessor implements JANI2PRISMProcessorStrict {

	private Destinations destinations = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Destinations; 
		
		destinations = (Destinations) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert destinations != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		boolean remaining = false;
		for (Destination destination : destinations) {
			processor = ProcessorRegistrar.getProcessor(destination);
			if (remaining) {
				processor.setPrefix(" + ");
			} else {
				remaining = true;
			}
			prism.append(processor.toPRISM().toString());
		}
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert destinations != null;
		
		for (Destination destination : destinations) {
			ProcessorRegistrar.getProcessor(destination).validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert destinations != null;
		
		boolean usesTransient = false;
		for (Destination destination : destinations) {
			usesTransient |= ProcessorRegistrar.getProcessor(destination).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
