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
import java.util.Arrays;

import com.sun.jna.Memory;

import epmc.error.EPMCException;
import epmc.value.Value;

final class ValueArrayIntegerNative extends ValueArrayInteger implements ValueContentMemory {
	private final static String SPACE = " ";
	private final TypeArrayIntegerNative type;
    private Memory content;
	private boolean immutable;

    ValueArrayIntegerNative(TypeArrayIntegerNative type) {
    	assert type != null;
    	this.type = type;
        int numBytes = getTotalSize() * 4;
        if (numBytes == 0) {
            numBytes = 1;
        }
        this.content = new Memory(numBytes);
        for (int i = 0; i < getTotalSize(); i++) {
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
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.size() / 4 < getTotalSize()) {
            content = new Memory(getTotalSize() * 4);
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert index >= 0;
        assert index < getTotalSize() : index + SPACE + getTotalSize();
        content.setInt(index * 4, ValueInteger.asInteger(value).getInt());
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0;
        assert index < getTotalSize();
        int entry = content.getInt(index * 4);
        ValueAlgebra.asAlgebra(value).set(entry);
    }
    
    @Override
    public ByteBuffer getMemory() {
        return content.getByteBuffer(0, Integer.BYTES * getTotalSize());
    }

    @Override
    public int getInt(int index) {
        return content.getInt(4 * index);
    }

    @Override
    public void setInt(int value, int index) {
        assert !isImmutable();
        assert index >= 0;
        assert index < getTotalSize();
        content.setInt(index * 4, value);
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int entryNr = 0; entryNr < getTotalSize(); entryNr++) {
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

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
