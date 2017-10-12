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

public final class ValueArrayDoubleJava implements ValueArrayDouble, ValueContentDoubleArray {
    public static boolean isValueArrayDoubleJava(Value value) {
        return value instanceof ValueArrayDoubleJava;
    }

    public static ValueArrayDoubleJava asValueArrayDoubleJava(Value value) {
        if (isValueArrayDoubleJava(value)) {
            return (ValueArrayDoubleJava) value;
        } else {
            return null;
        }
    }

    private final static String SPACE = " ";
    private double[] content;
    private final TypeArrayDouble type;
    private int size;

    ValueArrayDoubleJava(TypeArrayDouble type) {
        assert type != null;
        this.type = type;
        this.content = new double[0];
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < size() : index + SPACE + size();
        content[index] = ValueNumber.asNumber(value).getDouble();
    }

    @Override
    public void set(int entry, int index) {
        content[index] = entry;
    }


    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < size() : index + SPACE + size();
        double entry = content[index];
        assert ValueDouble.isDouble(value);
        ValueDouble.asDouble(value).set(entry);
    }

    @Override
    public double[] getDoubleArray() {
        return content;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int entryNr = 0; entryNr < size(); entryNr++) {
            long entry = Double.doubleToRawLongBits(content[entryNr]);
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
            entry >>>= 32;
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public TypeArrayDouble getType() {
        return type;
    }

    @Override
    public void setSize(int size) {
        assert size >= 0;
        content = new double[size];
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
