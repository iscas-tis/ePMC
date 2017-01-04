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
        assert ValueInteger.isInteger(value);
        int index = walker.getIndex();
        values[index] = ValueInteger.asInteger(value).getInt();
        valueSet.set(index);
    }
    
    public void get(Value value) {
        assert value != null;
        assert ValueInteger.isInteger(value);
        assert valueSet.get(walker.getIndex());
        ValueAlgebra.asAlgebra(value).set(values[walker.getIndex()]);
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
