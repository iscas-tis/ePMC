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
import epmc.expression.Expression;
import epmc.modelchecker.RawProperty;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class JANIPropertiesProcessor implements JANI2PRISMProcessorStrict {

	private JANIProperties properties = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof JANIProperties; 
		
		properties = (JANIProperties) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert properties != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		for (RawProperty raw : properties.getRawProperties()) {
			Expression property = properties.getParsedProperty(raw);
			processor = ProcessorRegistrar.getProcessor(property);
			prism.append(processor.toPRISM().toString()).append("\n");
		}
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert properties != null;
		
		for (RawProperty raw : properties.getRawProperties()) {
			Expression property = properties.getParsedProperty(raw);
			ProcessorRegistrar.getProcessor(property).validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert properties != null;
		
		boolean usesTransient = false;
		for (RawProperty raw : properties.getRawProperties()) {
			Expression property = properties.getParsedProperty(raw);
			usesTransient |= ProcessorRegistrar.getProcessor(property).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
