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

package epmc.time;

import epmc.error.EPMCException;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;

public final class JANITypeClockProcessor implements JANI2PRISMProcessorStrict {
	
	private JANITypeClock clock = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj instanceof JANITypeClock;

		clock = (JANITypeClock) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert clock != null;
		
		return new StringBuilder("clock");
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert clock != null;
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert clock != null;
		
		return false;
	}	
}
