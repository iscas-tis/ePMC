package epmc.param.value.polynomial;

import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArrayAlgebra;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;

public final class ValueArrayPolynomial implements ValueArrayAlgebra {
    private final static String SPACE = " ";
    private final TypeArrayPolynomial type;

    private Value[] content;
    private int size;
    private OperatorEvaluator set;

    ValueArrayPolynomial(TypeArrayPolynomial type) {
        assert type !=  null;
        this.type = type;
        this.content = new Value[0];
        set = ContextValue.get().getEvaluator(OperatorSet.SET, type.getEntryType(), type.getEntryType());
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < size();
        set.apply(content[index], value);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < size() : index + SPACE + size();
        set.apply(value, content[index]);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < size(); i++) {
            hash = content[i].hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public void setSize(int size) {
        assert size >= 0;
        Type entryType = getType().getEntryType();
        this.content = new Value[size];
        for (int index = 0; index < size; index++) {
            this.content[index] = entryType.newValue();
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

    @Override
    public void set(int entry, int index) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TypeArrayAlgebra getType() {
        return type;
    }
}
