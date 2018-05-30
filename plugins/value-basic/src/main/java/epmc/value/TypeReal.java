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

public interface TypeReal extends TypeNumber, TypeWeight, TypeWeightTransition {
    static TypeReal get() {
        return ContextValue.get().getType(TypeReal.class);
    }

    static void set(TypeReal type) {
        assert type != null;
        ContextValue.get().setType(TypeReal.class, type);
    }

    static boolean is(Type type) {
        return type instanceof TypeReal;
    }

    static TypeReal as(Type type) {
        if (is(type)) {
            return (TypeReal) type;
        } else {
            return null;
        }
    }

    @Override
    TypeArrayReal getTypeArray();

    @Override
    ValueReal newValue();
}
