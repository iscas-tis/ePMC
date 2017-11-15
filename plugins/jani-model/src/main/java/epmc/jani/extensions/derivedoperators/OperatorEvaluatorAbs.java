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
import epmc.operator.OperatorAbs;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeNumber;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.ValueNumber;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

/**
 * Operator to compute absolute value of a value.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorEvaluatorAbs implements OperatorEvaluator {
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
            if (operator != OperatorAbs.ABS) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeNumber.is(types[0])) {
                return null;
            }
            return new OperatorEvaluatorAbs(this);
        }
    }

    private final Type resultType;

    private OperatorEvaluatorAbs(Builder builder) {
        this.resultType = builder.types[0];
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        assert operands[0] != null;
        if (ValueDouble.is(result)) {
            double value = ValueNumber.as(operands[0]).getDouble();
            ValueDouble.as(result).set(Math.abs(value));
        } else if (ValueInteger.is(result)) {
            int value = ValueNumber.as(operands[0]).getInt();
            ValueInteger.as(result).set(Math.abs(value));			
        } else {
            assert false;
        }
    }
}
