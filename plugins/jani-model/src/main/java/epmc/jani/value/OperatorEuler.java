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

package epmc.jani.value;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.Value;

/**
 * Operator representing the constant π (Pi).
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorEuler implements Operator {
	/** Identifier of the operator. */
	public final static String IDENTIFIER = "e"; //"π";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void apply(Value result, Value... operands) throws EPMCException {
		assert result != null;
		assert operands != null;
		assert operands.length == 0;
		// TODO
//		ValueReal.asReal(result).pi();
	}

	@Override
	public Type resultType(Type... types) {
		assert types != null;
		assert types.length == 0;
		return TypeReal.get();
	}
}
