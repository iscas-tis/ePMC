package epmc.qmc.value;

import epmc.value.TypeArrayReal;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

final class ValueArrayComplex implements ValueArrayAlgebra {
    private final static String SPACE = " ";
    private final TypeArrayComplex type;
    private ValueArrayAlgebra content;
    private int size;

    ValueArrayComplex(TypeArrayComplex type) {
        this.type = type;
        TypeReal typeReal = TypeReal.get();
        TypeArrayReal typeArrayReal = typeReal.getTypeArray();
        this.content = UtilValue.newArray(typeArrayReal, size() * 2);
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < size() : index + SPACE + size();
        if (ValueComplex.is(value)) {
            content.set(((ValueComplex) value).getRealPart(), index * 2);
            content.set(((ValueComplex) value).getImagPart(), index * 2 + 1);
        } else if (ValueReal.is(value) || ValueInteger.is(value)) {
            content.set(value, index * 2);
            content.set(0, index * 2 + 1);        	
        } else {
            assert false : value.getType();
        }
    }

    @Override
    public void set(int entry, int index) {
        assert index >= 0 : index;
        assert index < size() : index + SPACE + size();
        content.set(entry, index * 2);
        content.set(0, index * 2 + 1);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value instanceof ValueComplex : value.getType();
        assert index >= 0;
        assert index < size() : index + SPACE + size();
        content.get(((ValueComplex) value).getRealPart(), index * 2);
        content.get(((ValueComplex) value).getImagPart(), index * 2 + 1);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public TypeArrayComplex getType() {
        return type;
    }

    @Override
    public void setSize(int size) {
        assert size >= 0 : size;
        TypeReal typeReal = TypeReal.get();
        TypeArrayReal typeArrayReal = typeReal.getTypeArray();
        content = UtilValue.newArray(typeArrayReal, size * 2);
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }
}
