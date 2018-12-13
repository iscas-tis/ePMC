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

public interface TypeInteger extends TypeNumber, TypeBounded, TypeNumBitsKnown {
    public static TypeInteger get(int lowerBound, int upperBound) {
        return ContextValue.get().makeUnique(new TypeIntegerJava(lowerBound, upperBound));
    }

    public static TypeInteger get() {
        return ContextValue.get().getType(TypeInteger.class);
    }

    public static void set(TypeInteger type) {
        assert type != null;
        ContextValue.get().setType(TypeInteger.class, ContextValue.get().makeUnique(type));
    }

    public static boolean is(Type type) {
        return type instanceof TypeInteger;
    }

    public static TypeInteger as(Type type) {
        if (type instanceof TypeInteger) {
            return (TypeInteger) type;
        } else {
            return null;
        }
    }

    public static boolean isIntegerBothBounded(Type type) {
        if (!is(type)) {
            return false;
        }
        TypeInteger typeInteger = as(type);
        return typeInteger.isLeftBounded() && typeInteger.isRightBounded();
    }

    public static boolean isIntegerWithBounds(Type type) {
        if (!is(type)) {
            return false;
        }
        TypeInteger typeInteger = as(type);
        return typeInteger.isLeftBounded() || typeInteger.isRightBounded();
    }

    @Override
    TypeArrayInteger getTypeArray();
    
    int getLowerInt();

    int getUpperInt();

    default boolean isLeftBounded() {
        return false;
    }

    default boolean isRightBounded() {
        return false;
    }
    
    ValueInteger newValue();
}
