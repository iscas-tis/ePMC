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

import epmc.value.Type;

public interface TypeBounded extends TypeAlgebra {
    static boolean is(Type type) {
        return type instanceof TypeBounded;
    }

    static  TypeBounded as(Type type) {
        if (is(type)) {
            return (TypeBounded) type;
        } else {
            return null;
        }
    }

    static ValueAlgebra getLower(Type type) {
        TypeBounded typeBounded = as(type);
        if (typeBounded != null) {
            return typeBounded.getLower();
        } else {
            return null;
        }
    }

    static ValueAlgebra getUpper(Type type) {
        TypeBounded typeBounded = as(type);
        if (typeBounded != null) {
            return typeBounded.getUpper();
        } else {
            return null;
        }
    }

    default ValueAlgebra getLower() {
        return null;
    }

    default ValueAlgebra getUpper() {
        return null;
    }
}
