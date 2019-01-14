package epmc.param.value.dag.simplifier;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class DoubleStoreDisk implements DoubleStore {
    public final static String IDENTIFIER = "disk";
    
    public final static class Builder implements DoubleStore.Builder {

        @Override
        public DoubleStore build() {
            return new DoubleStoreDisk(this);
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
    
    private DoubleStoreDisk(Builder builder) {
        assert builder != null;
        try {
            File tempFile = File.createTempFile("doublestoredisk", ".dat");
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
        indexDivideBy = backBuffersSize / Double.BYTES;
    }

    @Override
    public void add(double entry) {
        frontByteBuffer.putDouble(entry);
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
        // TODO continue
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public double get(int index) {
        if (index < storedUpto) {
            int usedBackBuffer = -1;
            for (int i = numBackBuffers - 1; i >= 0; i--) {
                if (index / indexDivideBy == backArraysAt[i]) {
                    usedBackBuffer = i;
                    break;
                }
            }
            if (usedBackBuffer >= 0) {
                recentlyUsed.rem(usedBackBuffer);
                recentlyUsed.add(usedBackBuffer);
            }
            if (usedBackBuffer == -1) {
                usedBackBuffer = recentlyUsed.getInt(0);
                recentlyUsed.rem(usedBackBuffer);
                recentlyUsed.add(usedBackBuffer);
                backArraysAt[usedBackBuffer] = index / indexDivideBy;
                try {
                    long seekIndex = (long) backArraysAt[usedBackBuffer] * backBuffersSize;
                    storage.seek(seekIndex);
                    storage.read(backsArray[usedBackBuffer], 0, backBuffersSize);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(e);
                }
            }
            backByteBuffers[usedBackBuffer].position((index % indexDivideBy) * Double.BYTES);
            return backByteBuffers[usedBackBuffer].getDouble();
        } else {
            int oldPos = frontByteBuffer.position();
            frontByteBuffer.position((index - storedUpto) * Double.BYTES);
            double result = frontByteBuffer.getDouble();
            frontByteBuffer.position(oldPos);
            return result;
        }
    }
}
