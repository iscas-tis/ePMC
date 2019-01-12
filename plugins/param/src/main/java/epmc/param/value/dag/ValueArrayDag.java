package epmc.param.value.dag;

import java.math.BigInteger;

import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public final class ValueArrayDag implements ValueArrayAlgebra {
    public static boolean is(Value value) {
        return value instanceof ValueArrayDag;
    }
    
    public static ValueArrayDag as(Value value) {
        if (is(value)) {
            return (ValueArrayDag) value;
        } else {
            return null;
        }
    }

    private final TypeArrayFunctionDag type;
    private int[] entries = new int[0];

    ValueArrayDag(TypeArrayFunctionDag type) {
        assert type != null;
        this.type = type;
    }

    @Override
    public void setSize(int size) {
        entries = new int[size];
    }

    @Override
    public int size() {
        return entries.length;
    }

    @Override
    public void get(Value value, int index) {
        ValueDag valueDag = (ValueDag) value;
        valueDag.setNumber(entries[index]);
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < entries.length : index;
        ValueDag valueDag = (ValueDag) value;
        entries[index] = valueDag.getNumber();
    }

    @Override
    public void set(int entry, int index) {
        entries[index] = type.getEntryType().getDag().getNumber(new BigInteger(Integer.toString(entry)), BigInteger.ONE);
    }

    @Override
    public TypeArrayFunctionDag getType() {
        return type;
    }

    @Override
    public String toString() {
        int[] unique = new IntOpenHashSet(entries).toIntArray();
        return type.getEntryType().getDag().toString(unique);
        /*
        StringBuffer builder = new StringBuffer();
        builder.append("[");
        ValueDag valueDag = type.getEntryType().newValue();
        for (int index = 0; index < entries.length; index++) {
            if (index < entries.length - 1) {
                valueDag.setNumber(entries[index]);
                builder.append(valueDag);
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
        */
    }
}
