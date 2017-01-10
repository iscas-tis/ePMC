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
import epmc.value.Value;

final class ValueArrayIntegerBounded extends ValueArrayInteger {
    /** Log2 of {@link Long#SIZE}. */
    private static final int LOG2LONGSIZE = 6;
    /** String containing a single space. */
    private final static String SPACE = " ";
	private final TypeArrayIntegerBounded type;
    /** Lower bound of integers stored in this array. */
    private final int lower;
    /** Upper bound of integers stored in this array. */
    private final int upper;
    /** Content of the array. */
    private long[] content;
    /** Bits used to store a single entry.
     * Note that, while this number could be computed using
     * <code>getEntryType().getNumBits()</code>, we store this information in a
     * variable here because the computation would be too expensive.
     * */
    private final int bitsPerEntry;
	private boolean immutable;
    
    ValueArrayIntegerBounded(TypeArrayIntegerBounded type) {
        assert type != null;
        this.type = type;
        this.content = new long[0];
        this.lower = TypeInteger.asInteger(type.getEntryType()).getLowerInt();
        this.upper = TypeInteger.asInteger(type.getEntryType()).getUpperInt();
        this.bitsPerEntry = getType().getEntryType().getNumBits();
    }

    @Override
    public int getInt(int index) {
        assert index >= 0;
        assert index < getTotalSize();
        int number = 0;
        for (int bitNr = 0; bitNr < getBitsPerEntry(); bitNr++) {
            int bitIndex = index * getBitsPerEntry() + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
            boolean bitValue = (content[offset] & (1L << bitIndex)) != 0;
            if (bitValue) {
                number |= (1 << bitNr);
            }
        }
        number += lower;
        return number;
    }

    @Override
    public void setInt(int value, int index) {
        assert !isImmutable();
        assert index >= 0;
        assert index < getTotalSize();
        assert value >= lower : value + SPACE + lower;
        assert value <= upper : value + SPACE + upper;
        int number = value - lower;
        for (int bitNr = 0; bitNr < getBitsPerEntry(); bitNr++) {
            boolean bitValue = (number & (1 << bitNr)) != 0;
            int bitIndex = index * getBitsPerEntry() + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
            if (bitValue) {
                content[offset] |= 1L << bitIndex;
            } else {
                content[offset] &= ~(1L << bitIndex);
            }
        }
    }
    
    @Override
    public ValueArrayIntegerBounded clone() {
    	ValueArrayIntegerBounded other = new ValueArrayIntegerBounded(getType());
    	other.set(this);
    	return other;
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert getType().getEntryType().canImport(value.getType());
        assert index >= 0;
        assert index < getTotalSize();
        setInt(ValueInteger.asInteger(value).getInt(), index);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0;
        assert index < getTotalSize();
        ValueAlgebra.asAlgebra(value).set(getInt(index));
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        int numBits = getTotalSize() * getBitsPerEntry();
        int size = ((numBits - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[size];
    }

    private int getBitsPerEntry() {
        return bitsPerEntry;
    }
    
    @Override
    public TypeArrayIntegerBounded getType() {
    	return type;
    }
    
    public int getBoundLower() {
        return lower;
    }
    
    public int getBoundUpper() {
        return upper;
    }    
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = content.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
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
