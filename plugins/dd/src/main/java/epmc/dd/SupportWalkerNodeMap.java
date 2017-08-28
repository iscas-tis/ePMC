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

package epmc.dd;

import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;

public final class SupportWalkerNodeMap {
    private final SupportWalker walker;
    private final Type entryType;
    private final ValueArray values;
    private final Value helper;
    private final BitSet valueSet;

    SupportWalkerNodeMap(SupportWalker walker, Type type) {
        assert walker != null;
        assert type != null;
        this.valueSet = UtilBitSet.newBitSetUnbounded();
        this.entryType = type;
        this.walker = walker;
        TypeArray typeArray = type.getTypeArray();
        this.values = UtilValue.newArray(typeArray, walker.getNumNodes());
        this.helper = type.newValue();
    }

    public void set(Value value) {
        assert value != null;
        assert entryType.canImport(value.getType());
        int index = walker.getIndex();
        values.set(value, index);
        valueSet.set(index);
    }

    public void get(Value value) {
        assert value != null;
        assert entryType.canImport(value.getType());
        assert valueSet.get(walker.getIndex());
        values.get(value, walker.getIndex());
    }

    public int getInt() {
        assert TypeInteger.isInteger(entryType);
        get(helper);
        return ValueInteger.asInteger(helper).getInt();
    }

    public void set(int value) {
        assert TypeInteger.isInteger(entryType);
        ValueAlgebra.asAlgebra(helper).set(value);
        set(helper);
    }

    public boolean getBoolean() {
        assert TypeBoolean.isBoolean(entryType);
        get(helper);
        return ValueBoolean.asBoolean(helper).getBoolean();
    }

    public void set(boolean value) {
        assert TypeBoolean.isBoolean(entryType);
        ValueBoolean.asBoolean(helper).set(value);
        set(helper);
    }

    public boolean isSet() {
        return valueSet.get(walker.getIndex());
    }
}
