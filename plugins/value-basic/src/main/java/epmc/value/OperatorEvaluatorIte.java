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

public enum OperatorEvaluatorIte implements OperatorEvaluator {
	INSTANCE;

	@Override
	public boolean canApply(String operator, Type... types) {
		assert operator != null;
		assert types != null;
		for (Type type : types) {
			assert type != null;
		}
		if (!operator.equals(OperatorIte.IDENTIFIER)) {
			return false;
		}
		if (types.length != 3) {
			return false;
		}
		if (!TypeBoolean.isBoolean(types[0])) {
			return false;
		}
		return true;
	}

    @Override
    public Type resultType(String operator, Type... types) {
    	assert operator != null;
    	assert operator.equals(OperatorIte.IDENTIFIER);
        assert types != null;
    	for (Type type : types) {
    		assert type != null;
    	}
        if (!TypeBoolean.isBoolean(types[0])) {
            return null;
        }
        Type itUpper = UtilValue.upper(types[1], types[2]);
        if (itUpper == null) {
            return null;
        }
        Type result = itUpper;
        return result;
    }

    @Override
    public void apply(Value result, String operator, Value... operands) throws EPMCException {
    	assert result != null;
    	assert operator != null;
    	assert operator.equals(OperatorIte.IDENTIFIER);
    	assert operands != null;
    	for (Value operand : operands) {
    		assert operand != null;
    	}
    	if (ValueBoolean.asBoolean(operands[0]).getBoolean()) {
    		result.set(operands[1]);
    	} else {
    		result.set(operands[2]);    		
    	}
    }
}
