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

import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.operator.OperatorAddInverse;
import epmc.value.operator.OperatorEq;

public enum OperatorEvaluatorSgn implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorSgn.SGN;
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
        if (!TypeAlgebra.isAlgebra(types[0])) {
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
        return TypeInteger.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        assert operands[0] != null;
        Value zero = TypeInteger.get().getZero();
        Value one = TypeInteger.get().getOne();
        OperatorEvaluator addInverse = ContextValue.get().getOperatorEvaluator(OperatorAddInverse.ADD_INVERSE, TypeInteger.get());
        OperatorEvaluator eq = ContextValue.get().getOperatorEvaluator(OperatorEq.EQ, operands[0].getType(), zero.getType());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        eq.apply(cmp, operands[0], zero);
        if (cmp.getBoolean()) {
            result.set(zero);
        } else if (ValueAlgebra.asAlgebra(operands[0]).isGt(zero)) {
            result.set(one);
        } else if (ValueAlgebra.asAlgebra(operands[0]).isLt(zero)) {
            addInverse.apply(ValueAlgebra.asAlgebra(result), one);
        } else {
            assert false;
        }
    }
}
