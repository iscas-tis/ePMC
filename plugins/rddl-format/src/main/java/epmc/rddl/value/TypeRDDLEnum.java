package epmc.rddl.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;
import epmc.value.TypeEnumerable;
import epmc.value.TypeNumBitsKnown;

public class TypeRDDLEnum implements TypeEnumerable, TypeNumBitsKnown {
    private static final long serialVersionUID = 1L;
    private final ContextValueRDDL contextRDDL;
    private final String name;
    private final List<String> constants = new ArrayList<>();
    private int[] toInternalNumber;

    TypeRDDLEnum(ContextValueRDDL context, String name, List<String> constants) {
        assert context != null;
        this.contextRDDL = context;
        this.name = name;
        this.constants.addAll(constants);
        List<String> allConstants = context.getNumberToEnumConstant();
        this.toInternalNumber = new int[allConstants.size()];
        Arrays.fill(toInternalNumber, -1);
        for (int internalNumber = 0; internalNumber < constants.size(); internalNumber++) {
            String constant = constants.get(internalNumber);
            int externalNumber = context.getEnumConstantNumber(constant);
            toInternalNumber[externalNumber] = internalNumber;
        }
    }
    
    @Override
    public ContextValue getContext() {
        return contextRDDL.getContextValue();
    }

    public ContextValueRDDL getContextRDDL() {
        return contextRDDL;
    }
    
    @Override
    public ValueRDDLEnum newValue() {
        return new ValueRDDLEnum(this);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        
        return builder.toString();
    }

    public String definitionToString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < constants.size(); i++) {
            builder.append(constants.get(i));
            if (i < constants.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("}");
        
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeRDDLEnum)) {
            return false;
        }
        TypeRDDLEnum other = (TypeRDDLEnum) obj;
        if (!this.constants.equals(other.constants)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = name.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = constants.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    int[] getToInternalNumber() {
        return toInternalNumber;
    }
    
    public int toInternalNumber(String constant) {
        int number = contextRDDL.getEnumConstantNumber(constant);
        return toInternalNumber[number];
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public int getNumValues() {
    	return this.constants.size();
    }
    
    @Override
    public int getNumBits() {
		return Integer.SIZE - Integer.numberOfLeadingZeros(this.constants.size() - 1);
    }
    
    @Override
    public boolean canImport(Type type) {
    	assert type != null;
    	if (!(type instanceof TypeRDDLEnum)) {
    		return false;
    	}
    	return true;
    	// TODO Auto-generated method stub
//    	return Type.super.canImport(type);
    }
    
	@Override
    public TypeArray getTypeArray() {
        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
}
