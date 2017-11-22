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

import epmc.operator.Operator;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class OperatorEvaluatorIte implements OperatorEvaluator {
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
            if (operator != OperatorIte.ITE) {
                return null;
            }
            if (types.length != 3) {
                return null;
            }
            if (!TypeBoolean.is(types[0])) {
                return null;
            }
            return new OperatorEvaluatorIte(this);
        }
    }

    private final OperatorEvaluator setIf;
    private final OperatorEvaluator setElse;
    private final Type resultType;

    private OperatorEvaluatorIte(Builder builder) {
        resultType = UtilValue.upper(builder.types[1], builder.types[2]);
        setIf = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[1], resultType);
        setElse = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[2], resultType);
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        if (ValueBoolean.as(operands[0]).getBoolean()) {
            setIf.apply(result, operands[1]);
        } else {
            setElse.apply(result, operands[2]);
        }
    }
}
