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

public interface ValueBoolean extends ValueEnumerable, ValueBitStoreable, ValueSetString {
    public static boolean is(Value value) {
        return value instanceof ValueBoolean;
    }

    public static ValueBoolean as(Value value) {
        if (is(value)) {
            return (ValueBoolean) value;
        } else {
            return null;
        }
    }

    public static boolean isTrue(Value value) {
        ValueBoolean valueBoolean = as(value);
        if (valueBoolean == null) {
            return false;
        }
        if (valueBoolean.getBoolean()) {
            return true;
        }
        return false;
    }

    public static boolean isFalse(Value value) {
        ValueBoolean valueBoolean = as(value);
        if (valueBoolean == null) {
            return false;
        }
        if (!valueBoolean.getBoolean()) {
            return true;
        }
        return false;
    }

    boolean getBoolean();

    void set(boolean value);

    @Override
    TypeBoolean getType();

    @Override
    default int getValueNumber() {
        return getBoolean() ? 1 : 0;
    }

    @Override
    default void setValueNumber(int number) {
        assert number >= 0 : number;
        assert number < 2 : number;
        set(number == 1);
    }
}
