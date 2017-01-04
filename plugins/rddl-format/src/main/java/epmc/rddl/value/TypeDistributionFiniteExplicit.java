package epmc.rddl.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;
import epmc.value.Value;

public class TypeDistributionFiniteExplicit implements Type {
    private static final long serialVersionUID = 1L;
    private final ContextValue context;
    private final int size;
    private final Type entryType;

    TypeDistributionFiniteExplicit(ContextValue context, int size, Type entryType) {
        assert context != null;
        assert size >= 1;
        assert entryType != null;
        this.context = context;
        this.size = size;
        this.entryType = entryType;
    }
    
    @Override
    public ContextValue getContext() {
        return context;
    }

    @Override
    public Value newValue() {
        return new ValueDistributionFiniteExplicit(this);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("finite-distribution(");
        builder.append(size);
        builder.append(",");
        builder.append(entryType);
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeDistributionFiniteExplicit)) {
            return false;
        }
        TypeDistributionFiniteExplicit other = (TypeDistributionFiniteExplicit) obj;
        if (this.size != other.size) {
            return false;
        }
        if (this.entryType != other.entryType) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = size + (hash << 6) + (hash << 16) - hash;
        hash = entryType.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    public int size() {
        return size;
    }
    
    public Type getEntryType() {
        return entryType;
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
