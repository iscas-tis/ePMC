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

import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSgn implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private boolean built;
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            assert !built;
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            assert operator != null;
            assert types != null;
            built = true;
            for (Type type : types) {
                assert type != null;
            }
            if (operator != OperatorSgn.SGN) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeAlgebra.is(types[0])) {
                return null;
            }

            return new OperatorEvaluatorSgn(this);
        }
    }

    private OperatorEvaluatorSgn(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeInteger.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        assert operands[0] != null;
        Value zero = UtilValue.newValue(TypeInteger.get(), 0);
        Value one = UtilValue.newValue(TypeInteger.get(), 1);
        OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeInteger.get());
        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, operands[0].getType(), zero.getType());
        OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, operands[0].getType(), zero.getType());
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, operands[0].getType(), zero.getType());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        eq.apply(cmp, operands[0], zero);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, zero.getType(), result.getType());
        if (cmp.getBoolean()) {
            set.apply(result, zero);
            return;
        }
        lt.apply(cmp, operands[0], zero);
        if (cmp.getBoolean()) {
            set.apply(result, one);
            return;
        }
        gt.apply(cmp, operands[0], zero);
        if (cmp.getBoolean()) {
            addInverse.apply(ValueAlgebra.as(result), one);
            return;
        }
        assert false;
    }
}
