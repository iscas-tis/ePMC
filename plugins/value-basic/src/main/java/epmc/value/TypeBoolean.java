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
import epmc.value.TypeArray;

public final class TypeBoolean implements TypeEnumerable, TypeNumBitsKnown {
    private final ContextValue context;
    private final ValueBoolean valueFalse = new ValueBoolean(this, false);
    private final ValueBoolean valueTrue = new ValueBoolean(this, true);

    public static boolean isBoolean(Type type) {
    	return type instanceof TypeBoolean;
    }
    
    public static TypeBoolean asBoolean(Type type) {
    	if (isBoolean(type)) {
    		return (TypeBoolean) type;
    	} else {
    		return null;
    	}
    }
    
    public static TypeBoolean get(ContextValue context) {
        assert context != null;
        return context.getType(TypeBoolean.class);
    }
    
    public static void set(TypeBoolean type) {
        assert type != null;
        ContextValue context = type.getContext();
        context.setType(TypeBoolean.class, context.makeUnique(type));
    }

    public TypeBoolean(ContextValue context) {
        assert context != null;
        this.context = context;
        valueFalse.setImmutable();
        valueTrue.setImmutable();
    }
    
    @Override
    public boolean canImport(Type a) {
        assert a != null;
        return TypeBoolean.isBoolean(a) || TypeTernary.isTernary(a);
    }

    public ValueBoolean getFalse() {
        return valueFalse;
    }
    
    public ValueBoolean getTrue() {
        return valueTrue;
    }
        
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("bool");
        return builder.toString();
    }

    @Override
    public ValueBoolean newValue() {
        return new ValueBoolean(this);
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
    public ContextValue getContext() {
        return context;
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
        Type other = (Type) obj;
        if (this.getContext() != other.getContext()) {
            return false;
        }
        if (!canImport(other) || !other.canImport(this)) {
            return false;
        }
        return true;
    }
    
    @Override
    public TypeArray getTypeArray() {
        return context.makeUnique(new TypeArrayBoolean(this));
    }
}
