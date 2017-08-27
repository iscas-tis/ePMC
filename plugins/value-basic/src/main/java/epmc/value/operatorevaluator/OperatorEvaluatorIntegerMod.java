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

package epmc.value.operatorevaluator;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operator.OperatorMod;

public enum OperatorEvaluatorIntegerMod implements OperatorEvaluator {
	INSTANCE;

	@Override
	public Operator getOperator() {
		return OperatorMod.MOD;
	}
	
	@Override
	public boolean canApply(Type... types) {
		assert types != null;
		for (Type type : types) {
			assert type != null;
		}
		if (types.length != 2) {
			return false;
		}
		for (Type type : types) {
			if (!TypeInteger.isInteger(type)) {
				return false;
			}
		}
		return true;
	}

    @Override
    public Type resultType(Operator operator, Type... types) {
    	assert operator != null;
    	assert operator.equals(OperatorMod.MOD);
    	assert types != null;
    	for (Type type : types) {
    		assert type != null;
    	}
        Type result;
        if (!TypeInteger.isInteger(types[0]) || !TypeInteger.isInteger(types[1])) {
            return null;
        } else {
            result = TypeInteger.get();
        }
        return result;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	assert result != null;
    	assert operands != null;
    	for (Value operand : operands) {
    		assert operand != null;
    	}
    	int intOp1 = ValueInteger.asInteger(operands[0]).getInt();
    	int intOp2 = ValueInteger.asInteger(operands[1]).getInt();
        int resultInt = (((intOp1 % intOp2)) + intOp2) % intOp2;
        ValueInteger.asInteger(result).set(resultInt);
    }
}