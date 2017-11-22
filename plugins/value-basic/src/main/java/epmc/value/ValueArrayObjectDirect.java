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
import epmc.value.ValueArray;

final class ValueArrayObjectDirect implements ValueArray {
    private final TypeArrayObjectDirect type;
    private Object[] content;
    private int size;

    ValueArrayObjectDirect(TypeArrayObjectDirect type) {
        this.type = type;
        this.content = new Object[size()];
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert ValueObject.is(value);
        assert index >= 0;
        assert index < size();
        content[index] = ValueObject.as(value).getObject();
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueObject.is(value);
        assert index >= 0;
        assert index < size();
        Object entry = content[index];
        ValueObject.as(value).set(entry);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int entryNr = 0; entryNr < size(); entryNr++) {
            long entry = 0;
            if (content[entryNr] != null) {
                entry = content[entryNr].hashCode();
            }
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }        
        return hash;
    }

    @Override
    public TypeArrayObjectDirect getType() {
        return type;
    }

    @Override
    public void setSize(int size) {
        assert size >= 0;
        if (this.content.length < size) {
            content = new Object[size];
        }
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
