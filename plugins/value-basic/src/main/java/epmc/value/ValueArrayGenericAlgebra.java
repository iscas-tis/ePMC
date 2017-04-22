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

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.Value;

public final class ValueArrayGenericAlgebra extends ValueArrayAlgebra {
	private final static String SPACE = " ";
    private final TypeArrayGenericAlgebra type;
    private Value[] content;
    private boolean immutable;

    ValueArrayGenericAlgebra(TypeArrayGenericAlgebra type) {
        this.type = type;
        this.content = new Value[0];
    }
    
    @Override
    public ValueArrayGenericAlgebra clone() {
        ValueArrayGenericAlgebra clone = (ValueArrayGenericAlgebra) getType().newValue();
        clone.set(this);
        return clone;
    }

    void setContent(Type entryType, Value[] content) throws EPMCException {
        assert !isImmutable();
        for (int index = 0; index < getTotalSize(); index++) {
            this.content[index].set(content[index]);
        }
    }
    
    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        Type entryType = getType().getEntryType();
        if (this.content.length < getTotalSize()) {
            this.content = new Value[getTotalSize()];
            for (int index = 0; index < content.length; index++) {
                this.content[index] = entryType.newValue();
            }
        }
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert getType().getEntryType().canImport(value.getType()) : getType().getEntryType() + SPACE + value + SPACE + value.getType();
        assert index >= 0;
        assert index < getTotalSize();
        content[index].set(value);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType()) :
        	value.getType() + SPACE + getType().getEntryType();
        assert index >= 0 : index;
        assert index < getTotalSize() : index + SPACE + getTotalSize();
        value.set(content[index]);
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int i = 0; i < getTotalSize(); i++) {
            hash = content[i].hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public TypeArrayGenericAlgebra getType() {
        return type;
    }
    
    @Override
    public void setImmutable() {
        immutable = true;
    }
    
    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public void set(String value) throws EPMCException {
        // TODO Auto-generated method stub
        
    }

	@Override
	public void set(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOne() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}
}
