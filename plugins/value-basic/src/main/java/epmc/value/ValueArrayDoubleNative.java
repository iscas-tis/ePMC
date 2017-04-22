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

final class ValueArrayDoubleNative extends ValueArrayDouble implements ValueContentMemory {
	private final TypeArrayDoubleNative type;
    private Memory content;
	private boolean immutable;

    ValueArrayDoubleNative(TypeArrayDoubleNative type) {
        assert type != null;
        this.type = type;
        int numBytes = size() * 8;
        if (numBytes == 0) {
            numBytes = 1;
        }
        assert numBytes >= 1 : numBytes;
        this.content = new Memory(1);
    }
    
    @Override
    public ValueArrayDoubleNative clone() {
    	ValueArrayDoubleNative other = new ValueArrayDoubleNative(getType());
    	other.set(this);
    	return other;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.size() / 8 < size()) {
            content = new Memory(size() * 8);
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert getType().getEntryType().canImport(value.getType()) :  value;
        assert index >= 0 : index;
        assert index < size() : index + " " + size();
        content.setDouble(index * 8, ValueNumber.asNumber(value).getDouble());
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType()) : value;
        assert index >= 0;
        assert index < size();
        double entry = content.getDouble(index * 8);
        ValueReal.asReal(value).set(entry);
    }
    
    @Override
    public ByteBuffer getMemory() {
        return content.getByteBuffer(0, Double.BYTES * size());
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        for (int entryNr = 0; entryNr < size(); entryNr++) {
            long entry = Double.doubleToRawLongBits(content.getDouble(entryNr * 8));
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
            entry >>>= 32;
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    
    @Override
    public TypeArrayDoubleNative getType() {
    	return type;
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
}
