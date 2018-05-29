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

import epmc.operator.OperatorSet;
import epmc.value.Value;

public final class ValueInterval implements ValueAlgebra, ValueRange, ValueSetString {
    public static boolean is(Value value) {
        return value instanceof ValueInterval;
    }

    public static ValueInterval as(Value value) {
        if (is(value)) {
            return (ValueInterval) value;
        } else {
            return null;
        }
    }

    private final static String COMMA = ",";
    private final static String LBRACK = "[";
    private final static String RBRACK = "]";

    private final ValueReal lower;
    private final ValueReal upper;
    private final TypeInterval type;
 
    ValueInterval(TypeInterval type, ValueReal lower, ValueReal upper) {
        assert type != null;
        assert lower != null;
        assert upper != null;
        this.type = type;
        this.lower = UtilValue.clone(lower);
        this.upper = UtilValue.clone(upper);
    }

    ValueInterval(TypeInterval type) {
        this(type, UtilValue.newValue(type.getEntryType(), 0),
                UtilValue.newValue(type.getEntryType(), 0));
    }

    public ValueReal getIntervalLower() {
        return lower;
    }

    public ValueReal getIntervalUpper() {
        return upper;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueInterval)) {
            return false;
        }
        ValueInterval other = (ValueInterval) obj;
        return this.lower.equals(other.lower) && this.upper.equals(other.upper);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = lower.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = upper.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        return LBRACK + lower + COMMA + upper + RBRACK;
    }

    @Override
    public void set(int value) {
        lower.set(value);
        upper.set(value);
    }

    @Override
    public void set(String string) {
        assert string != null;
        string = string.trim();
        String[] parts = string.split(COMMA);
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, type.getEntryType(), type.getEntryType());
        if (parts.length == 1) {
            Value point = UtilValue.newValue(type.getEntryType(), parts[0]);
            setReal.apply(getIntervalLower(), point);
            setReal.apply(getIntervalUpper(), point);
        } else if (parts.length == 2) {
            String lowerString = parts[0].substring(1);
            String upperString = parts[1].substring(0, parts[1].length() - 1);
            Value lower = UtilValue.newValue(type.getEntryType(), lowerString);
            Value upper = UtilValue.newValue(type.getEntryType(), upperString);
            setReal.apply(getIntervalLower(), lower);
            setReal.apply(getIntervalUpper(), upper);
        } else {
            assert false;            
        }

    }

    @Override
    public boolean checkRange() {
        return ValueRange.checkRange(lower) && ValueRange.checkRange(upper);
    }

    @Override
    public TypeInterval getType() {
        return type;
    }

    public static ValueAlgebra getLower(Value operand) {
        if (is(operand)) {
            return ValueInterval.as(operand).getIntervalLower();
        } else {
            return ValueAlgebra.as(operand);
        }
    }

    public static Value getUpper(Value operand) {
        if (is(operand)) {
            return ValueInterval.as(operand).getIntervalUpper();
        } else {
            return operand;
        }
    }
}
