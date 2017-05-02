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

// TODO check whether the type is indeed used, or should indeed be used

public final class TypeUnknown implements Type {
    public static TypeUnknown get() {
        return ContextValue.get().getType(TypeUnknown.class);
    }
    
    public static void set(TypeUnknown type) {
        assert type != null;
        ContextValue context = ContextValue.get();
        context.setType(TypeUnknown.class, context.makeUnique(type));
    }

    public static boolean isUnknown(Type type) {
        return type instanceof TypeUnknown;
    }

    public TypeUnknown() {
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Type other = (Type) obj;
        if (!canImport(other) || !other.canImport(this)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public String toString() {
        return "unknown";
    }

    @Override
    public Value newValue() {
        assert false;
        return null;
    }
    
    public TypeArray getTypeArray() {
        assert false;
        return null;
    }

    @Override
    public boolean canImport(Type type) {
        assert type != null;
        if (this == type) {
            return true;
        }
        return false;
    }
}
