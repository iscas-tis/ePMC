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

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.util.Arrays;

import epmc.value.Value;
import epmc.value.ValueArray;

final class ValueArrayObjectNumerated implements ValueArray {
    private boolean objectIdentity;
    private static final int LOG2LONGSIZE = 6;
    private final TObjectIntMap<Object> objectToNumber;
    private Object[] numberToObject = new Object[1];
    private int numBits;

    private final TypeArrayObjectNumerated type;
    private long[] content;
    private boolean immutable;
    private int size;

    ValueArrayObjectNumerated(TypeArrayObjectNumerated type, boolean objectIdentity) {
        this.type = type;
        this.objectIdentity = objectIdentity;
        this.content = new long[0];
        if (objectIdentity) {
            objectToNumber = new TObjectIntCustomHashMap<>(
                    new IdentityHashingStrategy<>(), 10, 0.5f, -1);
        } else {
            objectToNumber = new TObjectIntHashMap<>();
        }

    }

    @Override
    public ValueArrayObjectNumerated clone() {
        ValueArrayObjectNumerated other = new ValueArrayObjectNumerated(getType(), objectIdentity);
        other.set(this);
        return other;
    }

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueObject.isObject(value);
        assert getType().getEntryType().canImport(value.getType());
        assert index >= 0;
        assert index < size();
        int number = objectToNumber(ValueObject.asObject(value).getObject());
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
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0;
        assert index < size();
        int number = 0;
        for (int bitNr = 0; bitNr < getBitsPerEntry(); bitNr++) {
            int bitIndex = index * getBitsPerEntry() + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
        boolean bitValue = (content[offset] & (1L << bitIndex)) != 0;
        if (bitValue) {
            number |= (1 << bitNr);
        }
        }
        ValueObject.asObject(value).set(numberToObject(number));
    }

    private int getBitsPerEntry() {
        return numBits;
    }

    private Object numberToObject(int number) {
        return numberToObject[number];
    }

    private int objectToNumber(Object object) {
        int number = objectToNumber.get(object);
        if (number == -1) {
            number = objectToNumber.size();
            objectToNumber.put(object, number);
            if (number >= numberToObject.length) {
                numberToObject = Arrays.copyOf(numberToObject,
                        2 * numberToObject.length);
            }
            numberToObject[number] = object;

            int newNumConstants = objectToNumber.size();
            int newNumBits = Integer.SIZE - Integer.numberOfLeadingZeros(newNumConstants - 1);
            if (newNumBits > numBits) {
                increaseNumBits();
                numBits = newNumBits;
            }
        }
        return number;
    }

    void increaseNumBits() {
        int totalSize = size();
        int oldNumBitsPerEntry = getBitsPerEntry();
        int newNumBitsPerEntry = oldNumBitsPerEntry + 1;
        int newNumBits = newNumBitsPerEntry * totalSize;
        int newNumLongs = ((newNumBits - 1) >> LOG2LONGSIZE) + 1;
        long[] newContent = new long[newNumLongs];
        for (int entryNr = 0; entryNr < totalSize; entryNr++) {
            int number = 0;
            for (int bitNr = 0; bitNr < oldNumBitsPerEntry; bitNr++) {
                int bitIndex = entryNr * oldNumBitsPerEntry + bitNr;
                int offset = bitIndex >> LOG2LONGSIZE;
            boolean bitValue = (content[offset] & (1L << bitIndex)) != 0;
            if (bitValue) {
                number |= (1 << bitNr);
            }
            }
            for (int bitNr = 0; bitNr < newNumBitsPerEntry; bitNr++) {
                boolean bitValue = (number & (1 << bitNr)) != 0;
                int bitIndex = entryNr * newNumBitsPerEntry + bitNr;
                int offset = bitIndex >> LOG2LONGSIZE;
                if (bitValue) {
                    newContent[offset] |= 1L << bitIndex;
                } else {
                    newContent[offset] &= ~(1L << bitIndex);
                }
            }
        }
        this.content = newContent;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = 0 + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public TypeArrayObjectNumerated getType() {
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
    public void setSize(int size) {
        assert !isImmutable();
        assert size >= 0;
        int numBits = size * getBitsPerEntry();
        int num = ((numBits - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[num];
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
