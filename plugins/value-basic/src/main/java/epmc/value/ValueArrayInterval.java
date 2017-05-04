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

import epmc.error.EPMCException;
import epmc.value.Value;

public final class ValueArrayInterval implements ValueArrayAlgebra, ValueContentDoubleArray {
	private final TypeArrayInterval type;
    private ValueArrayAlgebra content;
	private boolean immutable;
	private int size;

    ValueArrayInterval(TypeArrayInterval type) {
    	assert type != null;
    	this.type = type;
        this.content = UtilValue.newArray(type.getTypeArrayReal(), size() * 2);
    }
    
    @Override
    public TypeArrayInterval getType() {
    	return type;
    }
    
    @Override
    public ValueArrayInterval clone() {
        ValueArrayInterval clone = getType().newValue();
        clone.set(this);
        return clone;
    }

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueInterval.isInterval(value);
        assert index >= 0;
        assert index < size() : index + " " + size();
        content.set(ValueInterval.asInterval(value).getIntervalLower(), index * 2);
        content.set(ValueInterval.asInterval(value).getIntervalUpper(), index * 2 + 1);
    }

	@Override
	public void set(int entry, int index) {
        assert !isImmutable();
        assert index >= 0;
        assert index < size() : index + " " + size();
        content.set(entry, index * 2);
        content.set(entry, index * 2 + 1);
	}

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueInterval.isInterval(value);
        assert index >= 0;
        assert index < size() : index + " " + size();
        content.get(ValueInterval.asInterval(value).getIntervalLower(), index * 2);
        content.get(ValueInterval.asInterval(value).getIntervalUpper(), index * 2 + 1);
    }
    
    @Override
    public double[] getDoubleArray() {
        return ValueContentDoubleArray.getContent(content);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = content.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public void setImmutable() {
    	this.immutable = true;
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
	public void setSize(int size) {
        assert !isImmutable();
        assert size >= 0;
        content = UtilValue.newArray(getType().getTypeArrayReal(), size * 2);
        this.size = size;
	}

	@Override
	public int size() {
		return size;
	}
	
	@Override
	public String toString() {
		return UtilValue.arrayToString(this);
	}
}
