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

import static epmc.error.UtilError.fail;

import epmc.util.BitStream;
import epmc.value.Value;

public final class ValueInteger implements ValueNumber, ValueEnumerable, ValueNumBitsKnown, ValueRange, ValueBitStoreable, ValueSetString {
    public static boolean isInteger(Value value) {
        return value instanceof ValueInteger;
    }

    public static ValueInteger asInteger(Value value) {
        if (isInteger(value)) {
            return (ValueInteger) value;
        } else {
            return null;
        }
    }

    private final static String SPACE = " ";
    private int value;
    private final TypeInteger type;
    private boolean immutable;

    ValueInteger(TypeInteger type, int value) {
        assert type != null;
        this.type = type;
        this.value = value;
    }

    ValueInteger(TypeInteger type) {
        this(type, 0);
    }

    @Override
    public TypeInteger getType() {
        return type;
    }

    @Override
    public int getInt() {
        return value;
    }

    @Override
    public void set(int value) {
        assert !isImmutable();
        this.value = value;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueInteger)) {
            return false;
        }
        ValueInteger other = (ValueInteger) obj;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = value + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public void set(Value op) {
        assert !isImmutable();
        assert op != null;
        assert ValueInteger.isInteger(op) : op + SPACE + op.getType();
        set(ValueInteger.asInteger(op).getInt());
    }

    @Override
    public void read(BitStream reader) {
        assert !isImmutable();
        assert reader != null;
        int value = reader.read(getNumBits());
        if (TypeInteger.isIntegerBothBounded(getType())) {
            value += getBoundLower();
        }
        set(value);
    }

    @Override
    public void write(BitStream writer) {
        assert writer != null;
        int value = getInt();
        if (TypeInteger.isIntegerBothBounded(getType())) {
            value -= getBoundLower();
        }
        writer.write(value, getNumBits());
    }

    public int getBoundLower() {
        return getType().getLowerInt();
    }

    public int getBoundUpper() {
        return getType().getUpperInt();
    }

    @Override
    public double getDouble() {
        return value;
    }

    @Override
    public boolean checkRange() {
        return value >= getType().getLowerInt() && value <= getType().getUpperInt();
    }    

    public boolean isBothBounded() {
        return TypeInteger.isIntegerBothBounded(getType());
    }

    @Override
    public void set(String value) {
        assert value != null;
        try {
            this.value = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            fail(ProblemsValueBasic.VALUES_STRING_INVALID_VALUE, e, value, type);
        }
    }

    @Override
    public int getValueNumber() {
        return value - getType().getLowerInt();
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
        assert number < type.getUpperInt() + 1 - type.getLowerInt() :
            number + SPACE + type.getLowerInt() + SPACE + type.getUpperInt();
        set(type.getLowerInt() + number);
    }
}
