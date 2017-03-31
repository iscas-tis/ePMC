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

package epmc.expression.standard;

import epmc.error.EPMCException;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;

public class ExpressionIdentifierStandardProcessor implements JANI2PRISMProcessorStrict {

	private ExpressionIdentifierStandard identifier = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionIdentifierStandard; 
		
		identifier = (ExpressionIdentifierStandard) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert identifier != null;
		
		return new StringBuilder(JANIComponentRegistrar.getVariableNameByName(identifier.getName()));
	}
}
