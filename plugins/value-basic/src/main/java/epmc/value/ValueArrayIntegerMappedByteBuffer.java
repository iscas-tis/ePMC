package epmc.value;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;

final class ValueArrayIntegerMappedByteBuffer extends ValueArrayInteger {
    private FileChannel channel;
    private MappedByteBuffer buffer;
	private TypeArrayIntegerMappedByteBuffer type;
	private boolean immutable;

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
        assert index < getTotalSize() : index + " " + getTotalSize();
        int result = 0;
        result = buffer.getInt(index * 4);
        return result;
    }
    
    @Override
    public ValueArrayIntegerMappedByteBuffer clone() {
        ValueArrayIntegerMappedByteBuffer result = new ValueArrayIntegerMappedByteBuffer(getType());
        result.set(this);
        return result;
    }

    @Override
    public void set(Value value, int index) {
        buffer.putInt(index * 4, ValueInteger.asInteger(value).getInt());
    }

    @Override
    public void get(Value value, int index) {
    	ValueAlgebra.asAlgebra(value).set(getInt(index));
    }

    @Override
    protected void setDimensionsContent() {
        try {
            Path tmpFile = Files.createTempFile("valueArrayIntegerMappedByteBuffer", "dat");
            channel.close();
            channel = FileChannel.open(tmpFile, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
            buffer = channel.map(MapMode.READ_WRITE, 0, getTotalSize() * 4);
        } catch (IOException e) {
            assert false;
        }
    }

    @Override
    public TypeArrayIntegerMappedByteBuffer getType() {
    	return type;
    }

    @Override
    public void setInt(int value, int index) {
        assert index >= 0;
        assert index < getTotalSize();
        buffer.putInt(index * 4, value);
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int i = 0; i < getTotalSize(); i++) {
            int entry = buffer.getInt(i * 4);
            hash = entry + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
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
	public void set(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOne() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
