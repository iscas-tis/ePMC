package epmc.param.value.dag;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class NodeStoreDisk implements NodeStore {
    public final static String IDENTIFIER = "disk";
    
    public final static class Builder implements NodeStore.Builder {

        @Override
        public NodeStore build() {
            return new NodeStoreDisk(this);
        }
        
    }

    private int storedUpto;
    private int size;
    private RandomAccessFile storage;
    private final int frontBufferSize = 1024 * 1024;
    private final int backBuffersSize = 1024 * 16;
    private final int indexDivideBy;
    private final byte[] frontArray;
    private final ByteBuffer frontByteBuffer;

    private int numBackBuffers = 16;
    private int[] backArraysAt;
    private final byte[][] backsArray;
    private final ByteBuffer backByteBuffers[];
    private final IntArrayList recentlyUsed = new IntArrayList();

    // TODO implement additional back buffer
    
    private NodeStoreDisk(Builder builder) {
        assert builder != null;
        try {
            File tempFile = File.createTempFile("nodestoredisk", ".dat");
            tempFile.deleteOnExit();
            storage = new RandomAccessFile(tempFile, "rw");
        } catch (IOException e) {
            // TODO provide nicer error message
            throw new RuntimeException(e);
        }
        frontArray = new byte[frontBufferSize];
        frontByteBuffer = ByteBuffer.wrap(frontArray);
        backArraysAt = new int[numBackBuffers];
        backsArray = new byte[numBackBuffers][];
        backByteBuffers = new ByteBuffer[numBackBuffers];
        for (int i = 0; i < numBackBuffers; i++) {
            backArraysAt[i] = -1;
            backsArray[i] = new byte[backBuffersSize];
            backByteBuffers[i] = ByteBuffer.wrap(backsArray[i]);
            recentlyUsed.add(i);
        }
        assert backBuffersSize % Long.BYTES == 0;
        indexDivideBy = backBuffersSize / Long.BYTES;
    }

    @Override
    public int add(OperatorType type, int operandLeft, int operandRight) {
        long entry = EntryUtil.makeEntry(type, operandLeft, operandRight);
        frontByteBuffer.putLong(entry);
        size++;
        if (frontByteBuffer.position() == frontBufferSize) {
            try {
                storage.seek(storage.length());
                storage.write(frontArray, 0, frontBufferSize);
                storedUpto = size;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            frontByteBuffer.position(0);
        }
        return size - 1;
        // TODO continue
    }

    @Override
    public int getNumNodes() {
        return size;
    }

    @Override
    public OperatorType getType(int number) {
        assert number >= 0 : number;
        return EntryUtil.getType(get(number));
    }
    
    @Override
    public int getOperandLeft(int number) {
        assert number >= 0 : number;
        return EntryUtil.getOperandLeft(get(number));
    }
    
    @Override
    public int getOperandRight(int number) {
        assert number >= 0 : number;
        return EntryUtil.getOperandRight(get(number));        
    }

    private long get(int index) {
        assert index >= 0 : index;
        if (index < storedUpto) {
            int usedBackBuffer = -1;
            for (int i = numBackBuffers - 1; i >= 0; i--) {
                if (index / indexDivideBy == backArraysAt[i]) {
                    usedBackBuffer = i;
                    break;
                }
            }
            if (usedBackBuffer >= 0) {
                recentlyUsed.removeInt(usedBackBuffer);
                recentlyUsed.add(usedBackBuffer);
            }
            if (usedBackBuffer == -1) {
                usedBackBuffer = recentlyUsed.getInt(0);
                recentlyUsed.removeInt(usedBackBuffer);
                recentlyUsed.add(usedBackBuffer);
                backArraysAt[usedBackBuffer] = index / indexDivideBy;
                try {
                    assert backArraysAt[usedBackBuffer] * backBuffersSize >= 0 : backArraysAt[usedBackBuffer] * backBuffersSize + " " + backArraysAt[usedBackBuffer] + " " + backBuffersSize;
                    long seekIndex = (long) backArraysAt[usedBackBuffer] * backBuffersSize;
                    storage.seek(seekIndex);
                    storage.read(backsArray[usedBackBuffer], 0, backBuffersSize);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(e);
                }
            }
            backByteBuffers[usedBackBuffer].position((index % indexDivideBy) * Long.BYTES);
            return backByteBuffers[usedBackBuffer].getLong();
        } else {
            int oldPos = frontByteBuffer.position();
            frontByteBuffer.position((index - storedUpto) * Long.BYTES);
            long result = frontByteBuffer.getLong();
            frontByteBuffer.position(oldPos);
            return result;
        }
    }

    @Override
    public boolean assertValidNumber(int number) {
        assert number < size : number + " " + size;
        return true;
    }

    @Override
    public void sendStatistics() {
        // TODO Auto-generated method stub
        
    }
}
