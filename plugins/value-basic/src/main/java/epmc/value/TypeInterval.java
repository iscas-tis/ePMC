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
import epmc.value.Value;

public final class TypeInterval implements TypeWeightTransition, TypeWeight {
    public static boolean isInterval(Type type) {
        return type instanceof TypeInterval;
    }

    public static TypeInterval asInterval(Type type) {
        if (type instanceof TypeInterval) {
            return (TypeInterval) type;
        } else {
            return null;
        }
    }

    private final ValueInterval one;
    private final ValueInterval zero;
    private final ValueInterval posInf;
    private final ValueInterval negInf;

    public static TypeInterval get() {
        return ContextValue.get().getType(TypeInterval.class);
    }

    public static void set(TypeInterval type) {
        assert type != null;
        ContextValue.get().setType(TypeInterval.class,
                ContextValue.get().makeUnique(type));
    }

    public TypeInterval() {
        TypeReal typeReal = TypeReal.get();
        one = new ValueInterval(this, typeReal.getOne(), typeReal.getOne());
        zero = new ValueInterval(this, typeReal.getZero(), typeReal.getZero());
        posInf = new ValueInterval(this, typeReal.getPosInf(), typeReal.getPosInf());
        negInf = new ValueInterval(this, typeReal.getNegInf(), typeReal.getNegInf());
    }

    @Override
    public boolean canImport(Type type) {
        assert type != null;
        if (type instanceof TypeInterval) {
            return true;
        }
        if (type instanceof TypeInteger) {
            return true;
        }
        if (TypeReal.isReal(type)) {
            return true;
        }
        return false;
    }

    @Override
    public ValueInterval newValue() {
        return new ValueInterval(this);
    }

    public ValueInterval newValue(Value lower, Value upper) {
        assert lower != null;
        assert upper != null;
        assert ValueReal.isReal(lower) || ValueInteger.isInteger(lower);
        assert ValueReal.isReal(upper) || ValueInteger.isInteger(upper);
        ValueInterval result = newValue();
        result.getIntervalLower().set(lower);
        result.getIntervalUpper().set(upper);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("interval");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Type other = (Type) obj;
        if (!canImport(other) || !other.canImport(this)) {
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
    public ValueInterval getOne() {
        return one;
    }

    @Override
    public ValueInterval getZero() {
        return zero;
    }

    public TypeReal getEntryType() {
        return TypeReal.get();
    }

    @Override
    public ValueInterval getPosInf() {
        return posInf;
    }

    @Override
    public TypeArrayInterval getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayInterval(this));
    }

    @Override
    public ValueAlgebra getNegInf() {
        return negInf;
    }
}
