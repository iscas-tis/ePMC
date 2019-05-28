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

public final class ValueIntegerJava implements ValueInteger, ValueEnumerable, ValueRange {
    public static boolean is(Value value) {
        return value instanceof ValueInteger;
    }

    public static ValueInteger as(Value value) {
        if (is(value)) {
            return (ValueInteger) value;
        } else {
            return null;
        }
    }

    private final static String SPACE = " ";
    private int value;
    private final TypeIntegerJava type;
    private final boolean bothBounded;

    ValueIntegerJava(TypeIntegerJava type) {
        assert type != null;
        this.type = type;
        bothBounded = TypeInteger.isIntegerBothBounded(getType());
    }

    @Override
    public TypeIntegerJava getType() {
        return type;
    }

    @Override
    public int getInt() {
        return value;
    }

    @Override
    public void set(int value) {
        this.value = value;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueIntegerJava)) {
            return false;
        }
        ValueIntegerJava other = (ValueIntegerJava) obj;
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
    public void read(BitStream reader) {
        assert reader != null;
        if (bothBounded) {
            int value = reader.readInt(getNumBits());
            value += getBoundLower();
            this.value = value;
        } else {
            int value = reader.readInt();
            this.value = value;
        }
    }

    @Override
    public void write(BitStream writer) {
        assert writer != null;
        if (bothBounded) {
            int value = this.value;
            value -= getBoundLower();
            writer.writeInt(value, getNumBits());
        } else {
            writer.writeInt(value);
        }
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

    @Override
    public void setValueNumber(int number) {
        assert number >= 0 : number;
        assert number < type.getUpperInt() + 1 - type.getLowerInt() :
            number + SPACE + type.getLowerInt() + SPACE + type.getUpperInt();
        set(type.getLowerInt() + number);
    }
}
