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

public final class ValueBooleanJava implements ValueBoolean {
    public static boolean is(Value value) {
        return value instanceof ValueBooleanJava;
    }

    public static ValueBooleanJava as(Value value) {
        if (is(value)) {
            return (ValueBooleanJava) value;
        } else {
            return null;
        }
    }

    public static boolean isTrue(Value value) {
        ValueBooleanJava valueBoolean = as(value);
        if (valueBoolean == null) {
            return false;
        }
        if (valueBoolean.getBoolean()) {
            return true;
        }
        return false;
    }

    public static boolean isFalse(Value value) {
        ValueBooleanJava valueBoolean = as(value);
        if (valueBoolean == null) {
            return false;
        }
        if (!valueBoolean.getBoolean()) {
            return true;
        }
        return false;
    }

    private final static String TRUE = "true";
    private final static String FALSE = "false";

    private boolean value;
    private final TypeBooleanJava type;
    private boolean immutable;

    ValueBooleanJava(TypeBooleanJava type, boolean value) {
        assert type != null;
        this.type = type;
        this.value = value;
    }

    ValueBooleanJava(TypeBooleanJava type) {
        this(type, false);
    }

    public boolean getBoolean() {
        return value;
    }

    public void set(boolean value) {
        assert !isImmutable();
        this.value = value;
    }

    @Override
    public TypeBoolean getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueBooleanJava)) {
            return false;
        }
        ValueBooleanJava other = (ValueBooleanJava) obj;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = value ? 1 : 0 + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public void read(BitStream reader) {
        assert !isImmutable();
        assert reader != null;
        set(reader.read());
    }

    @Override
    public void write(BitStream writer) {
        assert writer != null;
        writer.write(getBoolean());
    }

    @Override
    public void set(String string) {
        assert string != null;
        string = string.toLowerCase();
        string = string.trim();
        if (string.equals(TRUE)) {
            value = true;
        } else if (string.equals(FALSE)) {
            value = false;
        } else {
            fail(ProblemsValueBasic.VALUES_STRING_INVALID_VALUE, value, type);
        }
    }

    void setImmutable() {
        this.immutable = true;
    }

    boolean isImmutable() {
        return immutable;
    }

    @Override
    public int getValueNumber() {
        return value ? 1 : 0;
    }

    @Override
    public void setValueNumber(int number) {
        assert number >= 0 : number;
        assert number < 2 : number;
        set(number == 1);
    }
}
