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
import epmc.operator.OperatorId;
import epmc.operator.OperatorSet;
import epmc.value.EvaluatorCache;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;

public final class OperatorEvaluatorId implements OperatorEvaluator {
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
            if (operator != OperatorId.ID) {
                return null;
            }
            if (types.length < 1) {
                return null;
            }
            return new OperatorEvaluatorId(this);
        }
    }

    private final EvaluatorCache evaluatorCache = new EvaluatorCache();
    private final Type resultType;
    
    private OperatorEvaluatorId(Builder builder) {
        resultType = builder.types[0];
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
        OperatorEvaluator set = evaluatorCache.getEvaluator(OperatorSet.SET, operands[0].getType(), result.getType());
        set.apply(result, operands[0]);
    }
}
