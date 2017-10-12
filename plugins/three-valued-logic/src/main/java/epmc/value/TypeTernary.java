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
import epmc.value.TypeArray;

/**
 * Type allowing to store ternary truth values.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TypeTernary implements TypeEnumerable, TypeNumBitsKnown {
    static boolean is(Type type) {
        return type instanceof TypeTernary;
    }

    /** String "ternary", for {@link #toString()}. */
    private final static String TERNARY = "ternary";
    /** Three different values: {@code true}, {@code false}, {@code unknown}. */
    final static int NUM_VALUES = 3;
    /** We need ceil(log2(NUM_VALUES)) = 2 bits to store values of this type. */
    private final static int NUM_BITS = 2;
    /** Integer representing value {@code false}. */
    final static int FALSE_NUMBER = 0;
    /** Integer representing value {@code unknown}. */
    final static int UNKNOWN_NUMBER = 1;
    /** Integer representing value {@code true}. */
    final static int TRUE_NUMBER = 2;

    /** Value storing {@code false} (made immutable in constructor). */
    private final ValueTernary valueFalse = new ValueTernary(this, Ternary.FALSE);
    /** Value storing {@code true} (made immutable in constructor). */
    private final ValueTernary valueTrue = new ValueTernary(this, Ternary.TRUE);
    /** Value storing {@code unknown} (made immutable in constructor). */
    private final ValueTernary valueUnknown = new ValueTernary(this, Ternary.UNKNOWN);

    /**
     * Construct new three-valued truth value type.
     * The value context parameter may not be {@code null}.
     */
    public TypeTernary() {
//        valueFalse.setImmutable();
  //      valueTrue.setImmutable();
    //    valueUnknown.setImmutable();
    }

    public static TypeTernary get() {
        return ContextValue.get().getType(TypeTernary.class);
    }    

    public ValueTernary getFalse() {
        return valueFalse;
    }

    public ValueTernary getTrue() {
        return valueTrue;
    }

    public ValueTernary getUnknown() {
        return valueUnknown;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(TERNARY);
        return builder.toString();
    }

    @Override
    public ValueTernary newValue() {
        return new ValueTernary(this);
    }

    @Override
    public int getNumValues() {
        return NUM_VALUES;
    }

    @Override
    public int getNumBits() {
        return NUM_BITS;
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
    public TypeArray getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }
}
