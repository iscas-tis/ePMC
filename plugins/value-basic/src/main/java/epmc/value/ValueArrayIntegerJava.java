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

import epmc.util.BitStream;
import epmc.value.Value;

final class ValueArrayIntegerJava implements ValueArrayInteger, ValueContentIntArray, ValueBitStoreable {
    private final static String SPACE = " ";
    private final TypeArrayIntegerJava type;
    private int[] content;
    private int size;

    ValueArrayIntegerJava(TypeArrayIntegerJava type) {
        this.type = type;
        this.content = new int[0];
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < size() : index + SPACE + size();
        content[index] = ValueInteger.as(value).getInt();
    }

    @Override
    public void set(int entry, int index) {
        assert index >= 0;
        assert index < size() : index + SPACE + size();
        content[index] = entry;		
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < size();
        int entry = content[index];
        ValueAlgebra.as(value).set(entry);
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
    public void setSize(int size) {
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

    @Override
    public void read(BitStream reader) {
        int size = reader.readInt();
        int newLength = content.length;
        while (size < newLength) {
            newLength *= 2;
        }        
        content = new int[newLength];
        for (int index = 0; index < size; index++) {
            content[index] = reader.readInt();
        }
    }

    @Override
    public void write(BitStream writer) {
        writer.writeInt(size);
        for (int index = 0; index < size; index++) {
            writer.writeInt(content[index]);
        }
    }
}
