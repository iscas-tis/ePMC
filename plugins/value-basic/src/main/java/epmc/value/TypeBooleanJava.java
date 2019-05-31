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

public final class TypeBooleanJava implements TypeBoolean {
    private final static String BOOL = "bool";
    
    public static boolean is(Type type) {
        return type instanceof TypeBooleanJava;
    }

    public static TypeBooleanJava as(Type type) {
        if (is(type)) {
            return (TypeBooleanJava) type;
        } else {
            return null;
        }
    }

    public static TypeBooleanJava get() {
        return ContextValue.get().getType(TypeBooleanJava.class);
    }

    public static void set(TypeBooleanJava type) {
        assert type != null;
        ContextValue.get().setType(TypeBooleanJava.class, ContextValue.get().makeUnique(type));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(BOOL);
        return builder.toString();
    }

    @Override
    public ValueBoolean newValue() {
        return new ValueBooleanJava(this);
    }

    public ValueBoolean newValue(boolean i) {
        ValueBoolean result = newValue();
        result.set(i);
        return result;
    }

    @Override
    public int getNumBits() {
        return 1;
    }

    @Override
    public int getNumValues() {
        return 2;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public TypeArrayBoolean getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayBoolean(this));
    }
}
