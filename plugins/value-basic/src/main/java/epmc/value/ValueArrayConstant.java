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

public final class ValueArrayConstant implements ValueArray {
    private final TypeArrayConstant type;
    private final Value content;
    private int size;

    ValueArrayConstant(TypeArrayConstant type) {
        this.type = type;
        this.content = getType().getEntryType().newValue();
    }

    @Override
    public ValueArrayConstant clone() {
        ValueArrayConstant other = new ValueArrayConstant(getType());
        other.set(this);
        return other;
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert content.getType().canImport(value.getType());
        assert index >= 0;
        assert index < size() : index + " " + size();
        content.set(value);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < size();
        assert value.getType().canImport(content.getType());
        value.set(content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public TypeArrayConstant getType() {
        return type;
    }
    
    @Override
    public void setSize(int size) {
        assert size >= 0;
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
