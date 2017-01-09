package epmc.expression.standard.evaluatorexplicit;

import epmc.value.Value;

public final class ValuesVectorArray implements ValuesVector {
    private Value[] values;
    
    public void setValues(Value[] values) {
        this.values = values;
    }
    
    
    @Override
    public void get(Value result, int index) {
        assert values != null;
        for (int i = 0; i < values.length; i++) {
            assert values[i] != null;
        }
        result.set(values[index]);
    }

}
