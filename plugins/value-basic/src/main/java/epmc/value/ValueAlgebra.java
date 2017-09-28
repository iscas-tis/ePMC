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

package epmc.value;

import epmc.value.Value;
import epmc.value.operator.OperatorEq;

public interface ValueAlgebra extends Value {
    @Override
    TypeAlgebra getType();

    static boolean isAlgebra(Value value) {
        return value instanceof ValueAlgebra;
    }

    static ValueAlgebra asAlgebra(Value value) {
        if (isAlgebra(value)) {
            return (ValueAlgebra) value;
        } else {
            return null;
        }
    }

    void set(int value);

    void add(Value operand1, Value operand2);

    void multiply(Value operand1, Value operand2);

    boolean isZero();

    boolean isOne();

    boolean isPosInf();

    boolean isNegInf();

    // TODO move?
    default boolean isLt(Value other) {
        assert false : this.getType() + " " + other.getType();
        return false;
    }

    // TODO move?
    default boolean isLe(Value other) {
        OperatorEvaluator eq = ContextValue.get().getOperatorEvaluator(OperatorEq.EQ, getType(), other.getType());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        eq.apply(cmp, this, other);
        return isLt(other) || cmp.getBoolean();
    }

    // TODO move?
    default boolean isGe(Value other) {
        return ValueAlgebra.asAlgebra(other).isLe(this);
    }

    // TODO move?
    default boolean isGt(Value other) {
        return ValueAlgebra.asAlgebra(other).isLt(this);
    }    
}
