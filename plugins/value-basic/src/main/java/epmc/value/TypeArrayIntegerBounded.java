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

final class TypeArrayIntegerBounded implements TypeArrayInteger {
    private final static String ARRAY_INDICATOR = "[](integer-bounded)";
    private final TypeInteger entryType;

    TypeArrayIntegerBounded(TypeInteger entryType) {
        assert entryType != null;
        this.entryType = entryType;
        assert TypeInteger.isIntegerBothBounded(entryType);
    }
    @Override
    public TypeInteger getEntryType() {
        return entryType;
    }

    @Override
    public ValueArrayIntegerBounded newValue() {
        return new ValueArrayIntegerBounded(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeArrayIntegerBounded)) {
            return false;
        }
        TypeArrayIntegerBounded other = (TypeArrayIntegerBounded) obj;
        return this.getEntryType().equals(other.getEntryType());
    }
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = getEntryType().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEntryType());
        builder.append(ARRAY_INDICATOR);
        return builder.toString();
    }
    @Override
    public TypeArrayAlgebra getTypeArray() {
        // TODO Auto-generated method stub
        return null;
    }


}
