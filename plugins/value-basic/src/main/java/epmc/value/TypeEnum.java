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

import java.util.Arrays;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;

public final class TypeEnum implements TypeEnumerable, TypeNumBitsKnown {
    public static TypeEnum get(Class<? extends Enum<?>> enumClass) {
        return ContextValue.get().makeUnique(new TypeEnum(enumClass));
    }

    public static boolean is(Type type) {
        return type instanceof TypeEnum;
    }
    public static TypeEnum as(Type type) {
        if (is(type)) {
            return (TypeEnum) type;
        } else {
            return null;
        }
    }

    private final Class<? extends Enum<?>> enumClass;
    private final int numBits;
    private final int numConstants;

    TypeEnum(Class<? extends Enum<?>> enumClass) {
        assert enumClass != null;
        this.enumClass = enumClass;
        numConstants = enumClass.getEnumConstants().length;
        numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numConstants - 1);
    }


    public Class<? extends Enum<?>> getEnumClass() {
        return enumClass;
    }

    @Override
    public ValueEnum newValue() {
        return new ValueEnum(this);
    }

    public Value newValue(Enum<?> enumConst) {
        assert enumConst != null;
        Value value = newValue();
        ValueEnum.as(value).set(enumConst);
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TypeEnum other = (TypeEnum) obj;
        if (this.enumClass != other.enumClass) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = enumClass.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("enum(");
        builder.append(Arrays.toString(enumClass.getEnumConstants()));
        builder.append(")");
        return builder.toString();
    }

    @Override
    public int getNumBits() {
        return numBits;
    };

    @Override
    public int getNumValues() {
        return numConstants;
    }

    @Override
    public TypeArray getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayEnum(this));
    }
}
