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

package epmc.value;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;

/**
 * Operator to compute absolute value of a value.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OperatorEvaluatorExp implements OperatorEvaluator {
	INSTANCE;

	@Override
	public Operator getOperator() {
		return OperatorExp.EXP;
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
		if (!TypeReal.isReal(types[0]) && !TypeInteger.isInteger(types[0])) {
			return false;
		}
		return true;
	}
	
	@Override
	public TypeReal resultType(Operator operator, Type... types) {
		assert operator != null;
		assert types != null;
		assert types.length >= 1;
		assert types[0] != null;
		return TypeReal.get();
	}
	
	@Override
	public void apply(Value result, Value... operands) throws EPMCException {
		assert result != null;
		assert operands != null;
		assert operands.length >= 1;
		assert operands[0] != null;
    	double value1 = ValueDouble.isDouble(operands[0]) ? ValueDouble.asDouble(operands[0]).getDouble()
    			: ValueInteger.asInteger(operands[0]).getInt();
		ValueDouble.asDouble(result).set(Math.exp(value1));
	}
}
