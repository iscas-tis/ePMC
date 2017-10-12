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

import epmc.options.Options;
import epmc.value.Value;

public final class ValueDouble implements ValueReal, ValueSetString {
    public static boolean is(Value value) {
        return value instanceof ValueDouble;
    }

    public static ValueDouble as(Value value) {
        if (is(value)) {
            return (ValueDouble) value;
        } else {
            return null;
        }
    }

    private final static String NAN = "NaN";
    private final static String DIVIDED = "/";

    private final TypeDouble type;
    private double value;
    private boolean immutable;

    ValueDouble(TypeDouble type, double value) {
        assert type != null;
        this.type = type;
        this.value = value;
    }

    ValueDouble(TypeDouble type) {
        this(type, 0.0);
    }    

    @Override
    public TypeDouble getType() {
        return type;
    }

    @Override
    public double getDouble() {
        return value;
    }

    public void set(double value) {
        assert !isImmutable();
        this.value = value;
    }

    @Override
    public ValueDouble clone() {
        return new ValueDouble(getType(), value);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueDouble)) {
            return false;
        }
        ValueDouble other = (ValueDouble) obj;
        if (Double.isNaN(this.value) != Double.isNaN(other.value)) {
            return false;
        }
        if (!Double.isNaN(this.value) && this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        if (Double.isNaN(value)) {
            return -232;
        }
        if (Double.isInfinite(this.value)) {
            return -23333;
        }
        final int low = (int) Double.doubleToLongBits(value);
        final int up = (int) (Double.doubleToLongBits(value) >> 32);
        hash = low + (hash << 6) + (hash << 16) - hash;
        hash = up + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        if (Double.isNaN(value)) {
            return NAN;
        } else {
            Options options = Options.get();
            if (options.getBoolean(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_NATIVE)) {
                return String.valueOf(value);
            } else {
                String format = options.getString(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT);
                assert format != null;
                return String.format(format, value);
            }
        }
    }

    @Override
    public int getInt() {
        return (int) getDouble();
    }

    @Override
    public void set(int operand) {
        assert !isImmutable();
        set((double) operand);
    }

    @Override
    public void set(String string) {
        assert string != null;
        if (string.contains(DIVIDED)) {
            String[] parts = string.split(DIVIDED);
            assert parts.length == 2;
            String numString = parts[0];
            String denString = parts[1];
            double num = Double.parseDouble(numString);
            double den = Double.parseDouble(denString);
            value = num / den;
        } else {
            try {
                value = Double.parseDouble(string);
            } catch (NumberFormatException e) {
                fail(ProblemsValueBasic.VALUES_STRING_INVALID_VALUE, e, value, type);
            }
        }

    }

    void setImmutable() {
        this.immutable = true;
    }

    boolean isImmutable() {
        return immutable;
    }
}
