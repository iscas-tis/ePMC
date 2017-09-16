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

import epmc.value.Value;

final class ValueArrayIntegerJava implements ValueArrayInteger, ValueContentIntArray {
    private final static String SPACE = " ";
    private final TypeArrayIntegerJava type;
    private int[] content;
    private boolean immutable;
    private int size;

    ValueArrayIntegerJava(TypeArrayIntegerJava type) {
        this.type = type;
        this.content = new int[0];
    }

    @Override
    public ValueArrayIntegerJava clone() {
        ValueArrayIntegerJava clone = (ValueArrayIntegerJava) getType().newValue();
        clone.set(this);
        return clone;
    }

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert getType().getEntryType().canImport(value.getType());
        assert index >= 0;
        assert index < size() : index + SPACE + size();
        content[index] = ValueInteger.asInteger(value).getInt();
    }

    @Override
    public void set(int entry, int index) {
        assert !isImmutable();
        assert index >= 0;
        assert index < size() : index + SPACE + size();
        content[index] = entry;		
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0;
        assert index < size();
        int entry = content[index];
        ValueAlgebra.asAlgebra(value).set(entry);
    }

    @Override
    public int[] getIntArray() {
        return content;
    }

    @Override
    public int getInt(int index) {
        assert index >= 0;
        assert index < size() : index + " " + size();
        return content[index];
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int entryNr = 0; entryNr < size(); entryNr++) {
            int entry = content[entryNr];
            hash = entry + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public TypeArrayIntegerJava getType() {
        return type;
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public void setSize(int size) {
        assert !isImmutable();
        assert size >= 0;
        content = new int[size];
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return UtilValue.arrayToString(this);
    }
}
