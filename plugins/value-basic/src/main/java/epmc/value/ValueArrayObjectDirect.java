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
import epmc.value.ValueArray;

final class ValueArrayObjectDirect extends ValueArray {
	private final TypeArrayObjectDirect type;
    private Object[] content;
	private boolean immutable;

    ValueArrayObjectDirect(TypeArrayObjectDirect type) {
    	this.type = type;
        this.content = new Object[size()];
    }
    
    @Override
    public ValueArrayObjectDirect clone() {
    	ValueArrayObjectDirect other = new ValueArrayObjectDirect(getType());
    	other.set(this);
    	return other;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.length < size()) {
            content = new Object[size()];
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueObject.isObject(value);
        assert index >= 0;
        assert index < size();
        content[index] = ValueObject.asObject(value).getObject();
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueObject.isObject(value);
        assert index >= 0;
        assert index < size();
        Object entry = content[index];
        ValueObject.asObject(value).set(entry);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        for (int entryNr = 0; entryNr < size(); entryNr++) {
            long entry = 0;
            if (content[entryNr] != null) {
                entry = content[entryNr].hashCode();
            }
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }        
        return hash;
    }
    
    @Override
    public TypeArrayObjectDirect getType() {
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
}
