package epmc.param.value.dag;

public enum OperatorType {
    PARAMETER(true, 1),
    NUMBER(true, 2),
    ADD_INVERSE(false, 1),
    MULTIPLY_INVERSE(false, 1),
    ADD(false, 2),
    MULTIPLY(false, 2);
    
    private final boolean special;
    private final int arity;

    private OperatorType(boolean special, int arity) {
        this.special = special;
        this.arity = arity;
    }
    
    public int getArity() {
        return arity;
    }
    
    public boolean isSpecial() {
        return special;
    }
}
