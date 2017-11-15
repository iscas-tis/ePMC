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

package epmc.prism.value;

import epmc.operator.Operator;
import epmc.prism.operator.OperatorPRISMPow;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorPRISMPow implements OperatorEvaluator {
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
            if (operator != OperatorPRISMPow.PRISM_POW) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeInteger.is(types[0]) && !TypeDouble.is(types[0])) {
                return null;
            }
            if (!TypeInteger.is(types[1]) && !TypeDouble.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorPRISMPow(this);
        }
    }

    private final Type resultType;

    private OperatorEvaluatorPRISMPow(Builder builder) {
        boolean allInteger = true;
        for (Type type : builder.types) {
            allInteger &= TypeInteger.is(type);
        }
        if (allInteger) {
            resultType = TypeInteger.get();
        } else {
            resultType = TypeReal.get();
        }

    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        if (ValueInteger.is(result)) {
            int value1 = ValueInteger.as(operands[0]).getInt();
            int value2 = ValueInteger.as(operands[1]).getInt();
            ValueInteger.as(result).set((int) Math.pow(value1, value2));
        } else if (ValueReal.is(result)) {
            double value1 = ValueDouble.is(operands[0]) ? ValueDouble.as(operands[0]).getDouble()
                    : ValueInteger.as(operands[0]).getInt();
            double value2 = ValueDouble.is(operands[1]) ? ValueDouble.as(operands[1]).getDouble()
                    : ValueInteger.as(operands[1]).getInt();
            ValueDouble.as(result).set(Math.pow(value1, value2));
        } else {
            assert false : result.getType();
        }
    }
}
