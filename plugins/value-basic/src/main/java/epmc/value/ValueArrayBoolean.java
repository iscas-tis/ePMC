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

import java.util.Arrays;

import epmc.value.Value;
import epmc.value.ValueArray;

final class ValueArrayBoolean implements ValueArray {
    private final static String SPACE = " ";
    private static final int LOG2LONGSIZE = 6;
    private final TypeArrayBoolean type;
    private long[] content;
    private boolean immutable;
    private int size;

    ValueArrayBoolean(TypeArrayBoolean type) {
        assert type != null;
        this.type = type;
        int numLongs = ((size() - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[numLongs];
    }

    @Override
    public void setSize(int size) {
        assert !isImmutable();
        assert size >= 0;
        int num = ((size - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[num];
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueBoolean.is(value) : value + " " + value.getType();
        assert index >= 0;
        assert index < size();
        int offset = index >> LOG2LONGSIZE;
        if (ValueBoolean.as(value).getBoolean()) {
            content[offset] |= 1L << index;
        } else {
            content[offset] &= ~(1L << index);
        }
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueBoolean.is(value);
        assert index >= 0;
        assert index < size() : index + SPACE + size();
        int offset = index >> 6;
        ValueBoolean.as(value).set((content[offset] & (1L << index)) != 0);
    }    

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    @Override
    public TypeArrayBoolean getType() {
        return type;
    }

    void setImmutable() {
        immutable = true;
    }

    boolean isImmutable() {
        return immutable;
    }
    
    @Override
    public String toString() {
        return UtilValue.arrayToString(this);
    }
}
