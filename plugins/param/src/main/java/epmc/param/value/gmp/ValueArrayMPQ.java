package epmc.param.value.gmp;

import epmc.param.value.rational.ValueArrayRational;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class ValueArrayMPQ implements ValueArrayRational {
    public static boolean is(Value value) {
        return value instanceof ValueArrayMPQ;
    }
    
    public static ValueArrayMPQ as(Value value) {
        if (is(value)) {
            return (ValueArrayMPQ) value;
        } else {
            return null;
        }
    }
    
    private final static String SPACE = " ";
    private final TypeArrayMPQ type;
    private MPQArrayMemory content = new MPQArrayMemory(0);
    private int size;

    ValueArrayMPQ(TypeArrayMPQ type) {
        assert type != null;
        this.type = type;
    }
    
    @Override
    public void set(int entry, int index) {
        assert index >= 0 : index;
        assert index < size : index + SPACE + size;
        GMP.gmp_util_mpq_array_set_int(content, index, entry);
    }

    @Override
    public void setSize(int size) {
        assert size >= 0;
        this.size = size;
        content = new MPQArrayMemory(size);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < size : index + SPACE + size;
        assert ValueMPQ.is(value);
        ValueMPQ valueMPQ = ValueMPQ.as(value);
        GMP.gmp_util_mpq_array_get(content, index, valueMPQ.getContent());
    }

    @Override
    public void set(Value value, int index) {
        assert index >= 0 : index;
        assert index < size : index + SPACE + size;
        assert ValueMPQ.is(value);
        ValueMPQ valueMPQ = ValueMPQ.as(value);
        GMP.gmp_util_mpq_array_set(content, index, valueMPQ.getContent());
    }

    @Override
    public TypeArrayMPQ getType() {
        return type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueArrayMPQ)) {
            return false;
        }
        ValueArrayMPQ other = (ValueArrayMPQ) obj;
        if (type != other.type) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        if (GMP.gmp_util_mpq_array_equals(content, other.content, size) == 0) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        ValueMPQ value = type.getEntryType().newValue();
        for (int index = 0; index < size; index++) {
            get(value, index);
            hash = value.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    
    @Override
    public String toString() {
        return UtilValue.arrayToString(this);
    }
    
    public MPQArrayMemory getContent() {
        return content;
    }
}
