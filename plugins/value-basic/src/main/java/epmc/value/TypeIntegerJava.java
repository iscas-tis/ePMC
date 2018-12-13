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

public final class TypeIntegerJava implements TypeInteger, TypeEnumerable {
    public static TypeIntegerJava get(int lowerBound, int upperBound) {
        return ContextValue.get().makeUnique(new TypeIntegerJava(lowerBound, upperBound));
    }

    public static TypeIntegerJava get() {
        return ContextValue.get().getType(TypeIntegerJava.class);
    }

    public static void set(TypeIntegerJava type) {
        assert type != null;
        ContextValue.get().setType(TypeIntegerJava.class, ContextValue.get().makeUnique(type));
    }

    public static boolean is(Type type) {
        return type instanceof TypeIntegerJava;
    }

    public static TypeIntegerJava as(Type type) {
        if (type instanceof TypeIntegerJava) {
            return (TypeIntegerJava) type;
        } else {
            return null;
        }
    }

    public static boolean isIntegerBothBounded(Type type) {
        if (!is(type)) {
            return false;
        }
        TypeIntegerJava typeInteger = as(type);
        return typeInteger.isLeftBounded() && typeInteger.isRightBounded();
    }

    public static boolean isIntegerWithBounds(Type type) {
        if (!is(type)) {
            return false;
        }
        TypeIntegerJava typeInteger = as(type);
        return typeInteger.isLeftBounded() || typeInteger.isRightBounded();
    }

    private final ValueInteger lowerBound;
    private final ValueInteger upperBound;
    private final ValueInteger valueMax = new ValueIntegerJava(this);
    private final int numBits;

    public TypeIntegerJava(int lowerBound, int upperBound) {
        valueMax.set(Integer.MAX_VALUE);
        assert lowerBound <= upperBound;
        if (lowerBound != Integer.MIN_VALUE && upperBound != Integer.MAX_VALUE) {
            int numValues = upperBound - lowerBound + 1;
            numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1);
        } else {
            numBits = Integer.SIZE;
        }
        this.lowerBound = newValue();
        this.lowerBound.set(lowerBound);
        this.upperBound = newValue();
        this.upperBound.set(upperBound);
    }

    public TypeIntegerJava() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }    

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isLeftBounded() || isRightBounded()) {
            builder.append("[");
            builder.append(isLeftBounded() ? lowerBound : "-inf");
            builder.append("..");
            builder.append(isRightBounded() ? upperBound : "inf");
            builder.append("]");
        } else {
            builder.append("int");
        }
        return builder.toString();
    }

    @Override
    public ValueIntegerJava newValue() {
        if (isLeftBounded()) {
            ValueIntegerJava result = new ValueIntegerJava(this);
            result.set(lowerBound.getInt());
            return result;
        } else {
            return new ValueIntegerJava(this);
        }
    }

    public int getLowerInt() {
        return lowerBound.getInt();
    }

    public int getUpperInt() {
        return upperBound.getInt();
    }

    @Override
    public ValueInteger getLower() {
        return lowerBound;
    }

    @Override
    public ValueInteger getUpper() {
        return upperBound;
    }

    public boolean isLeftBounded() {
        return lowerBound != null && lowerBound.getInt() != Integer.MIN_VALUE;
    }

    public boolean isRightBounded() {
        return upperBound != null && upperBound.getInt() != Integer.MAX_VALUE;
    }

    @Override
    public int getNumBits() {
        return numBits;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TypeIntegerJava other = (TypeIntegerJava) obj;
        if (!this.lowerBound.equals(other.lowerBound)) {
            return false;
        }
        if (!this.upperBound.equals(other.upperBound)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = lowerBound.getInt() + (hash << 6) + (hash << 16) - hash;
        hash = upperBound.getInt() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public int getNumValues() {
        if (!TypeIntegerJava.isIntegerBothBounded(this)) {
            return Integer.MAX_VALUE;
        }
        return getUpperInt() + 1 - getLowerInt();
    }

    @Override
    public TypeArrayInteger getTypeArray() {
        if (TypeIntegerJava.isIntegerBothBounded(this)) {
            return ContextValue.get().makeUnique(new TypeArrayIntegerBounded(this));
        } else {
            return ContextValue.get().makeUnique(new TypeArrayIntegerJava(this));            
        }
    }    
}
