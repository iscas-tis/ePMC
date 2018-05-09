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

import epmc.value.ContextValue;
import epmc.value.Type;

public final class TypeDouble implements TypeWeight, TypeWeightTransition, TypeReal, TypeBounded, TypeNumBitsKnown {
    public static TypeDouble get() {
        return ContextValue.get().getType(TypeDouble.class);
    }

    public static void set(TypeDouble type) {
        assert type != null;
        ContextValue.get().setType(TypeDouble.class, ContextValue.get().makeUnique(type));
    }

    public static boolean is(Type type) {
        return type instanceof TypeDouble;
    }
    
    public static TypeDouble as(Type type) {
        if (is(type)) {
            return (TypeDouble) type;
        } else {
            return null;
        }
    }

    private final static String DOUBLE = "double";

    private final ValueDouble valueOne = new ValueDouble(this, 1.0);
    private final ValueDouble valueZero = new ValueDouble(this, 0.0);
    private final ValueDouble valuePosInf = new ValueDouble(this, Double.POSITIVE_INFINITY);
    private final ValueDouble valueNegInf = new ValueDouble(this, Double.NEGATIVE_INFINITY);
    private final ValueDouble lower;
    private final ValueDouble upper;

    public TypeDouble(ValueDouble lower, ValueDouble upper) {
        valueOne.setImmutable();
        valueZero.setImmutable();
        valuePosInf.setImmutable();
        valueNegInf.setImmutable();
        this.lower = lower == null ? null : UtilValue.clone(lower);
        this.upper = upper == null ? null : UtilValue.clone(upper);
        if (this.lower != null) {
            this.lower.setImmutable();
        }
        if (this.upper != null) {
            this.upper.setImmutable();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DOUBLE);
        return builder.toString();
    }

    @Override
    public ValueDouble newValue() {
        return new ValueDouble(this);
    }

    @Override
    public ValueDouble getZero() {
        return valueZero;
    }

    @Override
    public ValueDouble getOne() {
        return valueOne;
    }

    @Override
    public ValueDouble getPosInf() {
        return valuePosInf;
    }

    @Override
    public ValueDouble getNegInf() {
        return valueNegInf;
    }

    @Override
    public int getNumBits() {
        return Double.SIZE;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public ValueDouble getLower() {
        return lower;
    }

    @Override
    public ValueDouble getUpper() {
        return upper;
    }

    @Override
    public TypeArrayDouble getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayDouble(this));
    }
}
