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
import epmc.value.Type;
import epmc.value.Value;

public enum OperatorEvaluatorSqrt implements OperatorEvaluator {
	INSTANCE;

	@Override
	public Operator getOperator() {
		return OperatorSqrt.SQRT;
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
		for (Type type : types) {
			if (!TypeDouble.isDouble(type)) {
				return false;
			}
		}
		return true;
	}

    @Override
    public Type resultType(Operator operator, Type... types) {
    	assert operator != null;
    	assert operator.equals(OperatorAddInverse.ADD_INVERSE);
    	assert types != null;
    	for (Type type : types) {
    		assert type != null;
    	}
        return TypeReal.get();
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	assert result != null;
    	assert operands != null;
    	for (Value operand : operands) {
    		assert operand != null;
    	}
    	double value1 = ValueDouble.isDouble(operands[0]) ? ValueDouble.asDouble(operands[0]).getDouble()
    			: ValueInteger.asInteger(operands[0]).getInt();
    	ValueDouble.asDouble(result).set(Math.sqrt(value1));
    }
}
