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

import epmc.value.ContextValue;
import epmc.value.Type;

public interface TypeBoolean extends TypeEnumerable, TypeNumBitsKnown {
    public static boolean is(Type type) {
        return type instanceof TypeBoolean;
    }

    public static TypeBoolean as(Type type) {
        if (is(type)) {
            return (TypeBoolean) type;
        } else {
            return null;
        }
    }

    public static TypeBoolean get() {
        return ContextValue.get().getType(TypeBoolean.class);
    }

    public static void set(TypeBoolean type) {
        assert type != null;
        ContextValue.get().setType(TypeBoolean.class, ContextValue.get().makeUnique(type));
    }

    @Override
    ValueBoolean newValue();

    default ValueBoolean newValue(boolean i) {
        ValueBoolean value = newValue();
        value.set(i);
        return value;
    }

    @Override
    default int getNumBits() {
        return 1;
    }

    @Override
    default int getNumValues() {
        return 2;
    }

    @Override
    TypeArrayBoolean getTypeArray();
}
