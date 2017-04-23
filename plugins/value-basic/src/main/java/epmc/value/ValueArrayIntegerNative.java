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

import java.nio.ByteBuffer;

import com.sun.jna.Memory;

import epmc.error.EPMCException;
import epmc.value.Value;

final class ValueArrayIntegerNative implements ValueArrayInteger, ValueContentMemory {
	private final static String SPACE = " ";
	private final TypeArrayIntegerNative type;
    private Memory content;
	private boolean immutable;
	private int size;

    ValueArrayIntegerNative(TypeArrayIntegerNative type) {
    	assert type != null;
    	this.type = type;
        int numBytes = size() * Integer.BYTES;
        if (numBytes == 0) {
            numBytes = 1;
        }
        this.content = new Memory(numBytes);
        for (int i = 0; i < size(); i++) {
            set(getType().getEntryType().getZero(), i);
        }
    }
    
    @Override
    public ValueArrayIntegerNative clone() {
    	ValueArrayIntegerNative other = new ValueArrayIntegerNative(getType());
    	other.set(this);
    	return other;
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert index >= 0;
        assert index < size() : index + SPACE + size();
        content.setInt(index * 4, ValueInteger.asInteger(value).getInt());
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0;
        assert index < size();
        int entry = content.getInt(index * 4);
        ValueAlgebra.asAlgebra(value).set(entry);
    }
    
    @Override
    public ByteBuffer getMemory() {
        return content.getByteBuffer(0, Integer.BYTES * size());
    }

    @Override
    public int getInt(int index) {
        return content.getInt(4 * index);
    }

    @Override
    public void set(int value, int index) {
        assert !isImmutable();
        assert index >= 0;
        assert index < size();
        content.setInt(index * 4, value);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        for (int entryNr = 0; entryNr < size(); entryNr++) {
            long entry = content.getInt(entryNr * 4);
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    
    @Override
    public TypeArrayIntegerNative getType() {
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
	public void setSize(int size) {
        assert !isImmutable();
        assert size >= 0;
        content = new Memory(size * Integer.BYTES);
        for (int index = 0; index < size; index++) {
        	content.setInt(index * Integer.BYTES, 0);
        }
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
