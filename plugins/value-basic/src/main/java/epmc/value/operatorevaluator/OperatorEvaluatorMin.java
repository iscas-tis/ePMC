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

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.ProblemsValueBasic;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operator.OperatorMin;

public enum OperatorEvaluatorMin implements OperatorEvaluator {
	INSTANCE;

	@Override
	public Operator getOperator() {
		return OperatorMin.MIN;
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
			if (!TypeAlgebra.isAlgebra(type)) {
				return false;
			}
		}
		return true;
	}

    @Override
    public Type resultType(Operator operator, Type... types) {
    	assert operator != null;
    	assert operator.equals(OperatorMin.MIN);
    	assert types != null;
    	for (Type type : types) {
    		assert type != null;
    	}
        return UtilValue.algebraicResultType(types);
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	assert result != null;
    	assert operands != null;
    	for (Value operand : operands) {
    		assert operand != null;
    	}
    	Value operand1 = operands[0];
    	Value operand2 = operands[1];
        if (ValueAlgebra.asAlgebra(operand1).isLt(operand2)) {
            result.set(operand1);
        } else if (ValueAlgebra.asAlgebra(operand2).isLt(operand1)) {
        	result.set(operand2);
        } else if (operand2.isEq(operand1)) {
        	result.set(operand1);
        } else {
            ensure(false, ProblemsValueBasic.VALUES_INCOMPARABLE);
        }
    }
}
