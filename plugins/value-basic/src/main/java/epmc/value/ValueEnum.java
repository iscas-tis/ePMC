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

import epmc.util.BitStream;
import epmc.value.Value;

public final class ValueEnum implements ValueEnumerable, ValueNumBitsKnown, ValueBitStoreable {
    public static boolean is(Value value) {
        return value instanceof ValueEnum;
    }

    public static ValueEnum as(Value value) {
        if (is(value)) {
            return (ValueEnum) value;
        } else {
            return null;
        }
    }

    private Enum<?> value;
    private final TypeEnum type;
    private boolean immutable;

    ValueEnum(TypeEnum type) {
        assert type != null;
        assert TypeEnum.is(type);
        this.type = type;
        value = type.getEnumClass().getEnumConstants()[0];
    }

    ValueEnum(ValueEnum other) {
        assert other != null;
        this.type = other.getType();
        this.value = other.value;
    }

    @Override
    public TypeEnum getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public  <T extends Enum<?>> T getEnum() {
        return (T) value;
    }

    public Class<? extends Enum<?>> getEnumClass() {
        return getType().getEnumClass();
    }

    public void set(Enum<?> value) {
        assert !isImmutable();
        assert value != null;
        assert value.getClass() == getType().getEnumClass() : value.getClass()
                + " != " + getType().getEnumClass();
        this.value = value;
    }

    @Override
    public ValueEnum clone() {
        return new ValueEnum(this);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueEnum)) {
            return false;
        }
        ValueEnum other = (ValueEnum) obj;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public void write(BitStream writer) {
        assert writer != null;
        int value = this.value.ordinal();
        int marker = 1;
        for (int bitNr = 0; bitNr < getNumBits(); bitNr++) {
            writer.write((value & marker) != 0);
            marker <<= 1;
        }
    }

    @Override
    public void read(BitStream reader) {
        assert !isImmutable();
        assert reader != null;
        int value = 0;
        int marker = 1;
        for (int bitNr = 0; bitNr < getNumBits(); bitNr++) {
            if (reader.read()) {
                value |= marker;
            }
            marker <<= 1;
        }
        assert value >= 0;
        assert value < getType().getEnumClass().getEnumConstants().length;
        this.value = getType().getEnumClass().getEnumConstants()[value];
    }

    @Override
    public int getValueNumber() {
        return value.ordinal();
    }

    void setImmutable() {
        this.immutable = true;
    }

    boolean isImmutable() {
        return immutable;
    }

    @Override
    public void setValueNumber(int number) {
        assert number >= 0 : number;
        assert number < type.getNumValues() : number;
        set(type.getEnumClass().getEnumConstants()[number]);
    }
}
