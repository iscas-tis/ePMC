package epmc.rddl.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;
import epmc.value.Value;

public class TypeDistributionNormal implements Type {
    private static final long serialVersionUID = 1L;
    private final ContextValue context;

    TypeDistributionNormal(ContextValue context, Value knownValue) {
        assert context != null;
        this.context = context;
    }
    
    public TypeDistributionNormal(ContextValue context) {
        this(context, null);
    }
    
    @Override
    public ContextValue getContext() {
        return context;
    }

    @Override
    public Value newValue() {
        return new ValueDistributionNormal(this);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("normal-distribution()");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeDistributionNormal)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        return hash;
    }    
    
	@Override
    public TypeArray getTypeArray() {
        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
	
	@Override
	public boolean canImport(Type type) {
        assert type != null;
        if (this == type) {
            return true;
        }
        return false;
	}
}
