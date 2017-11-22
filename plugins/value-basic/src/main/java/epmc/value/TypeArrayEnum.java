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
import epmc.value.TypeArray;

final class TypeArrayEnum implements TypeArray {
    private final static String ARRAY_INDICATOR = "[](enum)";
    private final Class<? extends Enum<?>> type;
    private final Enum<?>[] constants;
    private final TypeEnum entryType;

    TypeArrayEnum(TypeEnum entryType) {
        assert entryType != null;
        this.entryType = entryType;
        type = entryType.getEnumClass();
        constants = type.getEnumConstants();
    }

    Enum<?>[] getConstants() {
        return constants;
    }

    @Override
    public ValueArrayEnum newValue() {
        return new ValueArrayEnum(this);
    }

    @Override
    public TypeEnum getEntryType() {
        return entryType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeArrayEnum)) {
            return false;
        }
        TypeArrayEnum other = (TypeArrayEnum) obj;
        return this.getEntryType().equals(other.getEntryType());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = getEntryType().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEntryType());
        builder.append(ARRAY_INDICATOR);
        return builder.toString();
    }

    @Override
    public TypeArray getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }
}
