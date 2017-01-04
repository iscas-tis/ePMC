package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PVariableEntry {
    private final PVariable pVariable;
    private final List<String> parameters;
    
    PVariableEntry(PVariable pVariable, List<String> parameters) {
        assert pVariable != null;
        assert parameters != null;
        for (String parameter : parameters) {
            assert parameter != null;
        }
        this.pVariable = pVariable;
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(pVariable);
        builder.append(",");
        builder.append(parameters);
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof PVariableEntry)) {
            return false;
        }
        PVariableEntry other = (PVariableEntry) obj;
        if (!this.pVariable.equals(other.pVariable)) {
            return false;
        }
        if (!this.parameters.equals(other.parameters)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = pVariable.hashCode() + (hash << 6) + (hash << 16) - hash;            
        hash = parameters.hashCode() + (hash << 6) + (hash << 16) - hash;            
        return hash;
    }
    
    PVariable getPVariable() {
        return pVariable;
    }
    
    List<String> getParameterStrings() {
        return parameters;
    }
    
    List<RDDLObjectValue> getParameters(Instance instance) {
    	assert instance != null;
    	List<RDDLObject> parameters = this.pVariable.getParameters(instance);
    	assert parameters.size() == this.parameters.size()
    			: this + "\n"
    			+ parameters.size() + " != " + this.parameters.size() + "\n"
    			+ parameters + "\n" + this.parameters;
    	List<RDDLObjectValue> result = new ArrayList<>();
    	for (int paramNr = 0; paramNr < parameters.size(); paramNr++) {
    		RDDLObject object = parameters.get(paramNr);
    		String paramValueName = this.parameters.get(paramNr);
    		result.add(object.getValue(paramValueName));
    	}
    	return result;
    }
}
