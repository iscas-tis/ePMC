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
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class ValueArrayGeneric extends ValueArray {
    private final TypeArrayGeneric type;
    private Value[] content;
    private boolean immutable;

    ValueArrayGeneric(TypeArrayGeneric type) {
        this.type = type;
        this.content = new Value[0];
    }
    
    @Override
    public ValueArrayGeneric clone() {
        ValueArrayGeneric clone = (ValueArrayGeneric) getType().newValue();
        clone.set(this);
        return clone;
    }

    void setContent(Type entryType, Value[] content) throws EPMCException {
        assert !isImmutable();
        for (int index = 0; index < size(); index++) {
            this.content[index].set(content[index]);
        }
    }
    
    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        Type entryType = getType().getEntryType();
        if (this.content.length < size()) {
            this.content = new Value[size()];
            for (int index = 0; index < content.length; index++) {
                this.content[index] = entryType.newValue();
            }
        }
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert getType().getEntryType().canImport(value.getType()) : getType().getEntryType() + " " + value + " " + value.getType();
        assert index >= 0;
        assert index < size();
        content[index].set(value);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0 : index;
        assert index < size() : index + " " + size();
        value.set(content[index]);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < size(); i++) {
            hash = content[i].hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public TypeArrayGeneric getType() {
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
