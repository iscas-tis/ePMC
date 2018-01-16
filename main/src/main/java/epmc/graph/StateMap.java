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

package epmc.graph;

import java.io.Closeable;

import epmc.operator.Operator;
import epmc.value.Type;
import epmc.value.Value;

// TODO complete documentation

public interface StateMap extends Closeable, Cloneable {
    @Override
    void close();

    Type getType();

    int size();

    StateSet getStateSet();

    StateMap restrict(StateSet to);

    StateMap apply(Operator operator, StateMap other);

    StateMap clone();

    Value applyOver(Operator operator, StateSet over);

    boolean isConstant();

    void getRange(Value range, StateSet of);

    void getSomeValue(Value to, StateSet of);

    default void getSomeValue(Value to) {
        assert to != null;
        getSomeValue(to, getStateSet());
    }

    default StateMap applyWith(Operator operator, StateMap operand) {
        StateMap result = apply(operator, operand);
        close();
        operand.close();
        return result;
    }

    default Value getSomeValue() {
        Value value = getType().newValue();
        getSomeValue(value);
        return value;
    }

    Value subsumeResult(StateSet initialStates);

    default Scheduler getScheduler() {
        return null;
    }
}
