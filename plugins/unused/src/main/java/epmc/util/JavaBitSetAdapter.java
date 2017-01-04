package epmc.util;

import epmc.util.BitSet;

public class JavaBitSetAdapter implements BitSet {
    private final java.util.BitSet inner;
    
    JavaBitSetAdapter(java.util.BitSet inner) {
        this.inner = inner;
    }

    @Override
    public void set(int bitIndex, boolean value) {
        this.inner.set(bitIndex, value);
    }

    @Override
    public boolean get(int bitIndex) {
        return this.inner.get(bitIndex);
    }

    @Override
    public int size() {
        return this.inner.size();
    }

    @Override
    public BitSet clone() {
        return new JavaBitSetAdapter((java.util.BitSet) this.inner.clone());
    }
}
