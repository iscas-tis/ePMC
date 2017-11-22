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

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import epmc.value.Value;

final class ValueArrayIntegerMappedByteBuffer implements ValueArrayInteger {
    private final static String TMP_PREFIX = "valueArrayIntegerMappedByteBuffer";
    private final static String TMP_ENDING = "dat";
    private FileChannel channel;
    private MappedByteBuffer buffer;
    private TypeArrayIntegerMappedByteBuffer type;
    private int size;

    ValueArrayIntegerMappedByteBuffer(TypeArrayIntegerMappedByteBuffer type) {
        assert type != null;
        this.type = type;
        try {
            Path tmpFile = Files.createTempFile("valueArrayIntegerMappedByteBuffer", "dat");
            channel = FileChannel.open(tmpFile, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
            buffer = channel.map(MapMode.READ_WRITE, 0, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getInt(int index) {
        assert index >= 0 : index;
        assert index < size() : index + " " + size();
        int result = 0;
        result = buffer.getInt(index * 4);
        return result;
    }

    @Override
    public void set(Value value, int index) {
        buffer.putInt(index * 4, ValueInteger.as(value).getInt());
    }

    @Override
    public void get(Value value, int index) {
        ValueAlgebra.as(value).set(getInt(index));
    }

    @Override
    public TypeArrayIntegerMappedByteBuffer getType() {
        return type;
    }

    @Override
    public void set(int value, int index) {
        assert index >= 0;
        assert index < size();
        buffer.putInt(index * 4, value);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < size(); i++) {
            int entry = buffer.getInt(i * 4);
            hash = entry + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public void setSize(int size) {
        assert size >= 0;
        try {
            Path tmpFile = Files.createTempFile(TMP_PREFIX, TMP_ENDING);
            channel.close();
            channel = FileChannel.open(tmpFile, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
            buffer = channel.map(MapMode.READ_WRITE, 0, size * Integer.BYTES);
        } catch (IOException e) {
            assert false;
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
