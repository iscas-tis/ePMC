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

package epmc.prism.exporter.operatorprocessor;

import epmc.operator.Operator;

public interface JANI2PRISMOperatorProcessorStrict {	
    default JANI2PRISMOperatorProcessorStrict setPrefix(String prefix) { return this; };

    default JANI2PRISMOperatorProcessorStrict setForDefinition(boolean forDefinition) { return this; };

    JANI2PRISMOperatorProcessorStrict setOperatorElement(Operator operator, Object obj);

    /**
     * Generate a PRISM representation of the component.
     * @return the PRISM representation
     */
    String toPRISM();
}
