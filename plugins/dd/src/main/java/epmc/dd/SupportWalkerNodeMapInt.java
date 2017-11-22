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
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueInteger;

public final class SupportWalkerNodeMapInt {
    private final SupportWalker walker;
    private final int[] values;
    private final BitSet valueSet;

    SupportWalkerNodeMapInt(SupportWalker walker) {
        assert walker != null;
        this.walker = walker;
        this.values = new int[walker.getNumNodes()];
        this.valueSet = UtilBitSet.newBitSetUnbounded();
    }

    public void set(Value value) {
        assert value != null;
        assert ValueInteger.is(value);
        int index = walker.getIndex();
        values[index] = ValueInteger.as(value).getInt();
        valueSet.set(index);
    }

    public void get(Value value) {
        assert value != null;
        assert ValueInteger.is(value);
        assert valueSet.get(walker.getIndex());
        ValueAlgebra.as(value).set(values[walker.getIndex()]);
    }

    public int getInt() {
        return values[walker.getIndex()];
    }

    public void set(int value) {
        int index = walker.getIndex();
        valueSet.set(index);
        values[index] = value;
    }

    public boolean getBoolean() {
        assert false;
        return false;
    }

    public void set(boolean value) {
        assert false;
    }

    public boolean isSet() {
        return valueSet.get(walker.getIndex());
    }
}
