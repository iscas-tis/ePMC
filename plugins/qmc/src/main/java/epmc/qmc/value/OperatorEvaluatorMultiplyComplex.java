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
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSubtract;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorMultiplyComplex implements OperatorEvaluator {
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
            for (Type type : types) {
                assert type != null;
            }
            built = true;
            if (operator != OperatorMultiply.MULTIPLY) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            for (Type type : types) {
                if (!TypeComplex.is(type)
                        && !TypeReal.is(type)
                        && !TypeInteger.is(type)) {
                    return null;
                }
            }
            if (!TypeComplex.is(types[0]) && !TypeComplex.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorMultiplyComplex(this);
        }
    }

    private final ValueReal accAC;
    private final ValueReal accBD;
    private final ValueReal accAD;
    private final ValueReal accBC;
    private final OperatorEvaluator add;
    private final OperatorEvaluator multiply;
    private final OperatorEvaluator subtract;
    
    private OperatorEvaluatorMultiplyComplex(Builder builder) {
        accAC = TypeReal.get().newValue();
        accBD = TypeReal.get().newValue();
        accAD = TypeReal.get().newValue();
        accBC = TypeReal.get().newValue();
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeReal.get(), TypeReal.get());
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeReal.get(), TypeReal.get());
        subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
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
        multiply.apply(accAC, getRealPart(operands[0]), getRealPart(operands[1]));
        multiply.apply(accBD, getImagPart(operands[0]), getImagPart(operands[1]));
        multiply.apply(accAD, getRealPart(operands[0]), getImagPart(operands[1]));
        multiply.apply(accBC, getImagPart(operands[0]), getRealPart(operands[1]));
        subtract.apply(getRealPart(result), accAC, accBD);
        add.apply(getImagPart(result), accAD, accBC);
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
