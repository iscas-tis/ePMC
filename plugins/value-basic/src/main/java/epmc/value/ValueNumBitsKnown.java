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

public interface ValueNumBitsKnown extends Value {
    static boolean is(Value value) {
        if (!(value instanceof ValueNumBitsKnown)) {
            return false;
        }
        ValueNumBitsKnown valueNumBitsKnown = (ValueNumBitsKnown) value;
        if (valueNumBitsKnown.getNumBits() == TypeNumBitsKnown.UNKNOWN) {
            return false;
        }
        return true;
    }

    static ValueNumBitsKnown as(Value value) {
        if (is(value)) {
            return (ValueNumBitsKnown) value;
        } else {
            return null;
        }
    }

    static int getNumBits(Value value) {
        ValueNumBitsKnown valueNumBitsKnown = ValueNumBitsKnown.as(value);
        if (valueNumBitsKnown != null) {
            return valueNumBitsKnown.getNumBits();
        } else {
            return TypeNumBitsKnown.UNKNOWN;
        }
    }

    default int getNumBits() {
        return TypeNumBitsKnown.getNumBits(getType());
    }
}
