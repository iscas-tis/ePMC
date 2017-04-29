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

package epmc.jani.model.type;

import epmc.error.EPMCException;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;

public final class JANITypeBoolProcessor implements JANI2PRISMProcessorStrict {
	
	private JANITypeBool bool = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) throws EPMCException {
		assert obj instanceof JANITypeBool;

		bool = (JANITypeBool) obj;
		return this;
	}

	@Override
	public String toPRISM() throws EPMCException {
		assert bool != null;
		
		return "bool";
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert bool != null;
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert bool != null;
		
		return false;
	}	
}
