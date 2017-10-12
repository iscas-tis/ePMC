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

public interface ValueEnumerable extends Value {
    static boolean is(Value value) {
        if (!(value instanceof ValueEnumerable)) {
            return false;
        }
        ValueEnumerable valueEnumerable = (ValueEnumerable) value;
        if (valueEnumerable.getType().getNumValues() == TypeEnum.UNBOUNDED_VALUES) {
            return false;
        }
        return true;
    }

    static ValueEnumerable as(Value value) {
        if (is(value)) {
            return (ValueEnumerable) value;
        } else {
            return null;
        }
    }

    @Override
    TypeEnumerable getType();

    int getValueNumber();

    void setValueNumber(int number);
}
