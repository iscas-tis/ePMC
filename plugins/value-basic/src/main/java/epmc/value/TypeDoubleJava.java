package epmc.value;

public final class TypeDoubleJava implements TypeDouble {
    private final static String DOUBLE = "double";

    // TODO get rid of these values
    private final ValueDouble lower;
    private final ValueDouble upper;

    public TypeDoubleJava() {
        this(null, null);
    }
    
    public TypeDoubleJava(ValueDouble lower, ValueDouble upper) {
        this.lower = lower == null ? null : UtilValue.clone(lower);
        this.upper = upper == null ? null : UtilValue.clone(upper);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DOUBLE);
        return builder.toString();
    }

    @Override
    public ValueDoubleJava newValue() {
        return new ValueDoubleJava(this);
    }

    @Override
    public int getNumBits() {
        return Double.SIZE;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public ValueDouble getLower() {
        return lower;
    }

    @Override
    public ValueDouble getUpper() {
        return upper;
    }

    @Override
    public TypeArrayDouble getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayDouble(this));
    }
}
