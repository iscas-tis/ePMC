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

package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorAddInverseComplex implements OperatorEvaluator {
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
            if (operator != OperatorAddInverse.ADD_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            for (Type type : types) {
                if (!TypeComplex.is(type)) {
                    return null;
                }
            }
            return new OperatorEvaluatorAddInverseComplex(this);
        }
    }

    private final OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());

    private OperatorEvaluatorAddInverseComplex(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeComplex.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        Value opReal = getRealPart(operands[0]);
        Value opImag = getImagPart(operands[0]);
        addInverse.apply(getRealPart(result), opReal);
        addInverse.apply(getImagPart(result), opImag);
    }

    private static Value getRealPart(Value value) {
        if (ValueComplex.is(value)) {
            return ValueComplex.as(value).getRealPart();
        } else if (ValueReal.is(value) || ValueInteger.is(value)) {
            return value;
        } else {
            assert false : value;
        return null;
        }
    }

    private static Value getImagPart(Value value) {
        if (ValueComplex.is(value)) {
            return ValueComplex.as(value).getImagPart();
        } else if (ValueReal.is(value)) {
            return UtilValue.newValue(ValueReal.as(value).getType(), 0);
        } else if (ValueInteger.is(value)) {
            return UtilValue.newValue(ValueInteger.as(value).getType(), 0);
        } else {
            assert false : value;
        return null;
        }
    }
}
