package epmc.util;

import epmc.util.BitSet;

public final class ReadonlyBitSet implements BitSet {
    private final BitSet inner;
    
    ReadonlyBitSet(BitSet inner) {
        this.inner = inner;
    }

    @Override
    public void set(int bitIndex, boolean value) {
        assert false;
    }

    @Override
    public boolean get(int bitIndex) {
        return inner.get(bitIndex);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public BitSet clone() {
        return inner.clone();
    }

    @Override
    public boolean equals(Object obj) {
        return inner.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return inner.hashCode();
    }
}
