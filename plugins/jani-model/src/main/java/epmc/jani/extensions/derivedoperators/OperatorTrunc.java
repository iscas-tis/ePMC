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

package epmc.jani.extensions.derivedoperators;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueNumber;

/**
 * Operator to truncate a real to an integer.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorTrunc implements Operator {
	/** Identifier of the operator. */
	public final static String IDENTIFIER = "trunc";

	@Override
	public void apply(Value result, Value... operands) throws EPMCException {
		assert result != null;
		assert operands != null;
		assert operands.length >= 1;
		assert operands[0] != null;
		ValueAlgebra.asAlgebra(result).set(ValueNumber.asNumber(operands[0]).intcastInt());
	}

	@Override
	public Type resultType(Type... types) {
		assert types != null;
		assert types.length >= 1;
		assert types[0] != null;
		return TypeInteger.get();
	}
}
