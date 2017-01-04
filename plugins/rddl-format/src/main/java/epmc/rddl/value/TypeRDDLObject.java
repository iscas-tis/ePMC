package epmc.rddl.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;
import epmc.value.Value;

public class TypeRDDLObject implements Type {
    private static final long serialVersionUID = 1L;
    private final ContextValueRDDL contextRDDL;
    private final String name;

    TypeRDDLObject(ContextValueRDDL context, String name) {
        assert context != null;
        this.contextRDDL = context;
        this.name = name;
   }
    
    @Override
    public ContextValue getContext() {
        return contextRDDL.getContextValue();
    }

    public ContextValueRDDL getContextRDDL() {
        return contextRDDL;
    }
    
    @Override
    public Value newValue() {
    	assert false;
        // TODO
        return null;
//        return new ValueRDDLEnum(this);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        return builder.toString();
    }

    public String toStringComplete() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeRDDLObject)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = name.hashCode();
        return hash;
    }
    
    public String getName() {
        return name;
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
