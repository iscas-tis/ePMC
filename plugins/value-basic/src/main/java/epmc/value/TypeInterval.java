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

public final class TypeInterval implements TypeWeightTransition, TypeWeight {
    public static boolean is(Type type) {
        return type instanceof TypeInterval;
    }

    public static TypeInterval as(Type type) {
        if (type instanceof TypeInterval) {
            return (TypeInterval) type;
        } else {
            return null;
        }
    }

    private final TypeReal typeReal;

    public static TypeInterval get() {
        return ContextValue.get().getType(TypeInterval.class);
    }

    public static void set(TypeInterval type) {
        assert type != null;
        ContextValue.get().setType(TypeInterval.class,
                ContextValue.get().makeUnique(type));
    }
    
    public TypeInterval(TypeReal typeReal) {
        assert typeReal != null;
        this.typeReal = typeReal;
    }

    public TypeInterval() {
        this(TypeReal.get());
    }

    @Override
    public ValueInterval newValue() {
        return new ValueInterval(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("interval[");
        builder.append(typeReal);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TypeInterval other = (TypeInterval) obj;
        if (!this.typeReal.equals(other.typeReal)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = typeReal.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    public TypeReal getEntryType() {
        return typeReal;
    }

    @Override
    public TypeArrayInterval getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayInterval(this));
    }
}
