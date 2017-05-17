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
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

public enum OperatorEvaluatorSgn implements OperatorEvaluator {
	INSTANCE;

	@Override
	public boolean canApply(String operator, Type... types) {
		assert operator != null;
		assert types != null;
		for (Type type : types) {
			assert type != null;
		}
		if (!operator.equals(OperatorSgn.IDENTIFIER)) {
			return false;
		}
		if (types.length != 1) {
			return false;
		}
		if (!TypeAlgebra.isAlgebra(types[0])) {
			return false;
		}
		return true;
	}
	
	@Override
	public Type resultType(String operator, Type... types) {
		assert operator != null;
		assert types != null;
		assert types.length >= 1;
		assert types[0] != null;
		return TypeInteger.get();
	}
	
	@Override
	public void apply(Value result, Value... operands) throws EPMCException {
		assert result != null;
		assert operands != null;
		assert operands.length >= 1;
		assert operands[0] != null;
		Value zero = TypeInteger.get().getZero();
		Value one = TypeInteger.get().getOne();
		if (operands[0].isEq(zero)) {
			result.set(zero);
		} else if (ValueAlgebra.asAlgebra(operands[0]).isGt(zero)) {
			result.set(one);
		} else if (ValueAlgebra.asAlgebra(operands[0]).isLt(zero)) {
			ValueAlgebra.asAlgebra(result).addInverse(one);
		} else {
			assert false;
		}
	}
}
