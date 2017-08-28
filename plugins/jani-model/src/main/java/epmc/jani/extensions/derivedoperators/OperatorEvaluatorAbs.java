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

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeNumber;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.ValueNumber;

/**
 * Operator to compute absolute value of a value.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OperatorEvaluatorAbs implements OperatorEvaluator {
	INSTANCE;
	
	@Override
	public Operator getOperator() {
		return OperatorAbs.ABS;
	}
	
	@Override
	public boolean canApply(Type... types) {
		assert types != null;
		for (Type type : types) {
			assert type != null;
		}
		if (types.length != 1) {
			return false;
		}
		if (!TypeNumber.isNumber(types[0])) {
			return false;
		}
		return true;
	}

	@Override
	public Type resultType(Operator operator, Type... types) {
		assert operator != null;
		assert types != null;
		assert types.length >= 1;
		assert types[0] != null;
		return types[0];
	}

	@Override
	public void apply(Value result, Value... operands) {
		assert result != null;
		assert operands != null;
		assert operands.length >= 1;
		assert operands[0] != null;
		if (ValueDouble.isDouble(result)) {
			double value = ValueNumber.asNumber(operands[0]).getDouble();
			ValueDouble.asDouble(result).set(Math.abs(value));
		} else if (ValueInteger.isInteger(result)) {
			int value = ValueNumber.asNumber(operands[0]).getInt();
			ValueInteger.asInteger(result).set(Math.abs(value));			
		} else {
			assert false;
		}
	}
}
