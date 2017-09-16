package epmc.value;

public interface ValueSetString extends Value {
    static boolean isValueSetString(Value value) {
        return value instanceof ValueSetString;
    }
    
    static ValueSetString asValueSetString(Value value) {
        if (isValueSetString(value)) {
            return (ValueSetString) value;
        } else {
            return null;
        }
    }
    
    void set(String value);
}
